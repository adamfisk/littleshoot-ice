package org.lastbamboo.common.ice;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.lastbamboo.common.ice.candidate.IceCandidate;
import org.lastbamboo.common.ice.candidate.IceCandidatePair;
import org.lastbamboo.common.ice.candidate.IceCandidatePairPriorityCalculator;
import org.lastbamboo.common.ice.candidate.IceCandidatePairState;
import org.lastbamboo.common.ice.candidate.IceCandidateVisitor;
import org.lastbamboo.common.ice.candidate.IceCandidateVisitorAdapter;
import org.lastbamboo.common.ice.candidate.IceTcpActiveCandidate;
import org.lastbamboo.common.ice.candidate.IceTcpHostPassiveCandidate;
import org.lastbamboo.common.ice.candidate.IceTcpPeerReflexiveCandidate;
import org.lastbamboo.common.ice.candidate.IceTcpRelayPassiveCandidate;
import org.lastbamboo.common.ice.candidate.IceTcpServerReflexiveSoCandidate;
import org.lastbamboo.common.ice.candidate.IceUdpHostCandidate;
import org.lastbamboo.common.ice.candidate.IceUdpPeerReflexiveCandidate;
import org.lastbamboo.common.ice.candidate.IceUdpRelayCandidate;
import org.lastbamboo.common.ice.candidate.IceUdpServerReflexiveCandidate;
import org.lastbamboo.common.ice.candidate.TcpIceCandidatePair;
import org.lastbamboo.common.ice.candidate.UdpIceCandidatePair;
import org.lastbamboo.common.tcp.frame.TcpFrameIoHandler;
import org.lastbamboo.common.util.Closure;
import org.lastbamboo.common.util.CollectionUtils;
import org.lastbamboo.common.util.CollectionUtilsImpl;
import org.lastbamboo.common.util.Pair;
import org.lastbamboo.common.util.PairImpl;
import org.lastbamboo.common.util.Predicate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class containing data and state for an ICE check list.<p>
 * 
 * See: http://tools.ietf.org/html/draft-ietf-mmusic-ice-17#section-5.7
 */
