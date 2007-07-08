package org.lastbamboo.common.ice;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.Collection;
import java.util.LinkedList;

import org.lastbamboo.common.ice.candidate.IceCandidate;
import org.lastbamboo.common.ice.candidate.IceTcpHostPassiveCandidate;
import org.lastbamboo.common.ice.candidate.IceTcpRelayPassiveCandidate;
import org.lastbamboo.common.ice.candidate.IceUdpHostCandidate;
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

    private final TurnClient m_turnClient;

    private final boolean m_controlling;

    /**
     * Creates a new class for gathering ICE candidates.
     * 
     * @param turnClient The TURN client for getting data about TURN 
     * candidates.
     * @param controlling Whether or not gathered candidates should be 
     *  controlling candidates.
     */
    public IceCandidateGathererImpl(final TurnClient turnClient, 
        final boolean controlling)
        {
        m_turnClient = turnClient;
        m_controlling = controlling;
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
        
        // Add the host candidate.
        final IceUdpHostCandidate hostCandidate = 
            new IceUdpHostCandidate(baseSocketAddress, this.m_controlling);
        candidates.add(hostCandidate);
        
        // Add the server reflexive candidate.
        final IceUdpServerReflexiveCandidate serverReflexiveCandidate =
            new IceUdpServerReflexiveCandidate(serverReflexiveAddress, 
                baseSocketAddress.getAddress(), 
                iceClient.getStunServerAddress(),
                baseSocketAddress.getAddress(), baseSocketAddress.getPort(),
                this.m_controlling);
        
        candidates.add(serverReflexiveCandidate);
        return candidates;
        }

    private Collection<IceCandidate> createTcpCandidates()
        {
        final Collection<IceCandidate> candidates = 
            new LinkedList<IceCandidate>();
        final InetSocketAddress relayAddress = 
            this.m_turnClient.getRelayAddress();
        final InetAddress stunServerAddress = 
            this.m_turnClient.getStunServerAddress();
        
        final InetSocketAddress baseSocketAddress = 
            this.m_turnClient.getBaseAddress();
        final InetAddress baseRelayAddress = baseSocketAddress.getAddress();
        final int baseRelayPort = baseSocketAddress.getPort();
        
        // Add the relay candidate.        
        final IceTcpRelayPassiveCandidate candidate = 
            new IceTcpRelayPassiveCandidate(relayAddress, baseRelayAddress, 
                stunServerAddress, baseRelayAddress, baseRelayPort,
                this.m_controlling);
        candidates.add(candidate);
        
        // Add the host candidate.
        final int port = ShootConstants.HTTP_PORT;
        final InetSocketAddress address = 
            new InetSocketAddress(baseRelayAddress, port);
        final IceTcpHostPassiveCandidate hostCandidate = 
            new IceTcpHostPassiveCandidate(address, this.m_controlling);
        candidates.add(hostCandidate);
        
        return candidates;
        }
    }
