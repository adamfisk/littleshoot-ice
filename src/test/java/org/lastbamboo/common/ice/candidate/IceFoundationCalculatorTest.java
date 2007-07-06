package org.lastbamboo.common.ice.candidate;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;

import junit.framework.TestCase;

/**
 * Tests calculating foundations for ICE candidates. 
 */
public class IceFoundationCalculatorTest extends TestCase
    {
    
    public void testCalculateFoundation() throws Exception
        {
        // Establish the bases.
        final InetSocketAddress hostAddress1 = 
            new InetSocketAddress("192.168.1.1", 2433);
        final InetSocketAddress hostAddress2 = 
            new InetSocketAddress("192.168.1.1", 33454);
        final InetSocketAddress hostAddress3 = 
            new InetSocketAddress("192.168.1.100", 33454);
        
        final InetAddress baseAddress1 = hostAddress1.getAddress();
        final InetAddress baseAddress2 = hostAddress2.getAddress();
        final InetAddress baseAddress3 = hostAddress3.getAddress();
        
        // Create the public addresses.
        final InetSocketAddress publicAddress1 = 
            new InetSocketAddress("92.68.1.1", 2433);
        final InetSocketAddress publicAddress2 = 
            new InetSocketAddress("92.68.1.1", 33454);
        final InetSocketAddress publicAddress3 = 
            new InetSocketAddress("92.68.1.100", 33454);
        
        // Create the STUN server addresses.
        final InetAddress stun1 = InetAddress.getByName("47.54.23.2");
        final InetAddress stun2 = InetAddress.getByName("4.4.23.2");
        final InetAddress stun3 = InetAddress.getByName("7.5.23.2");
        
        // Create the TCP host candidates.
        final Collection<IceCandidate> tcpHost = new LinkedList<IceCandidate>();
        tcpHost.add(new IceTcpHostPassiveCandidate(hostAddress1));
        tcpHost.add(new IceTcpHostPassiveCandidate(hostAddress2));
        tcpHost.add(new IceTcpHostPassiveCandidate(hostAddress3));
        runHostTests(tcpHost);
        
        final Collection<IceCandidate> udpHost = new LinkedList<IceCandidate>();
        udpHost.add(new IceUdpHostCandidate(hostAddress1));
        udpHost.add(new IceUdpHostCandidate(hostAddress2));
        udpHost.add(new IceUdpHostCandidate(hostAddress3));
        runHostTests(udpHost);
        
        final int relatedPort = 4729;
        
        // This is the related address for relayed candidates.
        final InetAddress mappedAddress = publicAddress1.getAddress();
        
        // Create the TCP server reflexive candidates.
        final Collection<IceCandidate> tcpSr = new LinkedList<IceCandidate>();
        tcpSr.add(new IceTcpServerReflexiveSoCandidate(publicAddress1, baseAddress1, stun1, baseAddress1, relatedPort));
        // Base address with different port.
        tcpSr.add(new IceTcpServerReflexiveSoCandidate(publicAddress1, baseAddress2, stun1, baseAddress2, relatedPort));
        // Base address with different IP.
        tcpSr.add(new IceTcpServerReflexiveSoCandidate(publicAddress1, baseAddress3, stun1, baseAddress3, relatedPort));
        // Different STUN server address.
        tcpSr.add(new IceTcpServerReflexiveSoCandidate(publicAddress1, baseAddress1, stun2, baseAddress1, relatedPort));
        // Different public address -- should have no effect on the foundation.
        tcpSr.add(new IceTcpServerReflexiveSoCandidate(publicAddress2, baseAddress1, stun1, baseAddress1, relatedPort));
        runStunCandidateTests(tcpSr);
        
        // Create the UDP server reflexive candidates.
        final Collection<IceCandidate> udpSr = new LinkedList<IceCandidate>();
        udpSr.add(new IceUdpServerReflexiveCandidate(publicAddress1, baseAddress1, stun1, baseAddress1, relatedPort));
        // Base address with different port.
        udpSr.add(new IceUdpServerReflexiveCandidate(publicAddress1, baseAddress2, stun1, baseAddress2, relatedPort));
        // Base address with different IP.
        udpSr.add(new IceUdpServerReflexiveCandidate(publicAddress1, baseAddress3, stun1, baseAddress3, relatedPort));
        // Different STUN server address.
        udpSr.add(new IceUdpServerReflexiveCandidate(publicAddress1, baseAddress1, stun2, baseAddress1, relatedPort));
        // Different public address -- should have no effect on the foundation.
        udpSr.add(new IceUdpServerReflexiveCandidate(publicAddress2, baseAddress1, stun1, baseAddress1, relatedPort));
        runStunCandidateTests(udpSr);
        
        // Create the TCP relay candidates.
        final Collection<IceCandidate> tcpRelay = new LinkedList<IceCandidate>();
        tcpRelay.add(new IceTcpRelayPassiveCandidate(publicAddress1, baseAddress1, stun1, mappedAddress, relatedPort));
        // Base address with different port.
        tcpRelay.add(new IceTcpRelayPassiveCandidate(publicAddress1, baseAddress2, stun1, mappedAddress, relatedPort));
        // Base address with different IP.
        tcpRelay.add(new IceTcpRelayPassiveCandidate(publicAddress1, baseAddress3, stun1, mappedAddress, relatedPort));
        // Different STUN server address.
        tcpRelay.add(new IceTcpRelayPassiveCandidate(publicAddress1, baseAddress1, stun2, mappedAddress, relatedPort));
        // Different public address -- should have no effect on the foundation.
        tcpRelay.add(new IceTcpRelayPassiveCandidate(publicAddress2, baseAddress1, stun1, mappedAddress, relatedPort));
        runStunCandidateTests(tcpRelay);
        
        // Create the UDP relay candidates.
        final Collection<IceCandidate> udpRelay = new LinkedList<IceCandidate>();
        udpRelay.add(new IceUdpRelayCandidate(publicAddress1, baseAddress1, stun1, mappedAddress, relatedPort));
        // Base address with different port.
        udpRelay.add(new IceUdpRelayCandidate(publicAddress1, baseAddress2, stun1, mappedAddress, relatedPort));
        // Base address with different IP.
        udpRelay.add(new IceUdpRelayCandidate(publicAddress1, baseAddress3, stun1, mappedAddress, relatedPort));
        // Different STUN server address.
        udpRelay.add(new IceUdpRelayCandidate(publicAddress1, baseAddress1, stun2, mappedAddress, relatedPort));
        // Different public address -- should have no effect on the foundation.
        udpRelay.add(new IceUdpRelayCandidate(publicAddress2, baseAddress1, stun1, mappedAddress, relatedPort));
        runStunCandidateTests(udpRelay);
        
        // Create the UDP peer reflexive candidates.
        final Collection<IceCandidate> udpPeer = new LinkedList<IceCandidate>();
        udpPeer.add(new IceUdpPeerReflexiveCandidate(publicAddress1, baseAddress1, stun1, baseAddress1, relatedPort));
        // Base address with different port.
        udpPeer.add(new IceUdpPeerReflexiveCandidate(publicAddress1, baseAddress2, stun1, baseAddress1, relatedPort));
        // Base address with different IP.
        udpPeer.add(new IceUdpPeerReflexiveCandidate(publicAddress1, baseAddress3, stun1, baseAddress1, relatedPort));
        // Different STUN server address.
        udpPeer.add(new IceUdpPeerReflexiveCandidate(publicAddress1, baseAddress1, stun2, baseAddress1, relatedPort));
        // Different public address -- should have no effect on the foundation.
        udpPeer.add(new IceUdpPeerReflexiveCandidate(publicAddress2, baseAddress1, stun1, baseAddress1, relatedPort));
        runStunCandidateTests(udpPeer);
        
        // Now test different candidate types to make sure they don't match.
        
        assertNoneEqual(tcpHost, udpHost);
        assertNoneEqual(tcpHost, tcpSr);
        assertNoneEqual(tcpHost, tcpRelay);
        assertNoneEqual(tcpHost, udpSr);
        assertNoneEqual(tcpHost, udpRelay);
        assertNoneEqual(tcpHost, udpPeer);
        }

    private void assertNoneEqual(final Collection<IceCandidate> candidates1, 
        final Collection<IceCandidate> candidates2)
        {
        //final IceFoundationCalculator calc = new IceFoundationCalculator();
        int outerIndex = 0;
        for (final IceCandidate c1 : candidates1)
            {
            int innerIndex = 0;
            for (final IceCandidate c2: candidates2)
                {
                //final int f1 = calc.calculateFoundation(c1);
                //final int f2 = calc.calculateFoundation(c2);
                final int f1 = c1.getFoundation();
                final int f2 = c2.getFoundation();
                assertFalse("Foundations equal: "+f1+" "+f2+"\n"+
                    "outerIndex: "+outerIndex+" innerIndex: "+innerIndex, 
                    f1 == f2);
                innerIndex++;
                }
            outerIndex++;
            }
        }
    

    /**
     * Runs tests on calculating foundations with candidates that use a STUN
     * server.
     * 
     * @param stunCandidates The candidates to test.
     */
    private void runStunCandidateTests(
        final Collection<IceCandidate> stunCandidates)
        {
        //final IceFoundationCalculator calc = new IceFoundationCalculator();
        final Iterator<IceCandidate> iter = stunCandidates.iterator();
        final IceCandidate c1 = iter.next();
        final IceCandidate c2 = iter.next();
        final IceCandidate c3 = iter.next();
        final IceCandidate c4 = iter.next();
        final IceCandidate c5 = iter.next();
        
        assertEquals(c1.getFoundation(), c2.getFoundation());
        assertFalse(c1.getFoundation() == c3.getFoundation());
        assertFalse(c1.getFoundation() == c4.getFoundation());
        assertEquals(c1.getFoundation(), c5.getFoundation());
        
        /*
        assertFalse(calc.calculateFoundation(c1) ==
            calc.calculateFoundation(c3));
        assertFalse(calc.calculateFoundation(c1) ==
            calc.calculateFoundation(c4));
        assertEquals(calc.calculateFoundation(c1), 
            calc.calculateFoundation(c5));
            */
        }

    private void runHostTests(final Collection<IceCandidate> hosts)
        {
        final Iterator<IceCandidate> iter = hosts.iterator();
        final IceCandidate c1 = iter.next();
        final IceCandidate c2 = iter.next();
        final IceCandidate c3 = iter.next();
        assertEquals(c1.getFoundation(), c2.getFoundation()); 
        assertFalse(c1.getFoundation() == c3.getFoundation());
        }
    }
