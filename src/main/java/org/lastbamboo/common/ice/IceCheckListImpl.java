package org.lastbamboo.common.ice;

import java.util.Collection;

import org.lastbamboo.common.ice.candidate.IceCandidatePair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class containing data and state for an ICE check list. 
 */
public class IceCheckListImpl implements IceCheckList
    {

    private final Logger m_log = LoggerFactory.getLogger(getClass());
    private final Collection<IceCandidatePair> m_pairs;
    private volatile IceCheckListState m_state;

    /**
     * Creates a new check list.
     * 
     * @param pairs The {@link Collection} of ICE candidate pairs.
     */
    public IceCheckListImpl(final Collection<IceCandidatePair> pairs)
        {
        m_pairs = pairs;
        m_state = IceCheckListState.RUNNING;
        }

    public Collection<IceCandidatePair> getPairs()
        {
        return m_pairs;
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

    }
