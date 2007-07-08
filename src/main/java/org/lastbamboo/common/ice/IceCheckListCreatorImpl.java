package org.lastbamboo.common.ice;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.util.Collection;
import java.util.Comparator;
import java.util.TreeSet;

import org.lastbamboo.common.ice.candidate.IceCandidate;
import org.lastbamboo.common.ice.candidate.IceCandidatePair;

/**
 * Class for forming ICE check lists.
 */
public class IceCheckListCreatorImpl implements IceCheckListCreator
    {

    public Collection<IceCandidatePair> createCheckList(
        final Collection<IceCandidate> localCandidates,
        final Collection<IceCandidate> remoteCandidates)
        {
        final Collection<IceCandidatePair> pairs = createPairsCollection();
            
        for (final IceCandidate localCandidate : localCandidates)
            {
            for (final IceCandidate remoteCandidate : remoteCandidates)
                {
                if (shouldPair(localCandidate, remoteCandidate))
                    {
                    final IceCandidatePair pair = 
                        new IceCandidatePairImpl(localCandidate, 
                            remoteCandidate);

                    pairs.add(pair);
                    }
                }
            }
        
        return prunePairs(pairs);
        }

    private Collection<IceCandidatePair> createPairsCollection()
        {
        final Comparator<IceCandidatePair> comparator = 
            new Comparator<IceCandidatePair>()
            {
            public int compare(final IceCandidatePair pair1, 
                final IceCandidatePair pair2)
                {
                final long pair1Priority = pair1.getPriority();
                final long pair2Priority = pair2.getPriority();
                
                if (pair1Priority > pair2Priority) return -1;
                if (pair1Priority < pair2Priority) return 1;
                return 0;
                }
            };
        
        // Just use a sorted set.
        return new TreeSet<IceCandidatePair>(comparator);
        }

    /**
     * Prunes pairs by converting any non-host local candidates to host 
     * candidates and removing any duplicates created.
     * 
     * @param pairs The pairs to prune.
     */
    private Collection<IceCandidatePair> prunePairs(
        final Collection<IceCandidatePair> pairs)
        {
        final Collection<IceCandidatePair> prunedPairs = createPairsCollection();
        
        for (final IceCandidatePair pair : pairs)
            {
            final IceCandidate local = pair.getLocalCandidate();
            // NOTE: This deviates from the spec slightly.  We just eliminate
            // any pairs with a local server reflexive candidate because 
            // we always will have a corresponding pair with a local host
            // candidate that matches the base of the server reflexive
            // candidate.
            if (local.getType() != IceCandidateType.SERVER_REFLEXIVE)
                {
                prunedPairs.add(pair);
                }
            }
        
        return prunedPairs;
        }


    private boolean shouldPair(final IceCandidate localCandidate, 
        final IceCandidate remoteCandidate)
        {
        return (
            (localCandidate.getComponentId() == 
            remoteCandidate.getComponentId()) &&
            addressTypesMatch(localCandidate, remoteCandidate));
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
    
    private static final class IceCandidatePairImpl implements IceCandidatePair
        {
    
        private final IceCandidate m_localCandidate;
        private final IceCandidate m_remoteCandidate;
        private final long m_priority;

        private IceCandidatePairImpl(final IceCandidate localCandidate, 
            final IceCandidate remoteCandidate)
            {
            m_localCandidate = localCandidate;
            m_remoteCandidate = remoteCandidate;
            m_priority = calculatePriority(localCandidate, remoteCandidate);
            }

        private long calculatePriority(final IceCandidate localCandidate, 
            final IceCandidate remoteCandidate)
            {
            // Here's the formula for calculating pair priorities:
            // G = the priority of the controlling candidate.
            // D = the priority of the controlled candidate.
            // pair priority = 2^32*MIN(G,D) + 2*MAX(G,D) + (G>D?1:0)
            // 
            // Below we use:
            // pair priority = A + B + C
            final long G;
            final long D;
            if (localCandidate.isControlling())
                {
                G = localCandidate.getPriority();
                D = remoteCandidate.getPriority();
                }
            else
                {
                G = remoteCandidate.getPriority();
                D = localCandidate.getPriority();
                }
            final long A = (long) (Math.pow(2, 32) * Math.min(G, D));
            final long B = 2 * Math.max(G, D);
            final int C = G > D ? 1 : 0;
            
            final long pairPriority = A + B + C;
            return pairPriority;
            }

        public IceCandidate getLocalCandidate()
            {
            return m_localCandidate;
            }

        public IceCandidate getRemoteCandidate()
            {
            return m_remoteCandidate;
            }

        public long getPriority()
            {
            return this.m_priority;
            }
        
        public String toString()
            {
            return 
                "priority: "+this.m_priority+"\n"+
                "local:    "+this.m_localCandidate.getPriority()+"\n"+
                "remote:   "+this.m_remoteCandidate.getPriority();
            }
        }

    }
