package org.lastbamboo.common.ice;

/**
 * This is a TCP server that is created for each media session to listen
 * for incoming TCP connections. 
 */
public interface IceTcpServer
    {

    /**
     * Accessor for the port the server is listening on.
     * 
     * @return The port the server is listening on.
     */
    int getPort();

    void start();

    }
