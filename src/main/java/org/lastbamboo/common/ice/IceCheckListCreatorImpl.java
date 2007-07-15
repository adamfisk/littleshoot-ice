package org.lastbamboo.common.ice;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.lastbamboo.common.ice.candidate.IceCandidate;
import org.lastbamboo.common.ice.candidate.IceCandidatePair;
import org.lastbamboo.common.ice.candidate.IceCandidatePairFactory;
import org.lastbamboo.common.ice.candidate.IceCandidatePairFactoryImpl;
import org.lastbamboo.common.ice.candidate.IceTcpActiveCandidate;
import org.lastbamboo.common.ice.candidate.IceTcpHostPassiveCandidate;
import org.lastbamboo.common.ice.candidate.IceTcpRelayPassiveCandidate;
import org.lastbamboo.common.ice.candidate.IceTcpServerReflexiveSoCandidate;
import org.lastbamboo.common.ice.candidate.IceUdpHostCandidate;
import org.lastbamboo.common.ice.candidate.IceUdpPeerReflexiveCandidate;
import org.lastbamboo.common.ice.candidate.IceUdpRelayCandidate;
import org.lastbamboo.common.ice.candidate.IceUdpServerReflexiveCandidate;
import org.lastbamboo.common.ice.candidate.UdpIceCandidatePair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class for forming ICE check lists.
 */
public class IceCheckListCreatorImpl implements IceCheckListCreator
    {
    
    private final Logger LOG = LoggerFactory.getLogger(getClass());

    public IceCheckList createCheckList(
        final Collection<IceCandidate> localCandidates,
        final Collection<IceCandidate> remoteCandidates)
        {
        final Collection<IceCandidatePair> pairs = createPairsDataStructure();
           
        final IceCandidatePairFactory factory = 
            new IceCandidatePairFactoryImpl();
        for (final IceCandidate localCandidate : localCandidates)
            {
            for (final IceCandidate remoteCandidate : remoteCandidates)
                {
                if (shouldPair(localCandidate, remoteCandidate))
                    {
                    final IceCandidatePair pair = 
                        factory.createPair(localCandidate, remoteCandidate);
                    pairs.add(pair);
                    }
                }
            }
        
        final List<IceCandidatePair> convertedPairs = convertPairs(pairs);
        System.out.println(convertedPairs.size()+" converted");
        final List<IceCandidatePair> pruned = prunePairs(convertedPairs);
        System.out.println(pruned.size()+" after pruned");
        final Collection<IceCandidatePair> sorted = sortPairs(pruned);
        return new IceCheckListImpl(sorted);
        }

    private Collection<IceCandidatePair> sortPairs(
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

    private List<IceCandidatePair> convertPairs(
        final Collection<IceCandidatePair> pairs)
        {
        final List<IceCandidatePair> convertedPairs = 
            createPairsDataStructure();
        
        for (final IceCandidatePair pair : pairs)
            {
            final IceCandidatePair converted = convertPair(pair);
            if (converted != null)
                {
                convertedPairs.add(converted);
                }
            }
        
        return convertedPairs;
        }
        
    private IceCandidatePair convertPair(final IceCandidatePair pair)
        {
        final IceCandidate remoteCandidate = pair.getRemoteCandidate();
        final IceCandidate localCandidate = pair.getLocalCandidate();
        
        // We have to convert all local UDP server reflexice candidates to
        // their base and we have to ignore all TCP passive candidates.
        final IceCandidateVisitor<IceCandidatePair> visitor =
            new IceCandidateVisitor<IceCandidatePair>()
            {
            public IceCandidatePair visitUdpServerReflexiveCandidate(
                final IceUdpServerReflexiveCandidate candidate)
                {
                final IceCandidate base = candidate.getBaseCandidate();
                return new UdpIceCandidatePair(base, remoteCandidate, 
                    pair.getPriority());
                }

            public void visitCandidates(Collection<IceCandidate> candidates)
                {
                // TODO Auto-generated method stub
                
                }

            public IceCandidatePair visitTcpActiveCandidate(
                final IceTcpActiveCandidate candidate)
                {
                return pair;
                }

            public IceCandidatePair visitTcpHostPassiveCandidate(
                final IceTcpHostPassiveCandidate candidate)
                {
                return null;
                }

            public IceCandidatePair visitTcpRelayPassiveCandidate(
                final IceTcpRelayPassiveCandidate candidate)
                {
                return null;
                }

            public IceCandidatePair visitTcpServerReflexiveSoCandidate(
                final IceTcpServerReflexiveSoCandidate candidate)
                {
                return pair;
                }

            public IceCandidatePair visitUdpHostCandidate(
                final IceUdpHostCandidate candidate)
                {
                return pair;
                }

            public IceCandidatePair visitUdpPeerReflexiveCandidate(
                final IceUdpPeerReflexiveCandidate candidate)
                {
                return pair;
                }

            public IceCandidatePair visitUdpRelayCandidate(
                final IceUdpRelayCandidate candidate)
                {
                return pair;
                }
            };
        
        return localCandidate.accept(visitor);
        }

    /**
     * Prunes pairs by converting any non-host local candidates to host 
     * candidates and removing any duplicates created.
     * 
     * @param pairs The pairs to prune.
     */
    private List<IceCandidatePair> prunePairs(
        final Collection<IceCandidatePair> pairs)
        {
        // Note the pairs override hashCode using the local and the remote
        // candidates.  We just use the map here to identify pairs with the
        // same address for the local and the remote candidates.  This is
        // possible because we just converted local server reflexive 
        // candidates to their associated bases, according to the algorithm.
        //
        // If we find a duplicate pair, we always take the one with the 
        // higher priority.
        final Map<IceCandidatePair, IceCandidatePair> pairsMap =
            new HashMap<IceCandidatePair, IceCandidatePair>();
        
        for (final IceCandidatePair outerPair : pairs)
            {
            final IceCandidatePair curPair = pairsMap.get(outerPair);
            if (curPair == null)
                {
                pairsMap.put(outerPair, outerPair);
                }
            else
                {
                // If there's already one there, take the one with the 
                // higher priority.
                if (outerPair.getPriority() > curPair.getPriority())
                    {
                    LOG.debug("Adding higher priority pair");
                    pairsMap.put(outerPair, outerPair);
                    }
                }
            }
        
        final Collection<IceCandidatePair> prunedPairs = pairsMap.values();
        final List<IceCandidatePair> prunedPairsList = 
            createPairsDataStructure();
        
        prunedPairsList.addAll(prunedPairs);
        
        // Limit attacks based on the number of pairs.
        if (prunedPairsList.size() > 100)
            {
            return prunedPairsList.subList(0, 100);
            }
        return prunedPairsList;
        }

    private boolean shouldPair(final IceCandidate localCandidate, 
        final IceCandidate remoteCandidate)
        {
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
            case UNKNOWN:
                LOG.warn("Found unknown local transport!!");
                return false;
            default:
                return false;
            }
        }
    }
