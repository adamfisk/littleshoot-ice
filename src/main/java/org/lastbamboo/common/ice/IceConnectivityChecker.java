package org.lastbamboo.common.ice;

import java.net.Socket;

/**
 * Interface for classes that check ICE connectivity. 
 */
public interface IceConnectivityChecker
    {

    /**
     * Checks connectivity.
     * 
     * @return The connected socket.
     */
    Socket check();

    }
