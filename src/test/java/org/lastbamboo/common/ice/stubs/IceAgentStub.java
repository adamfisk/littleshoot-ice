package org.lastbamboo.common.ice.stubs;

import java.io.IOException;
import java.math.BigInteger;
import java.net.Socket;
import java.util.Collection;
import java.util.Random;

import org.apache.mina.common.ByteBuffer;
import org.lastbamboo.common.ice.IceAgent;
import org.lastbamboo.common.ice.IceMediaStream;
import org.lastbamboo.common.ice.candidate.IceCandidate;

/**
 * Stun class for an ICE agent.
 */
public class IceAgentStub implements IceAgent
    {
    
    private final byte[] m_tieBreaker = 
        new BigInteger(64, new Random()).toByteArray();

    public byte[] getTieBreaker()
        {
        return m_tieBreaker;
        }

    public boolean isControlling()
        {
        // TODO Auto-generated method stub
        return false;
        }

    public long calculateDelay(int Ta_i)
        {
        // TODO Auto-generated method stub
        return 0;
        }

    public void onUnfreezeCheckLists(IceMediaStream mediaStream)
        {
        // TODO Auto-generated method stub
        
        }

    public void onValidPairsForAllComponents(IceMediaStream mediaStream)
        {
        // TODO Auto-generated method stub
        
        }

    public void setControlling(boolean controlling)
        {
        // TODO Auto-generated method stub
        
        }

    public Socket connect(ByteBuffer answer) throws IOException
        {
        // TODO Auto-generated method stub
        return null;
        }

    public Collection<IceCandidate> gatherCandidates()
        {
        // TODO Auto-generated method stub
        return null;
        }

    public Socket createSocket(ByteBuffer answer) throws IOException
        {
        // TODO Auto-generated method stub
        return null;
        }

    public byte[] generateAnswer()
        {
        // TODO Auto-generated method stub
        return null;
        }

    public byte[] generateOffer()
        {
        // TODO Auto-generated method stub
        return null;
        }

    public void recomputePairPriorities()
        {
        // TODO Auto-generated method stub
        
        }

    }
