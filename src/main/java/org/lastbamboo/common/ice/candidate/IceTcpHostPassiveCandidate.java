package org.lastbamboo.common.ice.candidate;

import java.net.InetSocketAddress;

import org.lastbamboo.common.ice.IceCandidateType;
import org.lastbamboo.common.ice.IceCandidateVisitor;
import org.lastbamboo.common.ice.IceTransportProtocol;

/**
 * ICE passive TCP candidate for the local host.
 */
public class IceTcpHostPassiveCandidate extends AbstractIceCandidate
    {

    /**
     * Creates a new TCP passive ICE candidate for the local host.
     * 
     * @param socketAddress The address of the local host.
     * @param controlling Whether or not this candidate is the controlling
     * candidate.
     */
    public IceTcpHostPassiveCandidate(final InetSocketAddress socketAddress,
        final boolean controlling)
        {
        super(socketAddress, socketAddress.getAddress(), IceCandidateType.HOST, 
            IceTransportProtocol.TCP_PASS, controlling);
        }

    public void accept(final IceCandidateVisitor visitor)
        {
        visitor.visitTcpHostPassiveCandidate(this);
        }

    }
