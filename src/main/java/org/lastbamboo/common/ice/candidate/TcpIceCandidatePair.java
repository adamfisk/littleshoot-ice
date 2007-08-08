package org.lastbamboo.common.ice.candidate;

import java.net.Socket;

import org.lastbamboo.common.ice.IceStunChecker;
import org.lastbamboo.common.stun.stack.message.BindingRequest;
import org.lastbamboo.common.stun.stack.message.StunMessage;

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
        super(localCandidate, remoteCandidate, createConnectivityChecker());
        }
    
    private static IceStunChecker createConnectivityChecker()
        {
        return new IceStunChecker()
            {
            public StunMessage write(final BindingRequest request, 
                final long rto)
                {
                // We don't perform STUN checks over TCP for now.
                return null;
                }

            public void cancelTransaction()
                {
                // TODO Auto-generated method stub
                
                }
            };
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
