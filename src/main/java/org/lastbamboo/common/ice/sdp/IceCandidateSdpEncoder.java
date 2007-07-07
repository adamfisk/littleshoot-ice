package org.lastbamboo.common.ice.sdp;

import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.util.Collection;
import java.util.Vector;

import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.ObjectUtils.Null;
import org.lastbamboo.common.ice.IceCandidateVisitor;
import org.lastbamboo.common.ice.candidate.AbstractStunServerIceCandidate;
import org.lastbamboo.common.ice.candidate.IceCandidate;
import org.lastbamboo.common.ice.candidate.IceTcpHostPassiveCandidate;
import org.lastbamboo.common.ice.candidate.IceTcpRelayPassiveCandidate;
import org.lastbamboo.common.ice.candidate.IceTcpServerReflexiveSoCandidate;
import org.lastbamboo.common.ice.candidate.IceUdpHostCandidate;
import org.lastbamboo.common.ice.candidate.IceUdpPeerReflexiveCandidate;
import org.lastbamboo.common.ice.candidate.IceUdpRelayCandidate;
import org.lastbamboo.common.ice.candidate.IceUdpServerReflexiveCandidate;
import org.lastbamboo.common.sdp.api.Attribute;
import org.lastbamboo.common.sdp.api.Connection;
import org.lastbamboo.common.sdp.api.MediaDescription;
import org.lastbamboo.common.sdp.api.Origin;
import org.lastbamboo.common.sdp.api.SdpException;
import org.lastbamboo.common.sdp.api.SdpFactory;
import org.lastbamboo.common.sdp.api.SessionDescription;
import org.lastbamboo.common.sdp.api.SessionName;
import org.lastbamboo.common.sdp.api.TimeDescription;
import org.lastbamboo.common.util.NetworkUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class for encoding ICE candidates into SDP.
 */
