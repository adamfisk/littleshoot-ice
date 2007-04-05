package org.lastbamboo.common.ice;

import java.net.InetSocketAddress;

import org.apache.commons.id.uuid.UUID;

/**
 * An ICE candidate for TCP simultaneous open.
 */
public class TcpSoIceCandidate extends AbstractIceCandidate
    {

    /**
     * Creates a new ICE candidate for TCP simultaneous open.
     * 
     * @param candidateId The unique ID of the candidate.
     * @param transportId The unique ID of the tranport address.
     * @param priority The priority of this candidate versus other candidates.
     * @param socketAddress The address and port of the candidate.
     */
    protected TcpSoIceCandidate(final int candidateId, final UUID transportId, 
        final int priority, final InetSocketAddress socketAddress)
        {
        super(candidateId, transportId, priority, socketAddress);
        }

    public String getTransport()
        {
        return IceConstants.TCP_SO;
        }

    public void accept(final IceCandidateVisitor visitor)
        {
        visitor.visitTcpSoIceCandidate(this);
        }
    }
