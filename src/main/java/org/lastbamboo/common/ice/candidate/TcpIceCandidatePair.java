package org.lastbamboo.common.ice.candidate;

import java.net.Socket;

import org.lastbamboo.common.ice.IceStunChecker;
import org.lastbamboo.common.ice.IceTcpStunChecker;
import org.lastbamboo.common.tcp.frame.TcpFrameIoHandler;
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
     * @param stunChecker The connectivity checker to use.
     */
    public TcpIceCandidatePair(final IceCandidate localCandidate, 
        final IceCandidate remoteCandidate, final IceStunChecker stunChecker)
        {
        super(localCandidate, remoteCandidate, stunChecker);
        this.m_pairId = s_pairId;
        s_pairId++;
        }
    
    public Socket getSocket()
        {
        //return this.m_tcpFrameIoHandler.getSocket();
        return ((TcpFrameIoHandler)((IceTcpStunChecker)m_stunChecker).getProtocolIoHandler()).getSocket();
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
        return getClass().getSimpleName() + " " + this.m_pairId +
            " with handler: " + ((IceTcpStunChecker)m_stunChecker).getProtocolIoHandler();
        }
    }
