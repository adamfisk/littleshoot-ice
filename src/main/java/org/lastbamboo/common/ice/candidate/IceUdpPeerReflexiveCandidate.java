package org.lastbamboo.common.ice.candidate;

import java.net.InetAddress;
import java.net.InetSocketAddress;

import org.lastbamboo.common.ice.IceCandidateType;
import org.lastbamboo.common.ice.IceCandidateVisitor;
import org.lastbamboo.common.ice.IceTransportProtocol;

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
     * @param baseAddress The address of the local base.
     * @param stunServerAddress The address of the STUN server used to 
     * determine this candidate.
     * @param relatedAddress The address related to this candidate.  In this
     * case, the base address.
     * @param relatedPort The port related to this candidate.  In this
     * case, the base port.
     * @param controlling Whether or not this candidate is the controlling
     * candidate.
     */
    public IceUdpPeerReflexiveCandidate(
        final InetSocketAddress peerReflexiveAddress,
        final InetAddress baseAddress, final InetAddress stunServerAddress,
        final InetAddress relatedAddress, final int relatedPort,
        final boolean controlling)
        {
        super(peerReflexiveAddress, baseAddress, 
            IceCandidateType.PEER_REFLEXIVE, IceTransportProtocol.UDP,
            stunServerAddress, relatedAddress, relatedPort, controlling);
        }

    public void accept(final IceCandidateVisitor visitor)
        {
        visitor.visitUdpPeerReflexiveCandidate(this);
        }

    }
