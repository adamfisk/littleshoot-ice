package org.lastbamboo.common.ice;

/**
 * Exception for an inability to connect to ICE candidates.
 */
public class IceException extends Exception
    {

    /**
     * Serialization ID for this class.
     */
    private static final long serialVersionUID = -608132433078810213L;

    /**
     * Creates a new ICE exception.
     * 
     * @param msg The message explaining the exception.
     */
    public IceException(final String msg)
        {
        super(msg);
        }

    }
