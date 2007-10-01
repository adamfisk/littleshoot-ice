package org.lastbamboo.common.ice.candidate;

import org.lastbamboo.common.ice.IceStunChecker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Class for a pair of ICE candidates. 
 */
public abstract class AbstractIceCandidatePair implements IceCandidatePair
    {

    private final Logger m_log = LoggerFactory.getLogger(getClass());
    
    private final IceCandidate m_localCandidate;
    private final IceCandidate m_remoteCandidate;
    private volatile long m_priority;
    private volatile IceCandidatePairState m_state;
    private final String m_foundation;
    private final int m_componentId;
    private volatile boolean m_nominated = false;
    protected final IceStunChecker m_stunChecker;
    private volatile boolean m_nominateOnSuccess = false;
    
    /**
     * Flag indicating whether or not this pair should include the 
     * USE CANDIDATE attribute in its Binding Requests during checks.
     */
    private volatile boolean m_useCandidate = false;

    /**
     * Creates a new pair.
     * 
     * @param localCandidate The local candidate.
     * @param remoteCandidate The candidate from the remote agent.
     * @param stunChecker The class that performs STUN checks for this pair.
     */
    public AbstractIceCandidatePair(final IceCandidate localCandidate, 
        final IceCandidate remoteCandidate, 
        final IceStunChecker stunChecker)
        {
        this(localCandidate, remoteCandidate, 
            IceCandidatePairPriorityCalculator.calculatePriority(
                localCandidate, remoteCandidate),
            stunChecker);
        }

    /**
     * Creates a new pair.
     * 
     * @param localCandidate The local candidate.
     * @param remoteCandidate The candidate from the remote agent.
     * @param priority The priority of the pair.
     * @param stunChecker The class that performs STUN checks for this pair.
     */
    public AbstractIceCandidatePair(final IceCandidate localCandidate, 
        final IceCandidate remoteCandidate, final long priority,
        final IceStunChecker stunChecker)
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
        this.m_stunChecker = stunChecker;
        }
    
    public void useCandidate()
        {
        this.m_useCandidate = true;
        }
    
    public boolean useCandidateSet()
        {
        return this.m_useCandidate;
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
        this.m_stunChecker.cancelTransaction();
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
        if (this.m_nominated)
            {
            m_log.debug("Trying to change the state of a nominated pair to: {}", 
                state);
            }
        this.m_state = state;
        if (state == IceCandidatePairState.FAILED)
            {
            getStunChecker().close();
            }
        }
    
    public int getComponentId()
        {
        return m_componentId;
        }
    
    public void nominate()
        {
        this.m_nominated = true;
        }
    
    public boolean isNominated()
        {
        return this.m_nominated;
        }
    
    public IceStunChecker getStunChecker()
        {
        return m_stunChecker;
        }
    
    @Override
    public String toString()
        {
        return 
            "local:      "+this.m_localCandidate+"\n"+
            "remote:     "+this.m_remoteCandidate+"\n"+
            "state:      "+this.m_state;
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
        final Long priority1 = Long.valueOf(m_priority);
        final Long priority2 = Long.valueOf(other.getPriority());
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
