package org.lastbamboo.common.ice;

import java.util.Collection;

import org.lastbamboo.common.ice.candidate.IceCandidatePair;

/**
 * Class containing data and state for an ICE check list. 
 */
public class IceCheckListImpl implements IceCheckList
    {

    private final Collection<IceCandidatePair> m_pairs;
    private IceCheckListState m_state;

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
        }

    public IceCheckListState getState()
        {
        return this.m_state;
        }

    }
