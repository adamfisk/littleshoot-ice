package org.lastbamboo.common.ice;

import java.net.InetSocketAddress;

import org.apache.commons.id.uuid.UUID;
import org.apache.mina.common.IoSession;
import org.lastbamboo.common.stun.stack.message.BindingErrorResponse;
import org.lastbamboo.common.stun.stack.message.BindingRequest;
import org.lastbamboo.common.stun.stack.message.StunMessage;
import org.lastbamboo.common.stun.stack.message.StunMessageVisitorAdapter;
import org.lastbamboo.common.stun.stack.message.BindingSuccessResponse;
import org.lastbamboo.common.stun.stack.message.turn.AllocateErrorResponse;
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

    private final IceAgent m_agent;

    /**
     * Creates a new message visitor for the specified session.
     * 
     * @param tracker The class that keeps track of outstanding STUN 
     * transactions.
     * @param session The session with the remote host.
     * @param agent The top-level ICE agent.
     */
    public IceStunMessageVisitor(final StunTransactionTracker tracker, 
        final IoSession session, final IceAgent agent)
        {
        m_transactionTracker = tracker;
        m_session = session;
        m_agent = agent;
        }

    public Void visitBindingRequest(final BindingRequest binding)
        {
        // Just echo back the response.
        m_log.debug("Visiting Binding Request...");
        
        // We need to check ICE controlling and controlled roles for conflicts.
        // This implements:
        // 7.2.1.1.  Detecting and Repairing Role Conflicts
        final IceRoleChecker checker = new IceRoleCheckerImpl();
        final BindingErrorResponse errorResponse = 
            checker.checkAndRepairRoles(binding, this.m_agent);
        
        if (errorResponse != null)
            {
            // This can happen in the rare case that there's a role conflict.
            this.m_session.write(errorResponse);
            }
        else
            {
            // We now implement the sections of 7.2 following 7.2.1.1 since
            // we're returning a success response.
            
            // TODO: This should include other attributes!!
            final InetSocketAddress remoteAddress = 
                (InetSocketAddress) m_session.getRemoteAddress();
            
            // Check to see if the remote address matches the address of
            // any remote candidates we know about.  If it does not, it's a
            // new peer reflexive address.
            
            
            final UUID transactionId = binding.getTransactionId();
            final StunMessage response = 
                new BindingSuccessResponse(transactionId.getRawBytes(), remoteAddress);
            
            this.m_session.write(response);
            }
        return null;
        }
    
    public Void visitBindingErrorResponse(final BindingErrorResponse response)
        {
        // This likey indicates a role-conflict.  
        if (m_log.isDebugEnabled())
            {
            m_log.warn("Received binding error response: "+
                response.getAttributes());
            }
        
        return notifyTransaction(response);
        }
    
    public Void visitBindingSuccessResponse(
        final BindingSuccessResponse response)
        {
        if (m_log.isDebugEnabled())
            {
            m_log.debug("Received binding response: "+response);
            }
        
        return notifyTransaction(response);
        }
    
    private Void notifyTransaction(final StunMessage response)
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

    public Void visitAllocateErrorResponse(final AllocateErrorResponse response)
        {
        // TODO We need to handle this once we fully integrate STUN and TURN
        // implementations.
        return null;
        }
    }
