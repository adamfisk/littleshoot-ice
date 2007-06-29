package org.lastbamboo.common.ice.sdp;

import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.util.Collection;
import java.util.Vector;

import org.lastbamboo.common.ice.IceCandidate;
import org.lastbamboo.common.ice.IceCandidateVisitor;
import org.lastbamboo.common.ice.TcpActiveIceCandidate;
import org.lastbamboo.common.ice.TcpPassiveIceCandidate;
import org.lastbamboo.common.ice.TcpSoIceCandidate;
import org.lastbamboo.common.ice.UdpIceCandidate;
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
public class IceCandidateSdpEncoder implements IceCandidateVisitor
    {
    
    private final Logger LOG = 
        LoggerFactory.getLogger(IceCandidateSdpEncoder.class);
    
    private final SdpFactory m_sdpFactory;
    private final SessionDescription m_sessionDescription;

    private final IceCandidateAttributeFactory 
        m_iceCandidateAttributeFactory;

    private final Vector<MediaDescription> m_mediaDescriptions;

    /**
     * Creates a new encoder for encoder ICE candidates into SDP.
     */
    public IceCandidateSdpEncoder()
        {
        this.m_sdpFactory = new SdpFactory();
        this.m_iceCandidateAttributeFactory = 
            new IceCandidateAttributeFactoryImpl(this.m_sdpFactory);
        
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

    public void visitTcpPassiveIceCandidate(
        final TcpPassiveIceCandidate candidate)
        {
        final InetSocketAddress address = candidate.getSocketAddress();
        final Attribute attribute = 
            this.m_iceCandidateAttributeFactory.createTcpIceCandidateAttribute(
                address, candidate.getCandidateId(), candidate.getPriority());
        addCandidate(candidate, attribute);
        }

    public void visitUdpIceCandidate(final UdpIceCandidate candidate)
        {
        final InetSocketAddress address = candidate.getSocketAddress();
        final Attribute attribute = 
            this.m_iceCandidateAttributeFactory.createUdpIceCandidateAttribute(
                address, candidate.getCandidateId(), candidate.getPriority());
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
            addMediaDescription(address, attributes, 
                candidate.getTransport().getName());
            }
        catch (final SdpException e)
            {
            LOG.error("Could not encode SDP!!", e);
            }
        }


    public void visitTcpActiveIceCandidate(
        final TcpActiveIceCandidate candidate)
        {
        // TODO Auto-generated method stub

        }
    
    public void visitTcpSoIceCandidate(final TcpSoIceCandidate candidate)
        {
        // TODO We don't support this yet.
        }

    public void visitUnknownIceCandidate(final IceCandidate candidate)
        {
        // TODO Auto-generated method stub

        }
    
    /**
     * Creates a new media description for transferring arbitrary TCP data.
     * This will include all ICE candidates for the media, such as TURN
     * or STUN-derived addresses.
     * @param descriptions The descriptions to add this description to.
     * @param address The available endpoint for this description.
     * @param priority The priority of this TCP ICE candidate.
     * @param attributes The SDP attributes this ICE candidate.
     * @param transportType 
     * @throws SdpException If there's any error generating SDP data.
     */
    private void addMediaDescription(final InetSocketAddress address, 
        final Vector attributes, final String transportType) 
        throws SdpException
        {    
        final MediaDescription md = 
            createMessageMediaDesc(address, transportType);
        
        md.setAttributes(attributes);
        
        LOG.debug("Adding media description");
        this.m_mediaDescriptions.add(md);
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

    }
