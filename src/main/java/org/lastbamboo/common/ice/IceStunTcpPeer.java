package org.lastbamboo.common.ice;

import java.net.InetAddress;
import java.net.InetSocketAddress;

import org.apache.mina.common.IoHandler;
import org.apache.mina.common.IoServiceListener;
import org.apache.mina.filter.codec.ProtocolCodecFactory;
import org.lastbamboo.common.stun.client.StunClient;
import org.lastbamboo.common.stun.server.StunServer;
import org.lastbamboo.common.stun.server.TcpStunServer;
import org.lastbamboo.common.stun.stack.StunIoHandler;
import org.lastbamboo.common.stun.stack.StunProtocolCodecFactory;
import org.lastbamboo.common.stun.stack.message.BindingRequest;
import org.lastbamboo.common.stun.stack.message.StunMessage;
import org.lastbamboo.common.stun.stack.message.StunMessageVisitorFactory;
import org.lastbamboo.common.tcp.frame.TcpFrame;
import org.lastbamboo.common.tcp.frame.TcpFrameIoHandler;
import org.lastbamboo.common.util.mina.DemuxingIoHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * ICE STUN peer for TCP.  This creates both the client and the server side
 * of STUN, reusing the same local port for both.
 *  
 * @param <T> The type returned by message visiting classes.
 */
public class IceStunTcpPeer<T> implements StunClient, StunServer
    {
    
    private final Logger m_log = LoggerFactory.getLogger(getClass());
    private final StunClient m_stunClient;
    private final StunServer m_stunServer;
    
    /**
     * Creates a new ICE STUN UDP peer.
     * 
     * @param tcpStunClient The TCP STUN client side handler.
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
        
        final ProtocolCodecFactory codecFactory = 
            new StunProtocolCodecFactory();
        final IoHandler stunIoHandler =
            new StunIoHandler<T>(messageVisitorFactory);
        final IoHandler ioHandler = 
            new DemuxingIoHandler<StunMessage, TcpFrame>(StunMessage.class, 
                stunIoHandler, TcpFrame.class, new TcpFrameIoHandler());
        this.m_stunServer =
            new TcpStunServer(codecFactory, ioHandler, controllingString);
        }

    public void connect()
        {
        this.m_stunClient.connect();
        this.m_stunServer.start(m_stunClient.getHostAddress());
        //this.m_upnpManager.mapAddress(m_stunClient.getHostAddress());
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

    public void addIoServiceListener(IoServiceListener serviceListener)
        {
        this.m_stunServer.addIoServiceListener(serviceListener);
        this.m_stunClient.addIoServiceListener(serviceListener);
        }

    public void close()
        {
        //this.m_upnpManager.unmapAddress(this.m_stunClient.getHostAddress());
        this.m_stunClient.close();
        this.m_stunServer.close();
        }
    }
