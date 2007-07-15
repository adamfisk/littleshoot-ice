package org.lastbamboo.common.ice.candidate;

import org.apache.mina.common.IoSession;


/**
 * A UDP ICE candidate pair. 
 */
public class UdpIceCandidatePair extends AbstractIceCandidatePair
    {

    private IoSession m_session;

    /**
     * Pair of UDP ICE candidates.
     * 
     * @param localCandidate The local candidate.
     * @param remoteCandidate The remote candidate.
     */
    public UdpIceCandidatePair(final IceCandidate localCandidate, 
        final IceCandidate remoteCandidate)
        {
        super(localCandidate, remoteCandidate);
        }

    public UdpIceCandidatePair(final IceCandidate localCandidate, 
        final IceCandidate remoteCandidate, final long priority)
        {
        super(localCandidate, remoteCandidate, priority);
        }
    
    public void setIoSession(final IoSession session)
        {
        m_session = session;
        }

    public IoSession getIoSession()
        {
        return m_session;
        }

    public <T> T accept(final IceCandidatePairVisitor<T> visitor)
        {
        return visitor.visitUdpIceCandidatePair(this);
        }

    }
