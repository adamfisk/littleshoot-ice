package org.lastbamboo.common.ice;

import java.net.Socket;

import org.apache.mina.common.ByteBuffer;
import org.lastbamboo.common.ice.stubs.StunClientStub;
import org.lastbamboo.common.offer.answer.OfferAnswer;
import org.lastbamboo.common.offer.answer.OfferAnswerFactory;
import org.lastbamboo.common.stun.client.StunClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import junit.framework.TestCase;

/**
 * Test connections between ICE agents.
 */
public class IceAgentImplTest extends TestCase
    {

    private final Logger m_log = LoggerFactory.getLogger(getClass());
    
    /**
     * Tests creating a local UDP connection using ICE.
     * 
     * @throws Exception If any unexpected error occurs.
     */
    public void testLocalUdpConnection() throws Exception
        {
        final IceMediaStreamDesc desc = 
            new IceMediaStreamDesc(false, true, "message", "http", 1);
        
        final StunClient tcpTurnClient = new StunClientStub();
        final IceMediaStreamFactory mediaStreamFactory = 
            new IceMediaStreamFactoryImpl(desc);
        
        final OfferAnswerFactory factory = 
            new IceOfferAnswerFactory(tcpTurnClient, mediaStreamFactory);
        
        final OfferAnswer offerer = factory.createOfferer();
        final byte[] offer = offerer.generateOffer();
        
        final OfferAnswer answerer = 
            factory.createAnswerer(ByteBuffer.wrap(offer));
        
        m_log.debug("Sending offer: {}", new String(offer));
        
        final byte[] answer = answerer.generateAnswer();
        
        m_log.debug("Processing answer: {}", new String(answer));
        
        final Socket sock = offerer.createSocket(ByteBuffer.wrap(answer));
        
        
        
        assertNotNull(sock);
        }
    }
