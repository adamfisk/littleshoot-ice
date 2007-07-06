package org.lastbamboo.common.ice.candidate;

import java.net.InetSocketAddress;
import java.util.Queue;
import java.util.concurrent.PriorityBlockingQueue;

import org.lastbamboo.common.ice.IceCandidateComparator;

import junit.framework.TestCase;

/**
 * Tests candidate priorities.
 */
public class PriorityTest extends TestCase
    {

    public void testPriority() throws Exception
        {
        final InetSocketAddress relayAddress = 
            new InetSocketAddress("32.43.21.3", 4235);
        final InetSocketAddress relatedAddress= 
            new InetSocketAddress("192.168.1.1", 42342);
        final IceTcpRelayPassiveCandidate relay = 
            new IceTcpRelayPassiveCandidate(relayAddress, 4243, 
                relatedAddress.getAddress(), relatedAddress.getPort());
        
        final IceTcpHostPassiveCandidate host = 
            new IceTcpHostPassiveCandidate(relatedAddress);
        
        assertTrue(host.getPriority() > relay.getPriority());
        
        final Queue<IceCandidate> candidates = 
            new PriorityBlockingQueue<IceCandidate>(4, new IceCandidateComparator());
        candidates.add(relay);
        candidates.add(host);
        
        final IceCandidate top = candidates.iterator().next();
        assertEquals(host, top);
        }
    }