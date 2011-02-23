package org.lastbamboo.common.ice;

import org.littleshoot.stun.stack.message.BindingRequest;
import org.littleshoot.stun.stack.message.StunMessage;

public interface IceBindingRequestTracker
    {

    boolean recentlyProcessed(BindingRequest request);

    void add(BindingRequest request);

    void addResponse(BindingRequest request, StunMessage response);

    StunMessage getResponse(BindingRequest request);

    }
