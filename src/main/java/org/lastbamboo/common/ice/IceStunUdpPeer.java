package org.lastbamboo.common.ice;

import java.net.InetAddress;
import java.net.InetSocketAddress;

import org.lastbamboo.common.stun.client.BoundStunClient;
import org.lastbamboo.common.stun.client.UdpStunClient;
import org.lastbamboo.common.stun.server.StunServer;
import org.lastbamboo.common.stun.server.StunServerImpl;
import org.lastbamboo.common.stun.stack.message.BindingRequest;
import org.lastbamboo.common.stun.stack.message.StunMessage;
import org.lastbamboo.common.stun.stack.message.StunMessageVisitorFactory;
import org.lastbamboo.common.stun.stack.transaction.StunTransactionTracker;
import org.lastbamboo.common.stun.stack.transaction.StunTransactionTrackerImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * ICE STUN peer class for UDP.
 * 
 * NOTE: This class takes a little work to wrap one's head around as far as
 * the socket SO_REUSEADDRESS option goes.  Basically, we run both a client
 * and a server on the same port.  We're using MINA as the underlying IO
 * framework, though, and MINA has connectors and acceptors.  MINA calls 
 * connect on the underlying DatagramChannel for the connector.  When that call
 * is made, the channel will only accept incoming data from the host it's
 * connected to.  Since we also bind to that port with an accepting channel,
 * though, we appear to override the restriction, and incoming data from 
 * other hosts seems to simply go to the accepting channel.  A little squirrely
 * all the way around, but it seems to work.
 */
public class IceStunUdpPeer implements BoundStunClient, StunServer
    {
    
    private final Logger m_log = LoggerFactory.getLogger(getClass());
    private final UdpStunClient m_stunClient;
    private final StunServer m_stunServer;
    
    /**
     * Creates a new ICE STUN UDP peer.
     * 
     * @param agent The ICE agent.
     * @param iceMediaStream The media stream this ICE UDP peer is working for.
     * @param checkerFactory The factory for creating classes to perform STUN
     * connectivity checks.
     */
    public IceStunUdpPeer(final IceAgent agent, 
        final IceMediaStream iceMediaStream, 
        final IceUdpStunCheckerFactory checkerFactory)
        {
        
        final StunTransactionTracker tracker = new StunTransactionTrackerImpl();
        
        // We use a special message visitor here because both the client and
        // the server can receive client and server messages under different
        // circumstances.  For ICE, these are just Binding Requests and 
        // Binding Responses, so we use a special visitor that handles only 
        // those but that handles both.
        final StunMessageVisitorFactory messageVisitorFactory =
            new IceStunServerMessageVisitorFactory(tracker, agent, 
                iceMediaStream, checkerFactory);
        
        // We generate a random port for the server. We use that as both the
        // acceptor port and the local port for the connector, as both
        // need to be the same for ICE to function.  Note this only works 
        // because both the client and server are using the SO_REUSEADDRESS
        // option.
        
        // We also add whether we're the offerer or answerer for thread
        // naming here just to make log reading easier.
        final String offererOrAnswerer;
        if (agent.isControlling())
            {
            offererOrAnswerer = "Offerer";
            }
        else
            {
            offererOrAnswerer = "Answerer";
            }
        this.m_stunServer = 
            new StunServerImpl(messageVisitorFactory, offererOrAnswerer);
        
        // We pass null here so the server binds to any available port.
        this.m_stunServer.start(null);
        
        final InetSocketAddress boundAddress = 
            this.m_stunServer.getBoundAddress();
        
        m_log.debug("Starting STUN client on local address: "+boundAddress);
        this.m_stunClient = 
            new UdpStunClient(boundAddress, tracker, messageVisitorFactory);
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

    public InetSocketAddress getBoundAddress()
        {
        return this.m_stunServer.getBoundAddress();
        }

    public void start()
        {
        // We've already started the server for ICE.
        }

    public void start(final InetSocketAddress bindAddress)
        {
        // We've already started the server for ICE.
        }
    }
