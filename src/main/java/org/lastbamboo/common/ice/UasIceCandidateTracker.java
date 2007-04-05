package org.lastbamboo.common.ice;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.lastbamboo.common.util.SocketHandler;

/**
 * Tracks ICE candidates for a SIP User-Agent Server.
 */
public final class UasIceCandidateTracker extends AbstractIceCandidateTracker
    implements UdpSocketListener
    {
    
    /**
     * Logger for this class.
     */
    private static final Log LOG = 
        LogFactory.getLog(UasIceCandidateTracker.class);

    private final SocketHandler m_socketHandler;

    /**
     * Creates a new tracker for TURN sockets created for a SIP User-Agent 
     * Server.  
     * 
     * @param socketHandler The class for handling the socket directly, 
     * typically passing it to an HTTP server.
     */
    public UasIceCandidateTracker(final SocketHandler socketHandler)
        {
        this.m_socketHandler = socketHandler;
        }
    
    public void visitUdpIceCandidate(final IceCandidate candidate)
        {
        // Call the super class to perform any generic operations on candidates.
        super.visitUdpIceCandidate(candidate);
        //final InetSocketAddress address = candidate.getSocketAddress();
        
        // TODO: Connect with some form of reliable UDP.
        //final Runnable udpConnector = new UdpConnector(this, address);
        
        // We only use an executor here in case there are multiple UDP 
        // candidates.
        //this.m_executor.submit(udpConnector);
        }

    // Note: This method is not used right now because the code is 
    // optimized for use with TURN servers.  This will likely be re-activated
    // in the future to support retrieving sockets from UDP or TCP across 
    // NATs.
    public Socket getBestSocket() throws IceException
        {
        LOG.warn("Getting best socket on UAS");
        // Ugly, I know.  In reality the UAC interface should be completely
        // separated from the UAS interface because they work so differently.
        throw new UnsupportedOperationException("Not used on UAS...");
        }
    
    public void onUdpConnect(final Socket sock)
        {
        try
            {
            this.m_socketHandler.handleSocket(sock);
            }
        catch (final IOException e)
            {
            LOG.debug("Could not process the UDP socket", e);
            }
        }
    
    private static final class UdpConnector implements Runnable
        {
    
        private final UdpSocketListener m_udpSocketListener;
        private final InetSocketAddress m_socketAddress;
        
        private UdpConnector(final UdpSocketListener listener, 
            final InetSocketAddress socketAddress)
            {
            this.m_udpSocketListener = listener;
            this.m_socketAddress = socketAddress;
            }
        
        public void run()
            {
            /*
            try
                {
                if (LOG.isDebugEnabled())
                    {
                    logUdpState();
                    }
                final Socket iceSocket = 
                    new UDPConnection(m_socketAddress.getAddress(), 
                        m_socketAddress.getPort());
                LOG.trace("Connected to UDP socket!!!");
                this.m_udpSocketListener.onUdpConnect(iceSocket);
                }
            catch (final IOException e)
                {
                LOG.debug("Could not connect to STUN candidate: " + 
                    this.m_socketAddress, e);
                this.m_udpSocketListener.onUdpConnect(null);
                }
                */
            }

        private void logUdpState()
            {
            /*
            LOG.debug("UDPService: "+UDPService.instance()+" hashCode: "+
                UDPService.instance().hashCode());
            LOG.debug("UDPService...listening: " +
                UDPService.instance().isListening());
            LOG.debug("UDPService...FW transfer: " +
                UDPService.instance().canDoFWT());
            LOG.debug("UDPService can received solicited: "+
                UDPService.instance().canReceiveSolicited());
            LOG.debug("UDPService ip pongs: "+
                UDPService.instance().receivedIpPong());
            
            LOG.debug("RouterService connected: "+
                RouterService.isConnected());
                */
            }    
        }
    }
