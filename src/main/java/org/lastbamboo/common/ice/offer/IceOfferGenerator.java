package org.lastbamboo.common.ice.offer;

import java.io.IOException;
import java.util.Collection;

import org.lastbamboo.common.ice.IceCandidateGenerator;
import org.lastbamboo.common.ice.candidate.IceCandidate;
import org.lastbamboo.common.ice.sdp.IceCandidateSdpEncoder;
import org.lastbamboo.common.offer.OfferGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class that generates offers for ICE.  In ICE, the offer is generated 
 * through gathering ICE candidates on all available interfaces.
 */
public class IceOfferGenerator implements OfferGenerator
    {
    
    private final Logger LOG = LoggerFactory.getLogger(getClass());
    
    private final IceCandidateGenerator m_candidateGenerator;
    
    /**
     * Creates a new class for creating ICE answers.
     * 
     * @param candidateGenerator Class for generating ICE candidates 
     */
    public IceOfferGenerator(final IceCandidateGenerator candidateGenerator)
        {
        m_candidateGenerator = candidateGenerator;
        }

    public byte[] generateOffer() throws IOException 
        {
        final Collection<IceCandidate> candidates = 
            this.m_candidateGenerator.generateCandidates(true);
        final IceCandidateSdpEncoder encoder = new IceCandidateSdpEncoder();
        encoder.visitCandidates(candidates);
        
        final byte[] sdp = encoder.getSdp();
        return sdp;
        }

    }
