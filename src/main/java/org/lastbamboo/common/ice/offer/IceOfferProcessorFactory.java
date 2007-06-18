package org.lastbamboo.common.ice.offer;

import org.lastbamboo.common.ice.answer.IceAnswerGenerator;
import org.lastbamboo.common.offer.OfferProcessor;
import org.lastbamboo.common.offer.OfferProcessorFactory;

/**
 * Creates an ICE implementation of processing an offer for an offer/answer
 * protocol such as SIP.
 */
public class IceOfferProcessorFactory implements OfferProcessorFactory
    {

    private final IceAnswerGenerator m_answerGenerator;

    /**
     * Creates a new factory for processing ICE offer messages.
     * 
     * @param answerGenerator The class for creating an ICE answer.
     */
    public IceOfferProcessorFactory(final IceAnswerGenerator answerGenerator)
        {
        this.m_answerGenerator = answerGenerator;
        }
    
    public OfferProcessor createOfferProcessor()
        {
        return new IceOfferProcessor(this.m_answerGenerator);
        }

    }
