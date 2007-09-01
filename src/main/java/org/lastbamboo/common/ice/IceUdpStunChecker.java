package org.lastbamboo.common.ice;

import java.net.InetSocketAddress;

import org.apache.mina.common.ConnectFuture;
import org.apache.mina.common.IoHandler;
import org.apache.mina.common.IoSession;
import org.apache.mina.common.ThreadModel;
import org.apache.mina.filter.codec.ProtocolCodecFactory;
import org.apache.mina.filter.codec.ProtocolCodecFilter;
import org.apache.mina.transport.socket.nio.DatagramConnector;
import org.apache.mina.transport.socket.nio.DatagramConnectorConfig;
import org.lastbamboo.common.ice.candidate.IceCandidate;
import org.lastbamboo.common.stun.stack.message.StunMessageVisitorFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class that performs STUN connectivity checks for ICE over UDP.  Each 
 * ICE candidate pair has its own connectivity checker. 
 */
public class IceUdpStunChecker extends AbstractIceStunChecker
    {

    private static final Logger LOG = 
        LoggerFactory.getLogger(IceUdpStunChecker.class);

    /**
     * Creates a new ICE connectivity checker over UDP.
     * 
     * @param localCandidate The local address.
     * @param remoteCandidate The remote address.
     * @param messageVisitorFactory The factory for creating visitors for 
     * incoming messages.
     * @param iceAgent The top-level ICE agent.
     * @param demuxingCodecFactory The {@link ProtocolCodecFactory} for 
     * demultiplexing between STUN and another protocol.
     * @param clazz The top-level message class the protocol other than STUN.
     * @param ioHandler The {@link IoHandler} to use for the other protocol.
     */
    public IceUdpStunChecker(final IceCandidate localCandidate, 
        final IceCandidate remoteCandidate, 
        final StunMessageVisitorFactory messageVisitorFactory, 
        final IceAgent iceAgent, 
        final ProtocolCodecFactory demuxingCodecFactory,
        final Class clazz, final IoHandler ioHandler)
        {
        super(localCandidate, remoteCandidate, messageVisitorFactory, 
            iceAgent, demuxingCodecFactory, clazz, ioHandler);
        }

    @Override
    protected void createConnector(
        final InetSocketAddress localAddress, 
        final InetSocketAddress remoteAddress, 
        final ThreadModel threadModel, 
        final ProtocolCodecFilter stunFilter, 
        final IoHandler demuxer)
        {
        final DatagramConnector connector = new DatagramConnector();
        
        final DatagramConnectorConfig cfg = connector.getDefaultConfig();
        cfg.getSessionConfig().setReuseAddress(true);
        cfg.setThreadModel(threadModel);
        
        connector.getFilterChain().addLast("stunFilter", stunFilter);
        LOG.debug("Connecting from "+localAddress+" to "+remoteAddress);
        final ConnectFuture cf = 
            connector.connect(remoteAddress, localAddress, demuxer);
        cf.join();
        final IoSession session = cf.getSession();
        
        if (session == null)
            {
            LOG.error("Could not create session from "+
                localAddress +" to "+remoteAddress);
            }
        this.m_ioSession = session;
        }

    @Override
    protected boolean connect()
        {
        // This should never happen, but you never know.
        return this.m_ioSession != null;
        }
    }
