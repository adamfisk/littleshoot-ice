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

    /**
     * Sets the state of the check list.
     * 
     * @param state The state of the check list.
     */
    void setState(IceCheckListState state);

    /**
     * Accessor for the state of the check list.
     * 
     * @return The state of the check list.
     */
    IceCheckListState getState();

    void check();

    /** 
     * Sets whether or not the check list is "active" and should count towards
     * the value of N in timer computation from section 5.8.
     * 
     * @param active Whether or not the check list is active.
     */
    void setActive(boolean active);
    
    /**
     * Returns whether or not this check list is considered "active" and should 
     * count towards the value of N in timer computation from section 5.8.
     * 
     * @return <code>true</code> if the check list is active, otherwise
     * <code>false</code>.
     */
    boolean isActive();

    /**
     * Adds a pair to the triggered check queue.
     * 
     * @param pair The pair to add.
     */
    void addTriggeredPair(IceCandidatePair pair);

    /**
     * Removes the top triggered pair.  Triggered pairs are maintained in a
     * FIFO queue.
     * 
     * @return The top triggered pair.
     */
    IceCandidatePair getTriggeredPair();

    /**
     * Recomputes the priorities of pairs in checklists.  This can happen,
     * for example, if our role has changed from controlling to controlled or
     * vice versa.
     */
    void recomputePairPriorities();
    }
