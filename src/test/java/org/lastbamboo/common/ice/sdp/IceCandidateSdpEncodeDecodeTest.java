package org.lastbamboo.common.ice.sdp;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.Collection;
import java.util.LinkedList;
import java.util.StringTokenizer;

import junit.framework.TestCase;

import org.apache.commons.lang.math.NumberUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.mina.common.ByteBuffer;
import org.lastbamboo.common.ice.IceCandidateType;
import org.lastbamboo.common.ice.candidate.IceCandidate;
import org.lastbamboo.common.ice.candidate.IceTcpHostPassiveCandidate;
import org.lastbamboo.common.ice.candidate.IceTcpRelayPassiveCandidate;
import org.lastbamboo.common.ice.candidate.IceUdpServerReflexiveCandidate;
import org.lastbamboo.common.sdp.api.Attribute;
import org.lastbamboo.common.sdp.api.SdpFactory;
import org.lastbamboo.common.util.NetworkUtils;

/**
 * Test for the class for generating SDP data.
 */
public final class IceCandidateSdpEncodeDecodeTest extends TestCase
    {

    /**
     * Logger for this class.
     */
    private static final Log LOG = 
        LogFactory.getLog(IceCandidateSdpEncodeDecodeTest.class);
    

    /**
     * Tests the method for creating SDP for the local host.
     * @throws Exception If any unexpected error occurs.
     */
    public void testDecode() throws Exception
        {
        // First encode everything so we test both sides.
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
        final InetAddress relayRelatedAddress = 
            InetAddress.getByName("192.168.1.1");
        final int relayRelatedPort = 8768;
        
        final IceUdpServerReflexiveCandidate udpServerReflexiveCandidate = 
            new IceUdpServerReflexiveCandidate(sa1, NetworkUtils.getLocalHost(), 
                sa1.getAddress(), relatedAddress, relatedPort, false);
        
        final IceTcpRelayPassiveCandidate tcpRelayPassiveCandidate =
            new IceTcpRelayPassiveCandidate(sa2, NetworkUtils.getLocalHost(), 
                stunServerAddress, relayRelatedAddress, relayRelatedPort, false);

        final IceTcpHostPassiveCandidate tcpHostPassiveCandidate =
            new IceTcpHostPassiveCandidate(sa3, false);
        
        final Collection<IceCandidate> candidates = 
            new LinkedList<IceCandidate>();
        
        candidates.add(udpServerReflexiveCandidate);
        candidates.add(tcpRelayPassiveCandidate);
        candidates.add(tcpHostPassiveCandidate);
        encoder.visitCandidates(candidates);
        final ByteBuffer sdp = ByteBuffer.wrap(encoder.getSdp());
        
        // Now decode it.
        final IceCandidateSdpDecoderImpl decoder = 
            new IceCandidateSdpDecoderImpl(new SdpFactory());
        
        final Collection<IceCandidate> decoded = decoder.decode(sdp, false);
        assertEquals(3, decoded.size());
        }
    }
