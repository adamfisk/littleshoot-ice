package org.lastbamboo.common.ice;

import java.net.InetSocketAddress;

import org.apache.mina.common.ConnectFuture;
import org.apache.mina.common.ExecutorThreadModel;
import org.apache.mina.common.IoHandler;
import org.apache.mina.common.IoSession;
import org.apache.mina.filter.codec.ProtocolCodecFactory;
import org.apache.mina.filter.codec.ProtocolCodecFilter;
import org.apache.mina.transport.socket.nio.DatagramConnector;
import org.apache.mina.transport.socket.nio.DatagramConnectorConfig;
import org.lastbamboo.common.ice.candidate.IceCandidate;
import org.lastbamboo.common.stun.stack.StunDemuxingIoHandler;
import org.lastbamboo.common.stun.stack.StunIoHandler;
import org.lastbamboo.common.stun.stack.message.StunMessage;
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
    
    protected IoSession createClientSession(
        final IceCandidate localCandidate, final IceCandidate remoteCandidate, 
        final boolean controlling,
        final StunMessageVisitorFactory<StunMessage> visitorFactory, 
        final ProtocolCodecFactory demuxingCodecFactory, final Class clazz, 
        final IoHandler protocolIoHandler)
        {
        final InetSocketAddress localAddress = localCandidate.getSocketAddress();
        final InetSocketAddress remoteAddress = 
            remoteCandidate.getSocketAddress();
        final DatagramConnector connector = new DatagramConnector();
        
        final DatagramConnectorConfig cfg = connector.getDefaultConfig();
        cfg.getSessionConfig().setReuseAddress(true);

        final String controllingString;
        if (controlling)
            {
            controllingString = "Controlling";
            }
        else
            {
            controllingString = "Not-Controlling";
            }
        
        cfg.setThreadModel(
            ExecutorThreadModel.getInstance(
                "IceUdpStunChecker-"+controllingString));
        final ProtocolCodecFilter stunFilter = 
            new ProtocolCodecFilter(demuxingCodecFactory);
        
        connector.getFilterChain().addLast("stunFilter", stunFilter);
        
        final IoHandler ioHandler = 
            new StunIoHandler<StunMessage>(visitorFactory);
        
        final IoHandler demuxer = new StunDemuxingIoHandler(clazz, 
            protocolIoHandler, ioHandler);
        LOG.debug("Connecting from "+localAddress+" to "+remoteAddress);
        final ConnectFuture cf = 
            connector.connect(remoteAddress, localAddress, demuxer);
        cf.join();
        final IoSession session = cf.getSession();
        
        if (session == null)
            {
            LOG.error("Could not create session from "+
                localAddress +" to "+remoteAddress);
            throw new NullPointerException("Could not create session!!");
            }
        return session;
        }
    }
