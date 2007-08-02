package org.lastbamboo.common.ice;

import java.net.InetSocketAddress;
import java.util.Collection;

import org.lastbamboo.common.ice.candidate.IceCandidate;
import org.lastbamboo.common.ice.candidate.IceCandidatePair;
import org.lastbamboo.common.stun.stack.message.BindingRequest;

/**
 * A media stream for an ICE.
 */
public interface IceMediaStream
    {

    Collection<IceCandidatePair> getValidPairs();
    
    void addValidPair(IceCandidatePair pair);


    /**
     * Accessor for the remote candidate associate with the specified remote
     * address.
     * 
     * @param remoteAddress The remote address to look for.
     * @return The candidate associated with the specified remote address,
     * or <code>null</code> if there's no associated candidate.
     */
    IceCandidate getRemoteCandidate(InetSocketAddress remoteAddress);
 
    /**
     * Accessor for the local candidate associate with the specified local
     * address.
     * 
     * @param localAddress The local address to look for.
     * @return The candidate associated with the specified local address,
     * or <code>null</code> if there's no associated candidate.
     */
    IceCandidate getLocalCandidate(InetSocketAddress localAddress);

    void addLocalCandidate(IceCandidate localCandidate);
    
    IceCandidatePair getPair(InetSocketAddress localAddress, 
        InetSocketAddress remoteAddress);

    /**
     * Called when connectivity checks have created a new valid pair.  
     * 
     * @param validPair The new valid pair.
     * @param generatingPair The pair that generated the valid pair.
     * @param useCandidate Whether or not the Binding Request for the check
     * included the USE CANDIDATE attribute.
     */
    void onValidPair(IceCandidatePair validPair, 
        IceCandidatePair generatingPair, boolean useCandidate);

    /**
     * Adds a pair to the triggered check queue.
     * 
     * @param pair The pair to add.
     */
    void addTriggeredCheck(IceCandidatePair pair);

    /**
     * Recomputes the priorities of pairs in checklists.  This can happen,
     * for example, if our role has changed from controlling to controlled or
     * vice versa.
     * @param controlling The current controlling status of the agent.
     */
    void recomputePairPriorities(boolean controlling);

    /**
     * Establishes a media stream using the answer data from the remote host.
     * 
     * @param remoteCandidates The answer from the remote host.
     */
    void establishStream(Collection<IceCandidate> remoteCandidates);

    /**
     * Checks whether or not the specified remote address matches any of
     * the addresses of remote candidates.  This is typically used when
     * checking for peer reflexive candidates.  If it's an address we don't 
     * know about, it's typically a new peer reflexive candidate.
     * 
     * @param remoteAddress The remote address to check.
     * @return <code>true</code> if the address matches the address of a 
     * remote candidate we already know about, otherwise <code>false</code>.
     */
    boolean hasRemoteCandidate(InetSocketAddress remoteAddress);

    /**
     * Adds a peer reflexive candidate to the list of remote candidates.
     * 
     * @param request The {@link BindingRequest} that initiated the 
     * establishment of a new peer reflexive candidate.
     * @param localAddress The local address the request was sent to.  This
     * allows us to match the local address with the local candidate it was
     * sent to.  We use that to determine the component ID of the new peer
     * reflexive candidate. 
     * @param remoteAddress The remote address of the peer that sent the 
     * Binding Request.
     * @return The new peer reflexive candidate.
     */
    IceCandidate addPeerReflexive(BindingRequest request, 
        InetSocketAddress localAddress, InetSocketAddress remoteAddress);
    
    /**
     * Encodes this media stream in SDP.
     * 
     * @return The media stream encoded in SDP.
     */
    byte[] encodeCandidates();
    
    }
