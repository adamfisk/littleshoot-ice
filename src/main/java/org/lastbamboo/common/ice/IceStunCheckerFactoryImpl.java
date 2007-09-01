package org.lastbamboo.common.ice;

import org.apache.mina.common.IoHandler;
import org.apache.mina.filter.codec.ProtocolCodecFactory;
import org.lastbamboo.common.ice.candidate.IceCandidate;
import org.lastbamboo.common.ice.candidate.IceCandidateVisitor;
import org.lastbamboo.common.ice.candidate.IceCandidateVisitorAdapter;
import org.lastbamboo.common.ice.candidate.IceTcpActiveCandidate;
import org.lastbamboo.common.ice.candidate.IceUdpHostCandidate;
import org.lastbamboo.common.stun.stack.message.StunMessageVisitorFactory;

/**
 * Class for creating STUN checker factories for both UDP and TCP.  Each
 * media stream requires its own factory because the checkers are coupled to
 * data for that specific stream.
 */
public class IceStunCheckerFactoryImpl implements IceStunCheckerFactory
    {

    private final IceAgent m_iceAgent;
    private final IceMediaStream m_iceMediaStream;
    private final ProtocolCodecFactory m_codecFactory;
    private final IoHandler m_clientDemuxIoHandler;
    private final IoHandler m_serverDemuxIoHandler;
    
    /**
     * The top-level class of media messages (non-STUN).
     */
    private final Class m_demuxClass;

    /**
     * Creates a new factory.  The checkes the factory creates can be either
     * for UDP or TCP.
     * 
     * @param iceAgent The ICE agent the factory is for.
     * @param iceMediaStream The media stream the factory is for.
     * @param codecFactory The codec factory we'll use to encode and decode
     * messages.
     * @param demuxClass The {@link IoHandler} we'll use to process sent and
     * received messages.
     * @param clientDemuxIoHandler The client-side {@link IoHandler} to use
     * for the media protocol.
     * @param serverDemuxIoHandler The server-side {@link IoHandler} to use
     * for the media protocol.
     */
    public IceStunCheckerFactoryImpl(final IceAgent iceAgent, 
        final IceMediaStream iceMediaStream, 
        final ProtocolCodecFactory codecFactory, final Class demuxClass,
        final IoHandler clientDemuxIoHandler, 
        final IoHandler serverDemuxIoHandler)
        {
        m_iceAgent = iceAgent;
        m_iceMediaStream = iceMediaStream;
        m_codecFactory = codecFactory;
        m_demuxClass = demuxClass;
        m_clientDemuxIoHandler = clientDemuxIoHandler;
        m_serverDemuxIoHandler = serverDemuxIoHandler;
        }

    public IceStunChecker createStunChecker(final IceCandidate localCandidate, 
        final IceCandidate remoteCandidate)
        {
        final StunMessageVisitorFactory messageVisitorFactory =
            new IceStunServerMessageVisitorFactory(this.m_iceAgent, 
                this.m_iceMediaStream, this);
        
        final IoHandler demuxIoHandler;
        
        // TODO: This does not currently handle the changing of roles.
        if (this.m_iceAgent.isControlling())
            {
            demuxIoHandler = this.m_clientDemuxIoHandler;
            }
        else
            {
            demuxIoHandler = this.m_serverDemuxIoHandler;
            }

        // Create the checker based on the type of the local candidate.
        // We'll likely support more candidate types in the future here, such
        // as relays.
        final IceCandidateVisitor<IceStunChecker> visitor =
            new IceCandidateVisitorAdapter<IceStunChecker>()
            {

            public IceStunChecker visitTcpActiveCandidate(
                final IceTcpActiveCandidate candidate)
                {
                return tcpChecker();
                }

            public IceStunChecker visitUdpHostCandidate(
                final IceUdpHostCandidate candidate)
                {
                return udpChecker();
                }
            
            private IceStunChecker udpChecker()
                {
                return new IceUdpStunChecker(localCandidate, remoteCandidate,
                    messageVisitorFactory,
                    m_iceAgent, m_codecFactory, 
                    m_demuxClass, demuxIoHandler);
                }
            
            private IceStunChecker tcpChecker()
                {
                return new IceTcpStunChecker(localCandidate, remoteCandidate,
                    messageVisitorFactory,
                    m_iceAgent, m_codecFactory, 
                    m_demuxClass, demuxIoHandler);
                }
            };
            
        return localCandidate.accept(visitor);
        }
    }
