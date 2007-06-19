package org.lastbamboo.common.ice.sdp;

import java.util.Collection;

import org.lastbamboo.common.ice.IceCandidate;
import org.lastbamboo.common.sdp.api.SdpException;
import org.lastbamboo.common.sdp.api.SessionDescription;

/**
 * Factory for generating ICE candidate classes from SDP.
 */
public interface IceCandidateSdpDecoder
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
    Collection<IceCandidate> decode(SessionDescription sdp) throws SdpException;
    }