public class IceCheckListImpl implements IceCheckList
    {

    private final Logger m_log = LoggerFactory.getLogger(getClass());
    
    /**
     * The triggered check queue.  This is a FIFO queue of checks that the
     * course of the connectivity check process "triggers", typically through
     * the discovery of new peer reflexive candidates.  
     */
    private final Queue<IceCandidatePair> m_triggeredQueue = 
        new ConcurrentLinkedQueue<IceCandidatePair>();
    
    private final List<IceCandidatePair> m_pairs =
        new LinkedList<IceCandidatePair>();

    private volatile IceCheckListState m_state = IceCheckListState.RUNNING;

    private final Collection<IceCandidate> m_localCandidates;

    private final IceStunCheckerFactory m_checkerFactory;

    /**
     * Creates a new check list, starting with only local candidates.
     * 
     * @param checkerFactory The factory for generating connectivity checker
     * classes. 
     * @param localCandidates The local candidates to use in the check list.
     */
    public IceCheckListImpl(
        final IceStunCheckerFactory checkerFactory,
        final Collection<IceCandidate> localCandidates)
        {
        m_checkerFactory = checkerFactory;
        m_localCandidates = localCandidates;
        }
    
    public IceCandidatePair removeTopTriggeredPair()
        {
        return this.m_triggeredQueue.poll();
        }

    public void setState(final IceCheckListState state)
        {
        if (this.m_state != IceCheckListState.COMPLETED)
            {
            this.m_state = state;
            synchronized (this)
                {
                m_log.debug("State changed to: {}", state);
                this.notify();
                }
            }
        }

    public IceCheckListState getState()
        {
        return this.m_state;
        }

    public void check()
        {
        synchronized (this)
            {
            while (this.m_state == IceCheckListState.RUNNING)
                {
                try
                    {
                    wait();
                    }
                catch (final InterruptedException e)
                    {
                    m_log.error("Interrupted??", e);
                    }
                }
            }
        m_log.debug("Returning from check");
        }

    public boolean isActive()
        {
        // TODO: I believe this should depend on the state of the check list.  
        // The active state is used in determing the value of N in timer
        // computations.
        return false;
        }

    public void addTriggeredPair(final IceCandidatePair pair)
        {
        m_log.debug("Adding triggered pair...");
        synchronized (this)
            {
            this.m_triggeredQueue.add(pair);
            }
        }
    
    public void addPair(final IceCandidatePair pair)
        {
        if (pair == null)
            {
            m_log.error("Null pair");
            throw new NullPointerException("Null pair");
            }
        synchronized (this)
            {
            this.m_pairs.add(pair);
            Collections.sort(this.m_pairs);
            }
        }

    public void recomputePairPriorities(final boolean controlling)
        {
        synchronized (this)
            {
            recompute(this.m_triggeredQueue, controlling);
            recompute(this.m_pairs, controlling);
            sortPairs(this.m_pairs);
            }
        }

    private void recompute(final Collection<IceCandidatePair> pairs, 
        final boolean controlling)
        {
        final Closure<IceCandidatePair> closure = 
            new Closure<IceCandidatePair>()
            {
            public void execute(final IceCandidatePair pair)
                {
                final IceCandidate local = pair.getLocalCandidate();
                final IceCandidate remote = pair.getRemoteCandidate();
                local.setControlling(controlling);
                
                // Note we also set the controlling status of the remote 
                // candidate because there's nothing in the SDP specifying the
                // controlling status -- it's just an externally configured
                // property based on starting roles and any role conflicts that
                // may emerge over the course of establishing a media session.
                remote.setControlling(!controlling);
                pair.recomputePriority();
                }
            };
        executeOnPairs(pairs, closure);
        }

    public void formCheckList(final Collection<IceCandidate> remoteCandidates)
        {

        final Collection<Pair<IceCandidate, IceCandidate>> pairs = 
            new LinkedList<Pair<IceCandidate,IceCandidate>>();
        
        for (final IceCandidate localCandidate : m_localCandidates)
            {
            for (final IceCandidate remoteCandidate : remoteCandidates)
                {
                if (shouldPair(localCandidate, remoteCandidate))
                    {
                    final Pair<IceCandidate, IceCandidate> pair =
                        new PairImpl<IceCandidate, IceCandidate>(localCandidate, 
                            remoteCandidate);
                    pairs.add(pair);
                    }
                }
            }
        
        m_log.debug("Pairs before conversion: {}", pairs);
        
        // Convert server reflexive local candidates to their base and remove
        // pairs with TCP passive local candidates.
        final List<Pair<IceCandidate, IceCandidate>> convertedPairs = 
            convertPairs(pairs);
        m_log.debug("Pairs after conversion:  {}", convertedPairs);
        
        final Comparator<Pair<IceCandidate, IceCandidate>> comparator =
            new Comparator<Pair<IceCandidate, IceCandidate>>()
                {

                public int compare(
                    final Pair<IceCandidate, IceCandidate> pair1, 
                    final Pair<IceCandidate, IceCandidate> pair2)
                    {
                    final long pair1Priority = calculatePriority(pair1);
                    final long pair2Priority = calculatePriority(pair2);
                    
                    if (pair1Priority > pair2Priority) return -1;
                    if (pair1Priority < pair2Priority) return 1;
                    return 0;
                    }
                
                private long calculatePriority(
                    final Pair<IceCandidate, IceCandidate> pair)
                    {
                    return IceCandidatePairPriorityCalculator.calculatePriority(
                        pair.getFirst(), pair.getSecond());
                    }
                };

        Collections.sort(convertedPairs, comparator);
        
        m_log.debug(convertedPairs.size()+" converted");
        final List<IceCandidatePair> pruned = prunePairs(convertedPairs);
        m_log.debug(pruned.size()+" after pruned");
        final List<IceCandidatePair> sorted = sortPairs(pruned);
        synchronized (this)
            {
            this.m_pairs.addAll(sorted);
            m_log.debug("Created pairs:\n"+this.m_pairs);
            }
        }
    

    private List<IceCandidatePair> sortPairs(
        final List<IceCandidatePair> pairs)
        {
        synchronized (this)
            {
            Collections.sort(pairs);
            }
        return pairs;
        }

    /**
     * Removes any TCP passive local pairs and converts pairs with a local
     * UDP server reflexive candidate to the associated base candidate.
     * 
     * @param pairs The pairs to convert.
     * @return The {@link List} of pairs with TCP passive pairs removed and
     * server reflexive local candidates converted to their bases.  See
     * ICE section 5.7.3.
     */
    private static List<Pair<IceCandidate, IceCandidate>> convertPairs(
        final Collection<Pair<IceCandidate, IceCandidate>> pairs)
        {
        final List<Pair<IceCandidate, IceCandidate>> convertedPairs = 
            new LinkedList<Pair<IceCandidate,IceCandidate>>();
        
        for (final Pair<IceCandidate, IceCandidate> pair : pairs)
            {
            final Pair<IceCandidate, IceCandidate> converted = convertPair(pair);
            if (converted != null)
                {
                convertedPairs.add(converted);
                }
            }
        
        return convertedPairs;
        }
        
    private static Pair<IceCandidate, IceCandidate> convertPair(
        final Pair<IceCandidate, IceCandidate> pair)
        {
        final IceCandidate localCandidate = pair.getFirst();
        final IceCandidate remoteCandidate = pair.getSecond();
        
        // We have to convert all local UDP server reflexive candidates to
        // their base and we have to ignore all TCP passive candidates.
        final IceCandidateVisitor<Pair<IceCandidate, IceCandidate>> visitor =
            new IceCandidateVisitor<Pair<IceCandidate, IceCandidate>>()
            {
            public void visitCandidates(Collection<IceCandidate> candidates)
                {
                // Not used here.
                }
            
            public Pair<IceCandidate, IceCandidate> visitUdpServerReflexiveCandidate(
                final IceUdpServerReflexiveCandidate candidate)
                {
                // Convert server reflexive candidates to their base.
                final IceCandidate base = candidate.getBaseCandidate();
                return new PairImpl<IceCandidate, IceCandidate>(base, 
                    remoteCandidate);
                }

            public Pair<IceCandidate, IceCandidate> visitTcpActiveCandidate(
                final IceTcpActiveCandidate candidate)
                {
                return pair;
                }

            public Pair<IceCandidate, IceCandidate> visitTcpHostPassiveCandidate(
                final IceTcpHostPassiveCandidate candidate)
                {
                // Ignore all TCP passive local candidates.
                return null;
                }

            public Pair<IceCandidate, IceCandidate> visitTcpRelayPassiveCandidate(
                final IceTcpRelayPassiveCandidate candidate)
                {
                // Ignore all TCP passive local candidates.
                return null;
                }

            public Pair<IceCandidate, IceCandidate> visitTcpServerReflexiveSoCandidate(
                final IceTcpServerReflexiveSoCandidate candidate)
                {
                // TODO: We don't currently support TCP SO.
                return null;
                }

            public Pair<IceCandidate, IceCandidate> visitTcpPeerReflexiveCandidate(
                final IceTcpPeerReflexiveCandidate candidate)
                {
                // Should not visit peer reflexive in check lists.
                return null;
                }

            public Pair<IceCandidate, IceCandidate> visitUdpHostCandidate(
                final IceUdpHostCandidate candidate)
                {
                return pair;
                }

            public Pair<IceCandidate, IceCandidate> visitUdpPeerReflexiveCandidate(
                final IceUdpPeerReflexiveCandidate candidate)
                {
                return pair;
                }

            public Pair<IceCandidate, IceCandidate> visitUdpRelayCandidate(
                final IceUdpRelayCandidate candidate)
                {
                return pair;
                }
            };
        
        return localCandidate.accept(visitor);
        }

    /**
     * Prunes pairs by converting any non-host local candidates to host 
     * candidates and removing any duplicates created.  The pairs should already
     * be ordered by priority when this method is called.
     * 
     * @param pairs The pairs to prune.  This {@link List} MUST already be
     * sorted by pair priority prior to this call.
     */
    private List<IceCandidatePair> prunePairs(
        final List<Pair<IceCandidate, IceCandidate>> pairs)
        {
        // Note the pairs override hashCode using the local and the remote
        // candidates.  We just use the map here to identify pairs with the
        // same address for the local and the remote candidates.  This is
        // possible because we just converted local server reflexive 
        // candidates to their associated bases, according to the algorithm.
        //
        // If we find a duplicate pair, we always take the one with the 
        // higher priority.
        final List<IceCandidatePair> prunedPairs = 
            new LinkedList<IceCandidatePair>();
        final Set<Pair<IceCandidate, IceCandidate>> seenPairs =
            new HashSet<Pair<IceCandidate, IceCandidate>>();
        
        for (final Pair<IceCandidate, IceCandidate> pair : pairs)
            {
            if (!seenPairs.contains(pair))
                {
                seenPairs.add(pair);
                prunedPairs.add(createPair(pair));
                }
            }
        
        // Limit attacks based on the number of pairs.
        if (prunedPairs.size() > 100)
            {
            return prunedPairs.subList(0, 100);
            }
        return prunedPairs;
        }

    private IceCandidatePair createPair(
        final Pair<IceCandidate, IceCandidate> pair)
        {
        final IceCandidate localCandidate = pair.getFirst();
        final IceCandidate remoteCandidate = pair.getSecond();
        
        final IceCandidateVisitor<IceCandidatePair> visitor = 
            new IceCandidateVisitorAdapter<IceCandidatePair>()
            {
            public IceCandidatePair visitTcpActiveCandidate(
                final IceTcpActiveCandidate candidate)
                {
                final TcpFrameIoHandler frameIoHandler = 
                    new TcpFrameIoHandler();
                final IceStunChecker checker = 
                    m_checkerFactory.createStunChecker(candidate, 
                        remoteCandidate, frameIoHandler);
                return new TcpIceCandidatePair(candidate, remoteCandidate,
                    checker, frameIoHandler);
                }
            
            public IceCandidatePair visitUdpHostCandidate(
                final IceUdpHostCandidate candidate)
                {
                m_log.debug("Creating STUN checker...");
                final IceStunChecker checker = 
                    m_checkerFactory.createStunChecker(candidate, 
                        remoteCandidate);

                return new UdpIceCandidatePair(candidate, remoteCandidate, 
                    checker);
                }
            };
        
        return localCandidate.accept(visitor);
        }

    private static boolean shouldPair(final IceCandidate localCandidate, 
        final IceCandidate remoteCandidate)
        {
        // This is specified in ICE section 5.7.1
        return (
            (localCandidate.getComponentId() == 
            remoteCandidate.getComponentId()) &&
            addressTypesMatch(localCandidate, remoteCandidate) &&
            transportTypesMatch(localCandidate, remoteCandidate));
        }

    private static boolean addressTypesMatch(final IceCandidate localCandidate, 
        final IceCandidate remoteCandidate)
        {
        final InetAddress localAddress = 
            localCandidate.getSocketAddress().getAddress();
        final InetAddress remoteAddress =
            remoteCandidate.getSocketAddress().getAddress();
        
        final boolean localIsIpV4 = localAddress instanceof Inet4Address;
        final boolean remoteIsIpV4 = remoteAddress instanceof Inet4Address;
        
        if (localIsIpV4)
            {
            return remoteIsIpV4;
            }
        else
            {
            return !remoteIsIpV4;
            }
        }
    
    private static boolean transportTypesMatch(
        final IceCandidate localCandidate, final IceCandidate remoteCandidate)
        {
        final IceTransportProtocol localTransport = 
            localCandidate.getTransport();
        final IceTransportProtocol remoteTransport =
            remoteCandidate.getTransport();
        switch (localTransport)
            {
            case UDP:
                return remoteTransport == IceTransportProtocol.UDP;
            case TCP_SO:
                return remoteTransport == IceTransportProtocol.TCP_SO;
            case TCP_ACT:
                return remoteTransport == IceTransportProtocol.TCP_PASS;
            case TCP_PASS:
                return remoteTransport == IceTransportProtocol.TCP_ACT;
            }
        return false;
        }

    public boolean hasHigherPriorityPendingPair(final IceCandidatePair pair)
        {
        final long priority = pair.getPriority();
        final Predicate<IceCandidatePair> triggeredPred =
            new Predicate<IceCandidatePair>()
            {
            public boolean evaluate(final IceCandidatePair curPair)
                {
                if (curPair.getPriority() > priority) return true;
                return false;
                }
            };
        
        if (matchesAny(this.m_triggeredQueue, triggeredPred))
            {
            return true;
            }
        
        final Predicate<IceCandidatePair> pred =
            new Predicate<IceCandidatePair>()
            {

            public boolean evaluate(final IceCandidatePair curPair)
                {
                if (curPair.getPriority() > priority)
                    {
                    final IceCandidatePairState state = curPair.getState();
                    
                    switch (state)
                        {
                        case FROZEN:
                            // Fall through.
                        case WAITING:
                            // Fall through.
                        case IN_PROGRESS:
                            return true;
                        case SUCCEEDED:
                            // Fall through.
                        case FAILED:
                            return false;
                        }
                    }
                return false;
                }
            };
        
        return matchesAny(pred);
        }

    public void removeWaitingAndFrozenPairs(final IceCandidatePair pair)
        {
        // Lock the whole check list.
        synchronized (this)
            {
            for (final Iterator<IceCandidatePair> iter = m_pairs.iterator(); 
                iter.hasNext();)
                {
                final IceCandidatePair curPair = iter.next();
                final IceCandidatePairState state = curPair.getState();
                switch (state)
                    {
                    case FROZEN:
                        // Fall through.
                    case WAITING:
                        iter.remove();
                        break;
                    case IN_PROGRESS:
                        // The following is at SHOULD strength in 8.1.2
                        if (curPair.getPriority() < pair.getPriority())
                            {
                            pair.cancelStunTransaction();
                            }
                        break;
                    case SUCCEEDED:
                        // Do nothing.
                    case FAILED:
                        // Do nothing.
                    }
                }

            for (final Iterator<IceCandidatePair> iter = m_triggeredQueue.iterator();
                iter.hasNext();)
                {
                final IceCandidatePair curPair = iter.next();
                final IceCandidatePairState state = curPair.getState();
                switch (state)
                    {
                    case FROZEN:
                        // Fall through.
                    case WAITING:
                        iter.remove();
                        break;
                    case IN_PROGRESS:
                        // Do nothing in the case of triggered checks.
                    case SUCCEEDED:
                        // Do nothing.
                    case FAILED:
                        // Do nothing.
                    }
                }
            }
        }

    public void executeOnPairs(final Closure<IceCandidatePair> closure)
        {
        executeOnPairs(this.m_pairs, closure);
        }

    public IceCandidatePair selectPair(final Predicate<IceCandidatePair> pred)
        {
        synchronized (this)
            {
            final CollectionUtils utils = new CollectionUtilsImpl();
            return utils.selectFirst(this.m_pairs, pred);
            }
        }

    public boolean matchesAny(final Predicate<IceCandidatePair> pred)
        {
        return matchesAny(this.m_pairs, pred);
        }

    private boolean matchesAny(final Collection<IceCandidatePair> pairs,
        final Predicate<IceCandidatePair> pred)
        {
        synchronized (this)
            {
            final CollectionUtils utils = new CollectionUtilsImpl();
            return utils.matchesAny(pairs, pred);
            }
        }
    
    private void executeOnPairs(final Collection<IceCandidatePair> pairs, 
        final Closure<IceCandidatePair> closure)
        {
        synchronized (this)
            {
            final CollectionUtils utils = new CollectionUtilsImpl();
            utils.forAllDo(pairs, closure);
            }
        }
    }
