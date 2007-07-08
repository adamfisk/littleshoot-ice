package org.lastbamboo.common.ice.sdp;

import java.util.Collection;

import org.apache.mina.common.ByteBuffer;
import org.lastbamboo.common.ice.candidate.IceCandidate;
import org.lastbamboo.common.sdp.api.SdpException;

/**
 * Factory for generating ICE candidate classes from SDP.
 */
public interface IceCandidateSdpDecoder
    {

    /**
     * Creates a new <code>Collection</code> of <code>IceCandidate</code>
     * classes from the specified SDP data.
     * 
     * @param buf The SDP data to create ICE candidates from.
     * @param controlling Whether or not to generate controlling candidates. 
     * @return A new <code>Collection</code> of ICE candidates.
     * @throws SdpException If there's an error parsing out a candidate from 
     * the SDP.
     */
    Collection<IceCandidate> decode(ByteBuffer buf, boolean controlling) 
        throws SdpException;
    }
