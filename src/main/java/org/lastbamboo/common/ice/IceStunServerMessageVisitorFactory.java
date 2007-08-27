package org.lastbamboo.common.ice;

import org.apache.mina.common.IoSession;
import org.lastbamboo.common.stun.stack.message.StunMessageVisitor;
import org.lastbamboo.common.stun.stack.message.StunMessageVisitorFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Factory for creating STUN message visitors for ICE clients.  This visitors
 * are somewhat unique in that each client must handle both "client" and 
 * "server" side messages in ICE.
 */
public class IceStunServerMessageVisitorFactory 
    implements StunMessageVisitorFactory
    {
    
    private final Logger m_log = LoggerFactory.getLogger(getClass());
    private final IceAgent m_iceAgent;
    private final IceMediaStream m_iceMediaStream;
    private final IceUdpStunCheckerFactory m_udpStunCheckerFactory;

    /**
     * Creates a new STUN message visitor factory for ICE.
     * 
     * @param agent The top-level agent. 
     * @param iceMediaStream The media stream this factory is working for.
     */
    public IceStunServerMessageVisitorFactory(
        final IceAgent agent, final IceMediaStream iceMediaStream,
        final IceUdpStunCheckerFactory udpStunCheckerFactory)
        {
        m_iceAgent = agent;
        m_iceMediaStream = iceMediaStream;
        m_udpStunCheckerFactory = udpStunCheckerFactory;
        }

    public StunMessageVisitor createVisitor(final IoSession session)
        {
        return new IceStunServerConnectivityCheckerImpl( 
            this.m_iceAgent, this.m_iceMediaStream, 
                this.m_udpStunCheckerFactory, session);
        }

    }
