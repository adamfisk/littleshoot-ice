package org.lastbamboo.common.ice.candidate;

import java.net.Socket;

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
     */
    public TcpIceCandidatePair(final IceCandidate localCandidate, 
        final IceCandidate remoteCandidate)
        {
        super(localCandidate, remoteCandidate);
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
