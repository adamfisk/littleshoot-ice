package org.lastbamboo.common.ice;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;

import org.apache.commons.id.uuid.UUID;
import org.lastbamboo.common.ice.sdp.IceCandidateSdpEncoder;
import org.lastbamboo.common.util.NetworkUtils;
import org.lastbamboo.common.util.ShootConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class for generating TCP ICE candidates.  For a discussion of the priorities
 * associated with each candidate type and for a general discussion of ICE,
 * this was written based on draft-ietf-mmusic-ice-16 at:<p>
 * 
 * http://www.tools.ietf.org/html/draft-ietf-mmusic-ice-16
 * 
 * <p>
 * 
 * Here's the augmented BNF for candidates:
 * 
 * candidate-attribute   = "candidate" ":" foundation SP component-id SP
 *                         transport SP
 *                         priority SP
 *                         connection-address SP     ;from RFC 4566
 *                         port         ;port from RFC 4566
 *                         SP cand-type
 *                         [SP rel-addr]
 *                         [SP rel-port]
 *                         *(SP extension-att-name SP
 *                              extension-att-value)
 *                              
 * foundation            = 1*32ice-char
 * component-id          = 1*5DIGIT
 * transport             = "UDP" / transport-extension
 * transport-extension   = token              ; from RFC 3261
 * priority              = 1*10DIGIT
 * cand-type             = "typ" SP candidate-types
 * candidate-types       = "host" / "srflx" / "prflx" / "relay" / token
 * rel-addr              = "raddr" SP connection-address
 * rel-port              = "rport" SP port
 * extension-att-name    = byte-string    ;from RFC 4566
 * extension-att-value   = byte-string
 * ice-char              = ALPHA / DIGIT / "+" / "/"
 */
public class IceCandidateGeneratorImpl implements IceCandidateGenerator
    {

    /**
     * Component IDs start at 1 for non-RTP media, as specified in 
     * draft-ietf-mmusic-ice-16 section 14.  We only have one component of 
     * our media stream.
     */
    private static final int COMPONENT_ID = 1;
    
    /**
     * We currently only support ICE over a single interface, so we just
     * give that interface the highest value for "local preference".
     */
    private static final int DEFAULT_INTERFACE_LOCAL_PREFERENCE = 65535;
    
    /**
     * These are candidates for the public addresses of host candidates,
     * discovered using STUN Binding Request messages.
     */
    private static final int SERVER_REFLEXIVE_TYPE = 100;
    
    private static final int PEER_REFLEXIVE_TYPE = 110;
    
    /**
     * These are allocated relay addresses -- the lowest priority.
     */
    private static final int RELAYED_TYPE = 0;
    
    /**
     * Host candidates are local candidates.
     */
    private static final int HOST_TYPE = 126;
    
    private static final int TCP_PASSIVE_LOCAL_PRIORITY = 10;
    
    private static final int TCP_PASSIVE_RELAY_PRIORITY = 0;
    
    /**
     * Because we're transferring files and not media, we give TCP the
     * highest possible transport preference.
     */
    private static final int TCP_TRANSPORT_PREF = 15;
    
    /**
     * We give UDP the lower transport preference because we'd like to
     * transfer files over TCP.
     */
    private static final int UDP_TRANSPORT_PREF = 6;
    
    /**
     * The direction preference for TCP SO.
     */
    private static final int TCP_SO_DIRECTION_PREF = 7;
    
    /**
     * The direction preference for passive TCP.
     */
    private static final int TCP_PASSIVE_DIRECTION_PREF = 2;

    private final Logger LOG = LoggerFactory.getLogger(getClass());
    
    private final BindingTracker m_bindingTracker;

    /**
     * Creates a new factory instance.
     * 
     * @param tracker The tracker for accessing all available transport 
     * bindings.
     */
    public IceCandidateGeneratorImpl(final BindingTracker tracker)
        {
        this.m_bindingTracker = tracker;
        }

    public byte[] generateCandidates() throws IOException
        {
        // First, gather all the candidates.
        final Collection<IceCandidate> candidates = gatherCandidates();
        
        // Then encode the gathered candidates in SDP.
        final IceCandidateSdpEncoder encoder = new IceCandidateSdpEncoder();
        encoder.visitCandidates(candidates);
        return encoder.getSdp();
        }
    
    private Collection<IceCandidate> gatherCandidates()
        {
        final Collection<IceCandidate> candidates = 
            new LinkedList<IceCandidate>();
        
        final Collection<TcpPassiveIceCandidate> tcpCandidates = 
            createTcpCandidates();
        
        candidates.addAll(tcpCandidates);
        
        return candidates; 
        }
    
    private Collection<TcpPassiveIceCandidate> createTcpCandidates()
        {
        final Collection<InetSocketAddress> turnTcpAddresses = 
            this.m_bindingTracker.getTurnTcpBindings();
        final Collection<TcpPassiveIceCandidate> candidates = 
            new LinkedList<TcpPassiveIceCandidate>();
        
        // TODO: This does not follow the encoding from the latest ICE drafts!!
        for (final InetSocketAddress address : turnTcpAddresses)
            {
            final TcpPassiveIceCandidate candidate = 
                new TcpPassiveIceCandidate(COMPONENT_ID, UUID.randomUUID(), 
                    TCP_PASSIVE_RELAY_PRIORITY, address);
            candidates.add(candidate);
            }
        
        try
            {
            final InetAddress ia = NetworkUtils.getLocalHost();
            final int port = ShootConstants.HTTP_PORT;
            final InetSocketAddress address = 
                new InetSocketAddress(ia, port);
            final TcpPassiveIceCandidate candidate = 
                new TcpPassiveIceCandidate(COMPONENT_ID, UUID.randomUUID(), 
                    TCP_PASSIVE_LOCAL_PRIORITY, address);
            candidates.add(candidate);
            }
        catch (final UnknownHostException e)
            {
            LOG.error("Could not determine the local host", e);
            }
        
        return candidates;
        }

    private Collection<TcpPassiveIceCandidate> createTcpPassiveIceCandidates()
        {
        /*
        final InetSocketAddress localAddress = 
            this.m_iceTcpPassiveServer.getLocalAddress();
        final InetSocketAddress publicAddress = 
            this.m_iceTcpPassiveServer.getPublicAddress();
        
        final TcpPassiveIceCandidate localCandidate = 
            new TcpPassiveIceCandidate(1, UUID.randomUUID(), 
                 TCP_PASSIVE_LOCAL_PRIORITY, localAddress);
        final TcpPassiveIceCandidate publicCandidate = 
            new TcpPassiveIceCandidate(1, UUID.randomUUID(), 
                 TCP_PASSIVE_LOCAL_PRIORITY, publicAddress);
        
        final Collection<TcpPassiveIceCandidate> candidates = 
            new LinkedList<TcpPassiveIceCandidate>();
        candidates.add(localCandidate);
        candidates.add(publicCandidate);
        return candidates;
        */
        return Collections.emptySet();
        }
    }
