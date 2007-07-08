package org.lastbamboo.common.ice;

import java.util.Collection;

import org.lastbamboo.common.ice.candidate.IceCandidate;
import org.lastbamboo.common.ice.candidate.IceCandidatePair;

/**
 * Creates check lists from ICE candidates.
 */
public interface IceCheckListCreator
    {

    /**
     * Creates a {@link Collection} of {@link IceCandidatePair}s for 
     * connectivity checks.
     * 
     * @param localCandidates The candidates from the local agent.
     * @param remoteCandidates The candidates from the remote agent.
     * @return The {@link Collection} of paired candidates.
     */
    Collection<IceCandidatePair> createCheckList(
        Collection<IceCandidate> localCandidates, 
        Collection<IceCandidate> remoteCandidates);
    }
