package org.lastbamboo.common.ice;

import org.apache.mina.common.IoHandler;
import org.apache.mina.common.IoServiceListener;
import org.apache.mina.common.IoSession;
import org.apache.mina.filter.codec.ProtocolCodecFactory;
import org.apache.mina.handler.StreamIoHandler;
import org.lastbamboo.common.ice.candidate.IceCandidate;
import org.lastbamboo.common.stun.stack.StunIoHandler;
import org.lastbamboo.common.stun.stack.message.StunMessage;
import org.lastbamboo.common.stun.stack.message.StunMessageVisitorFactory;
import org.lastbamboo.common.stun.stack.transaction.StunTransactionTracker;

/**
 * Class for creating STUN checker factories for both UDP and TCP.  Each
 * media stream requires its own factory because the checkers are coupled to
 * data for that specific stream.
 */
public class IceStunCheckerFactoryImpl implements IceStunCheckerFactory
    {

    private final IceAgent m_iceAgent;
    private final ProtocolCodecFactory m_codecFactory;
    private final IoHandler m_udpProtocolIoHandler;
    
    /**
     * The top-level class of media messages (non-STUN).
     */
    private final Class m_demuxClass;
    private final StunTransactionTracker<StunMessage> m_transactionTracker;

    /**
     * Creates a new factory.  The checkes the factory creates can be either
     * for UDP or TCP.
     * 
     * @param iceAgent The ICE agent the factory is for.
     * @param codecFactory The codec factory we'll use to encode and decode
     * messages.
     * @param demuxClass The {@link IoHandler} we'll use to process sent and
     * received messages.
     */
    public IceStunCheckerFactoryImpl(final IceAgent iceAgent, 
        final ProtocolCodecFactory codecFactory, final Class demuxClass,
        final IoHandler udpProtocolIoHandler, 
        final StunTransactionTracker<StunMessage> transactionTracker)
        {
        m_iceAgent = iceAgent;
        m_codecFactory = codecFactory;
        m_demuxClass = demuxClass;
        m_udpProtocolIoHandler = udpProtocolIoHandler;
        m_transactionTracker = transactionTracker;
        }
    
    public IceStunChecker newTcpChecker(final IceCandidate localCandidate, 
        final IceCandidate remoteCandidate, 
        final StreamIoHandler protocolIoHandler, 
        final StunMessageVisitorFactory messageVisitorFactory,
        final IoServiceListener serviceListener)
        {
        return newTcpChecker(localCandidate, remoteCandidate, protocolIoHandler, 
            null, messageVisitorFactory, serviceListener);
        }
    
    public IceStunChecker newTcpChecker(final IceCandidate localCandidate, 
        final IceCandidate remoteCandidate, 
        final StreamIoHandler protocolIoHandler, final IoSession ioSession,
        final StunMessageVisitorFactory messageVisitorFactory,
        final IoServiceListener serviceListener)
        {
        final IoHandler stunIoHandler = 
            new StunIoHandler<StunMessage>(messageVisitorFactory);
        return new IceTcpStunChecker(localCandidate, remoteCandidate,
            stunIoHandler, m_iceAgent, ioSession, m_transactionTracker, 
            protocolIoHandler, serviceListener);
        }
    
    public IceStunChecker newUdpChecker(final IceCandidate localCandidate, 
        final IceCandidate remoteCandidate, 
        final StunMessageVisitorFactory messageVisitorFactory,
        final IoServiceListener ioServiceListener)
        {
        final IoHandler stunIoHandler =
            new StunIoHandler<StunMessage>(messageVisitorFactory);
     
        return new IceUdpStunChecker(localCandidate, remoteCandidate,
            stunIoHandler, m_iceAgent, m_codecFactory, 
            m_demuxClass, this.m_udpProtocolIoHandler, m_transactionTracker,
            ioServiceListener);
        }

    }
