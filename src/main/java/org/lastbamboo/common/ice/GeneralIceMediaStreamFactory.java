package org.lastbamboo.common.ice;

import org.apache.mina.common.IoHandler;
import org.lastbamboo.common.turn.client.TurnClientListener;
import org.lastbamboo.common.upnp.UpnpManager;
import org.lastbamboo.common.util.mina.DemuxableProtocolCodecFactory;

/**
 * Factory for creating ICE media streams.
 */
public interface GeneralIceMediaStreamFactory
    {

    <T> IceMediaStream newIceMediaStream(IceMediaStreamDesc streamDesc,
        IceAgent iceAgent, DemuxableProtocolCodecFactory rudpCodecFactory, 
        Class<T> name, IoHandler protocolIoHandler, 
        TurnClientListener delegateListener, UpnpManager upnpManager);

    }
