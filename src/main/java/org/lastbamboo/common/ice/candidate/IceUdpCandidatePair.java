package org.lastbamboo.common.ice.candidate;

import org.littleshoot.mina.common.IoSession;
import org.lastbamboo.common.ice.IceStunCheckerFactory;
import org.lastbamboo.common.ice.transport.IceConnector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A UDP ICE candidate pair. 
 */
public class IceUdpCandidatePair extends AbstractIceCandidatePair
    {

    private final Logger m_log = 
        LoggerFactory.getLogger(IceUdpCandidatePair.class);
    
    /**
     * Pair of UDP ICE candidates.  This constructor uses an existing 
     * connectivity checker.
     * 
     * @param localCandidate The local candidate.
     * @param remoteCandidate The remote candidate.
     * @param stunCheckerFactory The factory for creating connectivity checkers.
     * @param connector The class for creating connections between pairs.
     */
    public IceUdpCandidatePair(final IceCandidate localCandidate, 
        final IceCandidate remoteCandidate, 
        final IceStunCheckerFactory stunCheckerFactory, 
        final IceConnector connector)
        {
        super(localCandidate, remoteCandidate, stunCheckerFactory, connector);
        }
    
    /**
     * Pair of UDP ICE candidates.  This constructor uses an existing 
     * connectivity checker.
     * 
     * @param localCandidate The local candidate.
     * @param remoteCandidate The remote candidate.
     * @param ioSession The {@link IoSession} connecting to the two endpoints.
     * @param stunCheckerFactory The factory for creating connectivity checkers.
     */
    public IceUdpCandidatePair(final IceCandidate localCandidate, 
        final IceCandidate remoteCandidate, final IoSession ioSession,
        final IceStunCheckerFactory stunCheckerFactory)
        {
        super(localCandidate, remoteCandidate, ioSession, stunCheckerFactory);
        }

    public <T> T accept(final IceCandidatePairVisitor<T> visitor)
        {
        return visitor.visitUdpIceCandidatePair(this);
        }

    public boolean isTcp()
        {
        return false;
        }

    }
