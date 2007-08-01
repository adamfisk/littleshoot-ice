package org.lastbamboo.common.ice;

import java.net.InetSocketAddress;
import java.util.Collection;

import org.lastbamboo.common.ice.candidate.IceCandidate;
import org.lastbamboo.common.ice.candidate.IceCandidatePair;

/**
 * A media stream for an ICE.
 */
public interface IceMediaStream
    {

    Collection<IceCandidatePair> getValidPairs();
    
    void addValidPair(IceCandidatePair pair);

    void connect();

    IceCandidate getLocalCandidate(InetSocketAddress localAddress);

    void addLocalCandidate(IceCandidate localCandidate);
    
    IceCandidatePair getPair(InetSocketAddress localAddress, 
        InetSocketAddress remoteAddress);

    /**
     * Called when connectivity checks have created a new valid pair.  
     * 
     * @param validPair The new valid pair.
     * @param generatingPair The pair that generated the valid pair.
     * @param useCandidate Whether or not the Binding Request for the check
     * included the USE CANDIDATE attribute.
     */
    void onValidPair(IceCandidatePair validPair, 
        IceCandidatePair generatingPair, boolean useCandidate);

    /**
     * Adds a pair to the triggered check queue.
     * 
     * @param pair The pair to add.
     */
    void addTriggeredCheck(IceCandidatePair pair);

    /**
     * Recomputes the priorities of pairs in checklists.  This can happen,
     * for example, if our role has changed from controlling to controlled or
     * vice versa.
     * @param controlling The current controlling status of the agent.
     */
    void recomputePairPriorities(boolean controlling);

    }
