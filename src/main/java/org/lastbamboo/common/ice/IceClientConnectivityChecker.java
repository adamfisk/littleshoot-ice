package org.lastbamboo.common.ice;


/**
 * Interface for classes that check ICE connectivity from the client side.  
 * Both offerers and answerers perfrom client side checks.
 */
public interface IceClientConnectivityChecker
    {

    /**
     * Performs STUN client connectivity.  Note this is called on both the 
     * offerer and the answerer -- both perform "client side" checks.
     */
    void check();

    }
