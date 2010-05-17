package org.lastbamboo.common.ice.transport;

import java.net.InetSocketAddress;
import java.util.Collection;
import java.util.LinkedList;

import org.littleshoot.mina.common.ConnectFuture;
import org.littleshoot.mina.common.ExecutorThreadModel;
import org.littleshoot.mina.common.IoHandler;
import org.littleshoot.mina.common.IoServiceListener;
import org.littleshoot.mina.common.IoSession;
import org.littleshoot.mina.common.RuntimeIOException;
import org.littleshoot.mina.common.ThreadModel;
import org.littleshoot.mina.filter.codec.ProtocolCodecFactory;
import org.littleshoot.mina.filter.codec.ProtocolCodecFilter;
import org.littleshoot.mina.transport.socket.nio.DatagramConnector;
import org.littleshoot.mina.transport.socket.nio.DatagramConnectorConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class for creating a UDP "connection" for ICE.  This really just sets up
 * the UDP transport for a UDP connectivity check. 
 */
public class IceUdpConnector implements IceConnector
    {
    
    private final Logger m_log = LoggerFactory.getLogger(getClass());
    private final ProtocolCodecFactory m_demuxingCodecFactory;
    private final IoHandler m_demuxingIoHandler;
    private final boolean m_controlling;
    
    private final Collection<IoServiceListener> m_serviceListeners =
        new LinkedList<IoServiceListener>();

    /**
     * Creates a new UDP connector.
     * 
     * @param protocolCodecFactory The class for interpreting the protocol
     * for the connection.
     * @param demuxingIoHandler The class for processing read and written
     * messages.
     * @param controlling Whether or not we're the controlling agent.
     */
    public IceUdpConnector(
        final ProtocolCodecFactory protocolCodecFactory,
        final IoHandler demuxingIoHandler, final boolean controlling)
        {
        m_demuxingCodecFactory = protocolCodecFactory;
        m_demuxingIoHandler = demuxingIoHandler;
        m_controlling = controlling;
        }

    public IoSession connect(final InetSocketAddress localAddress,
        final InetSocketAddress remoteAddress)
        {
        final DatagramConnector connector = new DatagramConnector();
        synchronized (this.m_serviceListeners)
            {
            for (final IoServiceListener listener : this.m_serviceListeners)
                {
                connector.addListener(listener);
                }
            }
        final DatagramConnectorConfig cfg = connector.getDefaultConfig();
        cfg.getSessionConfig().setReuseAddress(true);
        
        final ThreadModel threadModel = ExecutorThreadModel.getInstance(
            getClass().getSimpleName() + 
            (this.m_controlling ? "-Controlling" : "-Not-Controlling"));
        
        final ProtocolCodecFilter demuxingFilter = 
            new ProtocolCodecFilter(this.m_demuxingCodecFactory);
        cfg.setThreadModel(threadModel);
        
        connector.getFilterChain().addLast("demuxFilter", demuxingFilter);

        m_log.debug("Connecting from "+localAddress+" to "+
            remoteAddress);
        
        final ConnectFuture cf = 
            connector.connect(remoteAddress, localAddress, 
                this.m_demuxingIoHandler);
        
        cf.join();
        try
            {
            final IoSession session = cf.getSession();
            if (session == null)
                {
                m_log.error("Could not create session from "+
                    localAddress +" to "+remoteAddress);
                throw new RuntimeIOException("Could not create session");
                }
            if (!session.isConnected())
                {
                throw new RuntimeIOException("Not connected");
                }
            return session;
            }
        catch (final RuntimeIOException e)
            {
            // I've seen this happen when the local address is already bound
            // for some reason (clearly without SO_REUSEADDRESS somehow).
            m_log.error("Could not create session from "+ localAddress +" to "+
                remoteAddress+" -- look at the CAUSE!!!", e);
            throw e;
            }
        }

    public void addIoServiceListener(final IoServiceListener serviceListener)
        {
        this.m_serviceListeners.add(serviceListener);
        }
    }
