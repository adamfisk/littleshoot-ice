package org.lastbamboo.common.ice;

import java.net.InetSocketAddress;

import org.apache.commons.id.uuid.UUID;
import org.apache.mina.common.IoSession;
import org.lastbamboo.common.ice.candidate.IceCandidate;
import org.lastbamboo.common.ice.candidate.IceCandidatePair;
import org.lastbamboo.common.ice.candidate.IceCandidatePairState;
import org.lastbamboo.common.ice.candidate.UdpIceCandidatePair;
import org.lastbamboo.common.stun.stack.message.BindingErrorResponse;
import org.lastbamboo.common.stun.stack.message.BindingRequest;
import org.lastbamboo.common.stun.stack.message.BindingSuccessResponse;
import org.lastbamboo.common.stun.stack.message.StunMessage;
import org.lastbamboo.common.stun.stack.message.StunMessageVisitorAdapter;
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

    private final IceMediaStream m_iceMediaStream;

    /**
     * Creates a new message visitor for the specified session.
     * 
     * @param tracker The class that keeps track of outstanding STUN 
     * transactions.
     * @param session The session with the remote host.
     * @param agent The top-level ICE agent.
     * @param iceMediaStream The media stream this STUN processor is working 
     * for. 
     */
    public IceStunMessageVisitor(final StunTransactionTracker tracker, 
        final IoSession session, final IceAgent agent, 
        final IceMediaStream iceMediaStream)
        {
        m_transactionTracker = tracker;
        m_session = session;
        m_agent = agent;
        m_iceMediaStream = iceMediaStream;
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
            // We now implement the remaining sections 7.2.1 following 7.2.1.1 
            // since we're returning a success response.
            final InetSocketAddress localAddress = 
                (InetSocketAddress) m_session.getLocalAddress();
            final InetSocketAddress remoteAddress = 
                (InetSocketAddress) m_session.getRemoteAddress();
            
            // TODO: This should include other attributes!!
            final UUID transactionId = binding.getTransactionId();
            final StunMessage response = 
                new BindingSuccessResponse(transactionId.getRawBytes(), 
                    remoteAddress);
            
            // We write the response as soon as possible.
            this.m_session.write(response);
            
            // Check to see if the remote address matches the address of
            // any remote candidates we know about.  If it does not, it's a
            // new peer reflexive address.  See ICE section 7.2.1.3
            final IceCandidate localCandidate;
            if (!this.m_iceMediaStream.hasRemoteCandidate(remoteAddress))
                {
                localCandidate = this.m_iceMediaStream.addPeerReflexive(
                    binding, localAddress, remoteAddress);
                }
            else
                {
                localCandidate = 
                    this.m_iceMediaStream.getLocalCandidate(localAddress);
                }
            
            if (localCandidate == null)
                {
                m_log.warn("Could not create local candidate.");
                return null;
                }
            
            final IceCandidate remoteCandidate = 
                this.m_iceMediaStream.getRemoteCandidate(remoteAddress);

            if (remoteCandidate == null)
                {
                // There should always be a remote candidate at this point 
                // because the peer reflexive check above should have added it
                // if it wasn't already there.
                m_log.warn("Could not find remote candidate.");
                return null;
                }
            
            // Now we need to handle triggered checks.
            final IceCandidatePair existingPair = 
                this.m_iceMediaStream.getPair(localAddress, remoteAddress);
            if (existingPair != null)
                {
                // This is the case where the new pair is already on the 
                // check list.  See ICE section 7.2.1.4. Triggered Checks
                final IceCandidatePairState state = existingPair.getState();
                switch (state)
                    {
                    case WAITING:
                        // Fall through.
                    case FROZEN:
                        this.m_iceMediaStream.addTriggeredCheck(existingPair);
                        break;
                    case IN_PROGRESS:
                        // We need to cancel the in-progress transaction.  
                        // This just means we won't re-submit requests and will
                        // not treat the lack of response as a failure.
                        existingPair.cancelStunTransaction();
                    
                        // Add the pair to the existing check queue.
                        existingPair.setState(IceCandidatePairState.WAITING);
                        this.m_iceMediaStream.addTriggeredCheck(existingPair);
                        break;
                    case FAILED:
                        existingPair.setState(IceCandidatePairState.WAITING);
                        this.m_iceMediaStream.addTriggeredCheck(existingPair);
                        break;
                    case SUCCEEDED:
                        // Nothing more to do.
                        break;
                    }
                }
            else
                {
                
                // Continue with the rest of ICE section 7.2.1.4, 
                // "Triggered Checks"
                
                // TODO: The remote candidate needs a username fragement and
                // password.  We don't implement this yet.  This is the 
                // description of what we need to do:
                
                // "The username fragment for the remote candidate is equal to 
                // the part after the colon of the USERNAME in the Binding 
                // Request that was just received.  Using that username 
                // fragment, the agent can check the SDP messages received 
                // from its peer (there may be more than one in cases of 
                // forking), and find this username fragment.  The
                // corresponding password is then selected."
                final IceCandidatePair pair = 
                    new UdpIceCandidatePair(localCandidate, remoteCandidate);
                pair.setState(IceCandidatePairState.WAITING);
                this.m_iceMediaStream.addPair(pair);
                this.m_iceMediaStream.addTriggeredCheck(pair);
                }
            
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