public class IceCandidateSdpEncoder implements IceCandidateVisitor<Null>
    {
    
    private final Logger LOG = 
        LoggerFactory.getLogger(IceCandidateSdpEncoder.class);
    
    private final SdpFactory m_sdpFactory;
    private final SessionDescription m_sessionDescription;

    private final Vector<Attribute> m_candidates;

    private IceUdpServerReflexiveCandidate m_udpServerReflexiveCandidate;

    /**
     * Creates a new encoder for encoder ICE candidates into SDP.
     */
    public IceCandidateSdpEncoder()
        {
        this.m_sdpFactory = new SdpFactory();
        final InetAddress address = getAddress();
        final String addrType = 
            address instanceof Inet6Address ? "IP6" : "IP4";
        
        this.m_candidates = new Vector<Attribute>();
        
        try
            {
            final Origin o = 
                this.m_sdpFactory.createOrigin("-", 0, 0, "IN", addrType,
                address.getHostAddress());
            // "s=-"
            final SessionName s = this.m_sdpFactory.createSessionName("-");
            
            // "t=0 0"
            final TimeDescription t = this.m_sdpFactory.createTimeDescription();
            final Vector<TimeDescription> timeDescriptions = 
                new Vector<TimeDescription>();
            timeDescriptions.add(t);
            
            //this.m_mediaDescriptions = new Vector<MediaDescription>();
            this.m_sessionDescription = createSessionDescription(); 
            this.m_sessionDescription.setVersion(
                this.m_sdpFactory.createVersion(0));
            this.m_sessionDescription.setOrigin(o);
            this.m_sessionDescription.setSessionName(s);
            this.m_sessionDescription.setTimeDescriptions(timeDescriptions);
            }
        catch (final SdpException e)
            {
            LOG.error("Could not create SDP", e);
            throw new IllegalArgumentException("Could not create SDP", e);
            }
        }
    

    /**
     * Accesses the SDP as an array of bytes.
     * 
     * @return The SDP as an array of bytes.
     */
    public byte[] getSdp()
        {
        return this.m_sessionDescription.toBytes();
        }

    private InetAddress getAddress()
        {
        try
            {
            return NetworkUtils.getLocalHost();
            }
        catch (final UnknownHostException e)
            {
            // Should never happen.
            LOG.error("Could not resolve host", e);
            throw new RuntimeException("Could not resolve host", e);
            }
        }

    private SessionDescription createSessionDescription()
        {
        try
            {
            return this.m_sdpFactory.createSessionDescription();
            }
        catch (final SdpException e)
            {
            LOG.error("Could not create SDP", e);
            throw new IllegalArgumentException("Could not create SDP", e);
            }
        }

    public void visitCandidates(final Collection<IceCandidate> candidates)
        {
        for (final IceCandidate candidate : candidates)
            {
            candidate.accept(this);
            }
        
        try
            {
            // Use the UDP server reflexive address as the top level address.
            // This is slightly hacky because it relies on the UDP server
            // reflexive candidate being there.
            final MediaDescription md = 
                createMessageMediaDesc(this.m_udpServerReflexiveCandidate);
            md.setAttributes(m_candidates);
            
            LOG.debug("Adding media description");
            final Vector<MediaDescription> mediaDescriptions = 
                new Vector<MediaDescription>();
            mediaDescriptions.add(md);
            this.m_sessionDescription.setMediaDescriptions(
                mediaDescriptions);
            }
        catch (final SdpException e)
            {
            LOG.error("Could not add the media descriptions", e);
            }
        }
    
    public Null visitTcpHostPassiveCandidate(
        final IceTcpHostPassiveCandidate candidate)
        {
        addAttribute(candidate);
        return ObjectUtils.NULL;
        }


    public Null visitTcpRelayPassiveCandidate(
        final IceTcpRelayPassiveCandidate candidate)
        {
        addAttributeWithRelated(candidate);
        return ObjectUtils.NULL;
        }


    public Null visitTcpServerReflexiveSoCandidate(
        final IceTcpServerReflexiveSoCandidate candidate)
        {
        addAttributeWithRelated(candidate);
        return ObjectUtils.NULL;
        }


    public Null visitUdpHostCandidate(final IceUdpHostCandidate candidate)
        {
        addAttribute(candidate);
        return ObjectUtils.NULL;
        }


    public Null visitUdpPeerReflexiveCandidate(
        final IceUdpPeerReflexiveCandidate candidate)
        {
        addAttributeWithRelated(candidate);
        return ObjectUtils.NULL;
        }


    public Null visitUdpRelayCandidate(final IceUdpRelayCandidate candidate)
        {
        addAttributeWithRelated(candidate);
        return ObjectUtils.NULL;
        }


    public Null visitUdpServerReflexiveCandidate(
        final IceUdpServerReflexiveCandidate candidate)
        {
        addAttributeWithRelated(candidate);
        this.m_udpServerReflexiveCandidate = candidate;
        return ObjectUtils.NULL;
        }

    /**
     * Creates a new media description with ICE candidates for the specified
     * addresses and for the specified protocol.
     * 
     * @param socketAddress The address for the media.
     * @param protocol The protocol of the media.
     * @return The new media description.
     * @throws SdpException If the data could not be generated for any
     * reason.
     */
    private MediaDescription createMessageMediaDesc(
        final IceCandidate candidate)
        //final InetSocketAddress socketAddress, final String protocol) 
        throws SdpException
        {        
        final InetSocketAddress socketAddress = candidate.getSocketAddress();
        final String protocol = candidate.getTransport().getName();
        final MediaDescription md = 
            this.m_sdpFactory.createMediaDescription("message", 
                socketAddress.getPort(), 1, protocol, new String[]{"http"});
    
        final Connection conn = this.m_sdpFactory.createConnection("IN", 
            Connection.IP4, socketAddress.getAddress().getHostAddress());
        md.setConnection(conn);
        return md;
        }
    
    private StringBuilder createBaseCandidateAttribute(
        final IceCandidate candidate)
        {
        final String space = " ";
        final StringBuilder sb = new StringBuilder();
        sb.append(candidate.getFoundation());
        sb.append(space);
        sb.append(candidate.getComponentId());
        sb.append(space);
        sb.append(candidate.getTransport().getName());
        sb.append(space);
        sb.append(candidate.getPriority());
        sb.append(space);
        final InetSocketAddress sa = candidate.getSocketAddress();
        final InetAddress ia = sa.getAddress();
        sb.append(ia.getHostAddress());
        sb.append(space);
        sb.append(candidate.getSocketAddress().getPort());
        sb.append(space);
        sb.append("typ");
        sb.append(space);
        sb.append(candidate.getType().toSdp());
        return sb;
        }

    private void addAttribute(final IceCandidate candidate)
        {
        final StringBuilder sb = createBaseCandidateAttribute(candidate);
        final Attribute attribute = 
            this.m_sdpFactory.createAttribute("candidate", sb.toString());
        m_candidates.add(attribute);
        }
    
    /**
     * Encodes a candidate attribute with the related address and related
     * port field filled in.
     * 
     * @param candidate The candidate.
     */
    private void addAttributeWithRelated(
        final AbstractStunServerIceCandidate candidate)
        {
        final StringBuilder sb = createBaseCandidateAttribute(candidate);
        final String space = " ";
        sb.append(space);
        sb.append("raddr");
        sb.append(space);
        sb.append(candidate.getRelatedAddress().getHostAddress());
        sb.append(space);
        sb.append("rport");
        sb.append(space);
        sb.append(candidate.getRelatedPort());
        final Attribute attribute = 
            this.m_sdpFactory.createAttribute("candidate", sb.toString());
        m_candidates.add(attribute);
        }
    }
