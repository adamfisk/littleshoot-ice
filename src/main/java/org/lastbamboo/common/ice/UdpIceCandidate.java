package org.lastbamboo.common.ice;

import java.net.InetSocketAddress;

import org.apache.commons.id.uuid.UUID;

/**
 * ICE candidate for transport of a specific media over UDP.
 */
public final class UdpIceCandidate extends AbstractIceCandidate
    {

    /**
     * Creates a new ICE candidate for a specific media.  This encapsulates
     * the data for one ICE candidate.
     * @param candidateId The unique ID of the candidate.
     * @param transportId The unique ID of the tranport address.
     * @param priority The priority of this candidate versus other candidates.
     * @param socketAddress The socket address of the candidate.
     */
    public UdpIceCandidate(final int candidateId, final UUID transportId, 
        final int priority, final InetSocketAddress socketAddress)
        {
        super(candidateId, transportId, priority, socketAddress);
        }

    public IceTransportProtocol getTransport()
        {
        return IceTransportProtocol.UDP;
        }

    public void accept(final IceCandidateVisitor visitor)
        {
        visitor.visitUdpIceCandidate(this);
        }
    }
