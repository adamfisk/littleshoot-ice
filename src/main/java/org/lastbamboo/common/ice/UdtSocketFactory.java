package org.lastbamboo.common.ice;

import java.io.IOException;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;

import org.lastbamboo.common.offer.answer.OfferAnswerListener;
import org.littleshoot.mina.common.IoSession;
import org.littleshoot.mina.transport.socket.nio.support.DatagramSessionImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import udt.UDTClient;
import udt.UDTReceiver;
import udt.UDTServerSocket;
import udt.UDTSocket;

public class UdtSocketFactory implements UdpSocketFactory
    {
    
    private final Logger m_log = LoggerFactory.getLogger(getClass());

    public void newSocket(final IoSession session, final boolean controlling,
        final OfferAnswerListener socketListener) 
        {
        if (session == null)
            {
            m_log.error("Null session: {}", session);
            return;
            }
        
        UDTReceiver.connectionExpiryDisabled=true;
        if (!controlling)
            {
            m_log.debug(
                "Creating UDT client socket on CONTROLLED agent.");
            final Runnable clientRunner = new Runnable()
                {
                public void run()
                    {
                    try
                        {
                        openClientSocket(session, socketListener);
                        }
                    catch (final Throwable t)
                        {
                        m_log.error("Client socket exception", t);
                        }
                    }
                };
    
            final Thread rudpClientThread = 
                new Thread(clientRunner, "RUDP Client Thread");
            rudpClientThread.setDaemon(true);
            rudpClientThread.start();
            }
        else
            {
            m_log.debug(
                "Creating UDT server socket on CONTROLLING agent.");
            m_log.debug("Listening on: {}", session);
            
            // If we call "accept" right away here, we'll kill the
            // IoSession thread and won't receive messages, so we 
            // need to start a new thread.
            final Runnable serverRunner = new Runnable ()
                {
                public void run()
                    {
                    try
                        {
                        openServerSocket(session, socketListener);
                        }
                    catch (final Throwable t)
                        {
                        m_log.error("Server socket exception", t);
                        }
                    }
                };
            final Thread serverThread = 
                new Thread (serverRunner, "RUDP Accepting Thread");
            serverThread.setDaemon(true);
            serverThread.start();
            }
        }

    protected void openClientSocket(final IoSession session,
        final OfferAnswerListener socketListener) 
        throws InterruptedException, IOException 
        {
        final InetSocketAddress local = 
            (InetSocketAddress) session.getLocalAddress();
        final InetSocketAddress remote = 
            (InetSocketAddress) session.getRemoteAddress();

        session.close();
        final DatagramSessionImpl dgSession = (DatagramSessionImpl)session;
        final DatagramSocket dgSock = dgSession.getSocket();
        dgSock.close();
        Thread.sleep(2000);
        
        //final DatagramSessionImpl dgSession = (DatagramSessionImpl)session;
        //final DatagramSocket dgSock = dgSession.getSocket();
        
        //final UDTClient client = new UDTClient(new UDPEndPoint(dgSock));
        final UDTClient client = new UDTClient(local.getAddress(), local.getPort());
        
        client.connect(remote.getAddress(), remote.getPort());
        final Socket sock = client.getSocket();
        socketListener.onUdpSocket(sock);
        }

    protected void openServerSocket(final IoSession session,
        final OfferAnswerListener socketListener) throws SocketException, 
        UnknownHostException, InterruptedException 
        {
        final InetSocketAddress local = 
            (InetSocketAddress) session.getLocalAddress();

        session.close();
        final DatagramSessionImpl dgSession = (DatagramSessionImpl)session;
        final DatagramSocket dgSock = dgSession.getSocket();
        dgSock.close();
        Thread.sleep(2000);

        final UDTServerSocket server = 
            new UDTServerSocket(local.getAddress(), local.getPort());
        
        final UDTSocket sock = server.accept();
        socketListener.onUdpSocket(sock);
        }
    }