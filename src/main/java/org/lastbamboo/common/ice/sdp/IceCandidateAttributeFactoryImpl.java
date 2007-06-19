package org.lastbamboo.common.ice.sdp;

import java.net.InetSocketAddress;


import org.apache.commons.id.uuid.UUID;
import org.lastbamboo.common.ice.IceCandidate;
import org.lastbamboo.common.ice.TcpPassiveIceCandidate;
import org.lastbamboo.common.ice.UdpIceCandidate;
import org.lastbamboo.common.sdp.api.Attribute;
import org.lastbamboo.common.sdp.api.SdpFactory;

/**
 * Implementation of a factory for encoding ICE candidates into SDP.
 */
public final class IceCandidateAttributeFactoryImpl implements
    IceCandidateAttributeFactory
    {

    private final SdpFactory m_sdpFactory;

    /**
     * Creates a new factory for encoding ICE candidate bindings in SDP.
     * 
     * @param sdpFactory The factory for creating SDP attributes.
     */
    public IceCandidateAttributeFactoryImpl(final SdpFactory sdpFactory)
        {
        this.m_sdpFactory = sdpFactory;
        }
    
    public Attribute createTcpIceCandidateAttribute(
        final InetSocketAddress socketAddress, final int candidateId, 
        final int priority)
        {
        final IceCandidate candidate = 
            new TcpPassiveIceCandidate(candidateId, UUID.randomUUID(),
                priority, socketAddress);
        
        return createIceCandidateAttribute(candidate);
        }

    public Attribute createUdpIceCandidateAttribute(
        final InetSocketAddress socketAddress, final int candidateId, 
        final int priority)
        {
        final IceCandidate candidate = 
            new UdpIceCandidate(candidateId, UUID.randomUUID(),
                priority, socketAddress);
        
        return createIceCandidateAttribute(candidate);
        }
    
    private Attribute createIceCandidateAttribute(final IceCandidate candidate)
        {
        final StringBuilder sb = new StringBuilder();
        sb.append(candidate.getCandidateId());
        sb.append(" ");
        sb.append(candidate.getTransportId());
        sb.append(" ");
        sb.append(candidate.getTransport().getName());
        sb.append(" ");
        sb.append(candidate.getPriority());
        sb.append(" ");
        sb.append(candidate.getSocketAddress().getAddress().getHostAddress());
        sb.append(" ");
        sb.append(candidate.getSocketAddress().getPort());
        
        return this.m_sdpFactory.createAttribute("candidate", sb.toString());
        }
    }
