package org.lastbamboo.common.ice;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;

import org.lastbamboo.common.ice.candidate.IceCandidate;
import org.lastbamboo.common.ice.candidate.IceCandidatePair;
import org.lastbamboo.common.ice.candidate.IceTcpHostPassiveCandidate;
import org.lastbamboo.common.ice.candidate.IceTcpRelayPassiveCandidate;
import org.lastbamboo.common.util.NetworkUtils;

import junit.framework.TestCase;

public class IceCheckListCreatorImplTest extends TestCase
    {

    public void testCreateCheckList() throws Exception
        {
        final IceCheckListCreatorImpl creator = new IceCheckListCreatorImpl();
        Collection<IceCandidate> localCandidates = createCandidates(true);
        Collection<IceCandidate> remoteCandidates = createCandidates(false);
        final Collection<IceCandidatePair> checkList = 
            creator.createCheckList(localCandidates, remoteCandidates);
        
        for (final IceCandidatePair pair : checkList)
            {
            System.out.println(pair+"\n");
            }
        
        final Iterator<IceCandidatePair> iter = checkList.iterator();
        final IceCandidatePair pair1 = iter.next();
        final IceCandidatePair pair2 = iter.next();
        final IceCandidatePair pair3 = iter.next();
        final IceCandidatePair pair4 = iter.next();
        
        final IceCandidate local1 = pair1.getLocalCandidate();
        final IceCandidate remote1 = pair1.getRemoteCandidate();
        assertTrue(local1.getType() == IceCandidateType.HOST);
        assertTrue(remote1.getType() == IceCandidateType.HOST);
        
        final IceCandidate local2 = pair2.getLocalCandidate();
        final IceCandidate remote2 = pair2.getRemoteCandidate();
        assertTrue(local2.getType() == IceCandidateType.HOST);
        assertTrue(remote2.getType() == IceCandidateType.RELAYED);
        
        final IceCandidate local3 = pair3.getLocalCandidate();
        final IceCandidate remote3 = pair3.getRemoteCandidate();
        assertTrue(local3.getType() == IceCandidateType.RELAYED);
        assertTrue(remote3.getType() == IceCandidateType.HOST);
        
        final IceCandidate local4 = pair4.getLocalCandidate();
        final IceCandidate remote4 = pair4.getRemoteCandidate();
        assertTrue(local4.getType() == IceCandidateType.RELAYED);
        assertTrue(remote4.getType() == IceCandidateType.RELAYED);
        
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
        candidates.add(c1);
        candidates.add(c2);
        return candidates;
        }
    }
