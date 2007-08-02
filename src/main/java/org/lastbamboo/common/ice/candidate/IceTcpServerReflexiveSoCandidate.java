package org.lastbamboo.common.ice.candidate;

import java.net.InetSocketAddress;

import org.lastbamboo.common.ice.IceTransportProtocol;
import org.lastbamboo.common.stun.client.StunClient;

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
     * @param stunClient The ICE STUN client class.
     * @param controlling Whether or not this candidate is the controlling
     * candidate.
     */
    public IceTcpServerReflexiveSoCandidate(
        final InetSocketAddress socketAddress, final IceCandidate baseCandidate, 
        final StunClient stunClient, final boolean controlling)
        {
        super(socketAddress, 
            IceFoundationCalculator.calculateFoundation(
                IceCandidateType.SERVER_REFLEXIVE, 
                baseCandidate.getSocketAddress().getAddress(), 
                IceTransportProtocol.TCP_SO, stunClient.getStunServerAddress()), 
        IceCandidateType.SERVER_REFLEXIVE, 
        IceTransportProtocol.TCP_SO, controlling, baseCandidate,
        baseCandidate.getSocketAddress().getAddress(),
        baseCandidate.getSocketAddress().getPort(), stunClient);
        }

    public <T> T accept(IceCandidateVisitor<T> visitor)
        {
        return visitor.visitTcpServerReflexiveSoCandidate(this);
        }
    }
