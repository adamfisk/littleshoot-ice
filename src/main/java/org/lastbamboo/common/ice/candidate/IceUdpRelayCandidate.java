package org.lastbamboo.common.ice.candidate;

import java.net.InetAddress;
import java.net.InetSocketAddress;

import org.lastbamboo.common.ice.IceCandidateType;
import org.lastbamboo.common.ice.IceCandidateVisitor;
import org.lastbamboo.common.ice.IceTransportProtocol;
import org.lastbamboo.common.stun.client.StunClient;

/**
 * Relay ICE UDP candidate.
 */
public class IceUdpRelayCandidate extends AbstractStunServerIceCandidate
    {

    /**
     * Creates a new UDP ICE candidate for a relay candidate.  Note the base
     * candidate for relays is the candidate itself.
     * 
     * @param relayAddress The address of the relay candidate.
     * @param iceStunClient The ICE STUN client class.
     * @param relatedAddress The address related to this candidate.  In this
     * case, the mapped address received in the Allocate Response.
     * @param relatedPort The port related to this candidate.  In this
     * case, the port in the mapped address received in the Allocate Response.
     * @param controlling Whether or not this candidate is the controlling
     * candidate.
     */
    public IceUdpRelayCandidate(final InetSocketAddress relayAddress, 
        final StunClient iceStunClient,
        final InetAddress relatedAddress, final int relatedPort,
        final boolean controlling)
        {
        super(relayAddress, IceCandidateType.RELAYED, 
            IceTransportProtocol.UDP, iceStunClient,
            relatedAddress, relatedPort, controlling);
        }

    public <T> T accept(IceCandidateVisitor<T> visitor)
        {
        return visitor.visitUdpRelayCandidate(this);
        }

    }
