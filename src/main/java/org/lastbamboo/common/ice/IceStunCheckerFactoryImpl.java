package org.lastbamboo.common.ice;

import org.apache.mina.common.IoHandler;
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
public class IceStunCheckerFactoryImpl 
    implements IceStunCheckerFactory<StunMessage>
    {

    private final IceAgent m_iceAgent;
    private final ProtocolCodecFactory m_codecFactory;
    private final IoHandler m_clientDemuxIoHandler;
    private final IoHandler m_serverDemuxIoHandler;
    
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
     * @param clientDemuxIoHandler The client-side {@link IoHandler} to use
     * for the media protocol.
     * @param serverDemuxIoHandler The server-side {@link IoHandler} to use
     * for the media protocol.
     * @param transactionTracker 
     */
    public IceStunCheckerFactoryImpl(final IceAgent iceAgent, 
        final ProtocolCodecFactory codecFactory, final Class demuxClass,
        final IoHandler clientDemuxIoHandler, 
        final IoHandler serverDemuxIoHandler, 
        final StunTransactionTracker<StunMessage> transactionTracker)
        {
        m_iceAgent = iceAgent;
        m_codecFactory = codecFactory;
        m_demuxClass = demuxClass;
        m_clientDemuxIoHandler = clientDemuxIoHandler;
        m_serverDemuxIoHandler = serverDemuxIoHandler;
        m_transactionTracker = transactionTracker;
        }
    
    public IceStunChecker newTcpChecker(final IceCandidate localCandidate, 
        final IceCandidate remoteCandidate, 
        final StreamIoHandler protocolIoHandler, 
        final StunMessageVisitorFactory<StunMessage> messageVisitorFactory)
        {
        return newTcpChecker(localCandidate, remoteCandidate, protocolIoHandler, 
            null, messageVisitorFactory);
        }
    
    public IceStunChecker newTcpChecker(final IceCandidate localCandidate, 
        final IceCandidate remoteCandidate, 
        final StreamIoHandler protocolIoHandler, final IoSession ioSession,
        final StunMessageVisitorFactory<StunMessage> messageVisitorFactory)
        {
        final IoHandler stunIoHandler = 
            new StunIoHandler<StunMessage>(messageVisitorFactory);
        return new IceTcpStunChecker(localCandidate, remoteCandidate,
            stunIoHandler, m_iceAgent, ioSession, m_transactionTracker, 
            protocolIoHandler);
        }
    
    public IceStunChecker newUdpChecker(final IceCandidate localCandidate, 
        final IceCandidate remoteCandidate, 
        final StunMessageVisitorFactory<StunMessage> messageVisitorFactory)
        {
        final IoHandler protocolIoHandler;
        
        // TODO: This does not currently handle the changing of roles.
        if (this.m_iceAgent.isControlling())
            {
            protocolIoHandler = this.m_clientDemuxIoHandler;
            }
        else
            {
            protocolIoHandler = this.m_serverDemuxIoHandler;
            }

        final IoHandler stunIoHandler =
            new StunIoHandler<StunMessage>(messageVisitorFactory);
     
        return new IceUdpStunChecker(localCandidate, remoteCandidate,
            stunIoHandler, m_iceAgent, m_codecFactory, 
            m_demuxClass, protocolIoHandler, m_transactionTracker);
        }

    }
