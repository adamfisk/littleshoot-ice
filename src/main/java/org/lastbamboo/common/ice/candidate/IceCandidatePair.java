package org.lastbamboo.common.ice.candidate;



/**
 * Interface for a pair of ICE candidates.
 */
public interface IceCandidatePair
    {

    /**
     * Accessor for the local candidate for the pair.
     * 
     * @return The local candidiate for the pair.
     */
    IceCandidate getLocalCandidate();
    
    /**
     * Accessor for the remote candidate for the pair.
     * 
     * @return the remote candidate for the pair.
     */
    IceCandidate getRemoteCandidate();

    /**
     * Accessor for the priority for the pair.
     * 
     * @return The priority for the pair.
     */
    long getPriority();
    
    /**
     * Accesses the state of the pair.
     * 
     * @return The state of the pair.
     */
    IceCandidatePairState getState();
    
    /**
     * Accessor for the foundation for the pair.
     * 
     * @return The foundation for the candidate pair.
     */
    int getFoundation();

    /**
     * Sets the state of the pair.
     * 
     * @param state The state of the pair.
     */
    void setState(IceCandidatePairState state);

    /**
     * Accessor for the component ID for the pair.  Note that both candidates
     * in the pair always have the same component ID.
     * 
     * @return The component ID for the pair.
     */
    int getComponentId();
    
    /**
     * Accepts the specified visitor to an ICE candidate pair.
     * 
     * @param <T> The class to return.
     * @param visitor The visitor to accept.
     * @return The class the visitor created. 
     */
    <T> T accept(IceCandidatePairVisitor<T> visitor);
    }
