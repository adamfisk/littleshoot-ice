package org.lastbamboo.common.ice.sdp;

import org.lastbamboo.common.sdp.api.SdpException;
import org.lastbamboo.common.sdp.api.SdpParseException;
import org.lastbamboo.common.sdp.api.SessionDescription;

/**
 * Factory for creating SDP messages.
 */
public interface SdpFactory
    {

    /**
     * Creates an SDP string for this host.  The SDP will likely use information
     * from STUN and TURN servers to report valid addresses on which to contact
     * this host.
     * 
     * @return The SDP string for this host.
     * @throws SdpException If we could not create the SDP for any reason.
     */
    SessionDescription createSdp() throws SdpException;

    /**
     * Creates a new SDP data instance using the specified data typically read
     * from the network.
     * @param sdpData The SDP string data to create a description from.
     * @return The new SDP data instance.
     * @throws SdpParseException If there's an error parsing the SDP string.
     */
    SessionDescription createSdp(final String sdpData) 
        throws SdpParseException;

    }
