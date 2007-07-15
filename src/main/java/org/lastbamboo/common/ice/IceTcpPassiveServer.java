package org.lastbamboo.common.ice;

import java.io.IOException;
import java.net.InetSocketAddress;

/**
 * Interface for an ICE passive TCP server.  This is specified as tcp-passive
 * in SDP.
 */
public interface IceTcpPassiveServer
    {

    /**
     * Accesses the local network address for this server.
     * 
     * @return The local network address for this server.
     */
    InetSocketAddress getLocalAddress();


    void start() throws IOException;

    }
