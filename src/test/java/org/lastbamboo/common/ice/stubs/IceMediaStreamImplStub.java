package org.lastbamboo.common.ice.stubs;

import java.net.InetSocketAddress;
import java.util.Collection;
import java.util.Queue;

import org.lastbamboo.common.ice.IceCheckListState;
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

    public IceCandidatePair getPair(InetSocketAddress localAddress,
            InetSocketAddress remoteAddress)
        {
        // TODO Auto-generated method stub
        return null;
        }

    public Queue<IceCandidatePair> getValidPairs()
        {
        // TODO Auto-generated method stub
        return null;
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

    public IceCheckListState getCheckListState()
        {
        // TODO Auto-generated method stub
        return null;
        }

    public boolean hasHigherPriorityPendingPair(IceCandidatePair pair)
        {
        // TODO Auto-generated method stub
        return false;
        }

    public void onNominated(IceCandidatePair pair)
        {
        // TODO Auto-generated method stub
        
        }

    public void setCheckListState(IceCheckListState state)
        {
        // TODO Auto-generated method stub
        
        }

    public void updateCheckListAndTimerStates()
        {
        // TODO Auto-generated method stub
        
        }

    public Queue<IceCandidatePair> getNominatedPairs()
        {
        // TODO Auto-generated method stub
        return null;
        }

    public int getStunServerPort()
        {
        // TODO Auto-generated method stub
        return 0;
        }

    public IceCandidate addRemotePeerReflexive(BindingRequest request, InetSocketAddress localAddress, InetSocketAddress remoteAddress, boolean isUdp)
        {
        // TODO Auto-generated method stub
        return null;
        }

    public IceCandidate getLocalCandidate(InetSocketAddress localAddress, boolean isUdp)
        {
        // TODO Auto-generated method stub
        return null;
        }

    public IceCandidate getRemoteCandidate(InetSocketAddress remoteAddress, boolean isUdp)
        {
        // TODO Auto-generated method stub
        return null;
        }

    public boolean hasRemoteCandidate(InetSocketAddress remoteAddress, boolean isUdp)
        {
        // TODO Auto-generated method stub
        return false;
        }

    }
