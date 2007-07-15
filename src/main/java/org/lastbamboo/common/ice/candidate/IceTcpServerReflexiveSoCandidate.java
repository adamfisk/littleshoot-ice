package org.lastbamboo.common.ice.candidate;

import java.net.InetSocketAddress;

import org.lastbamboo.common.ice.IceCandidateType;
import org.lastbamboo.common.ice.IceCandidateVisitor;
import org.lastbamboo.common.ice.IceTransportProtocol;
import org.lastbamboo.common.stun.client.StunClient;

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
     * @param baseCandidate The local base candidate.
     * @param iceStunClient The ICE STUN client class.
     * @param controlling Whether or not this candidate is the controlling
     * candidate.
     */
    public IceTcpServerReflexiveSoCandidate(
        final InetSocketAddress socketAddress, final IceCandidate baseCandidate, 
        final StunClient iceStunClient, final boolean controlling)
        {
        super(socketAddress, baseCandidate, IceCandidateType.SERVER_REFLEXIVE, 
            IceTransportProtocol.TCP_SO, iceStunClient, 
            baseCandidate.getSocketAddress().getAddress(),
            baseCandidate.getSocketAddress().getPort(),
            controlling);
        }

    public <T> T accept(IceCandidateVisitor<T> visitor)
        {
        return visitor.visitTcpServerReflexiveSoCandidate(this);
        }
    }
