package org.lastbamboo.common.ice;

import java.net.InetAddress;
import java.net.InetSocketAddress;

import org.lastbamboo.common.stun.client.StunClient;
import org.lastbamboo.common.stun.server.StunServer;
import org.lastbamboo.common.stun.server.UdpStunServer;
import org.lastbamboo.common.stun.stack.message.BindingRequest;
import org.lastbamboo.common.stun.stack.message.StunMessage;
import org.lastbamboo.common.stun.stack.message.StunMessageVisitorFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * ICE STUN peer class for UDP.  This is basically just a STUN client for 
 * candidate gathering and a STUN server for processing incoming requests
 * from peer reflexive candidates.  The incoming requests are generally 
 * peer reflexive because the other "connected" UDP connectivity classes take
 * care of processing requests from any candidates we know about.
 * 
 * NOTE: This class takes a little work to wrap one's head around as far as
 * the socket SO_REUSEADDRESS option goes.  Basically, we run both a client
 * and a server on the same port.  We're using MINA as the underlying IO
 * framework, though, and MINA has connectors and acceptors.  MINA calls 
 * connect on the underlying DatagramChannel for the connector.  When that call
 * is made, the channel will only accept incoming data from the host it's
 * connected to.  Since we also bind to that port with an accepting channel,
 * though, so incoming data from other hosts goes to the accepting channel.  
 */
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
        // We also add whether we're the offerer or answerer for thread
        // naming here just to make log reading easier.
        final String offererOrAnswerer;
        if (controlling)
            {
            offererOrAnswerer = "Offerer";
            }
        else
            {
            offererOrAnswerer = "Answerer";
            }
        this.m_stunServer = 
            new UdpStunServer(messageVisitorFactory, offererOrAnswerer);
        
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
