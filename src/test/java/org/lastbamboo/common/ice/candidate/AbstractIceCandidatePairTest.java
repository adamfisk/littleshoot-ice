package org.lastbamboo.common.ice.candidate;

import java.net.InetSocketAddress;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import junit.framework.TestCase;

import org.lastbamboo.common.ice.stubs.IoSessionStub;

/**
 * Test for ICE candidate pairs.
 */
public class AbstractIceCandidatePairTest extends TestCase
    {
    
    public void testCompareTo() throws Exception
        {
        
        // Create a UDP and a TCP pair and compare them
        final InetSocketAddress tcpLocalAddress = 
            new InetSocketAddress("192.168.2.3", 4322);
        final InetSocketAddress tcpRemoteAddress = 
            new InetSocketAddress("91.34.2.3", 4322);

        final IceCandidate tcpLocal = 
            new IceTcpActiveCandidate(tcpLocalAddress, true);
        final IceCandidate tcpRemote = 
            new IceTcpHostPassiveCandidate(tcpRemoteAddress, false);
        final IceTcpCandidatePair tcpPair = 
            new IceTcpCandidatePair(tcpLocal, tcpRemote, new IoSessionStub(), 
                null);
        
        final InetSocketAddress udpLocalAddress = 
            new InetSocketAddress("192.168.3.2", 8824);
        final InetSocketAddress udpRemoteAddress = 
            new InetSocketAddress("91.43.3.2", 3224);
        final IceCandidate udpLocal =
            new IceUdpHostCandidate(udpLocalAddress, true);
        final IceCandidate udpRemote =
            new IceUdpHostCandidate(udpRemoteAddress, false);
        
        final IceUdpCandidatePair udpPair =
            new IceUdpCandidatePair(udpLocal, udpRemote, new IoSessionStub(), null);

        // Throw in another TCP pair with a private remote address.
        final InetSocketAddress tcpLocalPrivateAddress = 
            new InetSocketAddress("192.168.2.3", 4322);
        final InetSocketAddress tcpRemotePrivateAddress = 
            new InetSocketAddress("192.168.2.2", 4322);
    
        final IceCandidate tcpLocalPrivate = 
            new IceTcpActiveCandidate(tcpLocalPrivateAddress, true);
        final IceCandidate tcpRemotePrivate = 
            new IceTcpHostPassiveCandidate(tcpRemotePrivateAddress, false);
        final IceTcpCandidatePair tcpPairPrivate = 
            new IceTcpCandidatePair(tcpLocalPrivate, tcpRemotePrivate, 
                new IoSessionStub(), null);
        
        final List<IceCandidatePair> pairs = new LinkedList<IceCandidatePair>();
        pairs.add(udpPair);
        pairs.add(tcpPair);
        pairs.add(tcpPairPrivate);
        
        assertTrue(tcpPair.getPriority() > udpPair.getPriority());
        
        Collections.sort(pairs);
        
        assertEquals(tcpPair, pairs.get(0));
        assertEquals(tcpPairPrivate, pairs.get(1));
        assertEquals(udpPair, pairs.get(2));
        }
    }
