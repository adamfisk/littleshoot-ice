package org.lastbamboo.common.ice;

import java.io.IOException;

/**
 * Interface for classes that generate ICE candidates.
 */
public interface IceCandidateGenerator
    {

    /**
     * Creates ICE candidates encoded in SDP.
     * 
     * @return The ICE candiates encoded in SDP.
     * @throws IOException If there's any error generating the candidates.
     */
    byte[] generateCandidates() throws IOException;

    }
