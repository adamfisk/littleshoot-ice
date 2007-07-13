package org.lastbamboo.common.ice;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

import org.lastbamboo.common.ice.candidate.IceCandidate;
import org.lastbamboo.common.ice.candidate.IceCandidatePair;
import org.lastbamboo.common.ice.candidate.AbstractIceCandidatePair;
import org.lastbamboo.common.ice.candidate.IceCandidatePairFactory;
import org.lastbamboo.common.ice.candidate.IceCandidatePairFactoryImpl;
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
        
        final List<IceCandidatePair> pruned = prunePairs(pairs);
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

    /**
     * Prunes pairs by converting any non-host local candidates to host 
     * candidates and removing any duplicates created.
     * 
     * @param pairs The pairs to prune.
     */
    private List<IceCandidatePair> prunePairs(
        final Collection<IceCandidatePair> pairs)
        {
        final List<IceCandidatePair> prunedPairs = 
            createPairsDataStructure();
        
        int count = 0;
        for (final IceCandidatePair pair : pairs)
            {
            // Limit attacks based on the number of pairs.  See:
            // draft-ietf-mmusic-ice-16.txt section 5.7.4.
            if (count > 100) break;
            
            final IceCandidate local = pair.getLocalCandidate();
            
            // NOTE: This deviates from the spec slightly.  We just eliminate
            // any pairs with a local server reflexive candidate because 
            // we always will have a corresponding pair with a local host
            // candidate that matches the base of the server reflexive
            // candidate.
            if (!prune(local))
                {
                prunedPairs.add(pair);
                }
            ++count;
            }
        
        return prunedPairs;
        }


    private boolean prune(final IceCandidate local)
        {
        if (local.getTransport() == IceTransportProtocol.UDP &&
            local.getType() == IceCandidateType.SERVER_REFLEXIVE)
            {
            // Prune it if it's server reflexive and UDP.
            return true;
            }
        // Prune all passive TCP in local candidates.
        if (local.getTransport() == IceTransportProtocol.TCP_PASS)
            {
            return true;
            }
        return false;
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
