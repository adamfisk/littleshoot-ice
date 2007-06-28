package org.lastbamboo.common.ice;

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

    }
