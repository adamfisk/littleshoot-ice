package org.lastbamboo.common.ice;

import org.apache.mina.common.IoSession;
import org.lastbamboo.common.ice.candidate.IceCandidate;
import org.lastbamboo.common.ice.candidate.IceCandidatePair;
import org.lastbamboo.common.ice.candidate.IceTcpCandidatePair;
import org.lastbamboo.common.ice.candidate.IceUdpCandidatePair;
import org.lastbamboo.common.tcp.frame.TcpFrameIoHandler;

public class ExistingSessionIceCandidatePairFactoryImpl 
    implements ExistingSessionIceCandidatePairFactory
    {
    
    private final IceStunCheckerFactory m_checkerFactory;

    public ExistingSessionIceCandidatePairFactoryImpl(
        final IceStunCheckerFactory checkerFactory)
        {
        m_checkerFactory = checkerFactory;
        }

    public IceCandidatePair newUdpPair(final IceCandidate localCandidate,
        final IceCandidate remoteCandidate, final IoSession ioSession)
        {
        return new IceUdpCandidatePair(localCandidate, 
            remoteCandidate, ioSession, this.m_checkerFactory);
        }

    public IceCandidatePair newTcpPair(final IceCandidate localCandidate, 
        final IceCandidate remoteCandidate, final IoSession ioSession, 
        final TcpFrameIoHandler frameIoHandler)
        {
        return new IceTcpCandidatePair(localCandidate, 
            remoteCandidate, ioSession, this.m_checkerFactory, frameIoHandler);
        }
    }
