package org.lastbamboo.common.ice.candidate;


/**
 * A UDP ICE candidate pair. 
 */
public class UdpIceCandidatePair extends AbstractIceCandidatePair
    {

    /**
     * Pair of UDP ICE candidates.
     * 
     * @param localCandidate The local candidate.
     * @param remoteCandidate The remote candidate.
     */
    public UdpIceCandidatePair(final IceCandidate localCandidate, 
        final IceCandidate remoteCandidate)
        {
        super(localCandidate, remoteCandidate);
        }

    public <T> T accept(final IceCandidatePairVisitor<T> visitor)
        {
        return visitor.visitUdpIceCandidatePair(this);
        }

    }
