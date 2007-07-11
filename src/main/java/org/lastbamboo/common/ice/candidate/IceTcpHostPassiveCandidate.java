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

    /**
     * Creates a new TCP passive ICE candidate for the local host.
     * 
     * @param socketAddress The address of the local host.
     * @param foundation The foundation for the candidate. 
     * @param controlling Whether or not this candidate is the controlling
     * candidate.
     * @param priority The priority.
     * @param componentId The component ID.
     */
    public IceTcpHostPassiveCandidate(final InetSocketAddress socketAddress, 
        final int foundation, final boolean controlling, final int priority, 
        final int componentId)
        {
        super(socketAddress, foundation,
            IceCandidateType.HOST, IceTransportProtocol.TCP_PASS,
            priority, controlling, componentId);
        }

    public void accept(final IceCandidateVisitor visitor)
        {
        visitor.visitTcpHostPassiveCandidate(this);
        }

    }
