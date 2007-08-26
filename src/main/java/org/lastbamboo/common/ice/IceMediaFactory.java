package org.lastbamboo.common.ice;

import java.net.Socket;


/**
 * Factory for creating media once an ICE exchange has completed. 
 */
public interface IceMediaFactory
    {

    Socket newSocket(IceAgent iceAgent);

    }
