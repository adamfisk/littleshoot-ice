package org.lastbamboo.common.ice;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implementation of a TCP server created to listen for incoming connections
 * for a single media stream for an ICE session.
 */
public class IceTcpServerImpl implements IceTcpServer
    {

    private final Logger m_log = LoggerFactory.getLogger(getClass());
    
    private final ServerSocket m_serverSocket;

    public IceTcpServerImpl()
        {
        ServerSocket sock;
        try
            {
            sock = new ServerSocket(0);
            }
        catch (final IOException e)
            {
            // Should never happen when allocating an ephemeral port.
            m_log.error("Could not allocate server", e);
            sock = null;
            }
        this.m_serverSocket = sock;
        }
    
    public int getPort()
        {
        return this.m_serverSocket.getLocalPort();
        }

    public void start()
        {
        
        
        //final Socket client = this.m_serverSocket.accept();
        }

    }
