package org.lastbamboo.common.ice.transport;

import java.net.InetSocketAddress;

import org.apache.mina.common.ConnectFuture;
import org.apache.mina.common.ExecutorThreadModel;
import org.apache.mina.common.IoHandler;
import org.apache.mina.common.IoServiceListener;
import org.apache.mina.common.IoSession;
import org.apache.mina.common.RuntimeIOException;
import org.apache.mina.common.ThreadModel;
import org.apache.mina.filter.codec.ProtocolCodecFactory;
import org.apache.mina.filter.codec.ProtocolCodecFilter;
import org.apache.mina.transport.socket.nio.DatagramConnector;
import org.apache.mina.transport.socket.nio.DatagramConnectorConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class for creating a UDP "connection" for ICE.  This really just sets up
 * the UDP transport for a UDP connnectivity check. 
 */
public class IceUdpConnector implements IceConnector
    {
    
    private final Logger m_log = LoggerFactory.getLogger(getClass());
    private final IoServiceListener m_ioServiceListener;
    private final ProtocolCodecFactory m_demuxingCodecFactory;
    private final IoHandler m_demuxingIoHandler;
    private final boolean m_controlling;

    /**
     * Creates a new UDP connector.
     * 
     * @param ioServiceListener The class that will listen to IO events for
     * the connection.
     * @param protocolCodecFactory The class for interpretting the protocol
     * for the connection.
     * @param demuxingIoHandler The class for processing read and written
     * messages.
     * @param controlling Whether or not we're the controlling agent.
     */
    public IceUdpConnector(final IoServiceListener ioServiceListener, 
        final ProtocolCodecFactory protocolCodecFactory,
        final IoHandler demuxingIoHandler, final boolean controlling)
        {
        m_ioServiceListener = ioServiceListener;
        m_demuxingCodecFactory = protocolCodecFactory;
        m_demuxingIoHandler = demuxingIoHandler;
        m_controlling = controlling;
        }

    public IoSession connect(final InetSocketAddress localAddress,
        final InetSocketAddress remoteAddress)
        {
        final DatagramConnector connector = new DatagramConnector();
        connector.addListener(this.m_ioServiceListener);
        final DatagramConnectorConfig cfg = connector.getDefaultConfig();
        cfg.getSessionConfig().setReuseAddress(true);
        
        final ThreadModel threadModel = ExecutorThreadModel.getInstance(
            getClass().getSimpleName() + 
            (this.m_controlling ? "-Controlling" : "-Not-Controlling"));
        
        final ProtocolCodecFilter demuxingFilter = 
            new ProtocolCodecFilter(this.m_demuxingCodecFactory);
        cfg.setThreadModel(threadModel);
        
        connector.getFilterChain().addLast("dexuxFilter", demuxingFilter);

        m_log.debug("Connecting from "+localAddress+" to "+
            remoteAddress);

        // If the address is on the local network, we should be able to 
        // connect more quickly.  If we can't, that likely indicates the 
        // address is just from a different local network.
        /*
        final InetAddress address = remoteAddress.getAddress();
        try
            {
            if (!address.isReachable(600)) 
                {
                m_log.debug("Address is not reachable: {}", address);
                return null;
                }
            }
        catch (final IOException e)
            {
            m_log.debug("IO error attempting to reach address: {}", address);
            return null;
            }
            */
        
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
    }