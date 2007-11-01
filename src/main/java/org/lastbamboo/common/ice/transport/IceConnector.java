package org.lastbamboo.common.ice.transport;

import java.net.InetSocketAddress;

import org.apache.mina.common.IoSession;

/**
 * Interface for classes that establish the transport layer for connectivity 
 * checks.
 */
public interface IceConnector
    {

    /**
     * Creates a new connection from the specified local address to the 
     * specified remote address.
     * 
     * @param localAddress The local address to connect from.
     * @param remoteAddress The remote address to connect to.
     * @return The MINA {@link IoSession} for the connection.
     */
    IoSession connect(InetSocketAddress localAddress, 
        InetSocketAddress remoteAddress);

    }
