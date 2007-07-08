package org.lastbamboo.common.ice;

import java.io.OutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;

import junit.framework.TestCase;

import org.lastbamboo.common.ice.candidate.IceTcpHostPassiveCandidate;

/**
 * Tests the tracker for UAC ICE candidates.
 */
public class UacIceCandidateTrackerImplTest extends TestCase
    {

    /**
     * Tests the method for accessing the best socket through just connecting
     * to a well-known host.
     * 
     * @throws Exception If any unexpected error occurs.
     */
    public void testGetBestSocket() throws Exception
        {        
        final UacIceCandidateTracker tracker = new UacIceCandidateTracker();
        
        final InetAddress ia = InetAddress.getByName("www.google.com");
        final InetSocketAddress socketAddress = new InetSocketAddress(ia, 80);
        final IceTcpHostPassiveCandidate candidate = 
            new IceTcpHostPassiveCandidate(socketAddress, false);
        
        tracker.visitTcpHostPassiveCandidate(candidate);
        
        final Socket sock = tracker.getBestSocket();
        assertNotNull(sock);
        assertTrue(sock.isConnected());
        assertEquals(socketAddress, sock.getRemoteSocketAddress());
        
        final OutputStream os = sock.getOutputStream();
        os.write("GET".getBytes());
        sock.close();
        }
    }
