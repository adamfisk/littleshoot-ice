package org.lastbamboo.common.ice;

import java.io.IOException;
import java.util.Collection;

import org.lastbamboo.common.ice.candidate.IceCandidate;

/**
 * Interface for classes that generate ICE candidates.
 */
public interface IceCandidateGenerator
    {

    /**
     * Creates ICE candidates encoded in SDP.
     * 
     * @param controlling Whether or not this candidate is the controlling
     * candidate. 
     * @return The ICE candiates.
     * @throws IOException If there's any error generating the candidates.
     */
    Collection<IceCandidate> generateCandidates(boolean controlling) 
        throws IOException;

    }
