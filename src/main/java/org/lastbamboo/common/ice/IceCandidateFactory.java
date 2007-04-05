package org.lastbamboo.common.ice;

import java.util.Collection;

import org.lastbamboo.common.sdp.api.SdpException;
import org.lastbamboo.common.sdp.api.SessionDescription;

/**
 * Factory for generating ICE candidate classes.
 */
public interface IceCandidateFactory
    {

    /**
     * Creates a new <code>Collection</code> of <code>IceCandidate</code>
     * classes from the specified SDP data.
     * 
     * @param sdp The SDP data to create ICE candidates from.
     * @return A new <code>Collection</code> of ICE candidates.
     * @throws SdpException If there's an error parsing out a candidate from 
     * the SDP.
     */
    Collection createCandidates(final SessionDescription sdp) 
        throws SdpException;
    }
