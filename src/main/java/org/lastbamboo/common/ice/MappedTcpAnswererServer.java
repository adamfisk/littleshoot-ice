package org.lastbamboo.common.ice;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;

import org.lastbamboo.common.portmapping.NatPmpService;
import org.lastbamboo.common.portmapping.PortMapErrorEvent;
import org.lastbamboo.common.portmapping.PortMapEvent;
import org.lastbamboo.common.portmapping.PortMapListener;
import org.lastbamboo.common.portmapping.UpnpService;
import org.lastbamboo.common.util.NetworkUtils;
import org.lastbamboo.common.util.RelayingSocketHandler;
import org.lastbamboo.common.util.ShootConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class is a server socket for all ICE answerers for a given user agent.
 * Using the single server socket allows us to map the port a single time. On
 * the answerer this makes sense because all the answerer does is forward 
 * data to the HTTP server. We can't do the same on the offerer/client side
 * because we have to map incoming sockets to the particular ICE session.
 */
public class MappedTcpAnswererServer implements PortMapListener 
    {

    private final Logger m_log = LoggerFactory.getLogger(getClass());
    
    private final NatPmpService m_natPmpService;
    private final UpnpService m_upnpService;
    private ServerSocket m_serverSocket;

    protected int m_upnpMappingIndex;

    protected int m_natPmpMappingIndex;

    /**
     * Creates a new mapped server for the answerer.
     * 
     * @param natPmpService The NAT PMP mapper.
     * @param upnpService The UPnP mapper.
     * @throws IOException IF there's an error starting the server.
     */
    public MappedTcpAnswererServer(final NatPmpService natPmpService, 
        final UpnpService upnpService) throws IOException
        {
        this.m_natPmpService = natPmpService;
        this.m_upnpService = upnpService;
        final Runnable serverRunner = new Runnable() 
            {
            public void run()
                {
                final RelayingSocketHandler relayingSocketHandler;
                try 
                    {
                    relayingSocketHandler = establishSocket();
                    }
                catch (final IOException e) 
                    {
                    return;
                    }
                
                // We just accept the single socket on this port instead of
                while (true)
                    {
                    try
                        {
                        final Socket sock = m_serverSocket.accept();
                        relayingSocketHandler.onSocket(sock);
                        }
                    catch (final IOException e)
                        {
                        m_log.info("Exception accepting socket!!", e);
                        }
                    }
                }

            private RelayingSocketHandler establishSocket() throws IOException
                {
                upnpService.addPortMapListener(MappedTcpAnswererServer.this);
                natPmpService.addPortMapListener(MappedTcpAnswererServer.this);

                m_serverSocket = new ServerSocket();
                m_serverSocket.bind(
                    new InetSocketAddress(NetworkUtils.getLocalHost(), 0));
                final InetSocketAddress socketAddress =
                    (InetSocketAddress) m_serverSocket.getLocalSocketAddress();
                final int port = socketAddress.getPort();
                m_upnpMappingIndex = upnpService.addUpnpMapping(2, port, port);
                m_natPmpMappingIndex =
                    natPmpService.addNatPmpMapping(2, port, port);
                
                final RelayingSocketHandler relayingSocketHandler =
                    new RelayingSocketHandler(NetworkUtils.getLocalHost(), 
                        ShootConstants.HTTP_PORT);
                return relayingSocketHandler;
                }
            };
        final Thread serverThread = new Thread(serverRunner, 
            "TCP-Ice-Server-Answerer-Thread-"+hashCode());
        serverThread.setDaemon(true);
        serverThread.start();
        
        // We just want to make sure we release our UPnP ports as soon as the
        // JVM exits.
        Runtime.getRuntime().addShutdownHook(new Thread(new Runnable()
            {
            public void run() 
                {
                m_upnpService.removeUpnpMapping(m_upnpMappingIndex);
                m_natPmpService.removeNatPmpMapping(m_natPmpMappingIndex);
                }
            },
            serverThread.getName() + "-ReleaseMappingsOnShutdown"));
        }
    
    public InetSocketAddress getHostAddress() 
        {
        return (InetSocketAddress) this.m_serverSocket.getLocalSocketAddress();
        }
        
    public void onPortMap(final PortMapEvent portMapEvent) 
        {
        m_log.info("Received port map event: {}", portMapEvent);
        }

    public void onPortMapError(final PortMapErrorEvent portMapErrorEvent) 
        {
        m_log.info("Got port map error: {}", portMapErrorEvent);
        }
    }
