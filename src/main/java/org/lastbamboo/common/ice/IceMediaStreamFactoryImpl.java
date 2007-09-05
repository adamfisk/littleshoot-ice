package org.lastbamboo.common.ice;

import org.apache.mina.common.IoHandler;
import org.apache.mina.filter.codec.ProtocolCodecFactory;
import org.lastbamboo.common.stun.client.StunClient;

/**
 * Factory for creating new media streams using ICE.
 */
public class IceMediaStreamFactoryImpl implements IceMediaStreamFactory
    {
    
    private final IceMediaStreamDesc m_streamDesc;
    private final ProtocolCodecFactory m_codecFactory;
    private final IoHandler m_clientMediaIoHandler;
    private final Class m_mediaClass;
    private final IoHandler m_serverMediaIoHandler;

    /**
     * Creates a new ICE media stream factory.
     * 
     * @param streamDesc The description of the media stream to create.
     */
    public IceMediaStreamFactoryImpl(final IceMediaStreamDesc streamDesc,
        final ProtocolCodecFactory codecFactory, final Class mediaClass,
        final IoHandler clientMediaIoHandler, 
        final IoHandler serverMediaIoHandler)
        {
        m_streamDesc = streamDesc;
        m_codecFactory = codecFactory;
        m_mediaClass = mediaClass;
        m_clientMediaIoHandler = clientMediaIoHandler;
        m_serverMediaIoHandler = serverMediaIoHandler;
        }

    public IceMediaStream newStream(final IceAgent iceAgent, 
        final StunClient tcpTurnClient)
        {
        
        return new IceMediaStreamImpl(iceAgent, this.m_streamDesc, 
            tcpTurnClient, this.m_codecFactory, this.m_mediaClass, 
            this.m_clientMediaIoHandler, this.m_serverMediaIoHandler);
        }

    }
