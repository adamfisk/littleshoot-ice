package org.lastbamboo.common.ice.sdp;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.StringTokenizer;

import junit.framework.TestCase;

import org.apache.commons.lang.math.NumberUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.lastbamboo.common.ice.IceTransportProtocol;
import org.lastbamboo.common.ice.candidate.IceCandidate;
import org.lastbamboo.common.ice.candidate.IceTcpHostPassiveCandidate;
import org.lastbamboo.common.ice.candidate.IceTcpRelayPassiveCandidate;
import org.lastbamboo.common.ice.candidate.IceUdpServerReflexiveCandidate;
import org.lastbamboo.common.sdp.api.Attribute;
import org.lastbamboo.common.sdp.api.MediaDescription;
import org.lastbamboo.common.sdp.api.SessionDescription;
import org.lastbamboo.common.util.NetworkUtils;

/**
 * Test for the class for generating SDP data.
 */
public final class IceCandidateSdpEncoderTest extends TestCase
    {

    /**
     * Logger for this class.
     */
    private static final Log LOG = 
        LogFactory.getLog(IceCandidateSdpEncoderTest.class);
    
    /**
     * Tests the method for creating SDP for the local host.
     * @throws Exception If any unexpected error occurs.
     */
    public void testCreateSdp() throws Exception
        {
        
        final org.lastbamboo.common.sdp.api.SdpFactory sdpFactory = 
            org.lastbamboo.common.sdp.api.SdpFactory.getInstance();
        
        final IceCandidateSdpEncoder encoder = new IceCandidateSdpEncoder();
        
        final InetAddress stunServerAddress = 
            InetAddress.getByName("23.42.4.96");
        final InetSocketAddress sa1 = 
            new InetSocketAddress("46.2.62.1", 5466);
        final InetSocketAddress sa2 = 
            new InetSocketAddress("12.12.32.1", 4232);
        final InetSocketAddress sa3 = 
            new InetSocketAddress("192.168.1.3", 7652);
        
        final InetAddress relatedAddress = InetAddress.getByName("42.12.32.1");
        final int relatedPort = 4728;
        
        final IceUdpServerReflexiveCandidate udpServerReflexiveCandidate = 
            new IceUdpServerReflexiveCandidate(sa1, NetworkUtils.getLocalHost(), 
                sa1.getAddress(), relatedAddress, relatedPort);
        //final UdpIceCandidate udpCandidate = 
          //  new UdpIceCandidate(1, UUID.randomUUID(), 3, sa1);
        
        final IceTcpRelayPassiveCandidate tcpRelayPassiveCandidate =
            new IceTcpRelayPassiveCandidate(sa2, NetworkUtils.getLocalHost(), 
                stunServerAddress, relatedAddress, relatedPort);
        //final TcpPassiveIceCandidate tcpPassiveCandidate = 
          //  new TcpPassiveIceCandidate(1, UUID.randomUUID(), 5, sa2);
        
        final IceTcpHostPassiveCandidate tcpHostPassiveCandidate =
            new IceTcpHostPassiveCandidate(sa3);
        //final TcpPassiveIceCandidate localCandidate = 
          //  new TcpPassiveIceCandidate(1, UUID.randomUUID(), 5, sa3);
        
        final Collection<IceCandidate> candidates = 
            new LinkedList<IceCandidate>();
        
        candidates.add(udpServerReflexiveCandidate);
        candidates.add(tcpRelayPassiveCandidate);
        candidates.add(tcpHostPassiveCandidate);
        encoder.visitCandidates(candidates);
        final byte[] sdpBytes = encoder.getSdp();
        
        final SessionDescription sdp = 
            sdpFactory.createSessionDescription(new String(sdpBytes));
        
        final Collection mediaDescriptions = sdp.getMediaDescriptions(true);
        
        // There should be 3 media descriptions -- one for UDP, and two for
        // TCP (TURN and local).
        assertEquals(3, mediaDescriptions.size());
        
        final Iterator iter = mediaDescriptions.iterator();
        final MediaDescription udpMediaDesc = (MediaDescription) iter.next();
        final MediaDescription tcpMediaDesc = (MediaDescription) iter.next();
        final MediaDescription localTcpMediaDesc = 
            (MediaDescription) iter.next();
        
        // Just create a collection with one element for the UDP test.
        final Collection<InetSocketAddress> udpBindings = 
            new HashSet<InetSocketAddress>();
        udpBindings.add(sa1);
        final Collection<InetSocketAddress> tcpBindings = 
            new HashSet<InetSocketAddress>();
        tcpBindings.add(sa2);
        final Collection<InetSocketAddress> localTcpBindings = 
            new HashSet<InetSocketAddress>();
        localTcpBindings.add(sa3);
        verifyCandidates(udpMediaDesc, udpBindings, "udp");
        verifyCandidates(tcpMediaDesc, tcpBindings, 
            IceTransportProtocol.TCP_PASS.getName());
        verifyCandidates(localTcpMediaDesc, localTcpBindings, 
            IceTransportProtocol.TCP_PASS.getName(), 6);
        }

    private void verifyCandidates(MediaDescription mediaDesc, 
        final Collection<InetSocketAddress> bindings, 
        final String transport, final int numElements) throws Exception
        {
        final Collection attributes = mediaDesc.getAttributes(true);
        assertTrue(attributes.size() >= bindings.size());
        int numCandidates = 0;
        for (final Iterator iter = attributes.iterator(); iter.hasNext();)
            {
            final Attribute attribute = (Attribute) iter.next();
            LOG.trace("Testing attribute: "+attribute);
            if (!attribute.getName().startsWith("candidate"))
                {
                continue;
                }
            numCandidates++;
            final StringTokenizer st = 
                new StringTokenizer(attribute.getValue(), " ");
            
            assertEquals(numElements, st.countTokens());
            
            // Parse the foundation.
            assertTrue(NumberUtils.isNumber(st.nextToken()));
            //assertEquals("1", st.nextToken());
            
            // Just parse the component ID.
            assertTrue(NumberUtils.isNumber(st.nextToken()));
            
            assertEquals(transport, st.nextToken());
            assertTrue(NumberUtils.isNumber(st.nextToken()));
            
            final InetAddress address = InetAddress.getByName(st.nextToken());
            final int port = Integer.parseInt(st.nextToken());
            final InetSocketAddress socketAddress = 
                new InetSocketAddress(address, port);
            
            assertTrue("Address "+socketAddress+" not in: "+bindings, 
                bindings.contains(socketAddress));
            
            if (st.hasMoreElements())
                {
                // Just make sure this doesn't throw an exception.
                InetAddress.getByName(st.nextToken());
                assertTrue(NumberUtils.isNumber(st.nextToken()));
                }
            }
        
        assertEquals(bindings.size(), numCandidates);
        }

    /**
     * Verifies that the candidates listed in the given media description
     * match the expected candidate addresses.
     * @param mediaDesc The media description to check.
     * @param bindings The expected candidate bindings.
     * @param transport The transport for the candidate, such as TCP or UDP. 
     */
    private void verifyCandidates(final MediaDescription mediaDesc, 
        final Collection<InetSocketAddress> bindings, final String transport) 
        throws Exception
        {
        verifyCandidates(mediaDesc, bindings, transport, 8);
        }
    }
