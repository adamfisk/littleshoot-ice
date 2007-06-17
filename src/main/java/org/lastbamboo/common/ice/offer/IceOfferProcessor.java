package org.lastbamboo.common.ice.offer;

import java.io.UnsupportedEncodingException;

import org.apache.mina.common.ByteBuffer;
import org.lastbamboo.common.ice.sdp.SdpFactory;
import org.lastbamboo.common.offer.OfferProcessor;
import org.lastbamboo.common.sdp.api.SdpException;
import org.lastbamboo.common.sdp.api.SessionDescription;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * ICE implementation of offer processing for an offer/answer protocol such
 * as SIP.
 */
public class IceOfferProcessor implements OfferProcessor
    {
    
    private final Logger LOG = LoggerFactory.getLogger(getClass());
    private final SdpFactory m_sdpFactory;
    private static final ByteBuffer EMPTY_RESPONSE = ByteBuffer.allocate(0);
    
    /**
     * Creates a new offer processor.
     * 
     * @param sdpFactory The factory for generating the SDP answer.
     */
    public IceOfferProcessor(final SdpFactory sdpFactory)
        {
        this.m_sdpFactory = sdpFactory;
        }
    
    public ByteBuffer answer(final ByteBuffer offer)
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
        return createAnswer();
        }

    private ByteBuffer createAnswer()
        {
        // Create the SDP.
        final SessionDescription sdpData;
        try
            {
            sdpData = this.m_sdpFactory.createSdp();
            }
        catch (final SdpException e)
            {
            // This should not happen when we're generating our own SDP.
            LOG.error("Failed to Generate an SDP description", e);
            return EMPTY_RESPONSE;
            }
        
        try
            {
            final ByteBuffer answer = 
                ByteBuffer.wrap(sdpData.toString().getBytes("US-ASCII"));
            return answer;
            }
        catch (final UnsupportedEncodingException e)
            {
            LOG.error("Unsupported encoding??", e);
            return EMPTY_RESPONSE;
            }
        }

    }
