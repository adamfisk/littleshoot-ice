package org.lastbamboo.common.ice;

import java.io.OutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;

import junit.framework.TestCase;

import org.apache.commons.id.uuid.UUID;

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
        final IceCandidate candidate = new TcpPassiveIceCandidate(1, 
            UUID.randomUUID(), 1, socketAddress);
        tracker.visitTcpPassiveIceCandidate(candidate);
        
        final Socket sock = tracker.getBestSocket();
        assertNotNull(sock);
        assertTrue(sock.isConnected());
        assertEquals(socketAddress, sock.getRemoteSocketAddress());
        
        final OutputStream os = sock.getOutputStream();
        os.write("GET".getBytes());
        sock.close();
        }
    }
