package org.lastbamboo.common.ice;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.Collection;

import org.lastbamboo.common.ice.candidate.IceCandidate;
import org.lastbamboo.common.ice.candidate.IceCandidatePair;
import org.lastbamboo.common.ice.candidate.IceCandidatePairState;
import org.lastbamboo.common.ice.candidate.IceTcpActiveCandidate;
import org.lastbamboo.common.ice.candidate.IceTcpHostPassiveCandidate;
import org.lastbamboo.common.ice.candidate.IceTcpRelayPassiveCandidate;
import org.lastbamboo.common.ice.candidate.IceTcpServerReflexiveSoCandidate;
import org.lastbamboo.common.ice.candidate.IceUdpHostCandidate;
import org.lastbamboo.common.ice.candidate.IceUdpPeerReflexiveCandidate;
import org.lastbamboo.common.ice.candidate.IceUdpRelayCandidate;
import org.lastbamboo.common.ice.candidate.IceUdpServerReflexiveCandidate;
import org.lastbamboo.common.util.NetworkUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class that performs ICE connectivity checks for a single pair of ICE 
 * candidates. 
 */
public class IceConnectivityCheckerImpl implements IceConnectivityChecker, 
    IceCandidateVisitor<Socket>
    {

    private final Logger m_log = LoggerFactory.getLogger(getClass());
    
    private final IceCandidatePair m_pair;
    private final IceCandidate m_remote;
    private final IceCandidate m_local;
    
    private Socket m_clientSocket;

    /**
     * Creates a new checker.
     * 
     * @param pair The pair to check
     */
    public IceConnectivityCheckerImpl(final IceCandidatePair pair)
        {
        m_log.debug("Created connectivity checker...");
        this.m_pair = pair;
        this.m_remote = m_pair.getRemoteCandidate();
        this.m_local = m_pair.getLocalCandidate();
        }

    public Socket check()
        {
        m_local.accept(this);
        return m_clientSocket;
        }

    public void visitCandidates(Collection<IceCandidate> candidates)
        {
        // TODO Auto-generated method stub
        
        }

    public Socket visitTcpActiveCandidate(final IceTcpActiveCandidate candidate)
        {
        final InetSocketAddress remote = this.m_remote.getSocketAddress();
        
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
                m_clientSocket = client;
                
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

    public Socket visitTcpHostPassiveCandidate(IceTcpHostPassiveCandidate candidate)
        {
        // TODO Auto-generated method stub
        return null;
        }

    public Socket visitTcpRelayPassiveCandidate(IceTcpRelayPassiveCandidate candidate)
        {
        // TODO Auto-generated method stub
        return null;
        }

    public Socket visitTcpServerReflexiveSoCandidate(IceTcpServerReflexiveSoCandidate candidate)
        {
        // TODO Auto-generated method stub
        return null;
        }

    public Socket visitUdpHostCandidate(IceUdpHostCandidate candidate)
        {
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
