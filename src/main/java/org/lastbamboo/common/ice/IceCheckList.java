package org.lastbamboo.common.ice;

import java.util.Collection;

import org.lastbamboo.common.ice.candidate.IceCandidatePair;

/**
 * Interface for ICE check lists. 
 */
public interface IceCheckList
    {

    /**
     * Accessor for the candidate pairs in the check list.
     * 
     * @return The check list.
     */
    Collection<IceCandidatePair> getPairs();
    }
