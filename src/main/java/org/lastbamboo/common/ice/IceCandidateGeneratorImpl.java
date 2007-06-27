package org.lastbamboo.common.ice;

import java.io.IOException;
import java.util.Collection;

import org.lastbamboo.common.ice.sdp.IceCandidateSdpEncoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class for generating TCP ICE candidates.  For a discussion of the priorities
 * associated with each candidate type and for a general discussion of ICE,
 * this was written based on draft-ietf-mmusic-ice-16 at:<p>
 * 
 * http://www.tools.ietf.org/html/draft-ietf-mmusic-ice-16
 * 
 * <p>
 * 
 * Here's the augmented BNF for candidates:
 * 
 * candidate-attribute   = "candidate" ":" foundation SP component-id SP
 *                         transport SP
 *                         priority SP
 *                         connection-address SP     ;from RFC 4566
 *                         port         ;port from RFC 4566
 *                         SP cand-type
 *                         [SP rel-addr]
 *                         [SP rel-port]
 *                         *(SP extension-att-name SP
 *                              extension-att-value)
 *                              
 * foundation            = 1*32ice-char
 * component-id          = 1*5DIGIT
 * transport             = "UDP" / transport-extension
 * transport-extension   = token              ; from RFC 3261
 * priority              = 1*10DIGIT
 * cand-type             = "typ" SP candidate-types
 * candidate-types       = "host" / "srflx" / "prflx" / "relay" / token
 * rel-addr              = "raddr" SP connection-address
 * rel-port              = "rport" SP port
 * extension-att-name    = byte-string    ;from RFC 4566
 * extension-att-value   = byte-string
 * ice-char              = ALPHA / DIGIT / "+" / "/"
 */
public class IceCandidateGeneratorImpl implements IceCandidateGenerator
    {

    private final Logger LOG = LoggerFactory.getLogger(getClass());
    
    private final BindingTracker m_bindingTracker;

    /**
     * Creates a new factory instance.
     * 
     * @param tracker The tracker for accessing all available transport 
     * bindings.
     */
    public IceCandidateGeneratorImpl(final BindingTracker tracker)
        {
        this.m_bindingTracker = tracker;
        }

    public byte[] generateCandidates() throws IOException
        {
        // First, gather all the candidates.
        final IceCandidateGatherer gatherer = 
            new IceCandidateGathererImpl(this.m_bindingTracker);
        final Collection<IceCandidate> candidates = gatherer.gatherCandidates();
        
        // Then encode the gathered candidates in SDP.
        final IceCandidateSdpEncoder encoder = new IceCandidateSdpEncoder();
        encoder.visitCandidates(candidates);
        return encoder.getSdp();
        }
    
        
    }
