package org.lastbamboo.common.ice.candidate;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;

import junit.framework.TestCase;

import org.lastbamboo.common.ice.IceCheckList;
import org.lastbamboo.common.ice.IceCheckListCreator;
import org.lastbamboo.common.ice.IceCheckListCreatorImpl;
import org.lastbamboo.common.ice.IcePriorityCalculator;
import org.lastbamboo.common.ice.IceTransportProtocol;
import org.lastbamboo.common.util.NetworkUtils;

/**
 * Test for check list creation.
 */
public class IceCheckListCreatorImplTest extends TestCase
    {

    /**
     * Test checklist creation.
     * 
     * @throws Exception If any unexpected error occurs.
     */
    public void testCreateCheckList() throws Exception
        {
        final Collection<IceCandidate> localCandidates = createCandidates(true);
        final Collection<IceCandidate> remoteCandidates = createCandidates(false);
        
        final IceCheckListCreator creator = new IceCheckListCreatorImpl();
        final IceCheckList checkList = 
            creator.createCheckList(localCandidates, remoteCandidates);
        
        final Collection<IceCandidatePair> pairs = checkList.getPairs();
        
        // The pruning process should prune down to 2 pairs since the local
        // host candidate and the local server reflexive candidate become the
        // same.
        assertEquals(4, pairs.size());
        
        final Iterator<IceCandidatePair> iter = pairs.iterator();
        final IceCandidatePair pair1 = iter.next();
        final IceCandidatePair pair2 = iter.next();
        final IceCandidatePair pair3 = iter.next();
        final IceCandidatePair pair4 = iter.next();
        
        final IceCandidate local1 = pair1.getLocalCandidate();
        final IceCandidate local2 = pair2.getLocalCandidate();
        final IceCandidate local3 = pair3.getLocalCandidate();
        final IceCandidate local4 = pair4.getLocalCandidate();
        
        final IceCandidate remote1 = pair1.getRemoteCandidate();
        final IceCandidate remote2 = pair2.getRemoteCandidate();
        final IceCandidate remote3 = pair3.getRemoteCandidate();
        final IceCandidate remote4 = pair4.getRemoteCandidate();
        
        assertEquals(IceTransportProtocol.TCP_ACT, local1.getTransport());
        assertEquals(IceTransportProtocol.UDP, local2.getTransport());
        assertEquals(IceTransportProtocol.UDP, local3.getTransport());
        assertEquals(IceTransportProtocol.TCP_ACT, local4.getTransport());
        
        assertEquals(IceTransportProtocol.TCP_PASS, remote1.getTransport());
        assertEquals(IceTransportProtocol.UDP, remote2.getTransport());
        assertEquals(IceTransportProtocol.UDP, remote3.getTransport());
        assertEquals(IceTransportProtocol.TCP_PASS, remote4.getTransport());
        
        assertEquals(IceCandidateType.HOST, remote1.getType());
        assertEquals(IceCandidateType.HOST, remote2.getType());
        assertEquals(IceCandidateType.SERVER_REFLEXIVE, remote3.getType());
        assertEquals(IceCandidateType.RELAYED, remote4.getType());
        }

    private Collection<IceCandidate> createCandidates(
        final boolean controlling) throws Exception
        {
        final Collection<IceCandidate> candidates =
            new LinkedList<IceCandidate>();
        
        final InetSocketAddress local = 
            new InetSocketAddress(NetworkUtils.getLocalHost(), 4279);
        final InetSocketAddress srvRfl = 
            new InetSocketAddress("78.2.24.9", 8675);
        
        final long localPriority = IcePriorityCalculator.calculatePriority(
            IceCandidateType.HOST, IceTransportProtocol.UDP);
        
        final IceCandidate udpHost = 
            new IceUdpHostCandidate(local, "42342", localPriority, 
                controlling, 1); 
        candidates.add(udpHost);
        
        final InetAddress stunServerAddress = 
            InetAddress.getByName("35.52.3.53");
        final IceCandidate udpServerReflexive = 
            new IceUdpServerReflexiveCandidate(srvRfl, udpHost, stunServerAddress, 
                controlling);
        candidates.add(udpServerReflexive);
        
        final IceCandidate tcpActiveCandidate =
            new IceTcpActiveCandidate(local, controlling);
        
        final IceCandidate tcpHostPassiveCandidate =
            new IceTcpHostPassiveCandidate(srvRfl, controlling);
        
        final IceCandidate tcpRelayPassiveCandidate =
            new IceTcpRelayPassiveCandidate(srvRfl, stunServerAddress, 
                InetAddress.getByName("32.43.87.6"), 7429, controlling);
        
        candidates.add(tcpActiveCandidate);
        candidates.add(tcpHostPassiveCandidate);
        candidates.add(tcpRelayPassiveCandidate);
        return candidates;
        }

    }
