package org.lastbamboo.common.ice;

import java.io.IOException;

/**
 * Interface for classes that generate ICE candidates.
 */
public interface IceCandidateGenerator
    {

    byte[] generateCandidates() throws IOException;

    }
