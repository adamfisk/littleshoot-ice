package org.lastbamboo.common.ice;

import org.lastbamboo.common.turn.client.TurnClientListener;
import org.lastbamboo.common.util.mina.DemuxableProtocolCodecFactory;
import org.littleshoot.mina.common.IoHandler;
import org.littleshoot.mina.common.IoServiceListener;

/**
 * Factory for creating ICE media streams.
 */
public interface GeneralIceMediaStreamFactory
    {

    <T> IceMediaStream newIceMediaStream(IceMediaStreamDesc streamDesc,
        IceAgent iceAgent, DemuxableProtocolCodecFactory rudpCodecFactory, 
        Class<T> name, IoHandler protocolIoHandler, 
        TurnClientListener delegateListener,
        IoServiceListener udpServiceListener) 
        throws IceUdpConnectException;

    }
