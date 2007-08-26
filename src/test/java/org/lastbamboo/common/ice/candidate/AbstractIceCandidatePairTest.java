package org.lastbamboo.common.ice.candidate;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.lastbamboo.common.ice.IceStunChecker;
import org.lastbamboo.common.ice.IceStunServerConnectivityChecker;
import org.lastbamboo.common.ice.IceStunServerConnectivityCheckerImpl;
import org.lastbamboo.common.ice.IceUdpStunChecker;

import junit.framework.TestCase;

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
            new IceTcpActiveCandidate(tcpRemoteAddress, false);
        final TcpIceCandidatePair tcpPair = 
            new TcpIceCandidatePair(tcpLocal, tcpRemote);
        
        final InetAddress stunServerAddress = 
            InetAddress.getByName("64.2.1.86");
        final InetSocketAddress udpLocalAddress = 
            new InetSocketAddress("192.168.3.2", 8824);
        final InetSocketAddress udpRemoteAddress = 
            new InetSocketAddress("91.43.3.2", 3224);
        final IceCandidate udpLocal =
            new IceUdpHostCandidate(udpLocalAddress, true);
        final IceCandidate udpRemote =
            new IceUdpHostCandidate(udpRemoteAddress, false);
        
        final UdpIceCandidatePair udpPair =
            new UdpIceCandidatePair(udpLocal, udpRemote, null);

        final List<IceCandidatePair> pairs = new LinkedList<IceCandidatePair>();
        pairs.add(udpPair);
        pairs.add(tcpPair);
        
        assertTrue(tcpPair.getPriority() > udpPair.getPriority());
        
        Collections.sort(pairs);
        
        assertEquals(tcpPair, pairs.get(0));
        assertEquals(udpPair, pairs.get(1));
        
        }
    }
