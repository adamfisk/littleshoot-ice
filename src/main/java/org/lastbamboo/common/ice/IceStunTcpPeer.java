package org.lastbamboo.common.ice;

import java.net.InetAddress;
import java.net.InetSocketAddress;

import org.lastbamboo.common.stun.client.StunClient;
import org.lastbamboo.common.stun.server.StunServer;
import org.lastbamboo.common.stun.server.TcpStunServer;
import org.lastbamboo.common.stun.stack.message.BindingRequest;
import org.lastbamboo.common.stun.stack.message.StunMessage;
import org.lastbamboo.common.stun.stack.message.StunMessageVisitorFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class IceStunTcpPeer implements StunClient, StunServer
    {
    
    private final Logger m_log = LoggerFactory.getLogger(getClass());
    private final StunClient m_stunClient;
    private final StunServer m_stunServer;
    
    /**
     * Creates a new ICE STUN UDP peer.
     * 
     * @param messageVisitorFactory The factory for creating message visitors
     * on the server. 
     * @param controlling Whether or not this agent is controlling.
     */
    public IceStunTcpPeer(final StunClient tcpStunClient, 
        final StunMessageVisitorFactory messageVisitorFactory,
        final boolean controlling)
        {
        
        m_stunClient = tcpStunClient;
        
        // We also add whether we're the controlling agent for thread
        // naming here just to make log reading easier.
        final String controllingString;
        if (controlling)
            {
            controllingString = "Controlling";
            }
        else
            {
            controllingString = "Not-Controlling";
            }
        this.m_stunServer = 
            new TcpStunServer(messageVisitorFactory, controllingString);
        
        this.m_stunServer.start(tcpStunClient.getHostAddress());
        }

    public InetSocketAddress getHostAddress()
        {
        return this.m_stunClient.getHostAddress();
        }

    public InetSocketAddress getRelayAddress()
        {
        return this.m_stunClient.getRelayAddress();
        }

    public InetSocketAddress getServerReflexiveAddress()
        {
        return this.m_stunClient.getServerReflexiveAddress();
        }

    public InetAddress getStunServerAddress()
        {
        return this.m_stunClient.getStunServerAddress();
        }

    public StunMessage write(final BindingRequest request, 
        final InetSocketAddress remoteAddress)
        {
        return this.m_stunClient.write(request, remoteAddress);
        }
    
    public StunMessage write(final BindingRequest request, 
        final InetSocketAddress remoteAddress, final long rto)
        {
        return this.m_stunClient.write(request, remoteAddress, rto);
        }

    public void start()
        {
        // We've already started the server for ICE.
        }

    public void start(final InetSocketAddress bindAddress)
        {
        // We've already started the server for ICE.
        }

    public InetSocketAddress getBoundAddress()
        {
        return this.m_stunServer.getBoundAddress();
        }
    }
