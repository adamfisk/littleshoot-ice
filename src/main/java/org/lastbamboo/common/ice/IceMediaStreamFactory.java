package org.lastbamboo.common.ice;

import org.lastbamboo.common.stun.client.StunClient;

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
     * @param tcpTurnClient The TURN client for gathering TURN candidates.
     * @return The new ICE media stream.
     */
    IceMediaStream newStream(IceAgent iceAgent, StunClient tcpTurnClient);

    }
