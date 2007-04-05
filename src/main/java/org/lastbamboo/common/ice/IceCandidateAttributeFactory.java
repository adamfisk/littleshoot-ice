package org.lastbamboo.common.ice;

import java.net.InetSocketAddress;

import org.lastbamboo.common.sdp.api.Attribute;

/**
 * Factory for creating SDP attributes for ICE candidates.
 */
public interface IceCandidateAttributeFactory
    {

    /**
     * Creates a new ICE candidate attribute for candidates specified simply as
     * using using TCP transport.
     * @param socketAddress The address and port of the candidate.
     * @param candidateId The ID of the candidate.
     * @param priority The priority of the candidate versus other candidates.
     * @return The new candidate encoded as an SDP attribute following the
     * ICE draft.
     */
    Attribute createTcpIceCandidateAttribute(
        final InetSocketAddress socketAddress, final int candidateId, 
        final int priority);

    /**
     * Creates a new ICE candidate attribute for candidates specified simply as
     * using using UDP transport.
     * @param socketAddress The address and port of the candidate.
     * @param candidateId The ID of the candidate.
     * @param priority The priority of the candidate versus other candidates.
     * @return The new candidate encoded as an SDP attribute following the
     * ICE draft.
     */
    Attribute createUdpIceCandidateAttribute(
        final InetSocketAddress socketAddress, final int candidateId, 
        final int priority);
    }
