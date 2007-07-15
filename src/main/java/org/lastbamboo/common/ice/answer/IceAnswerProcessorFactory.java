package org.lastbamboo.common.ice.answer;

import org.lastbamboo.common.answer.AnswerProcessorFactory;
import org.lastbamboo.common.ice.sdp.IceCandidateSdpDecoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * ICE implementation of answer processing for an offer/answer protocol such
 * as SIP.
 */
public class IceAnswerProcessorFactory implements AnswerProcessorFactory
    {
    
    private final Logger LOG = LoggerFactory.getLogger(getClass());
    
    private final IceCandidateSdpDecoder m_iceCandidateDecoder;

    /**
     * Creates a new answer processor factory.
     * 
     * @param iceCandidateDecoder The ICE candidates decorder from SDP.
     */
    public IceAnswerProcessorFactory(
        final IceCandidateSdpDecoder iceCandidateDecoder)
        {
        m_iceCandidateDecoder = iceCandidateDecoder;
        }

    public IceAnswerProcessor createProcessor()
        {
        return new IceAnswerProcessor(this.m_iceCandidateDecoder);
        }
    }
