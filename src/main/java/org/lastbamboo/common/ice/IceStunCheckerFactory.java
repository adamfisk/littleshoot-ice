package org.lastbamboo.common.ice;

import org.apache.mina.common.IoHandler;
import org.apache.mina.common.IoSession;
import org.lastbamboo.common.ice.candidate.IceCandidate;

/**
 * Interface for classes that create new ICE STUN connectivity check classes
 * for different transports. 
 */
public interface IceStunCheckerFactory
    {

    /**
     * Creates a new STUN checker from the local candidate to the remote
     * candidate.
     * 
     * @param localCandidate The local candidate for a pair.
     * @param remoteCandidate The remote candidate for a pair.
     * @return The new STUN checking class.
     */
    IceStunChecker createStunChecker(IceCandidate localCandidate, 
            IceCandidate remoteCandidate);
    
    IceStunChecker createStunChecker(IceCandidate localCandidate, 
        IceCandidate remoteCandidate, IoSession session);

    IceStunChecker createStunChecker(IceCandidate localCandidate, 
        IceCandidate remoteCandidate, IoHandler ioHandler);

    IceStunChecker createStunChecker(IceCandidate localCandidate, 
        IceCandidate remoteCandidate, IoHandler ioHandler, IoSession session);

    }
