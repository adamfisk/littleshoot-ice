package org.lastbamboo.common.ice.candidate;

import java.net.InetAddress;
import java.net.InetSocketAddress;

import org.lastbamboo.common.ice.IceCandidateType;
import org.lastbamboo.common.ice.IceCandidateVisitor;
import org.lastbamboo.common.ice.IceTransportProtocol;

/**
 * Relay ICE UDP candidate.
 */
public class IceUdpRelayCandidate extends AbstractStunServerIceCandidate
    {

    /**
     * Creates a new UDP ICE candidate for a relay candidate.
     * 
     * @param relayAddress The address of the relay candidate.
     * @param baseAddress The address of the local base.
     * @param stunServerAddress The address of the STUN server used to obtain
     * the candidate address.
     * @param relatedAddress The address related to this candidate.  In this
     * case, the mapped address received in the Allocate Response.
     * @param relatedPort The port related to this candidate.  In this
     * case, the port in the mapped address received in the Allocate Response.
     * @param controlling Whether or not this candidate is the controlling
     * candidate.
     */
    public IceUdpRelayCandidate(final InetSocketAddress relayAddress, 
        final InetAddress baseAddress, final InetAddress stunServerAddress,
        final InetAddress relatedAddress, final int relatedPort,
        final boolean controlling)
        {
        super(relayAddress, baseAddress, IceCandidateType.RELAYED, 
            IceTransportProtocol.UDP, stunServerAddress, relatedAddress,
            relatedPort, controlling);
        }

    public <T> T accept(IceCandidateVisitor<T> visitor)
        {
        return visitor.visitUdpRelayCandidate(this);
        }

    }
