package org.lastbamboo.common.ice;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.util.Collection;
import java.util.HashSet;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.lastbamboo.client.util.settings.HttpSettings;
import org.lastbamboo.common.stun.client.StunClient;
import org.lastbamboo.common.stun.client.StunClientFactory;
import org.lastbamboo.common.turn.client.TurnClient;
import org.lastbamboo.common.util.NetworkUtils;


/**
 * Class for keeping track of address and port bindings.  These can be either
 * UDP or TCP.  For TCP inclusion in SDP, see the IETF draft
 * "TCP-Based Media Transport in the Session Description Protocol (SDP)".  The
 * "Interactive Connectivity Protocol (ICE)" work also describes this.
 */
public final class BindingTrackerImpl implements BindingTracker
    {

    private static final Log LOG = LogFactory.getLog(BindingTrackerImpl.class);

    private final TurnClient m_turnClient;

    /**
     * This address is used if we're on the open internet and can just report
     * the address of the HTTP server.
     */
    private InetSocketAddress m_publicTcpAddress;

    private final StunClientFactory m_stunClientFactory;

    /**
     * Creates a new tracker for available bindings for accessing this client.
     * 
     * @param turnClient The class for accessing TURN server bindings.
     * @param stunClientFactory Factory for creating STUN clients.
     */
    public BindingTrackerImpl(final TurnClient turnClient,
        final StunClientFactory stunClientFactory)
        {
        this.m_turnClient = turnClient;
        this.m_stunClientFactory = stunClientFactory;
        this.m_publicTcpAddress = discoverPublicAddress();
        }

    private InetSocketAddress discoverPublicAddress()
        {
        final InetAddress lh;
        try
            {
            lh = NetworkUtils.getLocalHost();
            }
        catch (final UnknownHostException e)
            {
            // Should never happen.
            throw new IllegalArgumentException("Could not get localhost!!", e);
            }
        if (NetworkUtils.isPrivateAddress(lh))
            {
            // Use the default STUN server.
            final StunClient client = 
                this.m_stunClientFactory.createUdpClient();
            return client.getPublicAddress(HttpSettings.HTTP_PORT.getValue());
            }
        return new InetSocketAddress(lh, HttpSettings.HTTP_PORT.getValue());
        }

    public InetSocketAddress getStunUdpBinding()
        {
        LOG.trace("Accessing UDP bindings...");
        
        /*
        final int udpPort = UDPService.instance().getStableUDPPort();
        final byte[] address = RouterService.getExternalAddress();
        try
            {
            final InetAddress ia = InetAddress.getByAddress(address);
            return new InetSocketAddress(ia, udpPort);
            }
        catch (final UnknownHostException e)
            {
            LOG.warn("Could not create InetAddress", e);
            return null;
            }
            */
        return null;
        }

    public InetSocketAddress getTcpSoBinding()
        {
        LOG.trace("Accessing STUN TCP bindings...");
        return null;
        }

    public Collection<InetSocketAddress> getTurnTcpBindings()
        {
        LOG.debug("Accessing TCP bindings...");
        final Collection<InetSocketAddress> addresses =     
            new HashSet<InetSocketAddress>();
        
        // Just use the public address if we're on the public internet.
        if (onPublicInternet())
            {
            addAddress(addresses, this.m_publicTcpAddress);
            }
        else
            {
            final InetSocketAddress turnAddress = 
                this.m_turnClient.getAllocatedAddress();
            addAddress(addresses, turnAddress);
            }
        
        return addresses;
        }

    private void addAddress(final Collection<InetSocketAddress> addresses, 
        final InetSocketAddress address)
        {
        if (address == null)
            {
            LOG.warn("Not adding null address");
            return;
            }
        addresses.add(address);
        }

    private boolean onPublicInternet()
        {
        try
            {
            return !NetworkUtils.isPrivateAddress(NetworkUtils.getLocalHost());
            }
        catch (final UnknownHostException e)
            {
            LOG.error("Could not resolve host!", e);
            return false;
            }
        }
    }
