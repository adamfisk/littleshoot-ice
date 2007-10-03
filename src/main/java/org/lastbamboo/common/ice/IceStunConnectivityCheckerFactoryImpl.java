package org.lastbamboo.common.ice;

import org.apache.mina.common.IoSession;
import org.lastbamboo.common.stun.stack.message.StunMessageVisitor;
import org.lastbamboo.common.stun.stack.message.StunMessageVisitorFactory;
import org.lastbamboo.common.stun.stack.transaction.StunTransactionTracker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class IceStunConnectivityCheckerFactoryImpl<T> implements
    StunMessageVisitorFactory<T, IceMediaStream>
    {
    
    private final Logger m_log = LoggerFactory.getLogger(getClass());
    private final IceAgent m_iceAgent;
    private final StunTransactionTracker<T> m_transactionTracker;
    private final IceStunCheckerFactory m_checkerFactory;
    private final IceBindingRequestTracker m_bindingRequestTracker =
        new IceBindingRequestTrackerImpl();

    public IceStunConnectivityCheckerFactoryImpl(final IceAgent iceAgent, 
        final StunTransactionTracker<T> transactionTracker, 
        final IceStunCheckerFactory checkerFactory)
        {
        m_iceAgent = iceAgent;
        m_transactionTracker = transactionTracker;
        m_checkerFactory = checkerFactory;
        }

    public StunMessageVisitor<T> createVisitor(final IoSession session)
        {
        // This can get called during testing.
        return createVisitor(session, null);
        }

    public StunMessageVisitor<T> createVisitor(final IoSession session, 
        final IceMediaStream attachment)
        {
        m_log.debug("Creating new message visitor for session: {}", session);
        return new IceStunConnectivityCheckerImpl<T>(this.m_iceAgent, 
            attachment, session, this.m_transactionTracker, 
            this.m_checkerFactory, m_bindingRequestTracker);
        }

    }
