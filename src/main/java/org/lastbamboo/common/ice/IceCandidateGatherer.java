package org.lastbamboo.common.ice;

import java.util.Collection;

import org.lastbamboo.common.ice.candidate.IceCandidate;

/**
 * Interface for classes that gather ICE candidates.
 */
public interface IceCandidateGatherer
    {

    /**
     * Gathers ICE candidates.
     * 
     * @return The {@link Collection} of gathered candidates.
     */
    Collection<IceCandidate> gatherCandidates();

    }
