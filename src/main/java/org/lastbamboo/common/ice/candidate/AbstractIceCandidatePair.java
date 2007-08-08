package org.lastbamboo.common.ice.candidate;

import org.lastbamboo.common.ice.IceStunChecker;


/**
 * Class for a pair of ICE candidates. 
 */
public abstract class AbstractIceCandidatePair implements IceCandidatePair
    {

    private final IceCandidate m_localCandidate;
    private final IceCandidate m_remoteCandidate;
    private long m_priority;
    private IceCandidatePairState m_state;
    private final String m_foundation;
    private final int m_componentId;
    private boolean m_nominated = false;
    private final IceStunChecker m_connectivityChecker;
    private boolean m_nominateOnSuccess = false;

    /**
     * Creates a new pair.
     * 
     * @param localCandidate The local candidate.
     * @param remoteCandidate The candidate from the remote agent.
     */
    public AbstractIceCandidatePair(final IceCandidate localCandidate, 
        final IceCandidate remoteCandidate, 
        final IceStunChecker connectivityChecker)
        {
        this(localCandidate, remoteCandidate, 
            IceCandidatePairPriorityCalculator.calculatePriority(
                localCandidate, remoteCandidate),
            connectivityChecker);
        }

    /**
     * Creates a new pair.
     * 
     * @param localCandidate The local candidate.
     * @param remoteCandidate The candidate from the remote agent.
     * @param priority The priority of the pair.
     */
    public AbstractIceCandidatePair(final IceCandidate localCandidate, 
        final IceCandidate remoteCandidate, final long priority,
        final IceStunChecker connectivityChecker)
        {
        m_localCandidate = localCandidate;
        m_remoteCandidate = remoteCandidate;
        
        // Note both candidates always have the same component ID, so we just
        // choose one for the pair.
        m_componentId = localCandidate.getComponentId();
        m_priority = priority;
        m_state = IceCandidatePairState.FROZEN;
        m_foundation = String.valueOf(localCandidate.getFoundation()) + 
            String.valueOf(remoteCandidate.getFoundation());
        this.m_connectivityChecker = connectivityChecker;
        }
    
    public void nominateOnSuccess()
        {
        this.m_nominateOnSuccess = true;
        }
    
    public boolean shouldNominateOnSuccess()
        {
        return this.m_nominateOnSuccess;
        }
    
    public void cancelStunTransaction()
        {
        this.m_connectivityChecker.cancelTransaction();
        }
    
    public void recomputePriority()
        {
        this.m_priority = IceCandidatePairPriorityCalculator.calculatePriority(
            this.m_localCandidate, this.m_remoteCandidate);
        }

    public IceCandidate getLocalCandidate()
        {
        return m_localCandidate;
        }

    public IceCandidate getRemoteCandidate()
        {
        return m_remoteCandidate;
        }

    public long getPriority()
        {
        return this.m_priority;
        }
    
    public IceCandidatePairState getState()
        {
        return this.m_state;
        }
    
    public String getFoundation()
        {
        return this.m_foundation;
        }
    
    public void setState(final IceCandidatePairState state)
        {
        this.m_state = state;
        }
    
    public int getComponentId()
        {
        return m_componentId;
        }
    
    public void nominate()
        {
        this.m_nominated = true;
        }
    
    public IceStunChecker getConnectivityChecker()
        {
        return m_connectivityChecker;
        }
    
    @Override
    public String toString()
        {
        return 
            "priority:   "+this.m_priority+"\n"+
            "local:      "+this.m_localCandidate+"\n"+
            "remote:     "+this.m_remoteCandidate+"\n"+
            "foundation: "+this.m_foundation;
        }

    @Override
    public int hashCode()
        {
        final int PRIME = 31;
        int result = 1;
        result = PRIME * result + ((m_localCandidate == null) ? 0 : m_localCandidate.hashCode());
        result = PRIME * result + ((m_remoteCandidate == null) ? 0 : m_remoteCandidate.hashCode());
        return result;
        }

    @Override
    public boolean equals(final Object obj)
        {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        final AbstractIceCandidatePair other = (AbstractIceCandidatePair) obj;
        if (m_localCandidate == null)
            {
            if (other.m_localCandidate != null)
                return false;
            }
        else if (!m_localCandidate.equals(other.m_localCandidate))
            return false;
        if (m_remoteCandidate == null)
            {
            if (other.m_remoteCandidate != null)
                return false;
            }
        else if (!m_remoteCandidate.equals(other.m_remoteCandidate))
            return false;
        return true;
        }
    
    public int compareTo(final Object obj)
        {
        final AbstractIceCandidatePair other = (AbstractIceCandidatePair) obj;
        final Long priority1 = new Long(m_priority);
        final Long priority2 = new Long(other.getPriority());
        final int priorityComparison = priority1.compareTo(priority2);
        if (priorityComparison != 0)
            {
            // We reverse this because we want to go from highest to lowest.
            return -priorityComparison;
            }
        if (!m_localCandidate.equals(other.m_localCandidate))
            return -1;
        else if (!m_remoteCandidate.equals(other.m_remoteCandidate))
            return -1;
        return 1;
        }
    }
