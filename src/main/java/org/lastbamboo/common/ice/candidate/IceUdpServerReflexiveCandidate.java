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
     */
    public IceUdpServerReflexiveCandidate(
        final InetSocketAddress serverReflexiveAddress, 
        final InetAddress baseAddress, final InetAddress stunServerAddress,
        final InetAddress relatedAddress, final int relatedPort)
        {
        super(serverReflexiveAddress, baseAddress, 
            IceCandidateType.SERVER_REFLEXIVE, IceTransportProtocol.UDP,
            stunServerAddress, relatedAddress, relatedPort);
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
     */
    public IceUdpServerReflexiveCandidate(
        final InetSocketAddress serverReflexiveAddress, final int foundation, 
        final InetAddress relatedAddress, final int relatedPort)
        {
        super(serverReflexiveAddress, foundation, 
            IceCandidateType.SERVER_REFLEXIVE, IceTransportProtocol.UDP, 
            relatedAddress, relatedPort);
        }

    public void accept(final IceCandidateVisitor visitor)
        {
        visitor.visitUdpServerReflexiveCandidate(this);
        }

    }
