package org.lastbamboo.common.ice.candidate;

import java.net.InetAddress;
import java.util.Collection;

import org.lastbamboo.common.turn.client.TurnClient;

public class TcpTurnIceCandidateGatherer implements IceCandidateGatherer
    {

    public TcpTurnIceCandidateGatherer(final TurnClient turnClient) 
        {
        }
    
    public void close() 
        {
        }

    public Collection<IceCandidate> gatherCandidates() 
        {
        return null;
        }

    public InetAddress getPublicAddress() 
        {
        return null;
        }

    }
