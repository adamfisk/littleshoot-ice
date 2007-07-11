package org.lastbamboo.common.ice.candidate;

import java.net.InetAddress;
import java.net.InetSocketAddress;

import org.lastbamboo.common.ice.IceCandidateType;
import org.lastbamboo.common.ice.IceCandidateVisitor;
import org.lastbamboo.common.ice.IceTransportProtocol;

/**
 * ICE passive TCP candidate for relayed hosts.
 */
public class IceTcpRelayPassiveCandidate extends AbstractStunServerIceCandidate
    {

    /**
     * Creates a new TCP passive ICE candidate for relayed hosts.
     * 
     * @param socketAddress The address of the relayed candidate.
     * @param baseAddress The address of the base interface for this candidate.
     * @param stunServerAddress The address of the STUN server used to obtain
     * the relay.
     * @param relatedAddress The address related to this candidate.  In this
     * case, the mapped address received in the Allocate Response.
     * @param relatedPort The port related to this candidate.  In this
     * case, the port in the mapped address received in the Allocate Response.
     * @param controlling Whether or not this candidate is the controlling
     * candidate.
     */
    public IceTcpRelayPassiveCandidate(final InetSocketAddress socketAddress,
        final InetAddress baseAddress, final InetAddress stunServerAddress,
        final InetAddress relatedAddress, final int relatedPort,
        final boolean controlling)
        {
        super(socketAddress, baseAddress, IceCandidateType.RELAYED, 
            IceTransportProtocol.TCP_PASS, stunServerAddress, relatedAddress,
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
        final int foundation, final InetAddress relatedAddress, 
        final int relatedPort, final boolean controlling, final long priority,
        final int componentId)
        {
        super(socketAddress, foundation, IceCandidateType.RELAYED, 
            IceTransportProtocol.TCP_PASS, relatedAddress, relatedPort, 
            controlling, priority, componentId);
        }

    public void accept(final IceCandidateVisitor visitor)
        {
        visitor.visitTcpRelayPassiveCandidate(this);
        }

    }
