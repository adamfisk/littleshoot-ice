package org.lastbamboo.common.ice;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;

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
import org.lastbamboo.common.stun.client.UdpStunClient;
import org.lastbamboo.common.stun.stack.message.SuccessfulBindingResponse;
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
    
    private final static class ConnectPairVisitor 
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
            final IceCandidate local = pair.getLocalCandidate();
            final UdpConnectCandidateVisitor visitor = 
                new UdpConnectCandidateVisitor(pair);
            final IoSession session = local.accept(visitor);
            pair.setIoSession(session);
            return session;
            }
    
        }
    
    private final static class UdpConnectCandidateVisitor 
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
            final StunClient client = candidate.getStunClient();
            
            final InetSocketAddress localAddress = client.getHostAddress();
            
            final StunClient newClient = 
                new UdpStunClient(localAddress, remoteAddress);
            
            /*
            final SuccessfulBindingResponse response = 
                newClient.getBindingResponse();
            
            if (response == null)
                {
                this.m_pair.setState(IceCandidatePairState.FAILED);
                return null;
                }
            else
                {
                // Construct a new valid pair based on the response data.  The
                // new pair's local address is the mapped address, and the
                // new pair's remote address is the address the STUN 
                // requests were sent to.
                // See draft-ietf-mmusic-ice-17.txt.
                final InetSocketAddress mappedAddress = 
                    response.getMappedAddress();
                
                final IceCandidate localCandidate =
                    new IceUdpHostCandidate(newClient, candidate.isControlling());
                //final IceCandidatePair newPair = new UdpIceCandidatePair()
                
                //this.m_pair.setState(IceCandidatePairState.SUCCEEDED);
                return newClient.getIoSession();
                }
                */
            return null;
            }
    
        public IoSession visitUdpPeerReflexiveCandidate(IceUdpPeerReflexiveCandidate candidate)
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
