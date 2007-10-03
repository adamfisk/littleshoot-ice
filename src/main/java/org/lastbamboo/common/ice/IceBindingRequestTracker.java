package org.lastbamboo.common.ice;

import org.lastbamboo.common.stun.stack.message.BindingRequest;

public interface IceBindingRequestTracker
    {

    boolean recentlyProcessed(BindingRequest request);

    void add(BindingRequest request);

    }
