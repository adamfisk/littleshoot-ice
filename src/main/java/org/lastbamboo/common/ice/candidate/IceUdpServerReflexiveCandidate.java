package org.lastbamboo.common.ice.candidate;

import java.net.InetAddress;
import java.net.InetSocketAddress;

import org.lastbamboo.common.ice.IceCandidateType;
import org.lastbamboo.common.ice.IceCandidateVisitor;
import org.lastbamboo.common.ice.IceTransportProtocol;
import org.lastbamboo.common.stun.client.StunClient;

/**
 * Server reflexive ICE UDP candidate.
 */
public class IceUdpServerReflexiveCandidate 
    extends AbstractStunServerIceCandidate
    {

    /**
     * Creates a new UDP ICE candidate for the server reflexive candidate.
     * 
     * @param serverReflexiveAddress The address of the server reflexive 
     * candidate.
     * @param baseCandidate The base candidate.
     * @param iceStunClient The ICE STUN client class.
     * @param controlling Whether or not this candidate is the controlling
     * candidate.
     */
    public IceUdpServerReflexiveCandidate(
        final InetSocketAddress serverReflexiveAddress, 
        final IceCandidate baseCandidate, 
        final StunClient iceStunClient,
        final boolean controlling)
        {
        super(serverReflexiveAddress, baseCandidate, 
            IceCandidateType.SERVER_REFLEXIVE, IceTransportProtocol.UDP,
            iceStunClient, 
            baseCandidate.getSocketAddress().getAddress(), 
            baseCandidate.getSocketAddress().getPort(), 
            controlling);
        }

    /**
     * Creates a new UDP ICE candidate for the server reflexive candidate.
     * 
     * @param serverReflexiveAddress The address of the server reflexive 
     * candidate.
     * the candidate address.
     * @param foundation The foundation.
     * @param relatedAddress The address related to this candidate.  In this
     * case, the base address.
     * @param relatedPort The port related to this candidate.  In this
     * case, the base port.
     * @param controlling Whether or not this candidate is the controlling
     * candidate.
     * @param priority The priority of the candidate.
     * @param componentId The component ID. 
     */
    public IceUdpServerReflexiveCandidate(
        final InetSocketAddress serverReflexiveAddress, final int foundation, 
        final InetAddress relatedAddress, final int relatedPort,
        final boolean controlling, final long priority, final int componentId)
        {
        super(serverReflexiveAddress, foundation, 
            IceCandidateType.SERVER_REFLEXIVE, IceTransportProtocol.UDP, 
            relatedAddress, relatedPort, controlling, priority, componentId);
        }

    public <T> T accept(final IceCandidateVisitor<T> visitor)
        {
        return visitor.visitUdpServerReflexiveCandidate(this);
        }

    }
