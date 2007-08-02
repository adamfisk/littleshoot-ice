package org.lastbamboo.common.ice.candidate;

import java.net.InetSocketAddress;

import org.lastbamboo.common.ice.IceCandidateType;
import org.lastbamboo.common.ice.IceCandidateVisitor;
import org.lastbamboo.common.ice.IceTransportProtocol;
import org.lastbamboo.common.stun.client.StunClient;

/**
 * Peer reflexive ICE UDP candidate.
 */
public class IceUdpPeerReflexiveCandidate extends AbstractIceCandidate
    {

    /**
     * Creates a new UDP ICE candidate for the server peer candidate.
     * 
     * @param peerReflexiveAddress The address of the peer reflexive 
     * candidate.
     * @param baseCandidate The local base candidate.
     * @param stunClient The ICE STUN client class.
     * @param controlling Whether or not this candidate is the controlling
     * candidate.
     */
    public IceUdpPeerReflexiveCandidate(
        final InetSocketAddress peerReflexiveAddress,
        final IceCandidate baseCandidate, final StunClient stunClient,
        final boolean controlling, final long priority)
        {
        super(peerReflexiveAddress, 
            IceFoundationCalculator.calculateFoundation(IceCandidateType.PEER_REFLEXIVE, 
               baseCandidate.getSocketAddress().getAddress(), 
               IceTransportProtocol.UDP, stunClient.getStunServerAddress()), 
            IceCandidateType.PEER_REFLEXIVE, 
            IceTransportProtocol.UDP, priority, controlling, 
            DEFAULT_COMPONENT_ID, baseCandidate, 
            baseCandidate.getSocketAddress().getAddress(), 
            baseCandidate.getSocketAddress().getPort(), stunClient);
        }
    
    /**
     * Creates a new UDP ICE candidate for the server peer candidate.
     * 
     * @param peerReflexiveAddress The address of the peer reflexive 
     * candidate.
     * @param foundation The foundation.
     * @param componentId The component ID.
     * @param controlling Whether or not this candidate is the controlling
     * candidate.
     * @param priority The priority for the candidate.
     */
    public IceUdpPeerReflexiveCandidate(
        final InetSocketAddress peerReflexiveAddress,
        final String foundation, final int componentId,
        final boolean controlling, final long priority)
        {
        super(peerReflexiveAddress, foundation, 
            IceCandidateType.PEER_REFLEXIVE, IceTransportProtocol.UDP, 
            priority, controlling, componentId, null, null, -1, null);
        }

    public <T> T accept(final IceCandidateVisitor<T> visitor)
        {
        return visitor.visitUdpPeerReflexiveCandidate(this);
        }

    }
