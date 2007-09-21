package org.lastbamboo.common.ice;


/**
 * Factory for creating specialized media streams, such as for RTP, file
 * transfer, etc.
 */
public interface IceMediaStreamFactory
    {

    /**
     * Creates a new ICE media stream class.
     * 
     * @param iceAgent The ICE agent.
     * @return The new ICE media stream.
     */
    IceMediaStream newStream(IceAgent iceAgent);

    }
