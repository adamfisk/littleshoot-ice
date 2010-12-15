package org.lastbamboo.common.ice.candidate;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;

import org.lastbamboo.common.ice.IceMediaStreamDesc;
import org.lastbamboo.common.stun.client.StunClient;
import org.lastbamboo.common.stun.stack.StunAddressProvider;
import org.lastbamboo.common.util.NetworkUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Gathers ICE candidates.
 */
public class IceCandidateGathererImpl implements IceCandidateGatherer {
    
    private final Logger m_log = LoggerFactory.getLogger(getClass());

    private final StunAddressProvider m_iceTcpStunPeer;

    private final StunClient m_iceUdpStunPeer;
    
    private final boolean m_controlling;

    private final IceMediaStreamDesc m_desc;

    private InetSocketAddress m_udpServerReflexiveAddress;

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
    public IceCandidateGathererImpl(final StunAddressProvider tcpTurnClient,
            final StunClient udpStunClient, final boolean controlling,
            final IceMediaStreamDesc desc) {
        if (desc.isUdp() && udpStunClient == null) {
            throw new IllegalArgumentException("No UDP client with UDP active");
        }
        if (desc.isTcp() && tcpTurnClient == null) {
            throw new IllegalArgumentException("No TCP client with TCP active");
        }
        this.m_iceTcpStunPeer = tcpTurnClient;
        this.m_iceUdpStunPeer = udpStunClient;
        this.m_controlling = controlling;
        this.m_desc = desc;
    }

    public Collection<IceCandidate> gatherCandidates() {
        final Collection<IceCandidate> candidates = 
            new ArrayList<IceCandidate>();

        if (this.m_desc.isUdp()) {
            final Collection<IceCandidate> udpCandidates = 
                createUdpCandidates(this.m_iceUdpStunPeer);

            // 4.1.3. Eliminating Redundant Candidates.
            eliminateRedundantCandidates(udpCandidates);
            candidates.addAll(udpCandidates);
        }

        if (this.m_desc.isTcp()) {
            final Collection<IceCandidate> tcpCandidates = 
                createTcpCandidates(this.m_iceTcpStunPeer);

            // 4.1.3. Eliminating Redundant Candidates.
            eliminateRedundantCandidates(tcpCandidates);
            candidates.addAll(tcpCandidates);
        }

        return candidates;
    }
    
    /**
     * Section 4.1.3.
     */
    private void eliminateRedundantCandidates(
            final Collection<IceCandidate> candidates) {
        final Map<InetSocketAddress, IceCandidate> addressesToCandidates = 
            new HashMap<InetSocketAddress, IceCandidate>();
        for (final Iterator<IceCandidate> iter = candidates.iterator(); iter
                .hasNext();) {
            final IceCandidate candidate = iter.next();
            m_log.debug("Checking: {}", candidate);
            final InetSocketAddress address = candidate.getSocketAddress();
            if (addressesToCandidates.containsKey(address)) {
                final IceCandidate existingCandidate = addressesToCandidates
                        .get(address);
                final IceCandidate base = existingCandidate.getBaseCandidate();
                if (base.equals(candidate.getBaseCandidate())) {
                    m_log.debug("Removing redundant candidate!!!!");
                    iter.remove();
                }
            } else {
                addressesToCandidates.put(address, candidate);
            }
        }
    }

    private Collection<IceCandidate> createUdpCandidates(final StunClient client) {
        final Collection<IceCandidate> candidates = new LinkedList<IceCandidate>();

        final InetAddress stunServerAddress = client.getStunServerAddress();

        // Add the host candidate. Note the host candidate is also used as
        // the BASE candidate for the server reflexive candidate below.
        final InetSocketAddress hostAddress = client.getHostAddress();

        final IceUdpHostCandidate hostCandidate = new IceUdpHostCandidate(
                hostAddress, this.m_controlling);
        candidates.add(hostCandidate);

        try {
            this.m_udpServerReflexiveAddress = 
                client.getServerReflexiveAddress();
        } catch (final IOException e) {
            m_log.error("Could not get UDP server reflexive candidate", e);
            return candidates;
        }

        final IceUdpServerReflexiveCandidate serverReflexiveCandidate = 
            new IceUdpServerReflexiveCandidate(
                m_udpServerReflexiveAddress, hostCandidate, stunServerAddress,
                this.m_controlling);

        candidates.add(serverReflexiveCandidate);

        return candidates;
    }

