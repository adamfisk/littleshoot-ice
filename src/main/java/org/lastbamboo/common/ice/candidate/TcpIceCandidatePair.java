package org.lastbamboo.common.ice.candidate;

import java.net.Socket;

import org.apache.mina.common.IoSession;
import org.lastbamboo.common.ice.IceStunCheckerFactory;
import org.lastbamboo.common.ice.util.IceConnector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A TCP ICE candidate pair. 
 */
public class TcpIceCandidatePair extends AbstractIceCandidatePair
    {

    private final Logger m_log = LoggerFactory.getLogger(getClass());
    
    private final int m_pairId;
    
    private static int s_pairId = 0;
    
    /**
     * Pair of TCP ICE candidates.
     * 
     * @param localCandidate The local candidate.
     * @param remoteCandidate The remote candidate.
     * @param ioSession The connection between the two endpoints.
     */
    public TcpIceCandidatePair(final IceCandidate localCandidate, 
        final IceCandidate remoteCandidate, final IoSession ioSession,
        final IceStunCheckerFactory stunCheckerFactory)
        {
        super(localCandidate, remoteCandidate, ioSession, stunCheckerFactory);
        this.m_pairId = s_pairId;
        s_pairId++;
        }
    
    /**
     * Pair of TCP ICE candidates.
     * 
     * @param localCandidate The local candidate.
     * @param remoteCandidate The remote candidate.
     */
    public TcpIceCandidatePair(final IceCandidate localCandidate, 
        final IceCandidate remoteCandidate,
        final IceStunCheckerFactory stunCheckerFactory,
        final IceConnector iceConnector)
        {
        super(localCandidate, remoteCandidate, stunCheckerFactory, iceConnector);
        this.m_pairId = s_pairId;
        s_pairId++;
        }
    
    public Socket getSocket()
        {
        //return this.m_tcpFrameIoHandler.getSocket();
        //return ((TcpFrameIoHandler)((IceTcpStunChecker)m_currentStunChecker).getProtocolIoHandler()).getSocket();
        return null;
        }

    public <T> T accept(final IceCandidatePairVisitor<T> visitor)
        {
        return visitor.visitTcpIceCandidatePair(this);
        }

    /*
    public TcpFrameIoHandler getIoHandler()
        {
        return m_tcpFrameIoHandler;
        }
        */
    
    @Override
    public String toString()
        {
        return getClass().getSimpleName() + " " + this.m_pairId;
        }
    }
