package org.lastbamboo.common.ice;

import org.apache.mina.common.IoSession;
import org.lastbamboo.common.ice.candidate.IceCandidate;
import org.lastbamboo.common.ice.candidate.IceCandidatePair;
import org.lastbamboo.common.ice.candidate.TcpIceCandidatePair;
import org.lastbamboo.common.ice.candidate.UdpIceCandidatePair;
import org.lastbamboo.common.ice.util.IceConnector;

public class IceCandidatePairFactoryImpl implements IceCandidatePairFactory
    {
    
    private final IceStunCheckerFactory m_checkerFactory;
    private final IceConnector m_udpConnector;
    private final IceConnector m_tcpConnector;

    public IceCandidatePairFactoryImpl(
        final IceStunCheckerFactory checkerFactory,
        final IceConnector udpConnector,
        final IceConnector tcpConnector)
        {
        m_checkerFactory = checkerFactory;
        m_udpConnector = udpConnector;
        m_tcpConnector = tcpConnector;
        }

    public IceCandidatePair newPair(final IceCandidate localCandidate,
        final IceCandidate remoteCandidate)
        {
        if (localCandidate.isUdp())
            {
            return new UdpIceCandidatePair(localCandidate, remoteCandidate, 
                this.m_checkerFactory, this.m_udpConnector);
            }
        else
            {
            return new TcpIceCandidatePair(localCandidate, remoteCandidate, 
                this.m_checkerFactory, this.m_tcpConnector);
            }
        }

    /*
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
        */
    }
