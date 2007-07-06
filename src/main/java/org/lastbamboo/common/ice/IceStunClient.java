package org.lastbamboo.common.ice;

import java.net.InetAddress;
import java.net.InetSocketAddress;

/**
 * Interface for making STUN client requests.
 */
public interface IceStunClient
    {

    /**
     * Gets the host address for this client, or the local address on a local
     * network interface.
     * 
     * @return The host address and port.
     */
    InetSocketAddress getHostAddress();

    /**
     * Accessor for the "server reflexive address" for this ICE candidate, or
     * the address from the perspective of a public STUN server.  This can
     * block for a little while as the client continues sending packets if 
     * there's packet loss.
     * 
     * @return The server reflexive address for this ICE candidate.
     */
    InetSocketAddress getServerReflexiveAddress();

    /**
     * Accessor for the address of the STUN server.
     * 
     * @return The address of the STUN server.
     */
    InetAddress getStunServerAddress();

    /**
     * Gets the base address on the local network (could be public Internet
     * address) -- the address of this network interface on the local network.
     * 
     * @return The base address.
     */
    InetSocketAddress getBaseAddress();

    }
