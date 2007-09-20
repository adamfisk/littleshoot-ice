package org.lastbamboo.common.ice;

import org.apache.mina.common.IoSession;
import org.lastbamboo.common.stun.stack.message.StunMessage;
import org.lastbamboo.common.stun.stack.message.StunMessageVisitor;
import org.lastbamboo.common.stun.stack.message.StunMessageVisitorFactory;
import org.lastbamboo.common.stun.stack.transaction.StunTransactionTracker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Factory for creating STUN message visitors for ICE clients.  This visitors
 * are somewhat unique in that each client must handle both "client" and 
 * "server" side messages in ICE.
 */
public class IceStunConnectivityCheckerFactory 
    implements StunMessageVisitorFactory<StunMessage>
    {
    
    private final Logger m_log = LoggerFactory.getLogger(getClass());
    private final IceAgent m_iceAgent;
    private final IceMediaStream m_iceMediaStream;
    private final StunTransactionTracker<StunMessage> m_transactionTracker;
    private final IceStunCheckerFactory m_checkerFactory;

    /**
     * Creates a new STUN message visitor factory for ICE.
     * 
     * @param agent The top-level agent. 
     * @param iceMediaStream The media stream this factory is working for.
     */
    public IceStunConnectivityCheckerFactory(
        final IceAgent agent, final IceMediaStream iceMediaStream,
        final StunTransactionTracker<StunMessage> transactionTracker,
        final IceStunCheckerFactory checkerFactory)
        {
        m_iceAgent = agent;
        m_iceMediaStream = iceMediaStream;
        m_transactionTracker = transactionTracker;
        m_checkerFactory = checkerFactory;
        }

    public StunMessageVisitor<StunMessage> createVisitor(
        final IoSession session)
        {
        return new IceStunConnectivityChecker( 
            this.m_iceAgent, this.m_iceMediaStream, 
                session, this.m_transactionTracker, this.m_checkerFactory,
                this);
        }

    }
