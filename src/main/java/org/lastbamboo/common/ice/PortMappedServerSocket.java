package org.lastbamboo.common.ice;

import java.net.ServerSocket;

public class PortMappedServerSocket 
    {

    private final ServerSocket m_serverSocket;

    public PortMappedServerSocket(final ServerSocket serverSocket)
        {
        this.m_serverSocket = serverSocket;
        }
    }
