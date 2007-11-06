package org.lastbamboo.common.ice;

import org.apache.mina.common.IoSession;
import org.apache.mina.common.TransportType;
import org.lastbamboo.common.ice.transport.IceTcpStunChecker;
import org.lastbamboo.common.ice.transport.IceUdpStunChecker;
import org.lastbamboo.common.stun.stack.message.StunMessage;
import org.lastbamboo.common.stun.stack.transaction.StunTransactionTracker;

/**
 * Class for creating STUN checker factories for both UDP and TCP.  Each
 * media stream requires its own factory because the checkers are coupled to
 * data for that specific stream.
 */
public class IceStunCheckerFactoryImpl implements IceStunCheckerFactory
    {

    private final StunTransactionTracker<StunMessage> m_transactionTracker;

    /**
     * Creates a new factory.  The checkes the factory creates can be either
     * for UDP or TCP.
     * @param transactionTracker The class that keeps track of STUN 
     * transactions.
     */
    public IceStunCheckerFactoryImpl(
        final StunTransactionTracker<StunMessage> transactionTracker)
        {
        m_transactionTracker = transactionTracker;
        }

    public IceStunChecker newChecker(final IoSession session)
        {
        final TransportType type = session.getTransportType();
        final boolean isUdp = type.isConnectionless();
        if (isUdp)
            {
            return new IceUdpStunChecker(session, m_transactionTracker);
            }
        else
            {
            return new IceTcpStunChecker(session, m_transactionTracker);
            }
        }

    }
