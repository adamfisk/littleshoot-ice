package org.lastbamboo.common.ice;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;

import junit.framework.TestCase;

import org.lastbamboo.common.ice.candidate.IceCandidate;
import org.lastbamboo.common.ice.candidate.IceCandidatePair;
import org.lastbamboo.common.ice.candidate.IceTcpActiveCandidate;
import org.lastbamboo.common.ice.candidate.IceTcpHostPassiveCandidate;
import org.lastbamboo.common.ice.candidate.IceTcpRelayPassiveCandidate;
import org.lastbamboo.common.ice.candidate.IceTcpServerReflexiveSoCandidate;
import org.lastbamboo.common.ice.stubs.StunClientStub;
import org.lastbamboo.common.stun.client.StunClient;
import org.lastbamboo.common.util.NetworkUtils;

/**
 * Test for the class for creating ICE check lists. 
 */
public class IceCheckListCreatorImplTest extends TestCase
    {

    public void testCreateCheckList() throws Exception
        {
        final IceCheckListCreatorImpl creator = new IceCheckListCreatorImpl();
        Collection<IceCandidate> localCandidates = createCandidates(true);
        Collection<IceCandidate> remoteCandidates = createCandidates(false);
        final IceCheckList checkList = 
            creator.createCheckList(localCandidates, remoteCandidates);
        
        final Collection<IceCandidatePair> pairs = checkList.getPairs();
        
        long lastPairPriority = Long.MAX_VALUE;
        IceCandidatePair lastPair = null;
        
        for (final IceCandidatePair pair : pairs)
            {
            assertTrue("Bad pair sorting:\n" +
                "pair:   "+lastPair+"\n" +
                "before: "+pair, 
                lastPairPriority >= pair.getPriority());
            lastPairPriority = pair.getPriority();
            lastPair = pair;
            }
        
        final Iterator<IceCandidatePair> iter = pairs.iterator();
        final IceCandidatePair pair1 = iter.next();
        final IceCandidatePair pair2 = iter.next();
        final IceCandidatePair pair3 = iter.next();
        
        final IceCandidate local1 = pair1.getLocalCandidate();
        final IceCandidate remote1 = pair1.getRemoteCandidate();
        assertEquals(IceCandidateType.HOST, local1.getType());
        assertEquals(IceTransportProtocol.TCP_ACT, local1.getTransport());
        assertEquals(IceCandidateType.HOST, remote1.getType());
        assertEquals(IceTransportProtocol.TCP_PASS, remote1.getTransport());
        
        final IceCandidate local2 = pair2.getLocalCandidate();
        final IceCandidate remote2 = pair2.getRemoteCandidate();
        assertEquals(IceCandidateType.SERVER_REFLEXIVE, local2.getType());
        assertEquals(IceTransportProtocol.TCP_SO, local2.getTransport());
        assertEquals(IceCandidateType.SERVER_REFLEXIVE, remote2.getType());
        assertEquals(IceTransportProtocol.TCP_SO, remote2.getTransport());
        
        
        final IceCandidate local3 = pair3.getLocalCandidate();
        final IceCandidate remote3 = pair3.getRemoteCandidate();
        assertEquals(IceCandidateType.HOST, local3.getType());
        assertEquals(IceTransportProtocol.TCP_ACT, local3.getTransport());
        assertEquals(IceCandidateType.RELAYED, remote3.getType());
        assertEquals(IceTransportProtocol.TCP_PASS, remote3.getTransport());
        }

    private Collection<IceCandidate> createCandidates(final boolean controlling)
        throws Exception
        {
        final Collection<IceCandidate> candidates = 
            new LinkedList<IceCandidate>();
        final InetSocketAddress socketAddress = 
            new InetSocketAddress("21.32.43.1", 4324);
        final IceCandidate c1 = 
            new IceTcpHostPassiveCandidate(socketAddress, controlling);
        final int relatedPort = 42389;
        final InetAddress relatedAddress = NetworkUtils.getLocalHost();
        final IceCandidate c2 = 
            new IceTcpRelayPassiveCandidate(socketAddress, 2, relatedAddress, 
                relatedPort, controlling, 573L, 1);
        
        final InetSocketAddress baseCandidateAddress = 
            new InetSocketAddress("192.168.1.100", 4242);
        StunClient iceStunClient = new StunClientStub(socketAddress.getAddress());
        IceCandidate baseCandidate = 
            new IceTcpHostPassiveCandidate(baseCandidateAddress, true);
        final IceCandidate c3 =
            new IceTcpServerReflexiveSoCandidate(socketAddress, baseCandidate, 
                iceStunClient, controlling);
        
        // Add the active candidate.
        final IceCandidate active = 
            new IceTcpActiveCandidate(socketAddress, controlling);
        
        candidates.add(c1);
        candidates.add(c2);
        candidates.add(c3);
        candidates.add(active);
        return candidates;
        }
    }
