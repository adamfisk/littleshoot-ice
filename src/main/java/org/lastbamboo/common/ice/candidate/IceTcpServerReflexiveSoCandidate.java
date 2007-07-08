package org.lastbamboo.common.ice.candidate;

import java.net.InetAddress;
import java.net.InetSocketAddress;

import org.lastbamboo.common.ice.IceCandidateType;
import org.lastbamboo.common.ice.IceCandidateVisitor;
import org.lastbamboo.common.ice.IceTransportProtocol;

/**
 * ICE simultaneous open TCP candidate for server reflexive hosts.
 */
public class IceTcpServerReflexiveSoCandidate 
    extends AbstractStunServerIceCandidate
    {

    /**
     * Creates a new ICE simultaneous open TCP candidate for server reflexive 
     * hosts.
     * 
     * @param socketAddress The address of the server reflexive candidate.
     * @param baseAddress The address of the base interface for this candidate.
     * @param stunServerAddress The address of the STUN server used to obtain
     * the server reflexive candidate.
     * @param relatedAddress The address related to this candidate.  In this
     * case, the base address.
     * @param relatedPort The port related to this candidate.  In this
     * case, the base port.
     * @param controlling Whether or not this candidate is the controlling
     * candidate.
     */
    public IceTcpServerReflexiveSoCandidate(
        final InetSocketAddress socketAddress, final InetAddress baseAddress, 
        final InetAddress stunServerAddress, final InetAddress relatedAddress,
        final int relatedPort, final boolean controlling)
        {
        super(socketAddress, baseAddress, IceCandidateType.SERVER_REFLEXIVE, 
            IceTransportProtocol.TCP_SO, stunServerAddress, relatedAddress,
            relatedPort, controlling);
        }

    public void accept(final IceCandidateVisitor visitor)
        {
        visitor.visitTcpServerReflexiveSoCandidate(this);
        }

    }
