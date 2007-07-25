package org.lastbamboo.common.ice.candidate;

import java.net.InetSocketAddress;

import org.lastbamboo.common.ice.IceCandidateType;
import org.lastbamboo.common.ice.IceCandidateVisitor;
import org.lastbamboo.common.ice.IceTransportProtocol;

/**
 * ICE active TCP candidate.
 */
public class IceTcpActiveCandidate extends AbstractIceCandidate
    {

    /**
     * Creates a new TCP active ICE candidate.
     * 
     * @param socketAddress The address of the local host.
     * @param controlling Whether or not this candidate is the controlling
     * candidate.
     */
    public IceTcpActiveCandidate(final InetSocketAddress socketAddress,
        final boolean controlling)
        {
        super(socketAddress, socketAddress.getAddress(), IceCandidateType.HOST, 
            IceTransportProtocol.TCP_ACT, controlling, null);
        }

    public <T> T accept(IceCandidateVisitor<T> visitor)
        {
        return visitor.visitTcpActiveCandidate(this);
        }

    }
