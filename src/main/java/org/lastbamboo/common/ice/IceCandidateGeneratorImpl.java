package org.lastbamboo.common.ice;

import java.io.IOException;
import java.util.Collection;

import org.lastbamboo.common.ice.candidate.IceCandidate;
import org.lastbamboo.common.stun.client.StunClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class for generating TCP ICE candidates.  
 */
public class IceCandidateGeneratorImpl implements IceCandidateGenerator
    {

    private final Logger LOG = LoggerFactory.getLogger(getClass());
    
    private final StunClient m_turnClient;

    /**
     * Creates a new factory instance.
     * 
     * @param turnClient The TURN client for generating TURN candidates.
     */
    public IceCandidateGeneratorImpl(final StunClient turnClient)
        {
        m_turnClient = turnClient;
        }

    public Collection<IceCandidate> generateCandidates(
        final boolean controlling) throws IOException
        {
        // First, gather all the candidates.
        final IceCandidateGatherer gatherer = 
            new IceCandidateGathererImpl(this.m_turnClient, controlling);
        final Collection<IceCandidate> candidates = gatherer.gatherCandidates();
        
        return candidates;
        }
    
        
    }
