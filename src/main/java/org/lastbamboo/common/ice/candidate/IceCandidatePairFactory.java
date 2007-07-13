package org.lastbamboo.common.ice.candidate;

/**
 * Factory for creating ICE candidate pairs. 
 */
public interface IceCandidatePairFactory
    {

    /**
     * Creates a new pair.
     * 
     * @param localCandidate The local candidate.
     * @param remoteCandidate The remote candidate.
     * @return The new pair.
     */
    IceCandidatePair createPair(IceCandidate localCandidate, 
        IceCandidate remoteCandidate);

    }
