package org.lastbamboo.common.ice;

import java.io.IOException;
import java.util.Collection;

import org.lastbamboo.common.ice.candidate.IceCandidate;
import org.lastbamboo.common.ice.sdp.IceCandidateSdpEncoder;
import org.lastbamboo.common.turn.client.TurnClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class for generating TCP ICE candidates.  
 */
public class IceCandidateGeneratorImpl implements IceCandidateGenerator
    {

    private final Logger LOG = LoggerFactory.getLogger(getClass());
    
    private final TurnClient m_turnClient;

    /**
     * Creates a new factory instance.
     * 
     * @param turnClient The TURN client for generating TURN candidates.
     */
    public IceCandidateGeneratorImpl(final TurnClient turnClient)
        {
        m_turnClient = turnClient;
        }

    public byte[] generateCandidates() throws IOException
        {
        // First, gather all the candidates.
        final IceCandidateGatherer gatherer = 
            new IceCandidateGathererImpl(this.m_turnClient);
        final Collection<IceCandidate> candidates = gatherer.gatherCandidates();
        
        // Then encode the gathered candidates in SDP.
        final IceCandidateSdpEncoder encoder = new IceCandidateSdpEncoder();
        encoder.visitCandidates(candidates);
        return encoder.getSdp();
        }
    
        
    }
