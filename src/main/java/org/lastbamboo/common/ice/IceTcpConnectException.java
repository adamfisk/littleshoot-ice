package org.lastbamboo.common.ice;

import java.io.IOException;

/**
 * Exception for when we can't establish a TCP peer for ICE.
 */
public class IceTcpConnectException extends Exception
    {

    /**
     * Generated server version ID. 
     */
    private static final long serialVersionUID = 3709224930811092106L;

    /**
     * Creates a new TCP connection exception.
     * 
     * @param message The error message.
     * @param cause The cause.
     */
    public IceTcpConnectException(final String message, final IOException cause)
        {
        super(message, cause);
        }

    }
