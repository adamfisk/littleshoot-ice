package org.lastbamboo.common.ice;

import java.net.InetSocketAddress;
import java.util.Collection;

import org.lastbamboo.common.ice.candidate.IceCandidate;
import org.lastbamboo.common.ice.candidate.IceCandidatePair;

/**
 * A media stream for an ICE.
 */
public interface IceMediaStream
    {

    Collection<IceCandidatePair> getValidPairs();
    
    void addValidPair(IceCandidatePair pair);

    void connect();

    /**
     * Whether or not this side is controlling the stream.
     * @return <code>true</code> if this side controls the stream, otherwise
     * <code>false</code>.
     */
    boolean isControlling();

    IceCandidate getLocalCandidate(InetSocketAddress localAddress);

    void addLocalCandidate(IceCandidate localCandidate);
    
    IceCandidatePair getPair(InetSocketAddress localAddress, 
        InetSocketAddress remoteAddress);

    }
