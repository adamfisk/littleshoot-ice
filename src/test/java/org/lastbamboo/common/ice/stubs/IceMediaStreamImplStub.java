package org.lastbamboo.common.ice.stubs;

import java.net.InetSocketAddress;
import java.util.Collection;

import org.lastbamboo.common.ice.IceMediaStream;
import org.lastbamboo.common.ice.candidate.IceCandidate;
import org.lastbamboo.common.ice.candidate.IceCandidatePair;
import org.lastbamboo.common.stun.stack.message.BindingRequest;

public class IceMediaStreamImplStub implements IceMediaStream
    {

    public void addLocalCandidate(IceCandidate localCandidate)
        {
        // TODO Auto-generated method stub

        }

    public IceCandidate addPeerReflexive(BindingRequest request,
            InetSocketAddress localAddress, InetSocketAddress remoteAddress)
        {
        // TODO Auto-generated method stub
        return null;
        }

    public void addTriggeredCheck(IceCandidatePair pair)
        {
        // TODO Auto-generated method stub

        }

    public void addValidPair(IceCandidatePair pair)
        {
        // TODO Auto-generated method stub

        }

    public byte[] encodeCandidates()
        {
        // TODO Auto-generated method stub
        return null;
        }

    public void establishStream(Collection<IceCandidate> remoteCandidates)
        {
        // TODO Auto-generated method stub

        }

    public IceCandidate getLocalCandidate(InetSocketAddress localAddress)
        {
        // TODO Auto-generated method stub
        return null;
        }

    public IceCandidatePair getPair(InetSocketAddress localAddress,
            InetSocketAddress remoteAddress)
        {
        // TODO Auto-generated method stub
        return null;
        }

    public IceCandidate getRemoteCandidate(InetSocketAddress remoteAddress)
        {
        // TODO Auto-generated method stub
        return null;
        }

    public Collection<IceCandidatePair> getValidPairs()
        {
        // TODO Auto-generated method stub
        return null;
        }

    public boolean hasRemoteCandidate(InetSocketAddress remoteAddress)
        {
        // TODO Auto-generated method stub
        return false;
        }

    public void updatePairStates(IceCandidatePair validPair,
            IceCandidatePair generatingPair, boolean useCandidate)
        {
        // TODO Auto-generated method stub

        }

    public void recomputePairPriorities(boolean controlling)
        {
        // TODO Auto-generated method stub

        }

    public void addPair(IceCandidatePair pair)
        {
        // TODO Auto-generated method stub
        
        }

    }
