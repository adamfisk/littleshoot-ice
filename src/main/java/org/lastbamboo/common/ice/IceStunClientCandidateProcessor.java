package org.lastbamboo.common.ice;

import java.net.InetSocketAddress;

import org.apache.mina.common.IoSession;
import org.lastbamboo.common.ice.candidate.IceCandidate;
import org.lastbamboo.common.ice.candidate.IceCandidatePair;
import org.lastbamboo.common.ice.candidate.IceCandidatePairState;
import org.lastbamboo.common.ice.candidate.IceCandidateType;
import org.lastbamboo.common.ice.candidate.IceCandidateVisitorAdapter;
import org.lastbamboo.common.ice.candidate.IceTcpActiveCandidate;
import org.lastbamboo.common.ice.candidate.IceTcpHostPassiveCandidate;
import org.lastbamboo.common.ice.candidate.IceTcpPeerReflexiveCandidate;
import org.lastbamboo.common.ice.candidate.IceUdpHostCandidate;
import org.lastbamboo.common.ice.candidate.IceUdpPeerReflexiveCandidate;
import org.lastbamboo.common.ice.candidate.TcpIceCandidatePair;
import org.lastbamboo.common.ice.candidate.UdpIceCandidatePair;
import org.lastbamboo.common.stun.stack.message.BindingErrorResponse;
import org.lastbamboo.common.stun.stack.message.BindingRequest;
import org.lastbamboo.common.stun.stack.message.BindingSuccessResponse;
import org.lastbamboo.common.stun.stack.message.CanceledStunMessage;
import org.lastbamboo.common.stun.stack.message.IcmpErrorStunMessage;
import org.lastbamboo.common.stun.stack.message.NullStunMessage;
import org.lastbamboo.common.stun.stack.message.StunMessage;
import org.lastbamboo.common.stun.stack.message.StunMessageVisitor;
import org.lastbamboo.common.stun.stack.message.StunMessageVisitorAdapter;
import org.lastbamboo.common.stun.stack.message.attributes.StunAttribute;
import org.lastbamboo.common.stun.stack.message.attributes.ice.IceControlledAttribute;
import org.lastbamboo.common.stun.stack.message.attributes.ice.IceControllingAttribute;
import org.lastbamboo.common.stun.stack.message.attributes.ice.IcePriorityAttribute;
import org.lastbamboo.common.stun.stack.message.attributes.ice.IceUseCandidateAttribute;
import org.lastbamboo.common.tcp.frame.TcpFrameIoHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class that performs connectivity checks for a single UDP or TCP pair.  This 
 * implements ICE section 7.1 from:<p>
 * 
 * http://tools.ietf.org/html/draft-ietf-mmusic-ice-17#section-7.1
 */
