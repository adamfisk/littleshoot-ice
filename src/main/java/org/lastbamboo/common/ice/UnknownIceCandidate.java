package org.lastbamboo.common.ice;

import java.net.InetSocketAddress;

import org.apache.commons.id.uuid.UUID;


/**
 * Placeholder class for an ICE candidate for media transport that we
 * don't understand.
 */
public final class UnknownIceCandidate extends AbstractIceCandidate
    {

    private final String m_transport;

    public UnknownIceCandidate(final int candidateId, final UUID transportId, 
        final int priority, final InetSocketAddress socketAddress, 
        final String transport)
        {
        super(candidateId, transportId, priority, socketAddress);
        this.m_transport = transport;
        }

    public IceTransportProtocol getTransport()
        {
        return IceTransportProtocol.UNKNOWN;
        }

    public void accept(final IceCandidateVisitor visitor)
        {
        visitor.visitUnknownIceCandidate(this);
        }

    }
