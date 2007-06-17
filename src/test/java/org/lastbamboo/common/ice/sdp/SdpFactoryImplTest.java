package org.lastbamboo.common.ice.sdp;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.StringTokenizer;

import junit.framework.TestCase;

import org.apache.commons.lang.math.NumberUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.easymock.MockControl;
import org.lastbamboo.common.ice.BindingTracker;
import org.lastbamboo.common.ice.IceCandidateAttributeFactory;
import org.lastbamboo.common.ice.IceCandidateAttributeFactoryImpl;
import org.lastbamboo.common.ice.IceConstants;
import org.lastbamboo.common.ice.sdp.SdpFactoryImpl;
import org.lastbamboo.common.sdp.api.Attribute;
import org.lastbamboo.common.sdp.api.MediaDescription;
import org.lastbamboo.common.sdp.api.SdpFactory;
import org.lastbamboo.common.sdp.api.SessionDescription;
import org.lastbamboo.common.util.NetworkUtils;

/**
 * Test for the class for generating SDP data.
 */
public final class SdpFactoryImplTest extends TestCase
    {

    /**
     * Logger for this class.
     */
    private static final Log LOG = LogFactory.getLog(SdpFactoryImplTest.class);
    
    /**
     * Tests the method for creating SDP for the local host.
     * @throws Exception If any unexpected error occurs.
     */
    public void testCreateSdp() throws Exception
        {
        final MockControl trackerControl = 
            MockControl.createControl(BindingTracker.class);
        final BindingTracker tracker = 
            (BindingTracker) trackerControl.getMock();
        
        tracker.getTurnTcpBindings();
        
        final Collection localTcpBindings = createTcpLocalAddresses();
        
        final Collection tcpBindings = createTcpAddresses();
        trackerControl.setReturnValue(tcpBindings, 1);
        tracker.getStunUdpBinding();
        final InetSocketAddress udpBinding = 
            new InetSocketAddress(InetAddress.getByName("59.8.46.6"), 7888);
        trackerControl.setReturnValue(udpBinding, 1);
        
        tracker.getTcpSoBinding();
        trackerControl.setReturnValue(null);
        
        trackerControl.replay();
        final IceCandidateAttributeFactory candidateAttributeFactory =
            new IceCandidateAttributeFactoryImpl(new SdpFactory());
        
        final org.lastbamboo.common.sdp.api.SdpFactory sdpFactory = 
            org.lastbamboo.common.sdp.api.SdpFactory.getInstance();
        
        final SdpFactoryImpl factory = 
            new SdpFactoryImpl(tracker, candidateAttributeFactory, sdpFactory);
        final SessionDescription sdp = factory.createSdp();
        trackerControl.verify();
        
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
        final Collection udpBindings = new HashSet();
        udpBindings.add(udpBinding);
        verifyCandidates(udpMediaDesc, udpBindings, "udp");
        verifyCandidates(tcpMediaDesc, tcpBindings, IceConstants.TCP_PASS);
        verifyCandidates(localTcpMediaDesc, localTcpBindings, IceConstants.TCP_PASS);
        }

    /**
     * Verifies that the candidates listed in the given media description
     * match the expected candidate addresses.
     * @param mediaDesc The media description to check.
     * @param bindings The expected candidate bindings.
     * @param transport The transport for the candidate, such as TCP or UDP. 
     */
    private void verifyCandidates(final MediaDescription mediaDesc, 
        final Collection bindings, final String transport) throws Exception
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
            
            assertEquals(6, st.countTokens());
            assertEquals("1", st.nextToken());
            
            // Just parse the TID for now.
            st.nextToken();
            
            assertEquals(transport, st.nextToken());
            assertTrue(NumberUtils.isNumber(st.nextToken()));
            
            final InetAddress address = InetAddress.getByName(st.nextToken());
            final int port = Integer.parseInt(st.nextToken());
            final InetSocketAddress socketAddress = 
                new InetSocketAddress(address, port);
            
            
            assertTrue("Address "+socketAddress+" not in: "+bindings, 
                bindings.contains(socketAddress));
            }
        
        assertEquals(bindings.size(), numCandidates);
        }

    private Collection createTcpLocalAddresses() throws UnknownHostException
        {
        final InetSocketAddress socketAddress = 
            new InetSocketAddress(NetworkUtils.getLocalHost(), 8107);
        final Collection addresses = new HashSet();
        addresses.add(socketAddress);      
        return addresses;
        }
    
    private Collection createTcpAddresses() throws UnknownHostException
        {
        final InetSocketAddress socketAddress = 
            new InetSocketAddress(InetAddress.getByName("39.5.46.6"), 7888);
        final Collection addresses = new HashSet();
        addresses.add(socketAddress);      
        return addresses;
        }
    }
