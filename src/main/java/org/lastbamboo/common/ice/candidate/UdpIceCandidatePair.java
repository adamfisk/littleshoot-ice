package org.lastbamboo.common.ice.candidate;

import org.apache.mina.common.IoSession;
import org.lastbamboo.common.ice.IceStunConnectivityChecker;
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
     * Pair of UDP ICE candidates.
     * 
     * @param localCandidate The local candidate.
     * @param remoteCandidate The remote candidate.
     */
    public UdpIceCandidatePair(final IceCandidate localCandidate, 
        final IceCandidate remoteCandidate)
        {
        super(localCandidate, remoteCandidate, 
            createConnectivityChecker(localCandidate, remoteCandidate));
        }

    /**
     * Pair of UDP ICE candidates.  This constructor is passed in the priority
     * explicitly, as required for some pairs discovered during the connectivity
     * check process.
     * 
     * @param localCandidate The local candidate.
     * @param remoteCandidate The remote candidate.
     * @param priority The explicit priority.
     */
    public UdpIceCandidatePair(final IceCandidate localCandidate, 
        final IceCandidate remoteCandidate, final long priority)
        {
        super(localCandidate, remoteCandidate, priority, 
            createConnectivityChecker(localCandidate, remoteCandidate));
        }
    
    private static IceStunConnectivityChecker createConnectivityChecker(
        final IceCandidate localCandidate, final IceCandidate remoteCandidate)
        {
        LOG.debug("Creating ICE connectivity checker from "+
            localCandidate+" to "+remoteCandidate);
        return new IceUdpStunConnectivityChecker(
            localCandidate.getSocketAddress(), 
            remoteCandidate.getSocketAddress());
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
