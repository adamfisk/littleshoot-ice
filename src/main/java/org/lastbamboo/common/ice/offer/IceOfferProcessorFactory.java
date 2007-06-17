package org.lastbamboo.common.ice.offer;

import org.lastbamboo.common.ice.sdp.SdpFactory;
import org.lastbamboo.common.offer.OfferProcessor;
import org.lastbamboo.common.offer.OfferProcessorFactory;

/**
 * Creates an ICE implementation of processing an offer for an offer/answer
 * protocol such as SIP.
 */
public class IceOfferProcessorFactory implements OfferProcessorFactory
    {

    private final SdpFactory m_sdpFactory;

    /**
     * Creates a new factory for processing ICE offer messages.
     * 
     * @param sdpFactory The factory for creating the SDP.
     */
    public IceOfferProcessorFactory(final SdpFactory sdpFactory)
        {
        this.m_sdpFactory = sdpFactory;
        }
    
    public OfferProcessor createOfferProcessor()
        {
        return new IceOfferProcessor(this.m_sdpFactory);
        }

    }
