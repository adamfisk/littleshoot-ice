package org.lastbamboo.common.ice;

import java.net.InetSocketAddress;

import org.apache.commons.id.uuid.UUID;
import org.apache.mina.common.IoSession;
import org.lastbamboo.common.stun.stack.message.BindingRequest;
import org.lastbamboo.common.stun.stack.message.StunMessage;
import org.lastbamboo.common.stun.stack.message.StunMessageVisitorAdapter;
import org.lastbamboo.common.stun.stack.message.SuccessfulBindingResponse;
import org.lastbamboo.common.stun.stack.transaction.StunClientTransaction;
import org.lastbamboo.common.stun.stack.transaction.StunTransactionTracker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * STUN message visitor for ICE.  ICE STUN only needs to handle Binding 
 * Requests and Binding Responses as opposed to all STUN messages.
 */
public class IceStunMessageVisitor extends StunMessageVisitorAdapter<Void>
    {

    private final Logger m_log = LoggerFactory.getLogger(getClass());
    
    private final IoSession m_session;

    private final StunTransactionTracker m_transactionTracker;

    /**
     * Creates a new message visitor for the specified session.
     * @param tracker 
     * 
     * @param session The session with the remote host.
     */
    public IceStunMessageVisitor(final StunTransactionTracker tracker, 
        final IoSession session)
        {
        m_transactionTracker = tracker;
        m_session = session;
        }

    public Void visitBindingRequest(final BindingRequest binding)
        {
        // Just echo back the response.
        m_log.debug("Visiting Binding Request...");
        // TODO: This should include other attributes!!
        final InetSocketAddress address = 
            (InetSocketAddress) m_session.getRemoteAddress();
        
        final UUID transactionId = binding.getTransactionId();
        final StunMessage response = 
            new SuccessfulBindingResponse(transactionId.getRawBytes(), address);
        
        this.m_session.write(response);
        return null;
        }
    
    public Void visitSuccessfulBindingResponse(
        final SuccessfulBindingResponse response)
        {
        if (m_log.isDebugEnabled())
            {
            m_log.debug("Received binding response: "+response);
            }
        
        return notifyTransaction(response);
        }
    
    private Void notifyTransaction(final SuccessfulBindingResponse response)
        {
        final StunClientTransaction ct = 
            this.m_transactionTracker.getClientTransaction(response);
        m_log.debug("Accessed transaction: "+ct);
        
        if (ct == null)
            {
            // This will happen fairly frequently with UDP because messages
            // are retransmitted in case any are lost.
            m_log.debug("No matching transaction for response: "+response);
            return null;
            }

        // Verify the addresses as specified in ICE section 7.1.2.2.
        if (isFromExpectedHost(ct))
            {
            response.accept(ct);
            }
        else
            {
            m_log.debug("Received response from unexpected source...");
            }

        return null;
        }

    private boolean isFromExpectedHost(final StunClientTransaction ct)
        {
        final InetSocketAddress responseSource = 
            (InetSocketAddress) this.m_session.getRemoteAddress();
        final InetSocketAddress intendedDestination =
            ct.getIntendedDestination();
        
        if (!responseSource.equals(intendedDestination))
            {
            return false;
            }

        final InetSocketAddress responseDestination = 
            (InetSocketAddress) this.m_session.getRemoteAddress();
        final InetSocketAddress intendedSource =
            ct.getIntendedDestination();
        
        if (!responseDestination.equals(intendedSource))
            {
            return false;
            }
        
        return true;
        }
    }
