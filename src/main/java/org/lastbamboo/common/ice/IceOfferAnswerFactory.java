package org.lastbamboo.common.ice;

import org.littleshoot.mina.common.ByteBuffer;
import org.lastbamboo.common.offer.answer.MediaOfferAnswer;
import org.lastbamboo.common.offer.answer.OfferAnswer;
import org.lastbamboo.common.offer.answer.OfferAnswerConnectException;
import org.lastbamboo.common.offer.answer.OfferAnswerFactory;
import org.lastbamboo.common.offer.answer.OfferAnswerListener;
import org.lastbamboo.common.offer.answer.OfferAnswerMediaListener;

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
        final MediaOfferAnswer udp;
        try
            {
            udp = new IceAgentImpl(
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
        
        final MediaOfferAnswer tcp = new TcpMediaOfferAnswer();
        
        // We create a high-level class that starts a race between the TCP
        // and UDP connections. The TCP approach does not use ICE, instead
        // simplifying things significantly through using straight sockets, 
        // either via UPnP, directly over an internal network, or when one of
        // the peers is on the public Internet.
        return new MediaOfferAnswer() {
            
            public void processOffer(final ByteBuffer offer,
                final OfferAnswerListener offerAnswerListener) 
                {
                udp.processOffer(offer, offerAnswerListener);
                }
            
            public void processAnswer(final ByteBuffer answer,
                final OfferAnswerListener offerAnswerListener) 
                {
                udp.processAnswer(answer, offerAnswerListener);
                }
            
            public byte[] generateOffer() 
                {
                return udp.generateOffer();
                }
            
            public byte[] generateAnswer() 
                {
                return udp.generateAnswer();
                }
            
            public void startMedia(final OfferAnswerMediaListener mediaListener) 
                {
                tcp.startMedia(mediaListener);
                udp.startMedia(mediaListener);
                }
            
            public void close() 
                {
                tcp.close();
                udp.close();
                }
            };
        }
    }
