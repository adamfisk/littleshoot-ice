package org.lastbamboo.common.ice;

import org.lastbamboo.common.stun.client.StunClient;

public class IceMediaStreamFactoryImpl implements IceMediaStreamFactory
    {
    
    private final IceMediaStreamDesc m_streamDesc;

    public IceMediaStreamFactoryImpl(final IceMediaStreamDesc streamDesc)
        {
        m_streamDesc = streamDesc;
        }

    public IceMediaStream createStream(final IceAgent iceAgent, 
        final StunClient tcpTurnClient)
        {
        return new IceMediaStreamImpl(iceAgent, this.m_streamDesc, 
            tcpTurnClient);
        }

    }
