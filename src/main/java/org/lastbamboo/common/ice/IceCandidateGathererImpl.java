package org.lastbamboo.common.ice;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.Collection;
import java.util.LinkedList;

import org.lastbamboo.common.ice.candidate.IceCandidate;
import org.lastbamboo.common.ice.candidate.IceTcpHostPassiveCandidate;
import org.lastbamboo.common.ice.candidate.IceTcpRelayPassiveCandidate;
import org.lastbamboo.common.ice.candidate.IceUdpServerReflexiveCandidate;
import org.lastbamboo.common.turn.client.TurnClient;
import org.lastbamboo.common.util.ShootConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Gathers ICE candidates.
 */
public class IceCandidateGathererImpl implements IceCandidateGatherer
    {
    
    private final Logger LOG = LoggerFactory.getLogger(getClass());
    

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
     * This priority is really high because we can likely traverse the firewall
     * using this priority.  It's higher than the TCP passive local priority 
     * because it's unlikely both hosts are on the same subnet. 
     * 
     * TODO: Figure out all the passive versus active stuff.
     */
    private static final int UDP_SERVER_REFLEXIVE_PRIORITY = 20;
    
    /**
     * The local UDP candidate priority. This is lower than the local TCP
     * one because we'd prefer TCP if we're on the same subnet.
     * 
     * TODO: Figure out all the passive versus active stuff.
     */
    private static final int UDP_HOST_PRIORITY = 9;
    
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

    //private final BindingTracker m_bindingTracker;


    private final TurnClient m_turnClient;

    /**
     * Creates a new class for gathering ICE candidates.
     * 
     * @param turnClient The TURN client for getting data about TURN 
     * candidates.
     */
    public IceCandidateGathererImpl(final TurnClient turnClient)
        {
        m_turnClient = turnClient;
        }

    public Collection<IceCandidate> gatherCandidates()
        {
        final Collection<IceCandidate> candidates = 
            new LinkedList<IceCandidate>();
        
        final Collection<IceCandidate> tcpCandidates = 
            createTcpCandidates();
        candidates.addAll(tcpCandidates);
        
        final Collection<IceCandidate> udpCandidates =
            createUdpCandidates();
        
        candidates.addAll(udpCandidates);
        return candidates; 
        }
    
    private Collection<IceCandidate> createUdpCandidates()
        {
        final Collection<IceCandidate> candidates =
            new LinkedList<IceCandidate>();

        final IceStunClient iceClient = new IceUdpStunClient();
        
        final InetSocketAddress serverReflexiveAddress = 
            iceClient.getServerReflexiveAddress();
        
        final InetSocketAddress baseSocketAddress = iceClient.getBaseAddress();
        
        final IceUdpServerReflexiveCandidate serverReflexiveCandidate =
            new IceUdpServerReflexiveCandidate(serverReflexiveAddress, 
                baseSocketAddress.getAddress(), 
                iceClient.getStunServerAddress(),
                baseSocketAddress.getAddress(), baseSocketAddress.getPort());
        
        candidates.add(serverReflexiveCandidate);
        return candidates;
        }

    private Collection<IceCandidate> createTcpCandidates()
        {
        final InetSocketAddress relayAddress = 
            this.m_turnClient.getRelayAddress();
        final InetAddress stunServerAddress = 
            this.m_turnClient.getStunServerAddress();
        
        final InetSocketAddress baseSocketAddress = 
            this.m_turnClient.getBaseAddress();
        final InetAddress baseRelayAddress = baseSocketAddress.getAddress();
        final int baseRelayPort = baseSocketAddress.getPort();
        
        // Add the relay candidate.
        final Collection<IceCandidate> candidates = 
            new LinkedList<IceCandidate>();
        
        final IceTcpRelayPassiveCandidate candidate = 
            new IceTcpRelayPassiveCandidate(relayAddress, baseRelayAddress, 
                stunServerAddress, baseRelayAddress, baseRelayPort);
        candidates.add(candidate);
        
        // Add the host candidate.
        final int port = ShootConstants.HTTP_PORT;
        final InetSocketAddress address = 
            new InetSocketAddress(baseRelayAddress, port);
        final IceTcpHostPassiveCandidate hostCandidate = 
            new IceTcpHostPassiveCandidate(address);
        candidates.add(hostCandidate);
        
        return candidates;
        }
    }
