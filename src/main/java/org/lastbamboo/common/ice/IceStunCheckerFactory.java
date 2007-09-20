package org.lastbamboo.common.ice;

import org.apache.mina.common.IoHandler;
import org.apache.mina.common.IoSession;
import org.lastbamboo.common.ice.candidate.IceCandidate;
import org.lastbamboo.common.stun.stack.message.StunMessage;
import org.lastbamboo.common.stun.stack.message.StunMessageVisitorFactory;

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
        IceCandidate remoteCandidate, 
        StunMessageVisitorFactory<StunMessage> visitorFactory);

    IceStunChecker createStunChecker(IceCandidate localCandidate, 
        IceCandidate remoteCandidate, IoHandler ioHandler, 
        StunMessageVisitorFactory<StunMessage> messageVisitorFactory);

    IceStunChecker createStunChecker(IceCandidate localCandidate, 
        IceCandidate remoteCandidate, IoHandler ioHandler, IoSession session, 
        StunMessageVisitorFactory<StunMessage> messageVisitorFactory);

    }
