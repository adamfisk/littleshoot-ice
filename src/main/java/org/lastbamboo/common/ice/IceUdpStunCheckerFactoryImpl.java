package org.lastbamboo.common.ice;

import org.apache.mina.common.IoHandler;
import org.apache.mina.filter.codec.ProtocolCodecFactory;
import org.lastbamboo.common.ice.candidate.IceCandidate;

public class IceUdpStunCheckerFactoryImpl implements IceUdpStunCheckerFactory
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

    public IceUdpStunCheckerFactoryImpl(final IceAgent iceAgent, 
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
        final IceBindingRequestHandler bindingRequestHandler =
            new IceBindingRequestHandlerImpl(m_iceAgent, 
                m_iceMediaStream, this);
        
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

        return new IceUdpStunChecker(localCandidate, remoteCandidate, 
            bindingRequestHandler, m_iceAgent, this.m_codecFactory, 
            this.m_demuxClass, demuxIoHandler);
        }
    }
