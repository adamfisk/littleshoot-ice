package org.lastbamboo.common.ice;

import java.net.InetSocketAddress;

import org.apache.commons.id.uuid.UUID;

/**
 * Candidate for ICE where the transport protocol is TCP and the connection
 * is passive -- the other side initiates the connection.
 */
public final class TcpPassiveIceCandidate extends AbstractIceCandidate
    {

    /**
     * Creates a new ICE candidate for a specific media.  This encapsulates
     * the data for one ICE candidate.
     * 
     * @param candidateId The unique ID of the candidate.
     * @param transportId The unique ID of the tranport address.
     * @param priority The priority of this candidate versus other candidates.
     * @param socketAddress The address and port of the candidate.
     */
    public TcpPassiveIceCandidate(final int candidateId, final UUID transportId, 
        final int priority, final InetSocketAddress socketAddress)
        {
        super(candidateId, transportId, priority, socketAddress);
        }

    public IceTransportProtocol getTransport()
        {
        return IceTransportProtocol.TCP_PASS;
        }

    public void accept(final IceCandidateVisitor visitor)
        {
        visitor.visitTcpPassiveIceCandidate(this);
        }

    }
