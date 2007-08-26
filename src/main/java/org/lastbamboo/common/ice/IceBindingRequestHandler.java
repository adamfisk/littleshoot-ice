package org.lastbamboo.common.ice;

import org.apache.mina.common.IoSession;
import org.lastbamboo.common.stun.stack.message.BindingRequest;

/**
 * Interface for ICE STUN server classes for processing Binding Requests.
 */
public interface IceBindingRequestHandler
    {

    /**
     * Handles a Binding Request message.
     * 
     * @param ioSession The session the request came in on.
     * @param binding The Binding Request.
     */
    void handleBindingRequest(IoSession ioSession, BindingRequest binding);
    }
