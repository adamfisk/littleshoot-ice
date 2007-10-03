package org.lastbamboo.common.ice;

import org.apache.mina.common.IoSession;
import org.lastbamboo.common.ice.candidate.IceCandidate;
import org.lastbamboo.common.ice.candidate.IceCandidatePair;

/**
 * Factory for creating ICE candidate pairs.
 */
public interface ExistingSessionIceCandidatePairFactory
    {

    IceCandidatePair newPair(IceCandidate localCandidate, 
        IceCandidate remoteCandidate, IoSession ioSession);

    }
