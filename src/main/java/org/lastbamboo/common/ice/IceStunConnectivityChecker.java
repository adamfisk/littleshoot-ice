package org.lastbamboo.common.ice;

import org.lastbamboo.common.stun.stack.message.BindingRequest;
import org.lastbamboo.common.stun.stack.message.StunMessage;

/**
 * Interface for classes that perform ICE connectivity checks using STUN. 
 */
public interface IceStunConnectivityChecker
    {

    /**
     * Writes a STUN binding request with the RTO value used for 
     * retransmissions explicitly set.
     * 
     * @param request The STUN binding request.
     * @param rto The value to use for RTO when calculating retransmission 
     * times.  Note this only applies to UDP.
     * @return The response message.
     */
    StunMessage write(BindingRequest request, long rto);
    }
