package org.lastbamboo.common.ice;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;

import org.lastbamboo.common.portmapping.NatPmpService;
import org.lastbamboo.common.portmapping.PortMapListener;
import org.lastbamboo.common.portmapping.PortMappingProtocol;
import org.lastbamboo.common.portmapping.UpnpService;
import org.lastbamboo.common.util.NetworkUtils;
import org.lastbamboo.common.util.SocketListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class is a server socket for all ICE answerers for a given user agent.
 * Using the single server socket allows us to map the port a single time. On
 * the answerer this makes sense because all the answerer does is forward 
 * data to the HTTP server. We can't do the same on the offerer/client side
 * because we have to map incoming sockets to the particular ICE session.
 */
public class MappedTcpAnswererServer implements PortMapListener {

    private final Logger m_log = LoggerFactory.getLogger(getClass());
    
    private ServerSocket m_serverSocket;

    /**
     * Creates a new mapped server for the answerer.
     * 
     * @param natPmpService The NAT PMP mapper.
     * @param upnpService The UPnP mapper.
     * @param socketListener Class for handling new incoming sockets.
     * @throws IOException If there's an error starting the server.
     */
    public MappedTcpAnswererServer(final NatPmpService natPmpService,
        final UpnpService upnpService, final SocketListener socketListener)
        throws IOException {
        final Runnable serverRunner = new Runnable() {
            public void run() {
                try {
                    establishSocket();
                } catch (final IOException e) {
                    m_log.warn("Could not create server socket or map port?",e);
                    return;
                }
                while (true) {
                    try {
                        final Socket sock = m_serverSocket.accept();
                        sock.setSoTimeout(100 * 60 * 1000);
                        m_log.info("ACCEPTED INCOMING SOCKET!!");
                        socketListener.onSocket(sock);
                    } catch (final IOException e) {
                        m_log.info("Exception accepting socket!!", e);
                    }
                }
            }

            private void establishSocket() throws IOException {
                m_serverSocket = new ServerSocket();
                m_serverSocket.bind(
                    new InetSocketAddress(NetworkUtils.getLocalHost(), 0));
                final InetSocketAddress socketAddress = 
                    (InetSocketAddress) m_serverSocket.getLocalSocketAddress();
                final int port = socketAddress.getPort();
                upnpService.addUpnpMapping(PortMappingProtocol.TCP, port, 
                    port, MappedTcpAnswererServer.this);
                natPmpService.addNatPmpMapping(PortMappingProtocol.TCP, port,
                    port, MappedTcpAnswererServer.this);
            }
        };
        final Thread serverThread = new Thread(serverRunner,
                "TCP-Ice-Server-Answerer-Thread-" + hashCode());
        serverThread.setDaemon(true);
        serverThread.start();
    }
    
    public InetSocketAddress getHostAddress() {
        return (InetSocketAddress) this.m_serverSocket.getLocalSocketAddress();
    }

    public void onPortMap(final int externalPort) {
        m_log.info("Received port maped: {}", externalPort);
    }

    public void onPortMapError() {
        m_log.info("Got port map error.");
    }
}
