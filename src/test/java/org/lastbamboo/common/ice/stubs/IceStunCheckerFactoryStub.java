package org.lastbamboo.common.ice.stubs;

import org.apache.mina.common.IoServiceListener;
import org.apache.mina.common.IoSession;
import org.apache.mina.handler.StreamIoHandler;
import org.lastbamboo.common.ice.IceStunChecker;
import org.lastbamboo.common.ice.IceStunCheckerFactory;
import org.lastbamboo.common.ice.candidate.IceCandidate;
import org.lastbamboo.common.stun.stack.message.StunMessageVisitorFactory;

public class IceStunCheckerFactoryStub implements IceStunCheckerFactory
    {

    public IceStunChecker newTcpChecker(IceCandidate localCandidate,
            IceCandidate remoteCandidate, StreamIoHandler ioHandler,
            StunMessageVisitorFactory messageVisitorFactory,
            IoServiceListener serviceListener)
        {
        // TODO Auto-generated method stub
        return null;
        }

    public IceStunChecker newTcpChecker(IceCandidate localCandidate,
            IceCandidate remoteCandidate, StreamIoHandler protocolIoHandler,
            IoSession ioSession,
            StunMessageVisitorFactory messageVisitorFactory,
            IoServiceListener serviceListener)
        {
        // TODO Auto-generated method stub
        return null;
        }

    public IceStunChecker newUdpChecker(IceCandidate localCandidate,
            IceCandidate remoteCandidate,
            StunMessageVisitorFactory visitorFactory,
            IoServiceListener ioServiceListener)
        {
        // TODO Auto-generated method stub
        return null;
        }

    }
