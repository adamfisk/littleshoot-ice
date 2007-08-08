package org.lastbamboo.common.ice.candidate;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;

import org.lastbamboo.common.ice.IceMediaStreamDesc;
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

    private final StunClient m_tcpTurnClient;

    private final boolean m_controlling;

    private final StunClient m_iceUdpStunPeer;

    private final IceMediaStreamDesc m_desc;

    /**
     * Creates a new class for gathering ICE candidates.
     * 
     * @param tcpTurnClient The TURN client for getting data about TURN 
     * candidates.
     * @param udpStunClient The client for connecting to the STUN server.  This
     * will also handle peer reflexive connectivity checks. 
     * @param controlling Whether or not this is the controlling agent at the
     * start of processing.
     * @param desc The description of the media stream to create.
     */
    public IceCandidateGathererImpl(final StunClient tcpTurnClient, 
        final StunClient udpStunClient, final boolean controlling, 
        final IceMediaStreamDesc desc)
        {
        this.m_tcpTurnClient = tcpTurnClient;
        this.m_iceUdpStunPeer = udpStunClient;
        this.m_controlling = controlling;
        this.m_desc = desc;
        }

    public Collection<IceCandidate> gatherCandidates()
        {
        final Collection<IceCandidate> candidates = 
            new LinkedList<IceCandidate>();
        
        if (this.m_desc.isTcp())
            {
            final Collection<IceCandidate> tcpCandidates = 
                createTcpCandidates();
            
            // 4.1.1.3. Eliminating Redundant Candidates.
            eliminateRedundantCandidates(tcpCandidates);
            candidates.addAll(tcpCandidates);
            }

        if (this.m_desc.isUdp())
            {
            final Collection<IceCandidate> udpCandidates =
                createUdpCandidates();
    
            // 4.1.1.3. Eliminating Redundant Candidates.
            eliminateRedundantCandidates(udpCandidates);
            candidates.addAll(udpCandidates);
            }
        
        return candidates; 
        }
    
    private void eliminateRedundantCandidates(
        final Collection<IceCandidate> candidates)
        {
        final Map<InetSocketAddress, IceCandidate> addressesToCandidates =
            new HashMap<InetSocketAddress, IceCandidate>();
        for (final Iterator<IceCandidate> iter = candidates.iterator(); 
            iter.hasNext();)
            {
            final IceCandidate candidate = iter.next();
            LOG.debug("Checking: {}", candidate);
            final InetSocketAddress address = candidate.getSocketAddress();
            if (addressesToCandidates.containsKey(address))
                {
                final IceCandidate existingCandidate = 
                    addressesToCandidates.get(address);
                final IceCandidate base = existingCandidate.getBaseCandidate();
                if (base.equals(candidate.getBaseCandidate()))
                    {
                    LOG.debug("Removing redundant candidate!!!!");
                    iter.remove();
                    }
                }
            else
                {
                addressesToCandidates.put(address, candidate);
                }
            }
        }

    private Collection<IceCandidate> createUdpCandidates()
        {
        final Collection<IceCandidate> candidates =
            new LinkedList<IceCandidate>();

        final InetSocketAddress serverReflexiveAddress = 
            this.m_iceUdpStunPeer.getServerReflexiveAddress();
        
        final InetAddress stunServerAddress = 
            this.m_iceUdpStunPeer.getStunServerAddress();
        
        // Add the host candidate.  Note the host candidate is also used as
        // the BASE candidate for the server reflexive candidate below.
        final InetSocketAddress hostAddress = 
            this.m_iceUdpStunPeer.getHostAddress();
        
        final IceUdpHostCandidate hostCandidate = 
            new IceUdpHostCandidate(hostAddress, this.m_controlling);
        candidates.add(hostCandidate);
        
        // Add the server reflexive candidate.
        final IceUdpServerReflexiveCandidate serverReflexiveCandidate =
            new IceUdpServerReflexiveCandidate(serverReflexiveAddress, 
                hostCandidate, stunServerAddress, this.m_controlling);
        
        candidates.add(serverReflexiveCandidate);
        return candidates;
        }

    private Collection<IceCandidate> createTcpCandidates()
        {
        final Collection<IceCandidate> candidates = 
            new LinkedList<IceCandidate>();
        final InetSocketAddress relayAddress = 
            this.m_tcpTurnClient.getRelayAddress();
        
        final InetSocketAddress baseSocketAddress = 
            this.m_tcpTurnClient.getHostAddress();
        final InetAddress baseRelayAddress = baseSocketAddress.getAddress();

        // For relayed candidates, the related address is the mapped address.
        final InetSocketAddress relatedAddress = 
            this.m_tcpTurnClient.getServerReflexiveAddress();
        
        final InetAddress stunServerAddress =
            this.m_tcpTurnClient.getStunServerAddress();
        // Add the relay candidate.  Note that for relay candidates, the base
        // candidate is the relay candidate itself. 
        final IceTcpRelayPassiveCandidate candidate = 
            new IceTcpRelayPassiveCandidate(relayAddress, 
                stunServerAddress, relatedAddress.getAddress(), 
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
