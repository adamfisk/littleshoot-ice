package org.lastbamboo.common.ice.candidate;

import java.net.Socket;

import org.lastbamboo.common.ice.IceStunChecker;

/**
 * A TCP ICE candidate pair. 
 */
public class TcpIceCandidatePair extends AbstractIceCandidatePair
    {

    private Socket m_socket;
    
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
        }
    
    public Socket getSocket()
        {
        return m_socket;
        }
    
    public void setSocket(final Socket sock)
        {
        m_socket = sock;
        }

    public <T> T accept(final IceCandidatePairVisitor<T> visitor)
        {
        return visitor.visitTcpIceCandidatePair(this);
        }
    }
