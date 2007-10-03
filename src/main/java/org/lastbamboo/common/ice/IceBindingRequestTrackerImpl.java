package org.lastbamboo.common.ice;

import java.util.Collection;
import java.util.HashSet;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.apache.commons.id.uuid.UUID;
import org.lastbamboo.common.stun.stack.message.BindingRequest;

public class IceBindingRequestTrackerImpl implements IceBindingRequestTracker
    {
    
    private final Collection<UUID> m_transactionIds = new HashSet<UUID>();
    private final Queue<UUID> m_fifoTransactionIds =
        new ConcurrentLinkedQueue<UUID>();
    
    private static final int UUIDS_TO_STORE = 100;
    
    public void add(final BindingRequest request)
        {
        synchronized (this)
            {
            if (this.m_transactionIds.size() >= UUIDS_TO_STORE)
                {
                final UUID lastIn = this.m_fifoTransactionIds.poll();
                this.m_transactionIds.remove(lastIn);
                }
            this.m_transactionIds.add(request.getTransactionId());
            this.m_fifoTransactionIds.add(request.getTransactionId());
            }
        }

    public boolean recentlyProcessed(final BindingRequest request)
        {
        return this.m_transactionIds.contains(request.getTransactionId());
        }

    }
