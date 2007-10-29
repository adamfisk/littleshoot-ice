package org.lastbamboo.common.ice.candidate;

import java.net.Socket;

import org.apache.mina.common.IoSession;
import org.lastbamboo.common.ice.IceStunCheckerFactory;
import org.lastbamboo.common.ice.transport.IceTcpConnector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A TCP ICE candidate pair. 
 */
public class IceTcpCandidatePair extends AbstractIceCandidatePair
    {

    private final Logger m_log = LoggerFactory.getLogger(getClass());
    
    /**
     * Pair of TCP ICE candidates.
     * 
     * @param localCandidate The local candidate.
     * @param remoteCandidate The remote candidate.
     * @param ioSession The connection between the two endpoints.
     */
    public IceTcpCandidatePair(final IceCandidate localCandidate, 
        final IceCandidate remoteCandidate, final IoSession ioSession, 
        final IceStunCheckerFactory stunCheckerFactory)
        {
        super(localCandidate, remoteCandidate, ioSession, stunCheckerFactory);
        if (ioSession == null)
            {
            throw new NullPointerException("Null session");
            }
        }
    
    /**
     * Pair of TCP ICE candidates.
     * 
     * @param localCandidate The local candidate.
     * @param remoteCandidate The remote candidate.
     */
    public IceTcpCandidatePair(final IceCandidate localCandidate, 
        final IceCandidate remoteCandidate,
        final IceStunCheckerFactory stunCheckerFactory,
        final IceTcpConnector iceConnector)
        {
        super(localCandidate, remoteCandidate, stunCheckerFactory, iceConnector);
        }
    
    public Socket getSocket()
        {
        return (Socket) this.m_ioSession.getAttribute("SOCKET");
        }

    public <T> T accept(final IceCandidatePairVisitor<T> visitor)
        {
        return visitor.visitTcpIceCandidatePair(this);
        }
    }
