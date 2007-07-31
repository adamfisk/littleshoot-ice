package org.lastbamboo.common.ice.candidate;

import java.net.InetAddress;
import java.net.InetSocketAddress;

import org.lastbamboo.common.ice.IceCandidateType;
import org.lastbamboo.common.ice.IceCandidateVisitor;
import org.lastbamboo.common.ice.IceTransportProtocol;
import org.lastbamboo.common.stun.client.StunClient;

/**
 * ICE passive TCP candidate for relayed hosts.
 */
public class IceTcpRelayPassiveCandidate extends AbstractIceCandidate
    {

    /**
     * Creates a new TCP passive ICE candidate for relayed hosts.
     * 
     * @param socketAddress The address of the relayed candidate.
     * @param iceStunClient The ICE STUN client class.
     * @param relatedAddress The address related to this candidate.  In this
     * case, the mapped address received in the Allocate Response.
     * @param relatedPort The port related to this candidate.  In this
     * case, the port in the mapped address received in the Allocate Response.
     * @param controlling Whether or not this candidate is the controlling
     * candidate.
     */
    public IceTcpRelayPassiveCandidate(final InetSocketAddress socketAddress,
        final StunClient iceStunClient,
        final InetAddress relatedAddress, final int relatedPort,
        final boolean controlling)
        {
        super(socketAddress, IceCandidateType.RELAYED, 
            IceTransportProtocol.TCP_PASS, iceStunClient, relatedAddress,
            relatedPort, controlling);
        }
    
    /**
     * Creates a new TCP passive ICE candidate for relayed hosts.
     * 
     * @param socketAddress The address of the relayed candidate.
     * @param foundation The foundation.
     * @param relatedAddress The address related to this candidate.  In this
     * case, the mapped address received in the Allocate Response.
     * @param relatedPort The port related to this candidate.  In this
     * case, the port in the mapped address received in the Allocate Response.
     * @param controlling Whether or not this candidate is the controlling
     * candidate.
     * @param priority The priority of the candidate.
     * @param componentId The component ID.
     */
    public IceTcpRelayPassiveCandidate(final InetSocketAddress socketAddress,
        final String foundation, final InetAddress relatedAddress, 
        final int relatedPort, final boolean controlling, final long priority,
        final int componentId)
        {
        super(socketAddress, foundation, IceCandidateType.RELAYED, 
            IceTransportProtocol.TCP_PASS, priority, controlling, 
            componentId, null, relatedAddress, relatedPort, null);
        }

    public <T> T accept(final IceCandidateVisitor<T> visitor)
        {
        return visitor.visitTcpRelayPassiveCandidate(this);
        }

    }
