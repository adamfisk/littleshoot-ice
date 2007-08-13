package org.lastbamboo.common.ice;

import org.lastbamboo.common.stun.client.StunClient;

/**
 * Factory for creating new media streams using ICE.
 */
public class IceMediaStreamFactoryImpl implements IceMediaStreamFactory
    {
    
    private final IceMediaStreamDesc m_streamDesc;

    /**
     * Creates a new ICE media stream factory.
     * 
     * @param streamDesc The description of the media stream to create.
     */
    public IceMediaStreamFactoryImpl(final IceMediaStreamDesc streamDesc)
        {
        m_streamDesc = streamDesc;
        }

    public IceMediaStream newStream(final IceAgent iceAgent, 
        final StunClient tcpTurnClient)
        {
        return new IceMediaStreamImpl(iceAgent, this.m_streamDesc, 
            tcpTurnClient);
        }

    }
