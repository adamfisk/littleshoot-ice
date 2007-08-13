package org.lastbamboo.common.ice;

import org.lastbamboo.common.stun.client.StunClient;

/**
 * Factory for creating specialized media streams, such as for RTP, file
 * transfer, etc.
 */
public interface IceMediaStreamFactory
    {

    IceMediaStream newStream(IceAgent iceAgent, StunClient tcpTurnClient);

    }
