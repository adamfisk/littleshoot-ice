package org.lastbamboo.common.ice;

import org.littleshoot.mina.common.ByteBuffer;
import org.lastbamboo.common.offer.answer.MediaOfferAnswer;
import org.lastbamboo.common.offer.answer.OfferAnswer;
import org.lastbamboo.common.offer.answer.OfferAnswerConnectException;
import org.lastbamboo.common.offer.answer.OfferAnswerFactory;

/**
 * Class for creating ICE agents that process ICE offers and answers.
 */
public class IceOfferAnswerFactory implements OfferAnswerFactory
    {

    private final IceMediaStreamFactory m_mediaStreamFactory;
    private final IceMediaFactory m_mediaFactory;

    /**
     * Creates a new ICE agent factory.  The factory maintains a reference to
     * the TCP TURN client because the client holds a persistent connection
     * to the TURN server and is used across all ICE sessions.
     * 
     * @param mediaStreamFactory The factory for creating ICE media streams.
     * @param mediaFactory The factory for creating the ultimate media.
     */
    public IceOfferAnswerFactory(
        final IceMediaStreamFactory mediaStreamFactory,
        final IceMediaFactory mediaFactory)
        {
        m_mediaStreamFactory = mediaStreamFactory;
        m_mediaFactory = mediaFactory;
        }

    public OfferAnswer createOfferer() throws OfferAnswerConnectException
        {
        return createMediaOfferer();
        }
    
    public MediaOfferAnswer createAnswerer(final ByteBuffer offer) 
        throws OfferAnswerConnectException 
        {
        try
            {
            return new IceAgentImpl( 
                this.m_mediaStreamFactory, false, this.m_mediaFactory);
            }
        catch (final IceTcpConnectException e)
            {
            throw new OfferAnswerConnectException(
                "Could not create TCP connection", e);
            }
        catch (final IceUdpConnectException e)
            {
            throw new OfferAnswerConnectException(
                "Could not create UDP connection", e);
            }
        }

    public MediaOfferAnswer createMediaOfferer() 
        throws OfferAnswerConnectException 
        {
        try
            {
            return new IceAgentImpl(
                this.m_mediaStreamFactory, true, this.m_mediaFactory);
            }
        catch (final IceTcpConnectException e)
            {
            throw new OfferAnswerConnectException(
                "Could not create TCP connection", e);
            }
        catch (final IceUdpConnectException e)
            {
            throw new OfferAnswerConnectException(
                "Could not create UDP connection", e);
            }
        }
    }
