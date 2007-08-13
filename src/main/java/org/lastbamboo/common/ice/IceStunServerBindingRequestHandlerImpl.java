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
import org.lastbamboo.common.stun.stack.message.attributes.StunAttributeType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * STUN message visitor for ICE.  ICE STUN only needs to handle Binding 
 * Requests and Binding Responses as opposed to all STUN messages.
 */
public class IceStunServerBindingRequestHandlerImpl 
    implements IceStunServerBindingRequestHandler
    {

    private final Logger m_log = LoggerFactory.getLogger(getClass());
    
    //private final IoSession m_session;

    private final IceAgent m_agent;

    private final IceMediaStream m_iceMediaStream;

    /**
     * Creates a new message visitor for the specified session.
     * 
     * @param session The session with the remote host.
     * @param agent The top-level ICE agent.
     * @param iceMediaStream The media stream this STUN processor is working 
     * for. 
     */
    public IceStunServerBindingRequestHandlerImpl(
        final IceAgent agent, 
        final IceMediaStream iceMediaStream)
        {
        m_agent = agent;
        m_iceMediaStream = iceMediaStream;
        }

    public void handleBindingRequest(final IoSession ioSession,
        final BindingRequest binding)
        {
        m_log.debug("Visiting Binding Request...");
        if (ioSession == null)
            {
            throw new NullPointerException("Null session!?!?");
            }
        
        // We need to check ICE controlling and controlled roles for conflicts.
        // This implements:
        // 7.2.1.1.  Detecting and Repairing Role Conflicts
        final IceRoleChecker checker = new IceRoleCheckerImpl();
        final BindingErrorResponse errorResponse = 
            checker.checkAndRepairRoles(binding, this.m_agent);
        
        if (errorResponse != null)
            {
            // This can happen in the rare case that there's a role conflict.
            this.m_log.debug("Sending error response...");
            ioSession.write(errorResponse);
            }
        else
            {
            // We now implement the remaining sections 7.2.1 following 7.2.1.1 
            // since we're returning a success response.
            final InetSocketAddress localAddress = 
                (InetSocketAddress) ioSession.getLocalAddress();
            final InetSocketAddress remoteAddress = 
                (InetSocketAddress) ioSession.getRemoteAddress();
            
            // TODO: This should include other attributes!!
            final UUID transactionId = binding.getTransactionId();
            final StunMessage response = 
                new BindingSuccessResponse(transactionId.getRawBytes(), 
                    remoteAddress);
            
            // We write the response as soon as possible.
            m_log.debug("Writing success response...");
            ioSession.write(response);
            
            // Check to see if the remote address matches the address of
            // any remote candidates we know about.  If it does not, it's a
            // new peer reflexive address.  See ICE section 7.2.1.3
            final IceCandidate localCandidate;
            final IceCandidate remoteCandidate;
            if (!this.m_iceMediaStream.hasRemoteCandidate(remoteAddress))
                {
                remoteCandidate = this.m_iceMediaStream.addPeerReflexive(
                    binding, localAddress, remoteAddress);
                m_log.debug("Added peer reflexive remote candidate.");
                }
            else
                {
                remoteCandidate = 
                    this.m_iceMediaStream.getRemoteCandidate(remoteAddress);
                }
            
            localCandidate = 
                this.m_iceMediaStream.getLocalCandidate(localAddress);
            
            m_log.debug("Using existing local candidate: {}", 
                    localCandidate);
            
            if (localCandidate == null)
                {
                m_log.warn("Could not create local candidate.");
                return;
                }

            if (remoteCandidate == null)
                {
                // There should always be a remote candidate at this point 
                // because the peer reflexive check above should have added it
                // if it wasn't already there.
                m_log.warn("Could not find remote candidate.");
                return;
                }
            
            // Now we need to handle triggered checks.
            final IceCandidatePair existingPair = 
                this.m_iceMediaStream.getPair(localAddress, remoteAddress);
            final IceCandidatePair computedPair;
            if (existingPair != null)
                {
                m_log.debug("Found existing pair");
                computedPair = existingPair;
                
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
                m_log.debug("Creating new UDP pair.");
                // TODO: Doesn't this need an existing connectivity checker?
                // Or maybe it's always a new remote candidate here?
                final IceStunChecker connectivityChecker = 
                    createConnectivityChecker(localCandidate, remoteCandidate);
                computedPair = 
                    new UdpIceCandidatePair(localCandidate, remoteCandidate,
                        connectivityChecker);
                // Continue with the rest of ICE section 7.2.1.4, 
                // "Triggered Checks"
                
                // TODO: The remote candidate needs a username fragment and
                // password.  We don't implement this yet.  This is the 
                // description of what we need to do:
                
                // "The username fragment for the remote candidate is equal to 
                // the part after the colon of the USERNAME in the Binding 
                // Request that was just received.  Using that username 
                // fragment, the agent can check the SDP messages received 
                // from its peer (there may be more than one in cases of 
                // forking), and find this username fragment.  The
                // corresponding password is then selected."
                computedPair.setState(IceCandidatePairState.WAITING);
                
                // TODO: Is this definitely correct to have the pairs on both
                // queues?  Doesn't this mean we'll often check this pair
                // twice?
                this.m_iceMediaStream.addPair(computedPair);
                this.m_iceMediaStream.addTriggeredCheck(computedPair);
                }
            
            // 7.2.1.5. Updating the Nominated Flag
            
            // If the ICE USE CANDIDATE attribute is set, and we're in the 
            // controlling role, we need to deal with nominating the pair.
            if (binding.getAttributes().containsKey(
                StunAttributeType.ICE_USE_CANDIDATE) &&
                !this.m_agent.isControlling())
                {
                final IceCandidatePairState state = computedPair.getState();
                
                switch (state)
                    {
                    case SUCCEEDED:
                        computedPair.nominate();
                        break;
                    case IN_PROGRESS:
                        computedPair.nominateOnSuccess();
                        break;
                    case WAITING:
                        // No action.
                    case FROZEN:
                        // No action.
                    case FAILED:
                        // No action.
                    }
                
                }
            
            }
        return;
        }
    
    private IceStunChecker createConnectivityChecker(
        final IceCandidate localCandidate, final IceCandidate remoteCandidate)
        {
        m_log.debug("Creating ICE connectivity checker from "+
            localCandidate+" to "+remoteCandidate);
        return new IceUdpStunChecker(
            localCandidate, remoteCandidate, this, m_agent);
        }
    }
