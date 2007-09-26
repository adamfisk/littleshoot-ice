package org.lastbamboo.common.ice;

import org.apache.mina.common.IoHandler;
import org.apache.mina.filter.codec.ProtocolCodecFactory;
import org.apache.mina.handler.StreamIoHandler;
import org.lastbamboo.common.ice.candidate.IceCandidateGatherer;
import org.lastbamboo.common.ice.candidate.IceCandidateGathererImpl;
import org.lastbamboo.common.stun.client.StunClient;
import org.lastbamboo.common.stun.stack.StunDemuxableProtocolCodecFactory;
import org.lastbamboo.common.stun.stack.StunIoHandler;
import org.lastbamboo.common.stun.stack.message.StunMessage;
import org.lastbamboo.common.stun.stack.message.StunMessageVisitorFactory;
import org.lastbamboo.common.stun.stack.transaction.StunTransactionTracker;
import org.lastbamboo.common.stun.stack.transaction.StunTransactionTrackerImpl;
import org.lastbamboo.common.tcp.frame.TcpFrameIoHandler;
import org.lastbamboo.common.turn.client.TcpFrameTurnClientListener;
import org.lastbamboo.common.turn.client.TurnClientListener;
import org.lastbamboo.common.util.mina.DemuxableProtocolCodecFactory;
import org.lastbamboo.common.util.mina.DemuxingIoHandler;
import org.lastbamboo.common.util.mina.DemuxingProtocolCodecFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Factory for creating media streams.  This factory offers a more complex
 * API intended for specialized ICE implementations of the simpler
 * {@link IceMediaStreamFactory} interface to use behind the scenes.
 */
public class GeneralIceMediaStreamFactoryImpl 
    implements GeneralIceMediaStreamFactory
    {

    private final Logger m_log = LoggerFactory.getLogger(getClass());
    
    public <T> IceMediaStream newIceMediaStream(
        final IceMediaStreamDesc streamDesc, final IceAgent iceAgent, 
        final DemuxableProtocolCodecFactory protocolCodecFactory, 
        final Class<T> protocolMessageClass, final IoHandler protocolIoHandler,
        final TurnClientListener delegateTurnClientListener)
        {
        final DemuxableProtocolCodecFactory stunCodecFactory =
            new StunDemuxableProtocolCodecFactory();
        final ProtocolCodecFactory codecFactory = 
            new DemuxingProtocolCodecFactory(
                stunCodecFactory, protocolCodecFactory);
        final StunTransactionTracker<StunMessage> transactionTracker =
            new StunTransactionTrackerImpl();

        final IceStunCheckerFactory checkerFactory =
            new IceStunCheckerFactoryImpl(iceAgent, 
                codecFactory, protocolMessageClass, protocolIoHandler, 
                transactionTracker);
        final StunMessageVisitorFactory udpMessageVisitorFactory =
            new IceUdpStunConnectivityCheckerFactory<StunMessage>(iceAgent, 
                transactionTracker, checkerFactory);
        
        final IoHandler stunIoHandler = 
            new StunIoHandler<StunMessage>(udpMessageVisitorFactory);
            
        final IoHandler udpIoHandler = 
            new DemuxingIoHandler<StunMessage, T>(
                StunMessage.class, stunIoHandler, protocolMessageClass, 
                protocolIoHandler);

        final StreamIoHandler streamIoHandler = new TcpFrameIoHandler();
        final StunMessageVisitorFactory tcpMessageVisitorFactory =
            new IceTcpStunConnectivityCheckerFactory<StunMessage>(iceAgent, 
                transactionTracker, checkerFactory, streamIoHandler);
        final StunClient udpStunPeer = 
            new IceStunUdpPeer(udpMessageVisitorFactory, 
                iceAgent.isControlling()); // TODO: Add the IO HANDLER!!!
        
        // This class just decodes the TCP frames.
        final TurnClientListener turnClientListener =
            new TcpFrameTurnClientListener(tcpMessageVisitorFactory, 
                delegateTurnClientListener);
        
        final StunClient tcpTurnClient = 
            new IceTcpTurnClient(turnClientListener);
        final StunClient tcpStunPeer = 
            new IceStunTcpPeer(tcpTurnClient, tcpMessageVisitorFactory, 
                iceAgent.isControlling(), streamIoHandler);
        
        final IceCandidateGatherer gatherer =
            new IceCandidateGathererImpl(tcpStunPeer, udpStunPeer, 
                iceAgent.isControlling(), streamDesc);
        
        final IceMediaStreamImpl stream = new IceMediaStreamImpl(iceAgent, 
            streamDesc, gatherer, checkerFactory, tcpMessageVisitorFactory, 
            udpMessageVisitorFactory);
        
        tcpStunPeer.addIoServiceListener(stream);
        udpStunPeer.addIoServiceListener(stream);
        
        m_log.debug("Added media stream as listener...connecting...");
        udpStunPeer.connect();
        tcpStunPeer.connect();
        stream.start(stream);
        return stream;
        }
    }
