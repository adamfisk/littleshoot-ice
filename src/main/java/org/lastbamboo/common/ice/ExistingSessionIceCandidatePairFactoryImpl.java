package org.lastbamboo.common.ice;

import org.apache.mina.common.IoSession;
import org.lastbamboo.common.ice.candidate.IceCandidate;
import org.lastbamboo.common.ice.candidate.IceCandidatePair;
import org.lastbamboo.common.ice.candidate.TcpIceCandidatePair;
import org.lastbamboo.common.ice.candidate.UdpIceCandidatePair;

public class ExistingSessionIceCandidatePairFactoryImpl 
    implements ExistingSessionIceCandidatePairFactory
    {
    
    private final IceStunCheckerFactory m_checkerFactory;

    public ExistingSessionIceCandidatePairFactoryImpl(
        final IceStunCheckerFactory checkerFactory)
        {
        m_checkerFactory = checkerFactory;
        }

    public IceCandidatePair newPair(final IceCandidate localCandidate,
        final IceCandidate remoteCandidate, final IoSession ioSession)
        {
        if (localCandidate.isUdp())
            {
            return new UdpIceCandidatePair(localCandidate, 
                remoteCandidate, ioSession, this.m_checkerFactory);
            }
        else
            {
            return new TcpIceCandidatePair(localCandidate, 
                remoteCandidate, ioSession, this.m_checkerFactory);
            }
        }
    }
