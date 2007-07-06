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

    private final Vector<MediaDescription> m_mediaDescriptions;

    /**
     * Creates a new encoder for encoder ICE candidates into SDP.
     */
    public IceCandidateSdpEncoder()
        {
        this.m_sdpFactory = new SdpFactory();
        final InetAddress address = getAddress();
        final String addrType = 
            address instanceof Inet6Address ? "IP6" : "IP4";
        
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
            
            this.m_mediaDescriptions = new Vector<MediaDescription>();
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
            this.m_sessionDescription.setMediaDescriptions(
                this.m_mediaDescriptions);
            }
        catch (final SdpException e)
            {
            LOG.error("Could not add the media descriptions", e);
            }
        }
    
    public Null visitTcpHostPassiveCandidate(
        final IceTcpHostPassiveCandidate candidate)
        {
        addCandidate(candidate);
        return ObjectUtils.NULL;
        }


    public Null visitTcpRelayPassiveCandidate(
        final IceTcpRelayPassiveCandidate candidate)
        {
        final Attribute attribute = createAttributeWithRelated(candidate);
        addCandidate(candidate, attribute);
        return ObjectUtils.NULL;
        }


    public Null visitTcpServerReflexiveSoCandidate(
        final IceTcpServerReflexiveSoCandidate candidate)
        {
        final Attribute attribute = createAttributeWithRelated(candidate);
        addCandidate(candidate, attribute);
        return ObjectUtils.NULL;
        }


    public Null visitUdpHostCandidate(final IceUdpHostCandidate candidate)
        {
        addCandidate(candidate);
        return ObjectUtils.NULL;
        }


    public Null visitUdpPeerReflexiveCandidate(
        final IceUdpPeerReflexiveCandidate candidate)
        {
        final Attribute attribute = createAttributeWithRelated(candidate);
        addCandidate(candidate, attribute);
        return ObjectUtils.NULL;
        }


    public Null visitUdpRelayCandidate(final IceUdpRelayCandidate candidate)
        {
        final Attribute attribute = createAttributeWithRelated(candidate);
        addCandidate(candidate, attribute);
        return ObjectUtils.NULL;
        }


    public Null visitUdpServerReflexiveCandidate(
        final IceUdpServerReflexiveCandidate candidate)
        {
        final Attribute attribute = createAttributeWithRelated(candidate);
        addCandidate(candidate, attribute);
        return ObjectUtils.NULL;
        }
    
    private void addCandidate(final IceCandidate candidate)
        {
        final Attribute attribute = createAttribute(candidate);
        addCandidate(candidate, attribute);
        }

    private void addCandidate(final IceCandidate candidate, 
        final Attribute attribute)
        {
        final InetSocketAddress address = candidate.getSocketAddress();
        final Vector<Attribute> attributes = new Vector<Attribute>();
        attributes.add(attribute);
        try
            {
            // TODO: Shouldn't there be multiple attributes for each media
            // description?  Should we have multiple media descriptions here?
            final MediaDescription md = 
                createMessageMediaDesc(address, 
                    candidate.getTransport().getName());
            
            md.setAttributes(attributes);
            
            LOG.debug("Adding media description");
            this.m_mediaDescriptions.add(md);
            }
        catch (final SdpException e)
            {
            LOG.error("Could not encode SDP!!", e);
            }
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
        final InetSocketAddress socketAddress, final String protocol) 
        throws SdpException
        {        
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
        sb.append(candidate.getSocketAddress().getAddress().getHostAddress());
        sb.append(space);
        sb.append(candidate.getSocketAddress().getPort());
        return sb;
        }

    private Attribute createAttribute(final IceCandidate candidate)
        {
        final StringBuilder sb = createBaseCandidateAttribute(candidate);
        return this.m_sdpFactory.createAttribute("candidate", sb.toString());
        }
    
    /**
     * Encodes a candidate attribute with the related address and related
     * port field filled in.
     * 
     * @param candidate The candidate.
     * @return The new attribute.
     */
    private Attribute createAttributeWithRelated(
        final AbstractStunServerIceCandidate candidate)
        {
        final StringBuilder sb = createBaseCandidateAttribute(candidate);
        final String space = " ";
        sb.append(space);
        sb.append(candidate.getRelatedAddress().getHostAddress());
        sb.append(space);
        sb.append(candidate.getRelatedPort());
        return this.m_sdpFactory.createAttribute("candidate", sb.toString());
        }
    }
