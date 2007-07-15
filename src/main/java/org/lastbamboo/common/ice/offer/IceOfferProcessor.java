package org.lastbamboo.common.ice.offer;

import java.io.IOException;

import org.apache.mina.common.ByteBuffer;
import org.lastbamboo.common.answer.AnswerGenerator;
import org.lastbamboo.common.offer.OfferProcessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * ICE implementation of offer processing for an offer/answer protocol such
 * as SIP.
 */
public class IceOfferProcessor implements OfferProcessor
    {
    
    private final Logger LOG = LoggerFactory.getLogger(getClass());
    private final AnswerGenerator m_iceAnswerGenerator;
    private static final ByteBuffer EMPTY_RESPONSE = ByteBuffer.allocate(0);
    
    /**
     * Creates a new offer processor.
     * 
     * @param answerGenerator The class for generating the ICE answer.
     */
    public IceOfferProcessor(final AnswerGenerator answerGenerator)
        {
        this.m_iceAnswerGenerator = answerGenerator;
        }
    
    public ByteBuffer answer(final ByteBuffer offer) throws IOException
        {
        if (!offer.hasRemaining())
            {
            LOG.warn("No SDP!!!");
            // TODO: Send an error response.  Technically, the invite does
            // not need to have SDP, as it can be in the ACK or the session
            // could not be using SDP at all, but our implementation always
            // includes SDP in the invite.
            return EMPTY_RESPONSE;
            }

        // TODO: We currently do nothing on the UAS side to process offers,
        // instead letting the UAC do all the work.  We just send the 
        // answer for now.
        final ByteBuffer answer = createAnswer();
        
        return answer;
        }

    private ByteBuffer createAnswer() throws IOException
        {
        final byte[] answer = this.m_iceAnswerGenerator.generateAnswer();
        return ByteBuffer.wrap(answer);
        }

    }
