package org.lastbamboo.common.ice;

import org.apache.mina.common.IoServiceListener;
import org.apache.mina.common.IoSession;
import org.apache.mina.handler.StreamIoHandler;
import org.lastbamboo.common.ice.candidate.IceCandidate;
import org.lastbamboo.common.ice.candidate.IceCandidatePair;
import org.lastbamboo.common.ice.candidate.TcpIceCandidatePair;
import org.lastbamboo.common.stun.stack.message.StunMessageVisitorFactory;
import org.lastbamboo.common.stun.stack.transaction.StunTransactionTracker;

public class IceTcpStunConnectivityChecker<T> extends
    AbstractIceStunConnectivityChecker<T>
    {

    private final StreamIoHandler m_streamIoHandler;

    public IceTcpStunConnectivityChecker(final IceAgent agent, 
        final IceMediaStream iceMediaStream, final IoSession session, 
        final StunTransactionTracker<T> transactionTracker, 
        final IceStunCheckerFactory checkerFactory, 
        final StunMessageVisitorFactory stunMessageVisitorFactory,
        final StreamIoHandler streamHandler)
        {
        super(agent, iceMediaStream, session, transactionTracker, checkerFactory,
            stunMessageVisitorFactory);
        this.m_streamIoHandler = streamHandler;
        }

    @Override
    protected IceCandidatePair newPair(final IceCandidate localCandidate, 
        final IceCandidate remoteCandidate, final IoSession ioSession, 
        final StunMessageVisitorFactory messageVisitorFactory,
        final IoServiceListener serviceListener)
        {
        // The request just arrived on an existing TCP session.  We
        // cannot create a new TCP connection from this acceptor to
        // the remote connector because the remote side is using a 
        // client and not an accepting socket, so we need to use
        // the existing connection.
        final IceStunChecker connectivityChecker =
            this.m_checkerFactory.newTcpChecker(localCandidate,
                remoteCandidate, this.m_streamIoHandler, ioSession, 
                messageVisitorFactory, serviceListener);

        return new TcpIceCandidatePair(localCandidate,
            remoteCandidate, connectivityChecker);
        }

    }
