package org.lastbamboo.common.ice;

import java.util.Collection;

import org.lastbamboo.common.ice.candidate.IceCandidatePair;

/**
 * A media stream for an ICE.
 */
public interface IceMediaStream
    {

    Collection<IceCandidatePair> getValidPairs();
    
    void addValidPair(IceCandidatePair pair);

    void connect();

    }
