package org.lastbamboo.common.ice.candidate;

import java.net.InetAddress;
import java.net.InetSocketAddress;

import org.lastbamboo.common.ice.IceCandidateType;
import org.lastbamboo.common.ice.IceCandidateVisitor;
import org.lastbamboo.common.ice.IceTransportProtocol;

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
     * @param baseAddress The address of the local base.
     * @param stunServerAddress The address of the STUN server used to obtain
     * the candidate address.
     * @param relatedAddress The address related to this candidate.  In this
     * case, the base address.
     * @param relatedPort The port related to this candidate.  In this
     * case, the base port.
     * @param controlling Whether or not this candidate is the controlling
     * candidate.
     */
    public IceUdpServerReflexiveCandidate(
        final InetSocketAddress serverReflexiveAddress, 
        final InetAddress baseAddress, final InetAddress stunServerAddress,
        final InetAddress relatedAddress, final int relatedPort, 
        final boolean controlling)
        {
        super(serverReflexiveAddress, baseAddress, 
            IceCandidateType.SERVER_REFLEXIVE, IceTransportProtocol.UDP,
            stunServerAddress, relatedAddress, relatedPort, controlling);
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
