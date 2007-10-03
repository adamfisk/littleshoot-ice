package org.lastbamboo.common.ice;

import org.apache.mina.common.IoServiceListener;
import org.apache.mina.common.IoSession;
import org.apache.mina.handler.StreamIoHandler;
import org.lastbamboo.common.ice.candidate.IceCandidate;
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
     * @param ioServiceListener Listener for MINA IO events.
     * @return The new STUN checking class.
     */
    /*
    IceStunChecker newUdpChecker(IceCandidate localCandidate, 
        IceCandidate remoteCandidate, 
        StunMessageVisitorFactory visitorFactory, 
        IoServiceListener ioServiceListener);
        
    
    IceStunChecker newUdpChecker(IceCandidate localCandidate, 
        IceCandidate remoteCandidate, IoSession ioSession, 
        StunMessageVisitorFactory messageVisitorFactory, 
        IoServiceListener ioServiceListener);
*/
    
    /*
    IceStunChecker newTcpChecker(IceCandidate localCandidate, 
        IceCandidate remoteCandidate, StreamIoHandler ioHandler, 
        StunMessageVisitorFactory messageVisitorFactory,
        IoServiceListener serviceListener);
    
    
    IceStunChecker newTcpChecker(IceCandidate localCandidate, 
        IceCandidate remoteCandidate, StreamIoHandler protocolIoHandler, 
        IoSession ioSession, StunMessageVisitorFactory messageVisitorFactory,
        IoServiceListener serviceListener);
        */


    IceStunChecker newChecker(IoSession session);

    
    }
