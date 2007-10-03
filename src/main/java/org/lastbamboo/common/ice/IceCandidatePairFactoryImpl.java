package org.lastbamboo.common.ice;

import org.lastbamboo.common.ice.candidate.IceCandidate;
import org.lastbamboo.common.ice.candidate.IceCandidatePair;
import org.lastbamboo.common.ice.candidate.TcpIceCandidatePair;
import org.lastbamboo.common.ice.candidate.UdpIceCandidatePair;
import org.lastbamboo.common.ice.transport.IceTcpConnector;
import org.lastbamboo.common.ice.util.IceUdpConnector;

public class IceCandidatePairFactoryImpl implements IceCandidatePairFactory
    {
    
    private final IceStunCheckerFactory m_checkerFactory;
    private final IceUdpConnector m_udpConnector;
    private final IceTcpConnector m_tcpConnector;

    public IceCandidatePairFactoryImpl(
        final IceStunCheckerFactory checkerFactory,
        final IceUdpConnector udpConnector,
        final IceTcpConnector tcpConnector)
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
    }
