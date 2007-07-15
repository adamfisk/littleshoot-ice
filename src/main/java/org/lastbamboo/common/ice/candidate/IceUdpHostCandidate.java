package org.lastbamboo.common.ice.candidate;

import java.net.InetSocketAddress;

import org.lastbamboo.common.ice.IceCandidateType;
import org.lastbamboo.common.ice.IceCandidateVisitor;
import org.lastbamboo.common.ice.IceTransportProtocol;
import org.lastbamboo.common.stun.client.StunClient;

/**
 * ICE UDP candidate for the local host.
 */
public class IceUdpHostCandidate extends AbstractIceCandidate
    {

    /**
     * Creates a new UDP ICE candidate for the local host.
     * 
     * @param stunClient The STUN client.
     * @param controlling Whether or not this candidate is the controlling
     * candidate.
     */
    public IceUdpHostCandidate(final StunClient stunClient, 
        final boolean controlling)
        {
        super(stunClient, IceCandidateType.HOST, IceTransportProtocol.UDP, 
            controlling);
        }


    /**
     * Creates a new UDP ICE candidate for the local host.
     * 
     * @param socketAddress The address of the local host.
     * @param foundation The foundation for the candidate.
     * @param priority The priority.
     * @param controlling Whether or not this candidate is the controlling
     * candidate.
     * @param componentId The component ID.
     */
    public IceUdpHostCandidate(final InetSocketAddress socketAddress, 
        final int foundation, final long priority, final boolean controlling,
        final int componentId)
        {
        super(socketAddress, foundation,
            IceCandidateType.HOST, IceTransportProtocol.UDP,
            priority, controlling, componentId, null);
        }

    public <T> T accept(IceCandidateVisitor<T> visitor)
        {
        return visitor.visitUdpHostCandidate(this);
        }

    }
