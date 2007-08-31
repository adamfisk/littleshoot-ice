package org.lastbamboo.common.ice;

import org.apache.mina.common.IoSession;
import org.lastbamboo.common.stun.client.StunClientMessageVisitor;
import org.lastbamboo.common.stun.stack.message.BindingRequest;
import org.lastbamboo.common.stun.stack.message.StunMessage;
import org.lastbamboo.common.stun.stack.message.StunMessageVisitor;
import org.lastbamboo.common.stun.stack.message.StunMessageVisitorFactory;
import org.lastbamboo.common.stun.stack.transaction.StunTransactionTracker;

/**
 * Class for creating STUN message visitors for STUN connectivity checks.  
 * This handles both client and server side messages.  For client side messages,
 * it keeps appropriate track of outstanding transactions, notifying the
 * appropriate listeners as transactions succeed or fail.
 */
public class IceStunCheckerMessageVisitorFactory
    implements StunMessageVisitorFactory<StunMessage>
    {

    private final StunMessageVisitorFactory m_messageVisitorFactory;
    private final StunTransactionTracker<StunMessage> m_transactionTracker;

    /**
     * Creates a new STUN message visitor factory for STUN messages received
     * during the connectivity checking process.  These messages can be 
     * either server side or client side messages.
     * 
     * @param messageVisitorFactory The factory for creating message visitors.
     * @param transactionTracker The class for keeping track of transactions
     * for STUN messages sent with this checker.  The transaction tracker 
     * handles notifying the appropriate listeners when transactions 
     * succeed or fail.
     */
    public IceStunCheckerMessageVisitorFactory(
        final StunMessageVisitorFactory messageVisitorFactory,
        final StunTransactionTracker<StunMessage> transactionTracker)
        {
        m_messageVisitorFactory = messageVisitorFactory;
        m_transactionTracker = transactionTracker;
        }
    
    public StunMessageVisitor<StunMessage> createVisitor(
        final IoSession session)
        {
        final StunMessageVisitor serverChecker = 
            m_messageVisitorFactory.createVisitor(session);
        return new IceConnectivityStunMessageVisitor(m_transactionTracker,
            serverChecker);
        }
    
    /**
     * Private class that visits client side messages and passes any server
     * side messages it receives to a delegate class responsible for 
     * server side messages.
     */
    private static class IceConnectivityStunMessageVisitor 
        extends StunClientMessageVisitor<StunMessage>
        {
        
        private final StunMessageVisitor m_serverVisitor;
    
        private IceConnectivityStunMessageVisitor(
            final StunTransactionTracker<StunMessage> transactionTracker, 
            final StunMessageVisitor serverVisitor)
            {
            super(transactionTracker);
            m_serverVisitor = serverVisitor;
            }
    
        public StunMessage visitBindingRequest(final BindingRequest binding)
            {
            this.m_serverVisitor.visitBindingRequest(binding);
            return null;
            }
        }
    }
