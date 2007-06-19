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
import org.lastbamboo.common.ice.IceCandidateGenerator;
import org.lastbamboo.common.ice.IceCandidateGeneratorImpl;
import org.lastbamboo.common.ice.IceConstants;
import org.lastbamboo.common.sdp.api.Attribute;
import org.lastbamboo.common.sdp.api.MediaDescription;
import org.lastbamboo.common.sdp.api.SessionDescription;
import org.lastbamboo.common.util.NetworkUtils;

/**
 * Test for the class for generating SDP data.
 */
public final class IceCandidateGeneratorImplTest extends TestCase
    {

    /**
     * Logger for this class.
     */
    private static final Log LOG = 
        LogFactory.getLog(IceCandidateGeneratorImplTest.class);
    
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
        //tracker.getStunUdpBinding();
        //final InetSocketAddress udpBinding = 
          //  new InetSocketAddress(InetAddress.getByName("59.8.46.6"), 7888);
        //trackerControl.setReturnValue(udpBinding, 1);
        
        //tracker.getTcpSoBinding();
        //trackerControl.setReturnValue(null);
        
        trackerControl.replay();
        //final IceCandidateAttributeFactory candidateAttributeFactory =
          //  new IceCandidateAttributeFactoryImpl(new SdpFactory());
        
        final IceCandidateGenerator generator = 
            new IceCandidateGeneratorImpl(tracker);
        
        final byte[] sdpBytes = generator.generateCandidates();
        final String sdpString = new String(sdpBytes, "US-ASCII");
        System.out.println(sdpString);
        
        final org.lastbamboo.common.sdp.api.SdpFactory sdpFactory = 
            org.lastbamboo.common.sdp.api.SdpFactory.getInstance();
        final SessionDescription sdp = 
            sdpFactory.createSessionDescription(sdpString);
        
        trackerControl.verify();
        
        final Collection mediaDescriptions = sdp.getMediaDescriptions(true);
        
        // There should be 3 media descriptions -- one for UDP, and two for
        // TCP (TURN and local).
        assertEquals(2, mediaDescriptions.size());
        
        final Iterator iter = mediaDescriptions.iterator();
        //final MediaDescription udpMediaDesc = (MediaDescription) iter.next();
        final MediaDescription tcpMediaDesc = (MediaDescription) iter.next();
        final MediaDescription localTcpMediaDesc = 
            (MediaDescription) iter.next();
        
        // Just create a collection with one element for the UDP test.
        //final Collection<InetSocketAddress> udpBindings = 
          //  new HashSet<InetSocketAddress>();
        //udpBindings.add(udpBinding);
        //verifyCandidates(udpMediaDesc, udpBindings, "udp");
        final int relayPriority = 
            verifyCandidates(tcpMediaDesc, tcpBindings, IceConstants.TCP_PASS);
        final int hostPriority = 
            verifyCandidates(localTcpMediaDesc, localTcpBindings, 
            IceConstants.TCP_PASS);
        
        assertFalse(relayPriority == -1);
        assertFalse(hostPriority == -1);
        
        // Make sure we favor candidates on the local network over relayed 
        // candidates.
        assertTrue("Unexpected priorities.\nHost:   "+
            hostPriority+"\nRelay: "+relayPriority, 
            hostPriority > relayPriority);
        //final IceCandidate relayTcpCandidate = tcpMediaDesc.get
        }

    /**
     * Verifies that the candidates listed in the given media description
     * match the expected candidate addresses.
     * @param mediaDesc The media description to check.
     * @param bindings The expected candidate bindings.
     * @param transport The transport for the candidate, such as TCP or UDP. 
     * @return The priority of the last candidate.
     */
    private int verifyCandidates(final MediaDescription mediaDesc, 
        final Collection bindings, final String transport) throws Exception
        {
        final Collection attributes = mediaDesc.getAttributes(true);
        assertTrue(attributes.size() >= bindings.size());
        int numCandidates = 0;
        
        // A little weird because we only currently have one candidate per
        // media description, allowing us to return the priority like this.
        int priority = -1;
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
            
            final String priorityString = st.nextToken();
            assertTrue(NumberUtils.isNumber(priorityString));
            priority = Integer.parseInt(priorityString);
            
            final InetAddress address = InetAddress.getByName(st.nextToken());
            final int port = Integer.parseInt(st.nextToken());
            final InetSocketAddress socketAddress = 
                new InetSocketAddress(address, port);
            
            
            assertTrue("Address "+socketAddress+" not in: "+bindings, 
                bindings.contains(socketAddress));
            }
        
        assertEquals(bindings.size(), numCandidates);
        return priority;
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
