package org.lastbamboo.common.ice.candidate;

import java.net.InetAddress;
import java.net.InetSocketAddress;

import org.lastbamboo.common.ice.IceTransportProtocol;

/**
 * ICE simultaneous open TCP candidate for server reflexive hosts.
 */
public class IceTcpServerReflexiveSoCandidate 
    extends AbstractIceCandidate
    {

    /**
     * Creates a new ICE simultaneous open TCP candidate for server reflexive 
     * hosts.
     * 
     * @param socketAddress The address of the server reflexive candidate.
     * @param baseCandidate The local base candidate.
     * @param stunServerAddress The address of the STUN server.
     * @param controlling Whether or not this candidate is the controlling
     * candidate.
     */
    public IceTcpServerReflexiveSoCandidate(
        final InetSocketAddress socketAddress, final IceCandidate baseCandidate, 
        final InetAddress stunServerAddress, final boolean controlling)
        {
        super(socketAddress, 
            IceFoundationCalculator.calculateFoundation(
                IceCandidateType.SERVER_REFLEXIVE, 
                baseCandidate.getSocketAddress().getAddress(), 
                IceTransportProtocol.TCP_SO, stunServerAddress), 
        IceCandidateType.SERVER_REFLEXIVE, 
        IceTransportProtocol.TCP_SO, controlling, baseCandidate,
        baseCandidate.getSocketAddress().getAddress(),
        baseCandidate.getSocketAddress().getPort());
        }

    public <T> T accept(IceCandidateVisitor<T> visitor)
        {
        return visitor.visitTcpServerReflexiveSoCandidate(this);
        }
    }
