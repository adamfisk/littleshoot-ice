package org.lastbamboo.common.ice.answer;

import java.io.IOException;

import org.lastbamboo.common.answer.AnswerGenerator;
import org.lastbamboo.common.ice.IceCandidateGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class that generates answers for ICE.  In ICE, the answer is generated 
 * through gathering ICE candidates on all available interfaces.
 */
public class IceAnswerGenerator implements AnswerGenerator
    {
    
    private final Logger LOG = LoggerFactory.getLogger(getClass());
    private final IceCandidateGenerator m_candidateGenerator;
    
    /**
     * Creates a new class for creating ICE answers.
     * 
     * @param candidateGenerator Class for generating ICE candidates 
     */
    public IceAnswerGenerator(final IceCandidateGenerator candidateGenerator)
        {
        m_candidateGenerator = candidateGenerator;
        }

    public byte[] generateAnswer() throws IOException 
        {
        return this.m_candidateGenerator.generateCandidates();
        }

    }
