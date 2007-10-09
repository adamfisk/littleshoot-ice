package org.lastbamboo.common.ice.transport;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;

import org.apache.mina.common.ConnectFuture;
import org.apache.mina.common.ExecutorThreadModel;
import org.apache.mina.common.IoHandler;
import org.apache.mina.common.IoService;
import org.apache.mina.common.IoServiceConfig;
import org.apache.mina.common.IoServiceListener;
import org.apache.mina.common.IoSession;
import org.apache.mina.common.RuntimeIOException;
import org.apache.mina.common.ThreadModel;
import org.apache.mina.filter.codec.ProtocolCodecFactory;
import org.apache.mina.filter.codec.ProtocolCodecFilter;
import org.apache.mina.transport.socket.nio.SocketConnector;
import org.apache.mina.transport.socket.nio.SocketConnectorConfig;
import org.lastbamboo.common.ice.util.IceConnector;
import org.lastbamboo.common.stun.stack.StunDemuxableProtocolCodecFactory;
import org.lastbamboo.common.stun.stack.StunIoHandler;
import org.lastbamboo.common.stun.stack.message.StunMessage;
import org.lastbamboo.common.stun.stack.message.StunMessageVisitorFactory;
import org.lastbamboo.common.tcp.frame.TcpFrame;
import org.lastbamboo.common.tcp.frame.TcpFrameCodecFactory;
import org.lastbamboo.common.tcp.frame.TcpFrameClientIoHandler;
import org.lastbamboo.common.util.mina.DemuxableProtocolCodecFactory;
import org.lastbamboo.common.util.mina.DemuxingIoHandler;
import org.lastbamboo.common.util.mina.DemuxingProtocolCodecFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class IceTcpConnector implements IceConnector, IoServiceListener
    {

    private final Logger m_log = LoggerFactory.getLogger(getClass());
    private final IoServiceListener m_ioServiceListener;
    private final boolean m_controlling;
    private final DemuxingIoHandler<StunMessage, TcpFrame> m_demuxingIoHandler;
    private final TcpFrameClientIoHandler m_streamIoHandler;

    public IceTcpConnector(final IoServiceListener ioServiceListener, 
        final StunMessageVisitorFactory messageVisitorFactory, 
        final boolean controlling)
        {
        m_ioServiceListener = ioServiceListener;
        m_controlling = controlling;
        // TODO: We don't currently support TCP-SO, so we don't bind to the 
        // local port.
        final IoHandler stunIoHandler = 
            new StunIoHandler<StunMessage>(messageVisitorFactory);

        this.m_streamIoHandler = new TcpFrameClientIoHandler();
        this.m_demuxingIoHandler = 
            new DemuxingIoHandler<StunMessage, TcpFrame>(
                StunMessage.class, stunIoHandler, 
                TcpFrame.class, m_streamIoHandler);
        }

    public IoSession connect(final InetSocketAddress localAddress,
        final InetSocketAddress remoteAddress)
        {
        final SocketConnector connector = new SocketConnector();
        connector.addListener(this.m_ioServiceListener);
        connector.addListener(this);
        
        final SocketConnectorConfig cfg = connector.getDefaultConfig();
        cfg.getSessionConfig().setReuseAddress(true);
        
        final ThreadModel threadModel = ExecutorThreadModel.getInstance(
            getClass().getSimpleName() +
            (this.m_controlling ? "-Controlling" : "-Not-Controlling"));
        final DemuxableProtocolCodecFactory stunCodecFactory =
            new StunDemuxableProtocolCodecFactory();
        final DemuxableProtocolCodecFactory tcpFramingCodecFactory =
            new TcpFrameCodecFactory();
        final ProtocolCodecFactory demuxingCodecFactory = 
            new DemuxingProtocolCodecFactory(stunCodecFactory, 
                tcpFramingCodecFactory);
        
        final ProtocolCodecFilter demuxingFilter = 
            new ProtocolCodecFilter(demuxingCodecFactory);
        
        cfg.setThreadModel(threadModel);
        
        connector.getFilterChain().addLast("demuxingFilter", demuxingFilter);

        m_log.debug("Establishing TCP connection...");
        final InetAddress address = remoteAddress.getAddress();
        final int connectTimeout;
        // If the address is on the local network, we should be able to 
        // connect more quickly.  If we can't, that likely indicates the 
        // address is just from a different local network.
        if (address.isSiteLocalAddress())
            {
            try
                {
                if (!address.isReachable(400))
                    {
                    return null;
                    }
                }
            catch (final IOException e)
                {
                return null;
                }
            m_log.debug("Address is reachable. Connecting:{}", address);

            // We should be able to connect to local, private addresses 
            // really quickly.  So don't wait around too long.
            connectTimeout = 3000;
            }
        else
            {
            connectTimeout = 12000;
            }
        
        final ConnectFuture cf = 
            connector.connect(remoteAddress, localAddress, 
                 this.m_demuxingIoHandler);
        cf.join(connectTimeout);
        try
            {
            final IoSession session = cf.getSession();
            if (session == null)
                {
                return null;
                }
            m_log.debug("TCP STUN checker connected on: {}",session);
            return session;
            }
        catch (final RuntimeIOException e)
            {
            // This happens when we can't connect.
            m_log.debug("Could not connect to host: {}", remoteAddress);
            m_log.debug("Reason for no connection: ", e);
            throw e;
            }
        }
    
    public void serviceActivated(final IoService service, 
        final SocketAddress serviceAddress, 
        final IoHandler handler, final IoServiceConfig config)
        {
        }

    public void serviceDeactivated(final IoService service, 
        final SocketAddress serviceAddress, final IoHandler handler, 
        final IoServiceConfig config)
        {
        }

    public void sessionCreated(final IoSession session)
        {
        session.setAttribute(TcpFrameClientIoHandler.class.getSimpleName(), 
            this.m_streamIoHandler);
        }

    public void sessionDestroyed(final IoSession session)
        {
        }

    public TcpFrameClientIoHandler getStreamIoHandler()
        {
        return this.m_streamIoHandler;
        }
    }
