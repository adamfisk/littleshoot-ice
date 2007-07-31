package org.lastbamboo.common.ice;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.Collection;
import java.util.LinkedList;

import org.apache.mina.common.IoSession;
import org.lastbamboo.common.ice.candidate.IceCandidate;
import org.lastbamboo.common.ice.candidate.IceCandidatePair;
import org.lastbamboo.common.ice.candidate.IceCandidatePairState;
import org.lastbamboo.common.ice.candidate.IceCandidatePairVisitor;
import org.lastbamboo.common.ice.candidate.IceCandidateVisitorAdapter;
import org.lastbamboo.common.ice.candidate.IceTcpActiveCandidate;
import org.lastbamboo.common.ice.candidate.IceUdpHostCandidate;
import org.lastbamboo.common.ice.candidate.IceUdpPeerReflexiveCandidate;
import org.lastbamboo.common.ice.candidate.IceUdpRelayCandidate;
import org.lastbamboo.common.ice.candidate.IceUdpServerReflexiveCandidate;
import org.lastbamboo.common.ice.candidate.TcpIceCandidatePair;
import org.lastbamboo.common.ice.candidate.UdpIceCandidatePair;
import org.lastbamboo.common.stun.client.StunClient;
import org.lastbamboo.common.stun.stack.message.BindingErrorResponse;
import org.lastbamboo.common.stun.stack.message.BindingRequest;
import org.lastbamboo.common.stun.stack.message.StunMessage;
import org.lastbamboo.common.stun.stack.message.StunMessageVisitor;
import org.lastbamboo.common.stun.stack.message.StunMessageVisitorAdapter;
import org.lastbamboo.common.stun.stack.message.BindingSuccessResponse;
import org.lastbamboo.common.stun.stack.message.attributes.StunAttribute;
import org.lastbamboo.common.stun.stack.message.attributes.ice.IceControlledAttribute;
import org.lastbamboo.common.stun.stack.message.attributes.ice.IceControllingAttribute;
import org.lastbamboo.common.stun.stack.message.attributes.ice.IcePriorityAttribute;
import org.lastbamboo.common.stun.stack.message.attributes.ice.IceUseCandidateAttribute;
import org.lastbamboo.common.util.NetworkUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class that performs ICE connectivity checks for a single pair of ICE 
 * candidates. 
 */
