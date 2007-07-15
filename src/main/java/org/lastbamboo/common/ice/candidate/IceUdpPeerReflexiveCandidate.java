package org.lastbamboo.common.ice.candidate;

import java.net.InetSocketAddress;

import org.lastbamboo.common.ice.IceCandidateType;
import org.lastbamboo.common.ice.IceCandidateVisitor;
import org.lastbamboo.common.ice.IceTransportProtocol;
import org.lastbamboo.common.stun.client.StunClient;

/**
 * Peer reflexive ICE UDP candidate.
 */
public class IceUdpPeerReflexiveCandidate extends AbstractStunServerIceCandidate
    {

    /**
     * Creates a new UDP ICE candidate for the server peer candidate.
     * 
     * @param peerReflexiveAddress The address of the peer reflexive 
     * candidate.
     * @param baseCandidate The local base candidate.
     * @param iceStunClient The ICE STUN client class.
     * @param controlling Whether or not this candidate is the controlling
     * candidate.
     */
    public IceUdpPeerReflexiveCandidate(
        final InetSocketAddress peerReflexiveAddress,
        final IceCandidate baseCandidate, final StunClient iceStunClient,
        final boolean controlling)
        {
        super(peerReflexiveAddress, baseCandidate, 
            IceCandidateType.PEER_REFLEXIVE, IceTransportProtocol.UDP,
            iceStunClient, 
            baseCandidate.getSocketAddress().getAddress(), 
            baseCandidate.getSocketAddress().getPort(), controlling);
        }

    public <T> T accept(IceCandidateVisitor<T> visitor)
        {
        return visitor.visitUdpPeerReflexiveCandidate(this);
        }

    }
