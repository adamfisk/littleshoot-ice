package org.lastbamboo.common.ice;

import java.io.IOException;

import org.apache.mina.common.ByteBuffer;
import org.lastbamboo.common.offer.answer.OfferAnswer;
import org.lastbamboo.common.offer.answer.OfferAnswerFactory;
import org.lastbamboo.common.offer.answer.MediaOfferAnswer;
import org.lastbamboo.common.stun.client.StunClient;

/**
 * Class for creating ICE agents that process ICE offers and answers.
 */
public class IceOfferAnswerFactory implements OfferAnswerFactory
    {

    private final StunClient m_tcpTurnClient;
    private final IceMediaStreamFactory m_mediaStreamFactory;
    private final IceMediaFactory m_mediaFactory;

    /**
     * Creates a new ICE agent factory.  The factory maintains a reference to
     * the TCP TURN client because the client holds a persistent connection
     * to the TURN server and is used across all ICE sessions.
     * 
     * @param tcpTurnClient The persistent TCP TURN client.
     * @param mediaStreamFactory The factory for creating ICE media streams.
     * @param mediaFactory The factory for creating the ultimate media.
     */
    public IceOfferAnswerFactory(final StunClient tcpTurnClient,
        final IceMediaStreamFactory mediaStreamFactory,
        final IceMediaFactory mediaFactory)
        {
        m_tcpTurnClient = tcpTurnClient;
        m_mediaStreamFactory = mediaStreamFactory;
        m_mediaFactory = mediaFactory;
        }

    public OfferAnswer createOfferer()
        {
        return createMediaOfferer();
        }
    
    public MediaOfferAnswer createAnswerer(final ByteBuffer offer) throws IOException
        {
        return new IceAgentImpl(this.m_tcpTurnClient, 
            this.m_mediaStreamFactory, false, this.m_mediaFactory);
        }

    public MediaOfferAnswer createMediaOfferer()
        {
        return new IceAgentImpl(this.m_tcpTurnClient, 
            this.m_mediaStreamFactory, true, this.m_mediaFactory);
        }
    }
