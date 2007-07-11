package org.lastbamboo.common.ice.candidate;

import java.net.Socket;

/**
 * Class for a pair of ICE candidates. 
 */
public class IceCandidatePairImpl implements IceCandidatePair
    {

    private final IceCandidate m_localCandidate;
    private final IceCandidate m_remoteCandidate;
    private final long m_priority;
    private IceCandidatePairState m_state;
    private final int m_foundation;
    private final int m_componentId;
    private Socket m_socket;

    /**
     * Creates a new pair.
     * 
     * @param localCandidate The local candidate.
     * @param remoteCandidate The candidate from the remote agent.
     */
    public IceCandidatePairImpl(final IceCandidate localCandidate, 
        final IceCandidate remoteCandidate)
        {
        m_localCandidate = localCandidate;
        m_remoteCandidate = remoteCandidate;
        
        // Note both candidates always have the same component ID, so we just
        // choose one for the pair.
        m_componentId = localCandidate.getComponentId();
        m_priority = calculatePriority(localCandidate, remoteCandidate);
        m_state = IceCandidatePairState.FROZEN;
        m_foundation = localCandidate.getFoundation() + 
            remoteCandidate.getFoundation();
        }

    private long calculatePriority(final IceCandidate localCandidate, 
        final IceCandidate remoteCandidate)
        {
        // Here's the formula for calculating pair priorities:
        // G = the priority of the controlling candidate.
        // D = the priority of the controlled candidate.
        // pair priority = 2^32*MIN(G,D) + 2*MAX(G,D) + (G>D?1:0)
        // 
        // Below we use:
        // pair priority = A + B + C
        final long G;
        final long D;
        if (localCandidate.isControlling())
            {
            G = localCandidate.getPriority();
            D = remoteCandidate.getPriority();
            }
        else
            {
            G = remoteCandidate.getPriority();
            D = localCandidate.getPriority();
            }
        final long A = (long) (Math.pow(2, 32) * Math.min(G, D));
        final long B = 2 * Math.max(G, D);
        final int C = G > D ? 1 : 0;
        
        final long pairPriority = A + B + C;
        return pairPriority;
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
    
    public int getFoundation()
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
    

    public Socket getSocket()
        {
        return this.m_socket;
        }
    
    public void setSocket(final Socket sock)
        {
        this.m_socket = sock;
        }
    
    public String toString()
        {
        return 
            "priority:   "+this.m_priority+"\n"+
            "local:      "+this.m_localCandidate.getPriority()+"\n"+
            "remote:     "+this.m_remoteCandidate.getPriority()+"\n"+
            "foundation: "+this.m_foundation;
        }

    @Override
    public int hashCode()
        {
        final int PRIME = 31;
        int result = 1;
        result = PRIME * result + (m_localCandidate.hashCode());
        result = PRIME * result + (int) (m_priority ^ (m_priority >>> 32));
        result = PRIME * result + (m_remoteCandidate.hashCode());
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
        final IceCandidatePairImpl other = (IceCandidatePairImpl) obj;
        if (!m_localCandidate.equals(other.m_localCandidate))
            return false;
        if (m_priority != other.m_priority)
            return false;
        if (!m_remoteCandidate.equals(other.m_remoteCandidate))
            return false;
        return true;
        }
    }
