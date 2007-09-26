package org.lastbamboo.common.ice;

import org.apache.mina.common.IoHandler;
import org.apache.mina.common.IoSession;
import org.apache.mina.handler.StreamIoHandler;
import org.lastbamboo.common.stun.stack.message.StunMessageVisitor;
import org.lastbamboo.common.stun.stack.message.StunMessageVisitorFactory;
import org.lastbamboo.common.stun.stack.transaction.StunTransactionTracker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Factory for creating STUN message visitors for ICE clients.  This visitors
 * are somewhat unique in that each client must handle both "client" and 
 * "server" side messages in ICE.
 * 
 * @param <T> The type STUN message visitor methods return.
 */
public class IceTcpStunConnectivityCheckerFactory<T> 
    implements StunMessageVisitorFactory<T, IceMediaStream>
    {
    
    private final Logger m_log = LoggerFactory.getLogger(getClass());
    private final IceAgent m_iceAgent;
    private final StunTransactionTracker<T> m_transactionTracker;
    private final IceStunCheckerFactory m_checkerFactory;
    private final StreamIoHandler m_streamIoHandler;


    /**
     * Creates a new STUN message visitor factory for ICE.
     * 
     * @param agent The top-level agent. 
     * @param transactionTracker The class that keeps track of STUN 
     * transactions. 
     * @param checkerFactory The class that creates new classes for handling
     * the lower level transport for checks. 
     * @param streamIoHandler The {@link IoHandler} that creates a TCP stream.
     */
    public IceTcpStunConnectivityCheckerFactory(
        final IceAgent agent, 
        final StunTransactionTracker<T> transactionTracker,
        final IceStunCheckerFactory checkerFactory,
        final StreamIoHandler streamIoHandler)
        {
        m_iceAgent = agent;
        m_transactionTracker = transactionTracker;
        m_checkerFactory = checkerFactory;
        m_streamIoHandler = streamIoHandler;
        }

    
    public StunMessageVisitor<T> createVisitor(final IoSession session)
        {
        throw new UnsupportedOperationException("Need to pass a media stream");
        }
    
    public StunMessageVisitor<T> createVisitor(final IoSession session, 
        final IceMediaStream attachment)
        {
        return new IceTcpStunConnectivityChecker<T>(this.m_iceAgent, 
            attachment, session, this.m_transactionTracker, 
            this.m_checkerFactory, this, this.m_streamIoHandler);
        }
    }
