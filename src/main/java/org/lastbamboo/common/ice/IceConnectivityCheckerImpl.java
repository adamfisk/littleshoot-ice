package org.lastbamboo.common.ice;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;

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

    /**
     * Creates a new checker.
     * 
     * @param pair The pair to check
     */
    public IceConnectivityCheckerImpl(final IceCandidatePair pair)
        {
        m_log.debug("Created connectivity checker...");
        this.m_pair = pair;
        }

    public boolean check()
        {
        final IceCandidatePairVisitor<Socket> visitor = 
            new ConnectPairVisitor();
        final Socket sock = m_pair.accept(visitor);
        return sock != null;
        }
    
    private final static class ConnectPairVisitor 
        implements IceCandidatePairVisitor<Socket>
        {

        public Socket visitTcpIceCandidatePair(final TcpIceCandidatePair pair)
            {
            final IceCandidate local = pair.getLocalCandidate();
            final TcpConnectCandidateVisitor visitor = 
                new TcpConnectCandidateVisitor(pair);
            final Socket sock = local.accept(visitor);
            pair.setSocket(sock);
            return sock;
            }

        public Socket visitUdpIceCandidatePair(final UdpIceCandidatePair pair)
            {
            final IceCandidate local = pair.getLocalCandidate();
            final UdpConnectCandidateVisitor visitor = 
                new UdpConnectCandidateVisitor(pair);
            local.accept(visitor);
            return null;
            }
    
        }
    
    private final static class UdpConnectCandidateVisitor 
        extends IceCandidateVisitorAdapter<Socket>
        {
        
        private final Logger m_log = LoggerFactory.getLogger(getClass());
        
        private final IceCandidatePair m_pair;
    
        private UdpConnectCandidateVisitor(final IceCandidatePair pair)
            {
            m_pair = pair;
            }
        
        public Socket visitUdpHostCandidate(final IceUdpHostCandidate candidate)
            {
            final IceCandidate remoteCandidate = 
                this.m_pair.getRemoteCandidate();
            final InetSocketAddress remote = remoteCandidate.getSocketAddress();
            final IceUdpStunClient client = new IceUdpStunClient(remote);
            
            // Sends the STUN messages to the remote host.
            final InetSocketAddress address = client.getServerReflexiveAddress();
            
            if (address != null)
                {
                m_log.debug("Got response from remote host!!", address);
                }
            // TODO Auto-generated method stub
            return null;
            }
    
        public Socket visitUdpPeerReflexiveCandidate(IceUdpPeerReflexiveCandidate candidate)
            {
            // TODO Auto-generated method stub
            return null;
            }
    
        public Socket visitUdpRelayCandidate(IceUdpRelayCandidate candidate)
            {
            // TODO Auto-generated method stub
            return null;
            }
    
        public Socket visitUdpServerReflexiveCandidate(IceUdpServerReflexiveCandidate candidate)
            {
            // TODO Auto-generated method stub
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
        
        public Socket visitTcpActiveCandidate(final IceTcpActiveCandidate candidate)
            {
            final IceCandidate remotePair = this.m_pair.getRemoteCandidate();
            final InetSocketAddress remote = remotePair.getSocketAddress();
            
            // TODO: We should really make sure this socket binds to the address
            // in the local candidate.
            final Socket client = new Socket();
            final InetAddress address = remote.getAddress();
            
            final int soTimeout;
            final int icmpTimeout;
            if (NetworkUtils.isPrivateAddress(remote.getAddress()))
                {
                // We should be able to connect to local, private addresses really,
                // really quickly.  So don't wait around too long.
                soTimeout = 4000;
                icmpTimeout = 600;
                }
            else
                {
                soTimeout = 10000;
                icmpTimeout = 3000;
                }
            try
                {
                // We should be able to get an ICMP response very quickly.
                //if (address.isReachable(icmpTimeout))
                    {
                    m_log.debug("Connecting to: {}", address);
                    client.connect(remote, soTimeout);
                    this.m_pair.setState(IceCandidatePairState.SUCCEEDED);
                    m_log.debug("Successfully connected!!");
                    return client;
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
