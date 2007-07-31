package org.lastbamboo.common.ice;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.util.Collection;
import java.util.LinkedList;

import org.lastbamboo.common.ice.candidate.IceCandidate;
import org.lastbamboo.common.ice.candidate.IceTcpActiveCandidate;
import org.lastbamboo.common.ice.candidate.IceTcpHostPassiveCandidate;
import org.lastbamboo.common.ice.candidate.IceTcpRelayPassiveCandidate;
import org.lastbamboo.common.ice.candidate.IceUdpHostCandidate;
import org.lastbamboo.common.ice.candidate.IceUdpServerReflexiveCandidate;
import org.lastbamboo.common.stun.client.StunClient;
import org.lastbamboo.common.util.NetworkUtils;
import org.lastbamboo.common.util.ShootConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Gathers ICE candidates.
 */
public class IceCandidateGathererImpl implements IceCandidateGatherer
    {
    
    private final Logger LOG = LoggerFactory.getLogger(getClass());

    private final StunClient m_turnClient;

    private final boolean m_controlling;

    private final StunClient m_udpStunClient;

    /**
     * Creates a new class for gathering ICE candidates.
     * 
     * @param tcpTurnClient The TURN client for getting data about TURN 
     * candidates.
     */
    public IceCandidateGathererImpl(final StunClient tcpTurnClient, 
        final StunClient udpStunClient,
        final boolean controlling)
        {
        this.m_turnClient = tcpTurnClient;
        this.m_udpStunClient = udpStunClient;
        this.m_controlling = controlling;
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

        // Not this class also processes server-side messages.
        //final StunClient stunClient = new IceStunUdpPeer(this.m_iceAgent);
        
        final InetSocketAddress serverReflexiveAddress = 
            this.m_udpStunClient.getServerReflexiveAddress();
        
        // Add the host candidate.  Note the host candidate is also used as
        // the BASE candidate for the server reflexive candidate below.
        final IceUdpHostCandidate hostCandidate = 
            new IceUdpHostCandidate(this.m_udpStunClient, this.m_controlling);
        candidates.add(hostCandidate);
        
        // Add the server reflexive candidate.
        final IceUdpServerReflexiveCandidate serverReflexiveCandidate =
            new IceUdpServerReflexiveCandidate(serverReflexiveAddress, 
                hostCandidate, this.m_udpStunClient, this.m_controlling);
        
        candidates.add(serverReflexiveCandidate);
        return candidates;
        }

    private Collection<IceCandidate> createTcpCandidates()
        {
        final Collection<IceCandidate> candidates = 
            new LinkedList<IceCandidate>();
        final InetSocketAddress relayAddress = 
            this.m_turnClient.getRelayAddress();
        
        final InetSocketAddress baseSocketAddress = 
            this.m_turnClient.getHostAddress();
        final InetAddress baseRelayAddress = baseSocketAddress.getAddress();

        // For relayed candidates, the related address is the mapped address.
        final InetSocketAddress relatedAddress = 
            this.m_turnClient.getServerReflexiveAddress();
        
        // Add the relay candidate.  Note that for relay candidates, the base
        // candidate is the relay candidate itself. 
        final IceTcpRelayPassiveCandidate candidate = 
            new IceTcpRelayPassiveCandidate(relayAddress, 
                this.m_turnClient, relatedAddress.getAddress(), 
                relatedAddress.getPort(), this.m_controlling);
        candidates.add(candidate);
        
        final int port = ShootConstants.HTTP_PORT;
        final InetSocketAddress address = 
            new InetSocketAddress(baseRelayAddress, port);
        final IceTcpHostPassiveCandidate hostCandidate = 
            new IceTcpHostPassiveCandidate(address, 
                this.m_controlling);
        candidates.add(hostCandidate);
        
        
        // Add the active candidate.
        try
            {
            final InetSocketAddress activeAddress = 
                new InetSocketAddress(NetworkUtils.getLocalHost(), 9);
            final IceTcpActiveCandidate activeCandidate = 
                new IceTcpActiveCandidate(activeAddress, this.m_controlling);
            candidates.add(activeCandidate);
            }
        catch (final UnknownHostException e)
            {
            LOG.error("Could not resolve host!", e);
            return candidates;
            }
        
        return candidates;
        }
    }
