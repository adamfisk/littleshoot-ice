package org.lastbamboo.common.ice.candidate;

import java.net.InetSocketAddress;
import java.net.Socket;

import org.lastbamboo.common.ice.IceCandidateType;
import org.lastbamboo.common.ice.IceCandidateVisitor;
import org.lastbamboo.common.ice.IceTransportProtocol;

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
     * Accessor for the priority of the candidate.
     * 
     * @return The priority of the candidate.
     */
    int getPriority();

    /**
     * Accessor for the type of transport of this candidate, such as TCP or
     * UDP.
     * 
     * @return The transport for this candidate.
     */
    IceTransportProtocol getTransport();

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

    /**
     * Gets the type of the ICE candidate.
     * 
     * @return The type of the ICE candidate.
     */
    IceCandidateType getType();

    /**
     * Accessor for the component ID of this candidate.  A component of a
     * candidate is the number of the component of the media stream it 
     * represents.  Many media streams will only have one component, starting
     * with "1", but others might have two or more, such as a media stream 
     * with RTP and RTCP.
     * 
     * @return The component ID.
     */
    int getComponentId();
    
    /**
     * Accessor for the candidate's foundation.
     * 
     * @return The candidate's foundation.
     */
    int getFoundation();

    }
