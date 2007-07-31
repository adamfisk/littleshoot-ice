package org.lastbamboo.common.ice;

import java.net.InetSocketAddress;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.lastbamboo.common.ice.candidate.IceCandidate;
import org.lastbamboo.common.ice.candidate.IceCandidatePair;
import org.lastbamboo.common.ice.candidate.IceCandidatePairState;
import org.lastbamboo.common.util.Closure;
import org.lastbamboo.common.util.CollectionUtils;
import org.lastbamboo.common.util.CollectionUtilsImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class containing an ICE media stream.  Each media stream contains a single
 * ICE check list, as described in ICE section 5.7.
 */
public class IceMediaStreamImpl implements IceMediaStream
    {

    private final Logger m_log = LoggerFactory.getLogger(getClass());
    private final IceCheckList m_checkList;
    
    private final Collection<IceCandidatePair> m_validPairs =
        new LinkedList<IceCandidatePair>();

    private final Collection<IceCandidate> m_localCandidates;
    private final IceAgent m_iceAgent;
    
    /**
     * Creates a new media stream for ICE.
     * 
     * @param iceAgent The top-level agent for this session.
     * @param localCandidates The candidates from the local agent.
     * @param remoteCandidates The candidates from the remote agent.
     */
    public IceMediaStreamImpl(
        final IceAgent iceAgent,
        final Collection<IceCandidate> localCandidates, 
        final Collection<IceCandidate> remoteCandidates)
        {
        m_iceAgent = iceAgent;
        final IceCheckListCreator checkListCreator = 
            new IceCheckListCreatorImpl();
        
        m_checkList = 
            checkListCreator.createCheckList(localCandidates, remoteCandidates);
        m_localCandidates = localCandidates;
        
        }

    public void connect()
        {
        m_log.debug("Processing check list");
        
        // NOTE: All classes are operating on shared pairs instances here,
        // so changes to any data in pairs here changes pair data in all 
        // associated classes.
        final Collection<IceCandidatePair> pairs = m_checkList.getPairs();
        
        processPairGroups(pairs);
        
        final IceCheckScheduler scheduler = 
            new IceCheckSchedulerImpl(this.m_iceAgent, this, m_checkList);
        scheduler.scheduleChecks();

        m_checkList.check();
        }
    

    /**
     * Groups the pairs as specified in ICE section 5.7.4. The purpose of this
     * grouping appears to be just to set the establish the waiting pair for
     * each foundation prior to running connectivity checks.
     * 
     * @param pairs The pairs to form into foundation-based groups for setting 
     * the state of the pair with the lowest component ID to waiting.
     */
    private void processPairGroups(final Collection<IceCandidatePair> pairs)
        {
        final Map<String, List<IceCandidatePair>> groupsMap = 
            new HashMap<String, List<IceCandidatePair>>();
        
        // Group together pairs with the same foundation.
        for (final IceCandidatePair pair : pairs)
            {
            final String foundation = pair.getFoundation();
            final List<IceCandidatePair> foundationPairs;
            if (groupsMap.containsKey(foundation))
                {
                foundationPairs = groupsMap.get(foundation);
                }
            else
                {
                foundationPairs = new LinkedList<IceCandidatePair>();
                groupsMap.put(foundation, foundationPairs);
                }
            foundationPairs.add(pair);
            }
        
        final Collection<List<IceCandidatePair>> groups = 
            groupsMap.values();
        
        m_log.debug(groups.size()+ " before sorting...");
        for (final List<IceCandidatePair> group : groups)
            {
            setLowestComponentIdToWaiting(group);
            }
        }
    
    private void setLowestComponentIdToWaiting(
        final List<IceCandidatePair> pairs)
        {
        IceCandidatePair pairToSet = null;
        for (final IceCandidatePair pair : pairs)
            {
            if (pairToSet == null)
                {
                pairToSet = pair;
                continue;
                }
            
            // Always use the lowest component ID.
            if (pair.getComponentId() < pairToSet.getComponentId())
                {
                pairToSet = pair;
                }
            
            // If the component IDs match, use the one with the highest
            // priority.  See ICE section 5.7.4
            else if (pair.getComponentId() == pairToSet.getComponentId())
                {
                if (pair.getPriority() > pairToSet.getPriority())
                    {
                    pairToSet = pair;
                    }
                }
            }
        
        if (pairToSet != null)
            {
            pairToSet.setState(IceCandidatePairState.WAITING);
            }
        else
            {
            m_log.warn("No pair to set!!!");
            }
        }

    public Collection<IceCandidatePair> getValidPairs()
        {
        return m_validPairs;
        }

    public void addLocalCandidate(final IceCandidate localCandidate)
        {
        this.m_localCandidates.add(localCandidate);
        }

    public IceCandidate getLocalCandidate(final InetSocketAddress localAddress)
        {
        // A little inefficient here, but we're not talking about a lot of
        // candidates.
        for (final IceCandidate candidate : this.m_localCandidates)
            {
            if (candidate.getSocketAddress().equals(localAddress))
                {
                return candidate;
                }
            }
        return null;
        }
    

    /**
     * Checks if the new pair matches any pair the media stream already 
     * knows about.
     * 
     * @param localAddress The local address of the new pair.
     * @param remoteAddress The remote address of the new pair.
     * @return The matching pair if it exists, otherwise <code>null</code>.
     */
    public IceCandidatePair getPair(final InetSocketAddress localAddress, 
        final InetSocketAddress remoteAddress)
        {
        final Collection<IceCandidatePair> pairs = this.m_checkList.getPairs();
        for (final IceCandidatePair pair : pairs)
            {
            if (pair.getLocalCandidate().getSocketAddress().equals(localAddress) &&
                pair.getRemoteCandidate().getSocketAddress().equals(remoteAddress))
                {
                return pair;
                }
            }
        return null;
        }

    public void onValidPair(final IceCandidatePair validPair, 
        final IceCandidatePair generatingPair, final boolean useCandidate)
        {
        this.m_validPairs.add(validPair);
        
        
        // Now set pairs with the same foundation as the pair that 
        // *generated* the check for this media stream to waiting.
        updateToWaiting(generatingPair);
        
        if (this.m_iceAgent.isControlling())
            {
            if (useCandidate)
                {
                validPair.setNominated(true);
                }
            }
        else
            {
            // Controlled agents are handled differently.  
            // See ICE Section 7.2.1.5.
            // TODO: Implemented controlled agent handling.
            }
        
        // Update check list and timer states.  See section 7.1.2.3.
        if (allFailedOrSucceeded())
            {
            // 1) Set the check list to failed if there is not a pair in the 
            // valid list for all componenents.
            
            // TODO: We only currently have one component!!
            if (this.m_validPairs.isEmpty())
                {
                this.m_checkList.setState(IceCheckListState.FAILED);
                }
            
            // 2) Agent changes state of pairs in frozen check lists.
            this.m_iceAgent.onUnfreezeCheckLists(this);
            }
        
        // The final part of this section states the following:
        //
        // If none of the pairs in the check list are in the Waiting or Frozen
        // state, the check list is no longer considered active, and will not
        // count towards the value of N in the computation of timers for
        // ordinary checks as described in Section 5.8.
        
        // NOTE:  This requires no action on our part.  The definition of 
        // and "active" check list is "a check list with at least one pair 
        // that is Waiting" from 5.7.4.  When computing the value of N, that's
        // the definition that's used, and the active state is determined
        // dynamically at that time.
        }

    private boolean allFailedOrSucceeded()
        {
        final Collection<IceCandidatePair> pairs = this.m_checkList.getPairs();
        for (final IceCandidatePair pair : pairs)
            {
            if (pair.getState() == IceCandidatePairState.SUCCEEDED ||
                pair.getState() == IceCandidatePairState.FAILED)
                {
                continue;
                }
            else
                {
                return false;
                }
            }
        return true;
        }

    public void addValidPair(final IceCandidatePair pair)
        {
        // Currently just used for TCP.  Not quite what we want probably.
        this.m_validPairs.add(pair);
        }
    
    private void updateToWaiting(final IceCandidatePair successfulPair)
        {
        final Closure<IceCandidatePair> closure =
            new Closure<IceCandidatePair>()
            {
            public void execute(final IceCandidatePair pair)
                {
                // We just update pairs with the same foundation that are in
                // the frozen state to the waiting state.
                if (pair.getFoundation().equals(successfulPair.getFoundation()) &&
                    pair.getState() == IceCandidatePairState.FROZEN)
                    {
                    pair.setState(IceCandidatePairState.WAITING);
                    }
                }
            };
        
        final Collection<IceCandidatePair> pairs = this.m_checkList.getPairs();
        final CollectionUtils utils = new CollectionUtilsImpl();
        utils.forAllDoSynchronized(pairs, closure);

        
        // TODO:  We only currently have a single component, so we call this
        // each time.  We need to implement ICE section 7.1.2.2.3, part 2.
        this.m_iceAgent.onValidPairsForAllComponents(this);
        }

    public void addTriggeredCheck(final IceCandidatePair pair)
        {
        this.m_checkList.addTriggeredPair(pair);
        }

    public void recomputePairPriorities()
        {
        this.m_checkList.recomputePairPriorities();
        }

    }
