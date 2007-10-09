package org.lastbamboo.common.ice;

import org.apache.mina.common.IoSession;
import org.lastbamboo.common.ice.candidate.IceCandidate;
import org.lastbamboo.common.ice.candidate.IceCandidatePair;
import org.lastbamboo.common.tcp.frame.TcpFrameClientIoHandler;

/**
 * Factory for creating ICE candidate pairs.
 */
public interface ExistingSessionIceCandidatePairFactory
    {

    IceCandidatePair newUdpPair(IceCandidate localCandidate, 
        IceCandidate remoteCandidate, IoSession ioSession);

    IceCandidatePair newTcpPair(IceCandidate localCandidate, 
        IceCandidate remoteCandidate, IoSession session, 
        TcpFrameClientIoHandler frameIoHandler);

    }
