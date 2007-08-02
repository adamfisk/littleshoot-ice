package org.lastbamboo.common.ice.candidate;

import java.net.InetAddress;
import java.net.InetSocketAddress;

import org.lastbamboo.common.ice.IceTransportProtocol;
import org.lastbamboo.common.stun.client.StunClient;

/**
 * Relay ICE UDP candidate.
 */
public class IceUdpRelayCandidate extends AbstractIceCandidate
    {

    /**
     * Creates a new UDP ICE candidate for a relay candidate.  Note the base
     * candidate for relays is the candidate itself.
     * 
     * @param relayAddress The address of the relay candidate.
     * @param stunClient The ICE STUN client class.
     * @param relatedAddress The address related to this candidate.  In this
     * case, the mapped address received in the Allocate Response.
     * @param relatedPort The port related to this candidate.  In this
     * case, the port in the mapped address received in the Allocate Response.
     * @param controlling Whether or not this candidate is the controlling
     * candidate.
     */
    public IceUdpRelayCandidate(final InetSocketAddress relayAddress, 
        final StunClient stunClient,
        final InetAddress relatedAddress, final int relatedPort,
        final boolean controlling)
        {
        super(relayAddress, 
            IceFoundationCalculator.calculateFoundation(IceCandidateType.RELAYED, 
               relayAddress.getAddress(), 
               IceTransportProtocol.UDP, stunClient.getStunServerAddress()), 
            IceCandidateType.RELAYED, IceTransportProtocol.UDP, controlling, 
            null, relatedAddress, relatedPort, stunClient);
        }

    public <T> T accept(IceCandidateVisitor<T> visitor)
        {
        return visitor.visitUdpRelayCandidate(this);
        }

    }
