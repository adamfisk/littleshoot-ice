package org.lastbamboo.common.ice;

import org.apache.mina.common.IoSession;
import org.apache.mina.handler.StreamIoHandler;
import org.lastbamboo.common.ice.candidate.IceCandidate;
import org.lastbamboo.common.stun.stack.message.StunMessageVisitorFactory;

/**
 * Interface for classes that create new ICE STUN connectivity check classes
 * for different transports. 
 */
public interface IceStunCheckerFactory<T>
    {

    /**
     * Creates a new STUN checker from the local candidate to the remote
     * candidate.
     * 
     * @param localCandidate The local candidate for a pair.
     * @param remoteCandidate The remote candidate for a pair.
     * @return The new STUN checking class.
     */
    IceStunChecker newUdpChecker(IceCandidate localCandidate, 
        IceCandidate remoteCandidate, 
        StunMessageVisitorFactory<T> visitorFactory);

    IceStunChecker newTcpChecker(IceCandidate localCandidate, 
        IceCandidate remoteCandidate, StreamIoHandler ioHandler, 
        StunMessageVisitorFactory<T> messageVisitorFactory);
    
    
    IceStunChecker newTcpChecker(IceCandidate localCandidate, 
        IceCandidate remoteCandidate, StreamIoHandler protocolIoHandler, 
        IoSession ioSession, 
        StunMessageVisitorFactory<T> messageVisitorFactory);

    }
