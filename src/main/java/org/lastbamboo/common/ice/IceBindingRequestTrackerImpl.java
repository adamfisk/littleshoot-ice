package org.lastbamboo.common.ice;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;

import org.apache.commons.id.uuid.UUID;
import org.lastbamboo.common.stun.stack.message.BindingRequest;

/**
 * Keeps track of Binding Requests we've seen to avoid processing a request
 * for the same transaction twice.
 */
public class IceBindingRequestTrackerImpl implements IceBindingRequestTracker
    {
    
    private final Collection<UUID> m_transactionIds = 
        Collections.synchronizedSet(new LinkedHashSet<UUID>());
    
    private static final int UUIDS_TO_STORE = 200;
    
    public void add(final BindingRequest request)
        {
        synchronized (m_transactionIds)
            {
            if (this.m_transactionIds.size() >= UUIDS_TO_STORE)
                {
                final UUID lastIn = this.m_transactionIds.iterator().next();
                this.m_transactionIds.remove(lastIn);
                }
            this.m_transactionIds.add(request.getTransactionId());
            }
        }

    public boolean recentlyProcessed(final BindingRequest request)
        {
        return this.m_transactionIds.contains(request.getTransactionId());
        }

    }
