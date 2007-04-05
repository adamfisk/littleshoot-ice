package org.lastbamboo.common.ice;

import java.net.InetSocketAddress;
import java.net.Socket;

import org.apache.commons.id.uuid.UUID;

/**
 * This is an interface for an ICE "candidate" as defined in 
 * the IETF draft "draft-ietf-mmusic-ice-05.txt".  A candidate is typically
 * a media-level attributed in SDP data transferred using SIP, but a 
 * node can learn of candidates using any other offer/answere protocol or
 * mode of describing the media.
 */
public interface IceCandidate
    {
    /**
     * Accessor for the address and port of the candidate.
     * @return The address and port of the candidate.
     */
    InetSocketAddress getSocketAddress();

    /**
     * Accessor the ID of the candidate.
     * 
     * @return The ID of the candidate.
     */
    int getCandidateId();

    /**
     * Accessor for the priority of the candidate.
     * 
     * @return The priority of the candidate.
     */
    int getPriority();

    /**
     * Accessor for the unique ID of the transport for the candidate.
     * @return The unique ID of the transport for the candidate.
     */
    UUID getTransportId();

    /**
     * Accessor for the type of transport of this candidate, such as TCP or
     * UDP.
     * @return The transport for this candidate.
     */
    String getTransport();

    /**
     * Accepts the specified visitor to an ICE candidate.
     * @param visitor The visitor to accept.
     */
    void accept(IceCandidateVisitor visitor);

    /**
     * Sets the socket.
     * 
     * @param sock The socket for the candidate.
     */
    void setSocket(Socket sock);

    /**
     * Accessor for the socket.
     * 
     * @return The socket.
     */
    Socket getSocket();
    }
