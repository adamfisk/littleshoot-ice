package org.lastbamboo.common.ice.candidate;

import org.apache.mina.common.IoSession;
import org.lastbamboo.common.ice.IceStunChecker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A UDP ICE candidate pair. 
 */
public class UdpIceCandidatePair extends AbstractIceCandidatePair
    {

    private final static Logger LOG = 
        LoggerFactory.getLogger(UdpIceCandidatePair.class);
    
    private IoSession m_session;
    
    /**
     * Pair of UDP ICE candidates.  This constructor uses an existing 
     * connectivity checker.
     * 
     * @param localCandidate The local candidate.
     * @param remoteCandidate The remote candidate.
     * @param connectivityChecker The connectivity checker to use.
     */
    public UdpIceCandidatePair(final IceCandidate localCandidate, 
        final IceCandidate remoteCandidate, 
        final IceStunChecker connectivityChecker)
        {
        super(localCandidate, remoteCandidate, connectivityChecker);
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
