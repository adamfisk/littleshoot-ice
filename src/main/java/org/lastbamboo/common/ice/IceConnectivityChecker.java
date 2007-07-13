package org.lastbamboo.common.ice;


/**
 * Interface for classes that check ICE connectivity. 
 */
public interface IceConnectivityChecker
    {

    /**
     * Checks connectivity.
     * 
     * @return <code>true</code> if the pair was nominated, otherwise 
     * <code>false</code>.
     */
    boolean check();

    }