public class IceConnectivityCheckerImpl implements IceConnectivityChecker
    {

    private final Logger m_log = LoggerFactory.getLogger(getClass());

    private final IceCandidatePair m_pair;

    private final IceMediaStream m_mediaStream;

    private final IceAgent m_iceAgent;

    /**
     * Creates a new checker.
     * 
     * @param iceAgent The ICE agent controller for this session.
     * @param mediaStream The high level media stream. 
     * @param pair The pair to check
     */
    public IceConnectivityCheckerImpl(final IceAgent iceAgent,
        final IceMediaStream mediaStream, 
        final IceCandidatePair pair)
        {
        this.m_iceAgent = iceAgent;
        this.m_pair = pair;
        this.m_mediaStream = mediaStream;
        }

    public boolean check()
        {
        m_log.debug("Checking pair...");
        final IceCandidatePairVisitor<Object> visitor = 
            new ConnectPairVisitor();
        final Object obj = m_pair.accept(visitor);
        if (obj != null)
            {
            this.m_mediaStream.addValidPair(this.m_pair);
            return true;
            }
        return false;
        }
    
    private final class ConnectPairVisitor 
        implements IceCandidatePairVisitor<Object>
        {

        public Object visitTcpIceCandidatePair(final TcpIceCandidatePair pair)
            {
            final IceCandidate local = pair.getLocalCandidate();
            final TcpConnectCandidateVisitor visitor = 
                new TcpConnectCandidateVisitor(pair);
            final Socket sock = local.accept(visitor);
            pair.setSocket(sock);
            return sock;
            //return null;
            }

        public Object visitUdpIceCandidatePair(final UdpIceCandidatePair pair)
            {
            /*
            final IceCandidate local = pair.getLocalCandidate();
            final UdpConnectCandidateVisitor visitor = 
                new UdpConnectCandidateVisitor(pair);
            final IoSession session = local.accept(visitor);
            pair.setIoSession(session);
            return session;
            */
            return null;
            }
    
        }
    
    private final class UdpConnectCandidateVisitor 
        extends IceCandidateVisitorAdapter<IoSession>
        {
        
        private final Logger m_log = LoggerFactory.getLogger(getClass());
        
        private final IceCandidatePair m_pair;
    
        private UdpConnectCandidateVisitor(final IceCandidatePair pair)
            {
            m_pair = pair;
            }
        
        public IoSession visitUdpHostCandidate(
            final IceUdpHostCandidate candidate)
            {
            m_log.debug("Checking UDP host candidate...");
            final IceCandidate remoteCandidate = 
                this.m_pair.getRemoteCandidate();
            final InetSocketAddress remoteAddress = 
                remoteCandidate.getSocketAddress();
            
            // We can't close the STUN "client" here because we're also a 
            // server and have to always be ready to receive incoming
            // server-side messages.
            final StunClient client = candidate.getStunClient();
            
            // Now send a BindingRequest with PRIORITY, USE-CANDIDATE, 
            // ICE-CONTROLLING etc.
            
            final Collection<StunAttribute> attributes = 
                new LinkedList<StunAttribute>();
            
            final long priority = 
                IcePriorityCalculator.calculatePriority(
                    IceCandidateType.PEER_REFLEXIVE, IceTransportProtocol.UDP);

            final IcePriorityAttribute priorityAttribute = 
                new IcePriorityAttribute(priority);
            
            final boolean hasUseCandidate;
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
                // We use aggressive nomination.
                attributes.add(new IceUseCandidateAttribute());
                hasUseCandidate = true;
                }
            else
                {
                controlling = new IceControlledAttribute(tieBreaker);
                hasUseCandidate = false;
                }
            
            attributes.add(priorityAttribute);
            attributes.add(controlling);
            
            // TODO: Add CREDENTIALS attribute.
            final BindingRequest request = new BindingRequest(attributes);
            
            final StunMessage response = client.write(request, remoteAddress);
            
            final StunMessageVisitor<IceCandidate> visitor = 
                new StunMessageVisitorAdapter<IceCandidate>()
                {
                
                public IceCandidate visitBindingSuccessResponse(
                    final BindingSuccessResponse sbr)
                    {
                    // TODO: We're supposed to verify the source IP and port as  
                    // well as the destination IP address and port with the 
                    // actual values the Binding Request was sent to and from,
                    // respectively.
                    
                    // **** We can do this by just adding the check to the 
                    // STUN transaction handling code, I think.  With normal
                    // STUN client/server interections, the above should
                    // always be the case, so adding the check should have no
                    // effect.
                    
                    // Now check the mapped address and see if it matches
                    // any of the local candidates we know about.  If it 
                    // does not, it's a new peer reflexive candidate.  If it 
                    // does, it's an existing candidate that will be added to
                    // the valid list.
                    final InetSocketAddress mappedAddress = 
                        sbr.getMappedAddress();
                    final IceCandidate matchingCandidate = 
                        m_mediaStream.getLocalCandidate(mappedAddress);
                    
                    if (matchingCandidate == null)
                        {
                        // Note the base candidate here is the local candidate
                        // from the pair, i.e. the candidate we're visiting.
                        
                        // We use the PRIORITY from the Binding Request, as
                        // specified in section 7.1.2.2.2.
                        final IceCandidate prc = 
                            new IceUdpPeerReflexiveCandidate(mappedAddress, 
                                candidate, client, m_iceAgent.isControlling(), 
                                priority);
                        m_mediaStream.addLocalCandidate(prc);
                        return prc;
                        }
                    else
                        {
                        return matchingCandidate;
                        }
                    }

                public IceCandidate visitBindingErrorResponse(
                    final BindingErrorResponse bindingErrorResponse)
                    {
                    // This is likely a role-conflict error.  We need to 
                    // handle it as specified in section 7.1.2.1.
                    final int errorCode = bindingErrorResponse.getErrorCode();
                    
                    if (errorCode != 487)
                        {
                        m_log.warn("Unexpected error response: " + 
                            bindingErrorResponse.getAttributes());
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
                    
                    // As stated in ICE 17:
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
                };
                
            final IceCandidate newLocalCandidate = response.accept(visitor);

            if (newLocalCandidate == null)
                {
                this.m_pair.setState(IceCandidatePairState.FAILED);
                return null;
                }
            else
                {
                return processSuccess(newLocalCandidate, remoteCandidate, 
                    hasUseCandidate);
                }
            
            }
    
        public IoSession visitUdpPeerReflexiveCandidate(
            final IceUdpPeerReflexiveCandidate candidate)
            {
            // TODO Auto-generated method stub
            return null;
            }
    
        public IoSession visitUdpRelayCandidate(IceUdpRelayCandidate candidate)
            {
            // TODO Auto-generated method stub
            return null;
            }
    
        public IoSession visitUdpServerReflexiveCandidate(IceUdpServerReflexiveCandidate candidate)
            {
            return null;
            }
        }
    
    private boolean isTriggeredCheck(final InetSocketAddress remoteAddress)
        {
        // TODO We don't currently deal with the triggered check case.
        // Implement this whenever we add triggered check handling!!
        return false;
        }
    
    private IoSession processSuccess(IceCandidate newLocalCandidate, 
        final IceCandidate remoteCandidate, final boolean useCandidate)
        {
        final InetSocketAddress remoteAddress = 
            remoteCandidate.getSocketAddress();
        
        final InetSocketAddress newLocalAddress = 
            newLocalCandidate.getSocketAddress();
        
        final IceCandidatePair pairToAdd;
        if (equalsOriginalPair(this.m_pair, newLocalAddress, remoteAddress))
            {
            // Just add the original pair;
            pairToAdd = this.m_pair;
            }
        else
            {
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
                //final long remotePriority;
                final IceCandidate newRemoteCandidate;
                if (isTriggeredCheck(remoteAddress))
                    {
                    // It's a triggered check, so we use the priority
                    // from the Binding Request we just sent.
                    
                    // TODO: We don't currently support triggered checks.
                    // We need to construct a new remote candidate in 
                    // this case!!
                    // remotePriority = priority;
                    
                    newRemoteCandidate = null;
                    throw new NullPointerException(
                        "We don't yet support triggered checks!!!");
                    }
                else
                    {
                    // It's not a triggered check, so use the original 
                    // candidate's priority.
                    //remotePriority = remoteCandidate.getPriority();
                    newRemoteCandidate = remoteCandidate;
                    }
                
                pairToAdd = 
                    new UdpIceCandidatePair(newLocalCandidate, newRemoteCandidate);
                }
            }
        
        // We now set the state of the pair that *generated* the check
        // to succeeded, as specified in ICE section:
        // 7.1.2.2.3.  Updating Pair States
        this.m_pair.setState(IceCandidatePairState.SUCCEEDED);
        
        m_mediaStream.onValidPair(pairToAdd, this.m_pair, useCandidate);
        
        return null;
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
    
    private final static class TcpConnectCandidateVisitor 
        extends IceCandidateVisitorAdapter<Socket>
        {
        
        private final Logger m_log = LoggerFactory.getLogger(getClass());
        
        private final IceCandidatePair m_pair;

        private TcpConnectCandidateVisitor(final IceCandidatePair pair)
            {
            m_pair = pair;
            }
        
        public Socket visitTcpActiveCandidate(
            final IceTcpActiveCandidate candidate)
            {
            m_log.debug("Checking active TCP candidate...");
            final IceCandidate remotePair = this.m_pair.getRemoteCandidate();
            final InetSocketAddress remote = remotePair.getSocketAddress();
            m_log.debug("Connecting to {}", remote);
            
            // TODO: We should really make sure this socket binds to the address
            // in the local candidate.
            final Socket client = new Socket();
            final InetAddress address = remote.getAddress();
            
            final int connectTimeout;
            final int icmpTimeout;
            if (NetworkUtils.isPrivateAddress(remote.getAddress()))
                {
                // We should be able to connect to local, private addresses 
                // really, really quickly.  So don't wait around too long.
                connectTimeout = 3000;
                icmpTimeout = 600;
                }
            else
                {
                connectTimeout = 12000;
                icmpTimeout = 3000;
                }
            
            /*
            final StunClient stunClient = 
                new TcpStunClient(remote, connectTimeout);
            
            if (stunClient.isConnected())
                {
                this.m_pair.setState(IceCandidatePairState.SUCCEEDED);
                m_log.debug("Successfully connected!!");
                return stunClient.getIoSession();
                }
            else
                {
                m_log.debug("Could not connect to candidate at: {}", remote);
                }
            this.m_pair.setState(IceCandidatePairState.FAILED);
            return null;
            */

            try
                {
                // We should be able to get an ICMP response very quickly.
                m_log.debug("Checking if address is reachable: {}", address);
                if (address.isReachable(icmpTimeout))
                    {
                    m_log.debug("Address is reachable. Connecting:{}", address);
                    client.connect(remote, connectTimeout);
                    this.m_pair.setState(IceCandidatePairState.SUCCEEDED);
                    m_log.debug("Successfully connected!!");
                    return client;
                    }
                else
                    {
                    m_log.debug("The address was not reachable within " +  
                        icmpTimeout + " milliseconds...");
                    }
                }
            catch (final IOException e)
                {
                m_log.debug("Could not access candidate at: {}", remote);
                }
            this.m_pair.setState(IceCandidatePairState.FAILED);
            return null;
            }
        }

    }
