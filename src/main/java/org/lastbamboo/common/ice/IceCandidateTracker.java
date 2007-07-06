package org.lastbamboo.common.ice;

import java.net.Socket;

/**
 * Interface for keeping track of available ICE candidates.
 * 
 * @param <T> The type of objects returned in visitor methods.
 */
public interface IceCandidateTracker<T> extends IceCandidateVisitor<T>
    {

    /**
     * Accessor for the best available <code>Socket</code> the tracker 
     * currently has created.
     * @return The best <code>Socket</code> the tracker has created.
     * @throws IceException If we could not connect to any of the ICE 
     * candidates.
     */
    Socket getBestSocket() throws IceException;

    }
