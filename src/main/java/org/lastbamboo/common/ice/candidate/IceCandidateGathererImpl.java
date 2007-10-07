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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Gathers ICE candidates.
 */
public class IceCandidateGathererImpl implements IceCandidateGatherer
    {
    
    private final Logger LOG = LoggerFactory.getLogger(getClass());

    private final StunClient m_iceTcpStunPeer;

    private final StunClient m_iceUdpStunPeer;
    
    private final boolean m_controlling;

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
        this.m_iceTcpStunPeer = tcpTurnClient;
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
                createTcpCandidates(this.m_iceTcpStunPeer);
            
            // 4.1.1.3. Eliminating Redundant Candidates.
            eliminateRedundantCandidates(tcpCandidates);
            candidates.addAll(tcpCandidates);
            }

        if (this.m_desc.isUdp())
            {
            final Collection<IceCandidate> udpCandidates =
                createUdpCandidates(this.m_iceUdpStunPeer);
    
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

    private Collection<IceCandidate> createUdpCandidates(
        final StunClient client)
        {
        final Collection<IceCandidate> candidates =
            new LinkedList<IceCandidate>();

        final InetSocketAddress serverReflexiveAddress = 
            client.getServerReflexiveAddress();
        
        final InetAddress stunServerAddress = 
            client.getStunServerAddress();
        
        // Add the host candidate.  Note the host candidate is also used as
        // the BASE candidate for the server reflexive candidate below.
        final InetSocketAddress hostAddress = 
            client.getHostAddress();
        
        final IceUdpHostCandidate hostCandidate = 
            new IceUdpHostCandidate(hostAddress, this.m_controlling);
        candidates.add(hostCandidate);
        
        addServerReflexive(candidates, serverReflexiveAddress, hostCandidate, 
            stunServerAddress, this.m_controlling);
        return candidates;
        }

    private static void addServerReflexive(
        final Collection<IceCandidate> candidates, 
        final InetSocketAddress srflxAddress, 
        final IceUdpHostCandidate hostCandidate, 
        final InetAddress stunServerAddress, 
        final boolean controlling)
        {
        /*
        final InetSocketAddress srflxAddress2 = 
            new InetSocketAddress(srflxAddress.getAddress(), 
                srflxAddress.getPort() + 1);
        final InetSocketAddress srflxAddress3 = 
            new InetSocketAddress(srflxAddress.getAddress(), 
                srflxAddress.getPort() + 2);
        final InetSocketAddress srflxAddress4 = 
            new InetSocketAddress(srflxAddress.getAddress(), 
                srflxAddress.getPort() + 3);
        final InetSocketAddress srflxAddress5 = 
            new InetSocketAddress(srflxAddress.getAddress(), 
                srflxAddress.getPort() + 4);
        */
        
        final IceUdpServerReflexiveCandidate serverReflexiveCandidate1 =
            new IceUdpServerReflexiveCandidate(srflxAddress, 
                hostCandidate, stunServerAddress, controlling);
        
        /*
        final IceUdpServerReflexiveCandidate serverReflexiveCandidate2 =
            new IceUdpServerReflexiveCandidate(srflxAddress2, 
                hostCandidate, stunServerAddress, controlling);
        final IceUdpServerReflexiveCandidate serverReflexiveCandidate3 =
            new IceUdpServerReflexiveCandidate(srflxAddress3, 
                hostCandidate, stunServerAddress, controlling);
        final IceUdpServerReflexiveCandidate serverReflexiveCandidate4 =
            new IceUdpServerReflexiveCandidate(srflxAddress4, 
                hostCandidate, stunServerAddress, controlling);
        final IceUdpServerReflexiveCandidate serverReflexiveCandidate5 =
            new IceUdpServerReflexiveCandidate(srflxAddress5, 
                hostCandidate, stunServerAddress, controlling);
        */
        
        candidates.add(serverReflexiveCandidate1);
        /*
        candidates.add(serverReflexiveCandidate2);
        candidates.add(serverReflexiveCandidate3);
        candidates.add(serverReflexiveCandidate4);
        candidates.add(serverReflexiveCandidate5);
        */
        }

    private Collection<IceCandidate> createTcpCandidates(
        final StunClient client)
        {
        final Collection<IceCandidate> candidates = 
            new LinkedList<IceCandidate>();
    
        final InetSocketAddress relayAddress = client.getRelayAddress();
        
        // For relayed candidates, the related address is the mapped address.
        final InetSocketAddress relatedAddress = 
            client.getServerReflexiveAddress();
        
        final InetAddress stunServerAddress = client.getStunServerAddress();
        
        // Add the relay candidate.  Note that for relay candidates, the base
        // candidate is the relay candidate itself. 
        final IceCandidate relayCandidate = 
            new IceTcpRelayPassiveCandidate(relayAddress, 
                stunServerAddress, relatedAddress.getAddress(), 
                relatedAddress.getPort(), this.m_controlling);
        candidates.add(relayCandidate);
        
        // Add the host candidate.  Note the host candidate is also used as
        // the BASE candidate for the server reflexive candidate below.
        final InetSocketAddress hostAddress = client.getHostAddress();
        
        final IceCandidate hostCandidate = 
            new IceTcpHostPassiveCandidate(hostAddress, this.m_controlling);
        candidates.add(hostCandidate);
        
        // Add the active candidate.
        try
            {
            final InetSocketAddress activeAddress = 
                new InetSocketAddress(NetworkUtils.getLocalHost(), 0);
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

    public void close()
        {
        if (this.m_iceTcpStunPeer != null)
            {
            this.m_iceTcpStunPeer.close();
            }
        if (this.m_iceUdpStunPeer != null)
            {
            this.m_iceUdpStunPeer.close();
            }
        }
    }
