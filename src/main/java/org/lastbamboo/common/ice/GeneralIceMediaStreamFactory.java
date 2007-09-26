package org.lastbamboo.common.ice;

import org.apache.mina.common.IoHandler;
import org.lastbamboo.common.turn.client.TurnClientListener;
import org.lastbamboo.common.util.mina.DemuxableProtocolCodecFactory;

public interface GeneralIceMediaStreamFactory
    {

    <T> IceMediaStream newIceMediaStream(IceMediaStreamDesc streamDesc,
        IceAgent iceAgent, DemuxableProtocolCodecFactory rudpCodecFactory, 
        Class<T> name, IoHandler protocolIoHandler, 
        TurnClientListener delegateListener);

    }