public class IceStunClientCandidateProcessor 
    extends IceCandidateVisitorAdapter<IoSession>
    {

    private final Logger m_log = LoggerFactory.getLogger(getClass());
    
    private final IceCandidatePair m_pair;

    private final IceAgent m_iceAgent;

    private final IceMediaStream m_mediaStream;

    /**
     * Creates a new connectiviy checker for a single UDP pair.
     * 
     * @param iceAgent The top-level ICE agent.
     * @param iceMediaStream The media stream this check is trying to establish.
     * @param udpPair The candidate pair.
     */
    public IceStunClientCandidateProcessor(final IceAgent iceAgent, 
        final IceMediaStream iceMediaStream, final IceCandidatePair udpPair)
        {
        m_iceAgent = iceAgent;
        m_mediaStream = iceMediaStream;
        m_pair = udpPair;
        }
    
    
    public IoSession visitUdpHostCandidate(
        final IceUdpHostCandidate candidate)
        {
        m_log.debug("Checking UDP host candidate...");
        return visitLocalCandidate(candidate);
        }
    
    public IoSession visitTcpActiveCandidate(
        final IceTcpActiveCandidate candidate)
        {
        m_log.debug("Visiting TCP active candidate: {}", candidate);
        return visitLocalCandidate(candidate);
        }
    
    public IoSession visitTcpHostPassiveCandidate(
        final IceTcpHostPassiveCandidate candidate)
        {
        m_log.debug("Visiting TCP host passive candidate: {}", candidate);
        return visitLocalCandidate(candidate);
        }
    
    public IoSession visitTcpPeerReflexiveCandidate(
        final IceTcpPeerReflexiveCandidate candidate)
        {
        m_log.debug("Visiting TCP peer reflexive candidate: {}", candidate);
        return visitLocalCandidate(candidate);
        }
    
    private IoSession visitLocalCandidate(final IceCandidate localCandidate)
        {
        // See ICE section 7 "Performing Connectivity Checks".
        final IceCandidate remoteCandidate = this.m_pair.getRemoteCandidate();
        
        // Now send a BindingRequest with PRIORITY, USE-CANDIDATE, 
        // ICE-CONTROLLING etc.
        final long requestPriority = 
            IcePriorityCalculator.calculatePriority(
                IceCandidateType.PEER_REFLEXIVE, IceTransportProtocol.UDP);

        final IcePriorityAttribute priorityAttribute = 
            new IcePriorityAttribute(requestPriority);
        
        final StunAttribute controlling;
        
        // The agent uses the same tie-breaker throughout the session.
        final byte[] tieBreaker = m_iceAgent.getTieBreaker();
        
        // We use a separate variable here because we need to know what
        // we sent in the case of error responses, and the data in the
        // ICE agent can change.
        final boolean isControlling = m_iceAgent.isControlling();
        if (isControlling)
            {
            controlling = new IceControllingAttribute(tieBreaker);
            }
        else
            {
            controlling = new IceControlledAttribute(tieBreaker);
            }
        
        // TODO: Add CREDENTIALS attribute.
        final BindingRequest request;
        
        // This could be for either regular or aggressive nomination.
        final boolean includedUseCandidate;
        if (this.m_pair.useCandidateSet())
            {
            request = new BindingRequest(priorityAttribute, controlling, 
                new IceUseCandidateAttribute());
            includedUseCandidate = true;
            }
        else
            {
            request = new BindingRequest(priorityAttribute, controlling);
            includedUseCandidate = false;
            }
        
        final IceStunChecker checker = this.m_pair.getStunChecker();
        
        // TODO: Obtain RTO properly.
        final long rto = 20L;
        
        m_log.debug("Writing Binding Request: {}", request);
        final StunMessage response = checker.write(request, rto);
        
        final StunMessageVisitor<IceCandidate> visitor = 
            new StunMessageVisitorAdapter<IceCandidate>()
            {
            
            @Override
            public IceCandidate visitBindingSuccessResponse(
                final BindingSuccessResponse sbr)
                {
                // Now check the mapped address and see if it matches
                // any of the local candidates we know about.  If it 
                // does not, it's a new peer reflexive candidate. 
                final InetSocketAddress mappedAddress = 
                    sbr.getMappedAddress();
                final IceCandidate matchingCandidate = 
                    m_mediaStream.getLocalCandidate(mappedAddress, 
                        localCandidate.isUdp());
                
                if (matchingCandidate == null)
                    {
                    // This basically indicates the NAT bound to a new port for
                    // the outgoing request to the new host, meaning the NAT
                    // is using at least address-dependent mapping and possibly
                    // address and port dependent mapping.
                    
                    // Note the base candidate here is the local candidate
                    // from the pair, i.e. the candidate we're visiting.
                    
                    // We use the PRIORITY from the Binding Request, as
                    // specified in section 7.1.2.2.1. and 7.1.2.2.2.
                    final IceCandidate prc;
                    if (localCandidate.isUdp())
                        {
                        prc = new IceUdpPeerReflexiveCandidate(mappedAddress, 
                            localCandidate, m_iceAgent.isControlling(), 
                            requestPriority);
                        }
                    else
                        {
                        prc = new IceTcpPeerReflexiveCandidate(mappedAddress, 
                            localCandidate, m_iceAgent.isControlling(), 
                            requestPriority);
                        }
                    m_mediaStream.addLocalCandidate(prc);
                    return prc;
                    }
                else
                    {
                    // This will have the priority signalled in the original
                    // SDP, as specified in 7.1.2.2.2.
                    return matchingCandidate;
                    }
                }

            @Override
            public IceCandidate visitBindingErrorResponse(
                final BindingErrorResponse bindingErrorResponse)
                {
                // This is likely a role-conflict error.  We need to 
                // handle it as specified in section 7.1.2.1.
                final int errorCode = bindingErrorResponse.getErrorCode();
                
                if (errorCode != 487)
                    {
                    // It's an error response we don't know how to handle.
                    m_log.warn("Unexpected error response: " + 
                        bindingErrorResponse.getAttributes());
                    m_pair.setState(IceCandidatePairState.FAILED);
                    return null;
                    }
                if (!isControlling)
                    {
                    m_iceAgent.setControlling(true);
                    }
                else
                    {
                    m_iceAgent.setControlling(false);
                    }
                
                // As stated in ICE 17 section 7.1.2.1. Failure Cases:
                // "the agent MUST enqueue the candidate pair whose check
                // generated the 487 into the triggered check queue.  The 
                // state of that pair is set to Waiting."
                
                // This has the effect of sending a new Binding Request to
                // the remote host reflecting the new role.
                
                // Note that we queue up a triggered check always here,
                // assuming that the role change was actually correct.
                m_pair.setState(IceCandidatePairState.WAITING);
                m_mediaStream.addTriggeredCheck(m_pair);
                
                return null;
                }

            @Override
            public IceCandidate visitNullMessage(final NullStunMessage message)
                {
                // See section 7.1.2.1. Failure Cases
                // This means we never received any response to our request,
                // interpretted as a failure.
                m_pair.setState(IceCandidatePairState.FAILED);
                return null;
                }

            @Override
            public IceCandidate visitCanceledMessage(
                final CanceledStunMessage message)
                {
                m_log.debug("Transaction was cancelled...");
                // The outgoing message was canceled.  This can happen when
                // we received a pair that generated a triggered check on 
                // a STUN server.  In this case, we don't treat it as a failure
                // but instead simply stop processing the pair.  It will be
                // added to the triggered check queue and will be re-checked.
                return null;
                }

            public IceCandidate visitIcmpErrorMesssage(
                final IcmpErrorStunMessage message)
                {
                // See section 7.1.2.1. Failure Cases.
                m_log.debug("Got ICMP error -- setting pair state to failed.");
                m_pair.setState(IceCandidatePairState.FAILED);
                return null;
                }
            };
            
        final IceCandidate newLocalCandidate = response.accept(visitor);
        if (newLocalCandidate == null)
            {
            m_log.debug("Check failed or was cancelled -- should happen " +
                "quite often");
            return null;
            }
        else
            {
            return processSuccess(newLocalCandidate, remoteCandidate, 
                includedUseCandidate, requestPriority);
            }
        }
    
    /**
     * Processes a successful response to a check.
     * 
     * @param newLocalCandidate The calculated local candidate.  This can be 
     * peer reflexive.
     * @param remoteCandidate The remote candidate from the original pair.
     * @param useCandidate Whether the Binding Request included the 
     * USE-CANDIDATE attribute.
     * @param bindingRequestPriority The priority of the Binding Request.
     * @return The generated {@link IoSession}.
     */
    private IoSession processSuccess(final IceCandidate newLocalCandidate, 
        final IceCandidate remoteCandidate, final boolean useCandidate, 
        final long bindingRequestPriority)
        {
        m_log.debug("Processing success...");
        final InetSocketAddress remoteAddress = 
            remoteCandidate.getSocketAddress();
        
        final InetSocketAddress newLocalAddress = 
            newLocalCandidate.getSocketAddress();
        
        final IceCandidatePair pairToAdd;
        if (equalsOriginalPair(this.m_pair, newLocalAddress, remoteAddress))
            {
            // Just add the original pair;
            m_log.debug("Using original pair...");
            pairToAdd = this.m_pair;
            }
        else
            {
            m_log.debug("Original pair not equal");
            final IceCandidatePair existingPair = 
                m_mediaStream.getPair(newLocalAddress, remoteAddress);
            if (existingPair != null)
                {
                pairToAdd = existingPair;
                }
            else
                {
                // The pair is a completely new pair.  
                // We've already calculated the priority of the local candidate,
                // but we still need the priority of the remote candidate.
                
                // Here's the description of calculating the remote priority:
                //
                // The priority of the remote candidate is taken from the 
                // SDP of the peer.  If the candidate does not appear there, 
                // then the check must have been a triggered check to a new 
                // remote candidate.  In that case, the priority is taken as the
                // value of the PRIORITY attribute in the Binding Request which
                // triggered the check that just completed.
                if (this.m_mediaStream.hasRemoteCandidate(remoteAddress, 
                    remoteCandidate.isUdp()))
                    {
                    // It's not a triggered check, so use the original 
                    // candidate's priority, or, i.e., use the original 
                    // candidate.
                    }
                else
                    {
                    // It's a triggered check, so we use the priority
                    // from the Binding Request we just sent, as specified in
                    // section 7.1.2.2.2.
                    
                    // TODO: Review this a little bit.  Is it OK to just use
                    // the remote candidate we started with and change the
                    // priority here?
                    remoteCandidate.setPriority(bindingRequestPriority);
                    }
                
                m_log.debug("Creating new pair...");
                
                // We use the connectivity checker of the original pair here.
                // If the local candidate is peer reflexive, it still has the
                // same base as the original local candidate, so the 
                // connectivity checker will be identical.
                if (newLocalCandidate.isUdp())
                    {
                    pairToAdd = new UdpIceCandidatePair(newLocalCandidate, 
                        remoteCandidate, this.m_pair.getStunChecker());
                    }
                else
                    {
                    final TcpFrameIoHandler handler = new TcpFrameIoHandler();
                    pairToAdd = new TcpIceCandidatePair(newLocalCandidate, 
                        remoteCandidate, this.m_pair.getStunChecker(),
                        handler);
                    }
                }
            }
        m_mediaStream.addValidPair(pairToAdd);
        
        // 7.1.2.2.3.  Updating Pair States
        
        // 1) Tell the media stream to update pair states as a result of 
        // a valid pair.  
        // This also handles nominated flag changes specified in 
        // "7.1.2.2.4. Updating the Nominated Flag" and check list and timer
        // state issues from "7.1.2.3. Check List and Timer State Updates"
        this.m_mediaStream.updatePairStates(pairToAdd, this.m_pair, 
            useCandidate);
    
        // 2) Tell the ICE agent to unfreeze check lists for other media 
        // streams.
        this.m_iceAgent.onUnfreezeCheckLists(m_mediaStream);
        
        this.m_iceAgent.onValidPairsForAllComponents(m_mediaStream);
        
        // Tell the ICE agent to consider this valid pair if it was not just
        // nominated.  Nominated pairs have already been considered as valid
        // pairs -- that's how they had their nominated flag set.
        if (!updateNominatedFlag(pairToAdd, useCandidate))
            {
            this.m_iceAgent.onValidPairs(m_mediaStream);
            }
        
        // 7.1.2.3. Check List and Timer State Updates
        m_mediaStream.updateCheckListAndTimerStates();
        return null;
        }
    
    /**
     * This implements:<p> 
     * 
     * 7.1.2.2.4.  Updating the Nominated Flag<p>
     * 
     * and part of:<p>
     * 
     * 7.2.1.5.  Updating the Nominated Flag<p>
     * 
     * @param validPair The valid pair to update.
     * @param sentUseCandidateInRequest Whether or not the USE-CANDIDATE 
     * attribute appeared in the original Binding Request.
     * @retuen <code>true</code> if the valid pair was nominated, otherwise
     * <code>false</code>.
     */
    private boolean updateNominatedFlag(final IceCandidatePair validPair, 
        final boolean sentUseCandidateInRequest)
        {
        if (this.m_iceAgent.isControlling() && sentUseCandidateInRequest)
            {
            validPair.nominate();
            this.m_iceAgent.onNominatedPair(validPair, this.m_mediaStream);
            return true;
            }
        
        // Second part of 7.2.15 -- the pair may have been previously 
        // In-Progress and should possibly have its nominated flag set upon
        // this successful response.
        else if (!this.m_iceAgent.isControlling())
            {
            // We synchronize here to avoid a race condition with the setting
            // of the flag for nominating pairs for controlling agents upon
            // successful checks.
            synchronized (validPair)
                {
                if (validPair.shouldNominateOnSuccess())
                    {
                    m_log.debug("Nominating new pair on controlled agent!!");
                    // We just put the pair in the successful state, so we know
                    // that's the state it's in (it has to be in the successful
                    // state for use to nominate it).
                    validPair.nominate();
                    this.m_iceAgent.onNominatedPair(validPair, 
                        this.m_mediaStream);
                    return true;
                    }
                }
            }
        return false;
        }

    /**
     * Checks if the new pair equals the original pair that generated
     * the check.
     * 
     * @param pair The original pair that generated the check.
     * @param newLocalAddress The new local candidate.
     * @param newRemoteAddress The new remote candidate.
     * @return <code>true</code> if the pairs match, otherwise 
     * <code>false</code>.
     */
    private boolean equalsOriginalPair(final IceCandidatePair pair, 
        final InetSocketAddress newLocalAddress, 
        final InetSocketAddress newRemoteAddress)
        {
        final InetSocketAddress oldLocalAddress =
            pair.getLocalCandidate().getSocketAddress();
        final InetSocketAddress oldRemoteAddress = 
            pair.getRemoteCandidate().getSocketAddress();
        return 
            newLocalAddress.equals(oldLocalAddress) &&
            newRemoteAddress.equals(oldRemoteAddress);
        }
    }
