package org.lastbamboo.common.ice.sdp;

import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.util.Collection;
import java.util.Iterator;
import java.util.Vector;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.lastbamboo.common.ice.BindingTracker;
import org.lastbamboo.common.ice.IceCandidateAttributeFactory;
import org.lastbamboo.common.ice.IceConstants;
import org.lastbamboo.common.sdp.api.Attribute;
import org.lastbamboo.common.sdp.api.Connection;
import org.lastbamboo.common.sdp.api.MediaDescription;
import org.lastbamboo.common.sdp.api.Origin;
import org.lastbamboo.common.sdp.api.SdpException;
import org.lastbamboo.common.sdp.api.SdpParseException;
import org.lastbamboo.common.sdp.api.SessionDescription;
import org.lastbamboo.common.sdp.api.SessionName;
import org.lastbamboo.common.sdp.api.TimeDescription;
import org.lastbamboo.common.sdp.api.Version;
import org.lastbamboo.common.util.NetworkUtils;
import org.lastbamboo.common.util.ShootConstants;

/**
 * Implementation of the class for generating SDP data.
 */
public final class SdpFactoryImpl implements SdpFactory
    {
    
    /**
     * Logger for this class.
     */
    private static final Log LOG = LogFactory.getLog(SdpFactoryImpl.class);

    private final BindingTracker m_bindingTracker;

    private final org.lastbamboo.common.sdp.api.SdpFactory m_sdpFactory;

    private final IceCandidateAttributeFactory m_iceCandidateAttributeFactory;
    
    /**
     * Creates a new factory instance.
     * @param tracker The tracker for accessing all available transport 
     * bindings.
     * @param candidateAttributeFactory Factory for creating attributes of
     * candidates in SDP.
     * @param sdpFactory Factory for generating SDP.
     */
    public SdpFactoryImpl(final BindingTracker tracker, 
        final IceCandidateAttributeFactory candidateAttributeFactory,
        final org.lastbamboo.common.sdp.api.SdpFactory sdpFactory)
        {
        this.m_bindingTracker = tracker;
        this.m_iceCandidateAttributeFactory = candidateAttributeFactory;
        this.m_sdpFactory = sdpFactory;
        }
    
    public SessionDescription createSdp() throws SdpException
        {
        LOG.trace("Generating SDP description...");

        final SessionDescription sessionDescription = 
            this.m_sdpFactory.createSessionDescription();
        // "v=0"
        final Version v = this.m_sdpFactory.createVersion(0);
        
        // This address will not actually be used for anything important --
        // don't worry!
        final InetAddress address;
        try
            {
            address = NetworkUtils.getLocalHost();
            }
        catch (final UnknownHostException e)
            {
            // Should never happen.
            LOG.error("Could not resolve host", e);
            throw new RuntimeException("Could not resolve host", e);
            }
        
        final String addrType = 
            address instanceof Inet6Address ? "IP6" : "IP4";

        final Origin o = 
            this.m_sdpFactory.createOrigin("-", 0, 0, "IN", addrType,
            address.getHostAddress());
        
        // "s=-"
        final SessionName s = this.m_sdpFactory.createSessionName("-");
        // c=
        //final Connection c = this.m_sdpFactory.createConnection(
          //  "IN", addrType, publicIpAddress.getHostAddress());
        // "t=0 0"
        final TimeDescription t = this.m_sdpFactory.createTimeDescription();
        final Vector timeDescriptions = new Vector();
        timeDescriptions.add(t);
        final Vector mediaDescriptions = createMediaDescriptions();
        sessionDescription.setVersion(v);
        sessionDescription.setOrigin(o);
        sessionDescription.setSessionName(s);
        sessionDescription.setTimeDescriptions(timeDescriptions);
        sessionDescription.setMediaDescriptions(mediaDescriptions);
            
        LOG.debug("Generated SDP - " + sessionDescription.toString()+
            " size: "+sessionDescription.toString().length());
        return sessionDescription;
        }

    private Vector createMediaDescriptions() throws SdpException
        {
        final Vector descriptions = new Vector();
        addUdpMediaDescription(descriptions);
        
        final Collection turnTcpAddresses = 
            this.m_bindingTracker.getTurnTcpBindings();
        
        // TODO: support multiple TURN servers in the future.
        final Iterator iter = turnTcpAddresses.iterator();
        if (iter.hasNext())
            {
            final InetSocketAddress turnTcpAddress = 
                (InetSocketAddress) turnTcpAddresses.iterator().next();
            
            final Vector turnTcpAttributes = new Vector(); 
            final Attribute turnTcpAttribute = 
                this.m_iceCandidateAttributeFactory.createTcpIceCandidateAttribute(
                    turnTcpAddress, 1, 3);
            turnTcpAttributes.add(turnTcpAttribute);
            addTcpMediaDescription(descriptions, turnTcpAddress, 
                turnTcpAttributes, IceConstants.TCP_PASS);
            }
        
        // Now add the local address.
        // TODO: This should really not be the HTTP server directly.  It should
        // rather be a separate server socket that may pass sockets to the 
        // HTTP server (on the UAS).  On the UAC, it does not make sense for 
        // socket to point to the HTTP server, as the UAC will be issuing
        // HTTP requests.
        //
        // The point is that ICE is a higher level abstraction -- it shouldn't
        // point directly to any HTTP service.  It should rather create 
        // sockets for both the HTTP client and the HTTP server to use
        // appropriately.
        try
            {
            final InetAddress ia = NetworkUtils.getLocalHost();
            final int port = ShootConstants.HTTP_PORT;
            final InetSocketAddress localAddress = 
                new InetSocketAddress(ia, port);
            final Vector localTcpAttributes = new Vector(); 
            
            // This has a higher priority than the TURN address.
            final Attribute turnTcpAttribute = 
                this.m_iceCandidateAttributeFactory.createTcpIceCandidateAttribute(
                    localAddress, 1, 1);
            localTcpAttributes.add(turnTcpAttribute);
            addTcpMediaDescription(descriptions, localAddress, 
                localTcpAttributes, IceConstants.TCP_PASS);
            }
        catch (final UnknownHostException e)
            {
            LOG.warn("Unknown local host", e);
            }
        
        
        // Add any candidates for TCP simultaneous open.
        final InetSocketAddress tcpSoAddress = 
            this.m_bindingTracker.getTcpSoBinding();
        
        if (tcpSoAddress == null)
            {
            return descriptions;
            }
        
        final Vector tcpSoAttributes = new Vector();
        final Attribute attribute = 
            this.m_iceCandidateAttributeFactory.createTcpIceCandidateAttribute(
                tcpSoAddress, 1, 2);
        tcpSoAttributes.add(attribute);
        addTcpMediaDescription(descriptions, tcpSoAddress, tcpSoAttributes, 
            IceConstants.TCP_SO);
        return descriptions;
        }
    
    /**
     * Creates a new media description for transferring arbitrary UDP data.
     * This will include all ICE candidates for the media, such as TURN
     * or STUN-derived addresses.
     * @param descriptions The descriptions to add this description to.
     * @throws SdpException If there's any error generating SDP data.
     */
    private void addUdpMediaDescription(final Collection descriptions) 
        throws SdpException
        {
        final InetSocketAddress udpAddress = 
            this.m_bindingTracker.getStunUdpBinding();
        
        if (udpAddress == null)
            {
            return;
            }
        
        final MediaDescription md = createMessageMediaDesc(udpAddress, "udp");
        
        final Attribute stunCandidateAttribute = 
            this.m_iceCandidateAttributeFactory.createUdpIceCandidateAttribute(
                udpAddress, 1, 1);
        final Vector attributes = new Vector();
        attributes.add(stunCandidateAttribute);
        //final Vector attributes = 
          //  createCandidateAttributes(udpAddress, "udp", 1);
        md.setAttributes(attributes);
        
        descriptions.add(md);
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
    private void addTcpMediaDescription(final Collection descriptions, 
        final InetSocketAddress address, final Vector attributes, 
        final String transportType) 
        throws SdpException
        {    
        final MediaDescription md = 
            createMessageMediaDesc(address, transportType);
        
        md.setAttributes(attributes);
        
        descriptions.add(md);
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


    public SessionDescription createSdp(final String sdpData) 
        throws SdpParseException
        {
        return this.m_sdpFactory.createSessionDescription(sdpData);
        }
    
    }
