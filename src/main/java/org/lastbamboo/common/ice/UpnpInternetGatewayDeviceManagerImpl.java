package org.lastbamboo.common.ice;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.Collection;
import java.util.HashSet;

import net.sbbi.upnp.impls.InternetGatewayDevice;
import net.sbbi.upnp.messages.UPNPResponseException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class that manages UPnP access. 
 */
public class UpnpInternetGatewayDeviceManagerImpl 
    implements UpnpInternetGatewayDeviceManager
    {
    
    private final Logger m_log = LoggerFactory.getLogger(getClass());
    private long m_lastUpnpCheck = 0;
    private volatile boolean m_lastUpnpSucceeded;
    
    private final Collection<InetSocketAddress> m_mappedAddresses =
        new HashSet<InetSocketAddress>();

    public void mapAddress(final InetSocketAddress socketAddress)
        {
        final InternetGatewayDevice igd = getIgd();
        if (igd == null)
            {
            m_log.debug("Could not map UPnP");
            m_lastUpnpSucceeded = false;
            return;
            }

        final String localHostIP = 
            socketAddress.getAddress().getHostAddress();
        
        // We assume that localHostIP is something else than 127.0.0.1.
        final int port = socketAddress.getPort();
        try
            {
            final boolean mapped = igd.addPortMapping(
                "ICE mapping", null, port, port, localHostIP, 0, "TCP");
            if (mapped)
                {
                m_log.debug("Port 9090 mapped to " + localHostIP);
                m_mappedAddresses.add(socketAddress);
                m_lastUpnpSucceeded = true;
                }
            else
                {
                m_log.debug("Could not map port.");
                m_lastUpnpSucceeded = false;
                }
            }
        catch (final IOException e)
            {
            m_log.debug("Could not map port", e);
            m_lastUpnpSucceeded = false;
            }
        catch (final UPNPResponseException e)
            {
            m_log.debug("Could not understand response", e);
            m_lastUpnpSucceeded = false;
            }
        }
    
    public void unmapAddress(final InetSocketAddress socketAddress)
        {
        if (!m_mappedAddresses.contains(socketAddress))
            {
            if (m_log.isDebugEnabled())
                {
                m_log.debug("No mataching address found for: "+
                    socketAddress+" in "+this.m_mappedAddresses);
                }
            return;
            }
        final InternetGatewayDevice igd1 = getIgd();
        if (igd1 == null)
            {
            return;
            }
        try
            {
            final int port = socketAddress.getPort();
            final boolean unmapped = 
                igd1.deletePortMapping(null, port, "TCP");
            m_log.debug("Port unmapped: {}", unmapped);
            if (!unmapped)
                {
                m_log.warn("Port not unmapped");
                }
            }
        catch (final IOException e)
            {
            m_log.debug("Could not delete mapping", e);
            }
        catch (final UPNPResponseException e)
            {
            m_log.debug("Could not understand response", e);
            }
        }
    
    private InternetGatewayDevice getIgd()
        {
        final long now = System.currentTimeMillis();
        final long elapsed = now - this.m_lastUpnpCheck;
        
        // Ignore the request if a recent one failed.
        if (!m_lastUpnpSucceeded && elapsed < (60 * 1000))
            {
            // Failed before, so ignoring.
            m_log.debug("Ignoring UPnP request.  Elapsed: {}", elapsed);
            return null;
            }
        try
            {
            m_log.debug("Trying UPnP...");
            // Use a quick timeout since we're on the local network.
            final InternetGatewayDevice[] gatewayDevices = 
                InternetGatewayDevice.getDevices(1500);
            if (gatewayDevices != null)
                {
                return gatewayDevices[0];
                }
            return null;
            }
        catch (final IOException e)
            {
            m_log.debug("Could not access gateway device", e);
            return null;
            }
        finally
            {
            m_lastUpnpCheck = System.currentTimeMillis();
            }
        }
    }
