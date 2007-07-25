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
import org.lastbamboo.common.stun.stack.message.BindingRequest;
import org.lastbamboo.common.stun.stack.message.StunMessage;
import org.lastbamboo.common.stun.stack.message.StunMessageVisitor;
import org.lastbamboo.common.stun.stack.message.StunMessageVisitorAdapter;
import org.lastbamboo.common.stun.stack.message.SuccessfulBindingResponse;
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

    /**
     * Creates a new checker.
     * 
     * @param mediaStream The high level media stream. 
     * @param pair The pair to check
     */
    public IceConnectivityCheckerImpl(final IceMediaStream mediaStream, 
        final IceCandidatePair pair)
        {
        m_log.debug("Created connectivity checker...");
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
            
            final InetSocketAddress localAddress = client.getHostAddress();
            
            // Now send a BindingRequest with PRIORITY, USE-CANDIDATE, 
            // ICE-CONTROLLING etc.
            
            final Collection<StunAttribute> attributes = 
                new LinkedList<StunAttribute>();
            
            final long priority = 
                IcePriorityCalculator.calculatePriority(
                    IceCandidateType.PEER_REFLEXIVE);

            final IcePriorityAttribute priorityAttribute = 
                new IcePriorityAttribute(priority);
            
            final StunAttribute controlling;
            if (m_mediaStream.isControlling())
                {
                controlling = new IceControllingAttribute();
                // We use aggressive nomination.
                attributes.add(new IceUseCandidateAttribute());
                }
            else
                {
                controlling = new IceControlledAttribute();
                }
            
            attributes.add(priorityAttribute);
            attributes.add(controlling);
            
            // TODO: Add CREDENTIALS attribute.
            final BindingRequest request = new BindingRequest(attributes);
            
            final StunMessage response = client.write(request, remoteAddress);
            
            final StunMessageVisitor<IceCandidate> visitor = 
                new StunMessageVisitorAdapter<IceCandidate>()
                {
                
                public IceCandidate visitSuccessfulBindingResponse(
                    final SuccessfulBindingResponse sbr)
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
                        final IceCandidate prc = 
                            new IceUdpPeerReflexiveCandidate(mappedAddress, 
                            candidate, client, m_mediaStream.isControlling(), 
                            priority);
                        m_mediaStream.addLocalCandidate(prc);
                        return prc;
                        }
                    else
                        {
                        return matchingCandidate;
                        }
                    }
                };
                
            final IceCandidate newCandidate = response.accept(visitor);
            return null;
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
