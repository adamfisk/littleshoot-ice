package org.lastbamboo.common.ice;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.channels.DatagramChannel;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

import org.lastbamboo.common.offer.answer.OfferAnswerListener;
import org.lastbamboo.common.stun.server.StunServer;
import org.littleshoot.mina.common.IoAcceptor;
import org.littleshoot.mina.common.IoService;
import org.littleshoot.mina.common.IoSession;
import org.littleshoot.mina.transport.socket.nio.support.DatagramSessionImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import udt.UDTClient;
import udt.UDTReceiver;
import udt.UDTServerSocket;
import udt.UDTSocket;

/**
 * Factory for creating UDT sockets.
 */
public class UdtSocketFactory implements UdpSocketFactory<Socket> {
    
    private final Logger log = LoggerFactory.getLogger(getClass());
    
    private final ExecutorService m_threadPool = 
        Executors.newCachedThreadPool(new ThreadFactory() {
            private int threadNumber = 0;
            public Thread newThread(final Runnable r) {
                final Thread t = 
                    new Thread(r, "UDT-Socket-Accept-Thread-"+threadNumber);
                t.setDaemon(true);
                threadNumber++;
                return t;
            }
        });
    
    @Override
    public void newEndpoint(final IoSession session, final boolean controlling,
        final OfferAnswerListener<Socket> socketListener, 
        final IceStunUdpPeer stunUdpPeer, final IceAgent iceAgent) {
        if (session == null) {
            log.error("Null session: {}", session);
            return;
        }

        // Wait for a bit before we clear the decoders and such on that port -
        // basically the client side may have sent a USE-CANDIDATE binding
        // request for a pair that's still in the in progress state on the
        // other end -- i.e. the server side hasn't verified the pair works
        // for it. So the server side could still be doing STUN checks at that
        // point, and we need to wait.
        //
        // We only do this on the controlling side due to an implementation
        // detail of how we're using this -- basically using HTTP the client
        // side always sends data before the server side (request -> response),
        // so there's no chance the server side could start sending media data
        // while we're still looking for STUN messages (the potential problem
        // on the server side that this sleep solves).
        if (controlling) {
            final long sleepTime = 1200;
            log.info("Client side sleeping for {} milliseconds", sleepTime);
            try {
                Thread.sleep(sleepTime);
            } catch (final InterruptedException e) {
                log.warn("Sleep interrupted?", e);
            }
        }
        
        UDTReceiver.connectionExpiryDisabled = true;
        clear(session, stunUdpPeer, iceAgent);
        if (!controlling) {
            // The CONTROLLED agent is notified to start the media stream first
            // in the ICE process, so this is called before the other side
            // starts sending media. We have to consider this in terms of
            // making sure we wait until the other side is ready.
            log.debug("Creating UDT client socket on CONTROLLED agent.");
            final Runnable clientRunner = new Runnable() {
                public void run() {
                    try {
                        // openClientSocket(session, socketListener);
                        openServerSocket(session, socketListener);
                    } catch (final Throwable t) {
                        log.error("Client socket exception", t);
                    }
                }
            };

            final Thread udtClientThread = new Thread(clientRunner,
                    "UDT Client Thread");
            udtClientThread.setDaemon(true);
            udtClientThread.start();
        } else {
            // This actually happens second in the ICE process -- the
            // controlled agent is notified to start sending media first!
            log.debug("Creating UDT server socket on CONTROLLING agent.");
            log.debug("Listening on: {}", session);

            // If we call "accept" right away here, we'll kill the
            // IoSession thread and won't receive messages, so we
            // need to start a new thread.
            final Runnable serverRunner = new Runnable() {
                public void run() {
                    try {
                        // openServerSocket(session, socketListener);
                        openClientSocket(session, socketListener);
                    } catch (final Throwable t) {
                        log.error("Server socket exception", t);
                    }
                }
            };
            final Thread serverThread = new Thread(serverRunner,
                    "UDT Accepting Thread");
            serverThread.setDaemon(true);
            serverThread.start();
        }
    }

    protected void openClientSocket(final IoSession session,
            final OfferAnswerListener<Socket> socketListener)
            throws InterruptedException, IOException {
        final InetSocketAddress local = (InetSocketAddress) session
                .getLocalAddress();
        final InetSocketAddress remote = (InetSocketAddress) session
                .getRemoteAddress();

        log.info("Session local was: {}", local);
        log.info("Binding to port: {}", local.getPort());

        final UDTClient client = new UDTClient(local.getAddress(),
                local.getPort());


        log.info("About to connect...");
        client.connect(remote.getAddress(), remote.getPort());
        log.info("Connected!!!");

        final Socket sock = client.getSocket();
        log.info("Got socket...notifying listener");

        socketListener.onUdpSocket(sock);
        log.info("Exiting...");
    }

    protected void openServerSocket(final IoSession session,
            final OfferAnswerListener<Socket> socketListener)
            throws InterruptedException, IOException {
        final InetSocketAddress local = (InetSocketAddress) session
                .getLocalAddress();

        log.info("Session local was: {}", local);
        log.info("Binding to port: {}", local.getPort());
        final UDTServerSocket server = new UDTServerSocket(local.getAddress(),
                local.getPort());

        final UDTSocket sock = server.accept();
        m_threadPool.execute(new RequestRunner(socketListener, sock));
    }

    public static class RequestRunner implements Runnable {

        private final Logger log = LoggerFactory.getLogger(getClass());
        private final UDTSocket sock;
        private final OfferAnswerListener<Socket> socketListener;

        public RequestRunner(final OfferAnswerListener<Socket> socketListener,
                final UDTSocket sock) {
            this.socketListener = socketListener;
            this.sock = sock;
        }

        public void run() {
            log.info("NOTIFYING SOCKET LISTENER!!");
            socketListener.onUdpSocket(sock);
        }
    }

    private void clear(final IoSession session, final IceStunUdpPeer stunUdpPeer, 
        final IceAgent iceAgent) {
        log.info("Closing ICE agent");
        iceAgent.close();
        log.info("Clearing session!!");
        final DatagramSessionImpl dgSession = (DatagramSessionImpl) session;
        final DatagramChannel dgChannel = dgSession.getChannel();
        session.close().join(10 * 1000);

        final StunServer stunServer = stunUdpPeer.getStunServer();
        stunServer.close();
        try {
            final IoService service = session.getService();
            log.info("Service is: {}", service);
            if (IoAcceptor.class.isAssignableFrom(service.getClass())) {
                log.info("Unbinding all!!");
                final IoAcceptor acceptor = (IoAcceptor) service;
                acceptor.unbindAll();
            }
            session.getService().getFilterChain().clear();
            dgChannel.disconnect();
            dgChannel.close();
        } catch (final Exception e) {
            log.error("Error clearing session!!", e);
        } finally {
            stunUdpPeer.close();
        }
    }
}
