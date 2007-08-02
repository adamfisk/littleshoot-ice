package org.lastbamboo.common.ice;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.lastbamboo.common.ice.candidate.IceCandidate;
import org.lastbamboo.common.ice.candidate.IceCandidatePair;
import org.lastbamboo.common.ice.candidate.IceCandidatePairComparator;
import org.lastbamboo.common.ice.candidate.IceCandidatePairPriorityCalculator;
import org.lastbamboo.common.ice.candidate.IceCandidateVisitor;
import org.lastbamboo.common.ice.candidate.IceCandidateVisitorAdapter;
import org.lastbamboo.common.ice.candidate.IceTcpActiveCandidate;
import org.lastbamboo.common.ice.candidate.IceTcpHostPassiveCandidate;
import org.lastbamboo.common.ice.candidate.IceTcpRelayPassiveCandidate;
import org.lastbamboo.common.ice.candidate.IceTcpServerReflexiveSoCandidate;
import org.lastbamboo.common.ice.candidate.IceUdpHostCandidate;
import org.lastbamboo.common.ice.candidate.IceUdpPeerReflexiveCandidate;
import org.lastbamboo.common.ice.candidate.IceUdpRelayCandidate;
import org.lastbamboo.common.ice.candidate.IceUdpServerReflexiveCandidate;
import org.lastbamboo.common.ice.candidate.TcpIceCandidatePair;
import org.lastbamboo.common.ice.candidate.UdpIceCandidatePair;
import org.lastbamboo.common.util.Pair;
import org.lastbamboo.common.util.PairImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class for forming ICE check lists.
 */
public class IceCheckListCreatorImpl implements IceCheckListCreator
    {
    
    private final Logger m_log = LoggerFactory.getLogger(getClass());

    public IceCheckList createCheckList(
        final Collection<IceCandidate> localCandidates,
        final Collection<IceCandidate> remoteCandidates)
        {
        final Collection<Pair<IceCandidate, IceCandidate>> pairs = 
            new LinkedList<Pair<IceCandidate,IceCandidate>>();
        
        for (final IceCandidate localCandidate : localCandidates)
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
        final List<IceCandidatePair> triggered = createPairsDataStructure();
        return new IceCheckListImpl(sorted, triggered);
        }

    private List<IceCandidatePair> sortPairs(
        final List<IceCandidatePair> pairs)
        {
        final Comparator<IceCandidatePair> comparator = 
            new IceCandidatePairComparator();
        
        Collections.sort(pairs, comparator);
        return pairs;
        }

    /**
     * Allows easy modification of the underlying data structure.
     * 
     * @return The data structure to use.
     */
    private List<IceCandidatePair> createPairsDataStructure()
        {
        return new LinkedList<IceCandidatePair>();
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
    private List<Pair<IceCandidate, IceCandidate>> convertPairs(
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
        
    private Pair<IceCandidate, IceCandidate> convertPair(
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
        //final Map<IceCandidatePair, IceCandidatePair> pairsMap =
          //  new HashMap<IceCandidatePair, IceCandidatePair>();
        
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
                return new TcpIceCandidatePair(candidate, remoteCandidate);
                }
            
            public IceCandidatePair visitUdpHostCandidate(
                final IceUdpHostCandidate candidate)
                {
                return new UdpIceCandidatePair(candidate, remoteCandidate);
                }
            };
        
        return localCandidate.accept(visitor);
        }

    private boolean shouldPair(final IceCandidate localCandidate, 
        final IceCandidate remoteCandidate)
        {
        // This is specified in ICE section 5.7.1
        return (
            (localCandidate.getComponentId() == 
            remoteCandidate.getComponentId()) &&
            addressTypesMatch(localCandidate, remoteCandidate) &&
            transportTypesMatch(localCandidate, remoteCandidate));
        }

    private boolean addressTypesMatch(final IceCandidate localCandidate, 
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
    
    private boolean transportTypesMatch(final IceCandidate localCandidate, 
        final IceCandidate remoteCandidate)
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
    }
