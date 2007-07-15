package org.lastbamboo.common.ice;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;

import org.lastbamboo.common.stun.stack.turn.RandomNonCollidingPortGenerator;
import org.lastbamboo.common.stun.stack.turn.RandomNonCollidingPortGeneratorImpl;

/**
 * Passive server for ICE.
 */
public class IceTcpPassiveServerImpl implements IceTcpPassiveServer
    {

    private ServerSocket m_serverSocket;
    
    public void start() throws IOException
        {
        // Very unlikely to collide, although it can because there are mulitple
        // generators in the client.
        final RandomNonCollidingPortGenerator portGenerator =
            new RandomNonCollidingPortGeneratorImpl();
        final int port = portGenerator.createRandomPort();
        this.m_serverSocket = new ServerSocket(port);
        
        final Socket client = this.m_serverSocket.accept();
        }
    
    public InetSocketAddress getLocalAddress()
        {
        return (InetSocketAddress) this.m_serverSocket.getLocalSocketAddress();
        }




    }