    private Collection<IceCandidate> createTcpCandidates(
            final StunAddressProvider client) {
        final Collection<IceCandidate> candidates = 
            new ArrayList<IceCandidate>();

        // Only add the TURN candidate on the non-controlling side to save
        // resources. This is non-standard as well, but we should only need
        // one TURN server per session.
        // if (!this.m_controlling && !NetworkUtils.isPublicAddress())
        // {
        addTcpTurnCandidate(client, candidates);
        // }

        // Add the host candidate. Note the host candidate is also used as
        // the BASE candidate for the server reflexive candidate below.
        final InetSocketAddress hostAddress = client.getHostAddress();

        final IceCandidate hostCandidate = new IceTcpHostPassiveCandidate(
                hostAddress, this.m_controlling);
        candidates.add(hostCandidate);

        // OK, the following is non-standard. If we have a public address
        // for the host from our UDP STUN check, we use the address part for
        // a new candidate because we always make an effort to map our TCP
        // host port with UPnP. This is not a simultaneous open candidate,
        // although there may be cases where this actually succeeds when UPnP
        // mapping failed due to simultaneous open behavior on the NAT.
        if (this.m_udpServerReflexiveAddress != null && client.hostPortMapped()) {
            // The port mapping maps the local port to the same port on the
            // public gateway.
            final InetSocketAddress publicHostAddress = new InetSocketAddress(
                    this.m_udpServerReflexiveAddress.getAddress(),
                    hostAddress.getPort());

            final IceCandidate publicHostCandidate = 
                new IceTcpHostPassiveCandidate(publicHostAddress, 
                    this.m_controlling);
            candidates.add(publicHostCandidate);
        }

        // Add the active candidate.
        try {
            final InetSocketAddress activeAddress = new InetSocketAddress(
                    NetworkUtils.getLocalHost(), 0);
            final IceTcpActiveCandidate activeCandidate = 
                new IceTcpActiveCandidate(activeAddress, this.m_controlling);
            candidates.add(activeCandidate);
        } catch (final UnknownHostException e) {
            m_log.error("Could not resolve host!", e);
            return candidates;
        }

        return candidates;
    }

    private void addTcpTurnCandidate(final StunAddressProvider client,
            final Collection<IceCandidate> candidates) {
        final InetSocketAddress relayAddress = client.getRelayAddress();

        // For relayed candidates, the related address is the mapped
        // address.
        final InetSocketAddress relatedAddress;
        try {
            relatedAddress = client.getServerReflexiveAddress();
        } catch (final IOException e) {
            m_log.error("Could not get server reflexive address and "
                    + "therefore TURN candidate", e);
            return;
        }

        final InetAddress stunServerAddress = client.getStunServerAddress();

        // Add the relay candidate. Note that for relay candidates, the
        // base candidate is the relay candidate itself.
        final IceCandidate relayCandidate = new IceTcpRelayPassiveCandidate(
                relayAddress, stunServerAddress, relatedAddress.getAddress(),
                relatedAddress.getPort(), this.m_controlling);
        candidates.add(relayCandidate);
    }

    public void close() {
        if (this.m_iceTcpStunPeer != null) {
            this.m_iceTcpStunPeer.close();
        }
        if (this.m_iceUdpStunPeer != null) {
            this.m_iceUdpStunPeer.close();
        }
    }

    public InetAddress getPublicAddress() {
        return this.m_udpServerReflexiveAddress.getAddress();
    }
}
