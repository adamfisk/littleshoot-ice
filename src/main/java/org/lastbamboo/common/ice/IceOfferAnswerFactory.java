package org.lastbamboo.common.ice;

import org.lastbamboo.common.offer.answer.OfferAnswer;
import org.lastbamboo.common.offer.answer.OfferAnswerFactory;
import org.lastbamboo.common.stun.client.StunClient;

/**
 * Class for creating ICE agents that process ICE offers and answers.
 */
public class IceOfferAnswerFactory implements OfferAnswerFactory
    {

    private final StunClient m_tcpTurnClient;

    /**
     * Creates a new ICE agent factory.  The factory maintains a reference to
     * the TCP TURN client because the client holds a persistent connection
     * to the TURN server and is used across all ICE sessions.
     * 
     * @param tcpTurnClient The persistent TCP TURN client.
     */
    public IceOfferAnswerFactory(final StunClient tcpTurnClient)
        {
        m_tcpTurnClient = tcpTurnClient;
        }

    public OfferAnswer createOfferer()
        {
        return new IceAgentImpl(this.m_tcpTurnClient, true);
        }
    
    public OfferAnswer createAnswerer()
        {
        return new IceAgentImpl(this.m_tcpTurnClient, false);
        }
    }
