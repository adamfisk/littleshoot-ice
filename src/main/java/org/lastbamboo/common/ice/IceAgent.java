package org.lastbamboo.common.ice;

import org.lastbamboo.common.offer.answer.OfferAnswer;

/**
 * Interface for ICE agents. 
 */
public interface IceAgent extends OfferAnswer
    {

    /**
     * Sets whether or not this agent is the controlling agent.
     * 
     * @param controlling Whether or not this agent is the controlling agent.
     */
    void setControlling(boolean controlling);
    
    /**
     * Returns whether or not this agent is the controlling agent.
     * 
     * @return <code>true</code> if this agent is the controlling agent,
     * otherwise <code>false</code>.
     */
    boolean isControlling();
    
    /**
     * Accessor for the role conflict tie-breaker for this agent.
     * 
     * @return The role conflict tie-breaker for this agent.
     */
    byte[] getTieBreaker();

    /**
     * Calculates the delay in milliseconds to use before initiating a new
     * transaction for a given media stream.  The agent handles this because
     * the number of outstanding media streams is taken into account when
     * calculating the delay, and only the agent has that information.
     * 
     * @param Ta_i The transaction delay for the specific media stream.
     * @return The transaction delay to use based on a formula taking into
     * account the number of active media streams.
     */
    long calculateDelay(int Ta_i);
    
    /**
     * Notifies the listener that the media stream now has a valid pair for
     * all components of the media stream.
     * 
     * @param mediaStream The media stream.
     */
    void onValidPairsForAllComponents(IceMediaStream mediaStream);

    /**
     * Tels the listener to unfreeze any other check lists.
     * 
     * @param mediaStream The media stream initiating the unfreeze operation.
     */
    void onUnfreezeCheckLists(IceMediaStream mediaStream);

    /**
     * Recomputes the priorities of pairs in checklists.  This can happen,
     * for example, if our role has changed from controlling to controlled or
     * vice versa.
     */
    void recomputePairPriorities();

    }
