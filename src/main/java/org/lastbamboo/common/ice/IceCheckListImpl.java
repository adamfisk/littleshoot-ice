package org.lastbamboo.common.ice;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.lastbamboo.common.ice.candidate.IceCandidate;
import org.lastbamboo.common.ice.candidate.IceCandidatePair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class containing data and state for an ICE check list. 
 */
public class IceCheckListImpl implements IceCheckList
    {

    private final Logger m_log = LoggerFactory.getLogger(getClass());
    
    private final List<IceCandidatePair> m_triggered;
    private final List<IceCandidatePair> m_pairs;

    private volatile IceCheckListState m_state;
    private volatile boolean m_active;

    /**
     * Creates a new check list.
     * 
     * @param pairs The {@link Collection} of ICE candidate pairs.
     * @param triggered The triggered list.  Initially empty.  This is passed
     * in the assure consistency of data structures.
     */
    public IceCheckListImpl(final List<IceCandidatePair> pairs, 
        final List<IceCandidatePair> triggered)
        {
        m_pairs = pairs;
        m_triggered = triggered;
        m_state = IceCheckListState.RUNNING;
        }

    public Collection<IceCandidatePair> getPairs()
        {
        return m_pairs;
        }
    
    public IceCandidatePair getTriggeredPair()
        {
        synchronized (this.m_triggered)
            {
            if (this.m_triggered.isEmpty())
                {
                return null;
                }
            return this.m_triggered.iterator().next();
            }
        }

    public void setState(final IceCheckListState state)
        {
        this.m_state = state;
        synchronized (this)
            {
            m_log.debug("State changed to: {}", state);
            this.notify();
            }
        }

    public IceCheckListState getState()
        {
        return this.m_state;
        }

    public void check()
        {
        synchronized (this)
            {
            while (this.m_state == IceCheckListState.RUNNING)
                {
                try
                    {
                    wait();
                    }
                catch (final InterruptedException e)
                    {
                    m_log.error("Interrupted??", e);
                    }
                }
            }
        }

    public void setActive(final boolean active)
        {
        this.m_active = active;
        }

    public boolean isActive()
        {
        return m_active;
        }

    public void addTriggeredPair(final IceCandidatePair pair)
        {
        synchronized (this.m_triggered)
            {
            this.m_triggered.add(pair);
            }
        }

    public void recomputePairPriorities(final boolean controlling)
        {
        recompute(this.m_triggered, controlling);
        recompute(this.m_pairs, controlling);
        }

    private static void recompute(final List<IceCandidatePair> pairs, 
        final boolean controlling)
        {
        synchronized (pairs)
            {
            for (final IceCandidatePair pair : pairs)
                {
                final IceCandidate local = pair.getLocalCandidate();
                local.setControlling(controlling);
                pair.recomputePriority();
                }
            
            Collections.sort(pairs);
            }
        }

    }
