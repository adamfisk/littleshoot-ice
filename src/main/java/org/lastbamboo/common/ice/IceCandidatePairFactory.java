package org.lastbamboo.common.ice;

import org.lastbamboo.common.ice.candidate.IceCandidate;
import org.lastbamboo.common.ice.candidate.IceCandidatePair;

/**
 * Factory for creating ICE candidate pairs.
 */
public interface IceCandidatePairFactory
    {

    IceCandidatePair newPair(IceCandidate localCandidate, 
        IceCandidate remoteCandidate);

    }
