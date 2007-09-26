package org.lastbamboo.common.ice;

import org.apache.mina.common.IoServiceListener;
import org.apache.mina.common.IoSession;
import org.lastbamboo.common.ice.candidate.IceCandidate;
import org.lastbamboo.common.ice.candidate.IceCandidatePair;
import org.lastbamboo.common.ice.candidate.UdpIceCandidatePair;
import org.lastbamboo.common.stun.stack.message.StunMessageVisitorFactory;
import org.lastbamboo.common.stun.stack.transaction.StunTransactionTracker;

public class IceUdpStunConnectivityChecker<T> extends
    AbstractIceStunConnectivityChecker<T>
    {

    public IceUdpStunConnectivityChecker(final IceAgent agent, 
        final IceMediaStream iceMediaStream, final IoSession session, 
        final StunTransactionTracker transactionTracker, 
        final IceStunCheckerFactory checkerFactory, 
        final StunMessageVisitorFactory stunMessageVisitorFactory)
        {
        super(agent, iceMediaStream, session, transactionTracker, checkerFactory,
            stunMessageVisitorFactory);
        }

    @Override
    protected IceCandidatePair newPair(final IceCandidate localCandidate, 
        final IceCandidate remoteCandidate, final IoSession ioSession, 
        final StunMessageVisitorFactory messageVisitorFactory,
        final IoServiceListener ioServiceListener)
        {
        final IceStunChecker connectivityChecker = 
            this.m_checkerFactory.newUdpChecker(localCandidate, 
                remoteCandidate, messageVisitorFactory, ioServiceListener);
        return new UdpIceCandidatePair(localCandidate, 
            remoteCandidate, connectivityChecker);
        }
    }
