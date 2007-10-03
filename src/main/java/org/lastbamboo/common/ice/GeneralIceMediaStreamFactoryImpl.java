package org.lastbamboo.common.ice;

import org.apache.mina.common.IoHandler;
import org.apache.mina.filter.codec.ProtocolCodecFactory;
import org.lastbamboo.common.ice.candidate.IceCandidateGatherer;
import org.lastbamboo.common.ice.candidate.IceCandidateGathererImpl;
import org.lastbamboo.common.stun.client.StunClient;
import org.lastbamboo.common.stun.stack.StunDemuxableProtocolCodecFactory;
import org.lastbamboo.common.stun.stack.StunIoHandler;
import org.lastbamboo.common.stun.stack.message.StunMessage;
import org.lastbamboo.common.stun.stack.message.StunMessageVisitorFactory;
import org.lastbamboo.common.stun.stack.transaction.StunTransactionTracker;
import org.lastbamboo.common.stun.stack.transaction.StunTransactionTrackerImpl;
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
        final Class<T> protocolMessageClass, 
        final IoHandler udpProtocolIoHandler,
        final TurnClientListener delegateTurnClientListener)
        {
        final DemuxableProtocolCodecFactory stunCodecFactory =
            new StunDemuxableProtocolCodecFactory();
        final ProtocolCodecFactory demuxingCodecFactory = 
            new DemuxingProtocolCodecFactory(
                stunCodecFactory, protocolCodecFactory);
        final StunTransactionTracker<StunMessage> transactionTracker =
            new StunTransactionTrackerImpl();

        final IceStunCheckerFactory checkerFactory =
            new IceStunCheckerFactoryImpl(transactionTracker);
        
        final StunMessageVisitorFactory messageVisitorFactory =
            new IceStunConnectivityCheckerFactoryImpl<StunMessage>(iceAgent, 
                transactionTracker, checkerFactory);
        final IoHandler stunIoHandler = 
            new StunIoHandler<StunMessage>(messageVisitorFactory);
        final IoHandler udpIoHandler = 
            new DemuxingIoHandler<StunMessage, T>(
                StunMessage.class, stunIoHandler, protocolMessageClass, 
                udpProtocolIoHandler);

        final StunClient udpStunPeer;
        if (streamDesc.isUdp())
            {
            udpStunPeer = 
                new IceStunUdpPeer(demuxingCodecFactory, udpIoHandler,
                    iceAgent.isControlling());
            }
        else
            {
            udpStunPeer = null;
            }
        
        // This class just decodes the TCP frames.

        final StunClient tcpStunPeer;
        if (streamDesc.isTcp())
            {
            final TurnClientListener turnClientListener =
                new TcpFrameTurnClientListener(messageVisitorFactory, 
                    delegateTurnClientListener);
            final StunClient tcpTurnClient = 
                new IceTcpTurnClient(turnClientListener);
            tcpStunPeer = 
                new IceStunTcpPeer(tcpTurnClient, messageVisitorFactory, 
                    iceAgent.isControlling());
            }
        else
            {
            tcpStunPeer = null;
            }
        
        final IceCandidateGatherer gatherer =
            new IceCandidateGathererImpl(tcpStunPeer, udpStunPeer, 
                iceAgent.isControlling(), streamDesc);
        
        final IceMediaStreamImpl stream = new IceMediaStreamImpl(iceAgent, 
            streamDesc, gatherer, checkerFactory, messageVisitorFactory, 
            demuxingCodecFactory, udpIoHandler);
        
        if (tcpStunPeer != null)
            {
            tcpStunPeer.addIoServiceListener(stream);
            }
        if (udpStunPeer != null)
            {
            udpStunPeer.addIoServiceListener(stream);
            }
        
        m_log.debug("Added media stream as listener...connecting...");
        if (tcpStunPeer != null)
            {
            tcpStunPeer.connect();
            }
        if (udpStunPeer != null)
            {
            udpStunPeer.connect();
            }
        stream.start(stream);
        return stream;
        }
    }
