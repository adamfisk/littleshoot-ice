package org.lastbamboo.common.ice;

import org.apache.mina.common.IoSession;
import org.lastbamboo.common.stun.stack.message.BindingRequest;

/**
 * Interface for ICE STUN server classes for processing connectivity checks.
 * This responds to incoming requests from clients.  This implements ICE
 * section 7.2 from:<p>
 * 
 * http://tools.ietf.org/html/draft-ietf-mmusic-ice-17#section-7.2
 */
public interface IceStunServerConnectivityChecker
    {

    /**
     * Handles a Binding Request message.
     * 
     * @param ioSession The session the request came in on.
     * @param binding The Binding Request.
     */
    void handleBindingRequest(IoSession ioSession, BindingRequest binding);
    }
