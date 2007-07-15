package org.lastbamboo.common.ice;

import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.lastbamboo.common.ice.candidate.IceCandidatePair;
import org.lastbamboo.common.ice.candidate.IceCandidatePairState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class containing an ICE media stream, including ICE check lists.
 */
public class IceMediaStreamImpl implements IceMediaStream
    {

    private final Logger m_log = LoggerFactory.getLogger(getClass());
    private final IceCheckList m_checkList;
    
    private final Collection<IceCandidatePair> m_validPairs;
    

    /**
     * Creates a new media stream for ICE.
     * 
     * @param checkList The initial check list.
     */
    public IceMediaStreamImpl(final IceCheckList checkList)
        {
        m_checkList = checkList;
        m_validPairs = new LinkedList<IceCandidatePair>();
        }

    public void addValidPair(final IceCandidatePair pair)
        {
        m_log.debug("Adding valid pair!");
        final Collection<IceCandidatePair> pairs = this.m_checkList.getPairs();
        if (pairs.contains(pair))
            {
            
            }
        this.m_validPairs.add(pair);
        }

    public void connect()
        {
        m_log.debug("Processing check list");
        final Collection<IceCandidatePair> pairs = m_checkList.getPairs();
        
        // TODO: Still not clear what the heck these groups are for.
        final Collection<List<IceCandidatePair>> mediaGroups = 
            createGroups(pairs);
        
        m_log.debug("Created "+mediaGroups.size()+" groups");
        final IceCheckScheduler scheduler = 
            new IceCheckSchedulerImpl(this, m_checkList);
        scheduler.scheduleChecks();

        m_checkList.check();
        }
    

    private Collection<List<IceCandidatePair>> createGroups(
        final Collection<IceCandidatePair> pairs)
        {
        final Map<Integer, List<IceCandidatePair>> groupsMap = 
            new HashMap<Integer, List<IceCandidatePair>>();
        
        // Group together pairs with the same foundation.
        for (final IceCandidatePair pair : pairs)
            {
            final int foundation = pair.getFoundation();
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
            sortPairs(group);
            setLowestComponentIdToWaiting(group);
            }

        return groups;
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
            // priority.
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

    private Collection<IceCandidatePair> sortPairs(
        final List<IceCandidatePair> pairs)
        {
        final Comparator<IceCandidatePair> comparator = 
            new IceCandidatePairComparator();
        
        Collections.sort(pairs, comparator);
        return pairs;
        }

    public Collection<IceCandidatePair> getValidPairs()
        {
        return m_validPairs;
        }
    }
