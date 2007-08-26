package org.lastbamboo.common.ice;

import org.lastbamboo.common.ice.candidate.IceCandidate;

/**
 * Interface for classes that create new ICE UDP STUN connectivity check
 * classes. 
 */
public interface IceUdpStunCheckerFactory
    {

    IceStunChecker createStunChecker(IceCandidate localCandidate, 
        IceCandidate remoteCandidate);

    }
