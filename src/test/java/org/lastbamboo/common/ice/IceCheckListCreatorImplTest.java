package org.lastbamboo.common.ice;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;

import junit.framework.TestCase;

import org.lastbamboo.common.ice.candidate.IceCandidate;
import org.lastbamboo.common.ice.candidate.IceCandidatePair;
import org.lastbamboo.common.ice.candidate.IceTcpHostPassiveCandidate;
import org.lastbamboo.common.ice.candidate.IceTcpRelayPassiveCandidate;
import org.lastbamboo.common.ice.candidate.IceTcpServerReflexiveSoCandidate;
import org.lastbamboo.common.util.NetworkUtils;

public class IceCheckListCreatorImplTest extends TestCase
    {

    public void testCreateCheckList() throws Exception
        {
        final IceCheckListCreatorImpl creator = new IceCheckListCreatorImpl();
        Collection<IceCandidate> localCandidates = createCandidates(true);
        Collection<IceCandidate> remoteCandidates = createCandidates(false);
        final Collection<IceCandidatePair> checkList = 
            creator.createCheckList(localCandidates, remoteCandidates);
        
        long lastPairPriority = Long.MAX_VALUE;
        IceCandidatePair lastPair = null;
        
        for (final IceCandidatePair pair : checkList)
            {
            assertTrue("Bad pair sorting:\n" +
                "pair:   "+lastPair+"\n" +
                "before: "+pair, 
                lastPairPriority > pair.getPriority());
            lastPairPriority = pair.getPriority();
            lastPair = pair;
            }
        
        final Iterator<IceCandidatePair> iter = checkList.iterator();
        final IceCandidatePair pair1 = iter.next();
        final IceCandidatePair pair2 = iter.next();
        final IceCandidatePair pair3 = iter.next();
        final IceCandidatePair pair4 = iter.next();
        final IceCandidatePair pair5 = iter.next();
        final IceCandidatePair pair6 = iter.next();
        
        final IceCandidate local1 = pair1.getLocalCandidate();
        final IceCandidate remote1 = pair1.getRemoteCandidate();
        assertTrue(local1.getType() == IceCandidateType.HOST);
        assertTrue(remote1.getType() == IceCandidateType.HOST);
        
        final IceCandidate local2 = pair2.getLocalCandidate();
        final IceCandidate remote2 = pair2.getRemoteCandidate();
        assertTrue(local2.getType() == IceCandidateType.HOST);
        assertTrue(remote2.getType() == IceCandidateType.SERVER_REFLEXIVE);
        
        final IceCandidate local3 = pair3.getLocalCandidate();
        final IceCandidate remote3 = pair3.getRemoteCandidate();
        assertTrue(local3.getType() == IceCandidateType.HOST);
        assertTrue(remote3.getType() == IceCandidateType.RELAYED);
        
        final IceCandidate local4 = pair4.getLocalCandidate();
        final IceCandidate remote4 = pair4.getRemoteCandidate();
        assertTrue(local4.getType() == IceCandidateType.RELAYED);
        assertTrue(remote4.getType() == IceCandidateType.HOST);
        
        final IceCandidate local5 = pair5.getLocalCandidate();
        final IceCandidate remote5 = pair5.getRemoteCandidate();
        assertTrue(local5.getType() == IceCandidateType.RELAYED);
        assertTrue(remote5.getType() == IceCandidateType.SERVER_REFLEXIVE);
        
        final IceCandidate local6 = pair6.getLocalCandidate();
        final IceCandidate remote6 = pair6.getRemoteCandidate();
        assertTrue(local6.getType() == IceCandidateType.RELAYED);
        assertTrue(remote6.getType() == IceCandidateType.RELAYED);
        
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
                relatedPort, controlling);
        
        InetAddress baseAddress = relatedAddress;
        InetAddress stunServerAddress = InetAddress.getByName("32.8.5.4");
        final IceCandidate c3 =
            new IceTcpServerReflexiveSoCandidate(socketAddress, baseAddress, 
                stunServerAddress, relatedAddress, relatedPort, controlling);
        candidates.add(c1);
        candidates.add(c2);
        candidates.add(c3);
        return candidates;
        }
    }
