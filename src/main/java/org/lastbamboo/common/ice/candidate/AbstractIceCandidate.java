package org.lastbamboo.common.ice.candidate;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;

import org.lastbamboo.common.ice.IceCandidateType;
import org.lastbamboo.common.ice.IcePriorityCalculator;
import org.lastbamboo.common.ice.IceTransportProtocol;
import org.lastbamboo.common.stun.client.StunClient;

/**
 * Class that abstracts out general attributes of all ICE session candidates.
 */
public abstract class AbstractIceCandidate implements IceCandidate, Comparable
    {

    private final InetSocketAddress m_address;
    
    private Socket m_socket;

    private final IceTransportProtocol m_transport;

    private final IceCandidateType m_candidateType;
    
    private final long m_priority;

    private final String m_foundation;
    
    private final boolean m_controlling;

    private final IceCandidate m_baseCandidate;
    
    private final int m_componentId;
    private final StunClient m_stunClient;
    
    private final InetAddress m_relatedAddress;
    private final int m_relatedPort;
    
    /**
     * The component ID is 1 unless otherwise specified.
     */
    protected final static int DEFAULT_COMPONENT_ID = 1;
    
    /**
     * Creates a new candidate where the candidate itself is also the base
     * candidate.
     * 
     * @param socketAddress The candidate address.
     * @param type The type of candidate, such as server reflexive.
     * @param transport The transport used, such as UDP or passive TCP.
     * @param stunClient The address of the STUN server used to 
     * determine the candidate address.
     * @param relatedAddress The related address. 
     * @param relatedPort The related port.
     * @param controlling Whether or not this candidate is the controlling
     * candidate.
     */
    public AbstractIceCandidate(final InetSocketAddress socketAddress, 
        final IceCandidateType type, final IceTransportProtocol transport, 
        final StunClient stunClient, final InetAddress relatedAddress, 
        final int relatedPort, final boolean controlling)
        {
        this(socketAddress, 
            IceFoundationCalculator.calculateFoundation(type, 
                socketAddress.getAddress(), 
                transport, stunClient.getStunServerAddress()), 
            type, transport, 
            IcePriorityCalculator.calculatePriority(type, transport), 
            controlling, DEFAULT_COMPONENT_ID, 
            null, relatedAddress, relatedPort, stunClient);
        }
    
    /**
     * Creates a new ICE candidate.
     * 
     * @param socketAddress The candidate address and port.
     * @param baseAddress The base address.
     * @param type The type of candidate.
     * @param transport The transport protocol.
     * @param controlling Whether or not this candidate is the controlling
     * candidate.
     */
    public AbstractIceCandidate(final InetSocketAddress socketAddress, 
        final InetAddress baseAddress, final IceCandidateType type, 
        final IceTransportProtocol transport, final boolean controlling,
        final StunClient stunClient)
        {
        this(socketAddress, 
            IceFoundationCalculator.calculateFoundation(type, baseAddress, 
                transport), 
            type, transport, 
            IcePriorityCalculator.calculatePriority(type, transport), controlling, 
            DEFAULT_COMPONENT_ID, null, null, -1, stunClient);
        }
 
    /**
     * Creates a new ICE candidate.
     * 
     * @param socketAddress The candidate address and port.
     * @param foundation The foundation.
     * @param type The type of candidate.
     * @param transport The transport protocol.
     * @param controlling Whether or not this candidate is the controlling
     * candidate.
     * @param baseCandidate The base candidate this candidate was formed from.
     */
    public AbstractIceCandidate(final InetSocketAddress socketAddress, 
        final String foundation, final IceCandidateType type, 
        final IceTransportProtocol transport, final boolean controlling,
        final IceCandidate baseCandidate, final InetAddress relatedAddress,
        final int relatedPort, final StunClient stunClient)
        {
        this(socketAddress, foundation, type, transport, 
            IcePriorityCalculator.calculatePriority(type, transport), controlling, 
            DEFAULT_COMPONENT_ID, baseCandidate, relatedAddress, relatedPort,
            stunClient);
        }

    protected AbstractIceCandidate(final InetSocketAddress socketAddress, 
        final String foundation, final IceCandidateType type, 
        final IceTransportProtocol transport, final long priority,
        final boolean controlling, final int componentId,
        final IceCandidate baseCandidate, final InetAddress relatedAddress, 
        final int relatedPort, final StunClient stunClient)
        {
        if (socketAddress == null)
            {
            throw new NullPointerException("Null socket address");
            }
        if (type == null)
            {
            throw new NullPointerException("Null type");
            }
        if (transport == null)
            {
            throw new NullPointerException("Null transport");
            }
        this.m_address = socketAddress;
        this.m_candidateType = type;
        this.m_transport = transport;
        this.m_priority = priority;
        this.m_foundation = foundation;
        this.m_controlling = controlling;
        this.m_componentId = componentId;
        if (baseCandidate == null)
            {
            this.m_baseCandidate = this;
            }
        else
            {
            this.m_baseCandidate = baseCandidate;
            }
        
        m_relatedAddress = relatedAddress;
        m_relatedPort = relatedPort;
        this.m_stunClient = stunClient;
        }

    public IceTransportProtocol getTransport()
        {
        return this.m_transport;
        }

    public IceCandidateType getType()
        {
        return this.m_candidateType;
        }

    public final InetSocketAddress getSocketAddress()
        {
        return m_address;
        }

    public final long getPriority()
        {
        return m_priority;
        }

    public Socket getSocket()
        {
        return m_socket;
        }

    public void setSocket(final Socket socket)
        {
        m_socket = socket;
        }

    public int getComponentId()
        {
        return m_componentId;
        }

    public String getFoundation()
        {
        return m_foundation;
        }
    
    public boolean isControlling()
        {
        return m_controlling;
        }
    
    public IceCandidate getBaseCandidate()
        {
        return m_baseCandidate;
        }
    
    public InetAddress getRelatedAddress()
        {
        return m_relatedAddress;
        }

    public int getRelatedPort()
        {
        return m_relatedPort;
        }

    public StunClient getStunClient()
        {
        return m_stunClient;
        }

    @Override
    public int hashCode()
        {
        final int PRIME = 31;
        int result = 1;
        result = PRIME * result +  m_address.hashCode();
        result = PRIME * result + (m_candidateType.hashCode());
        result = PRIME * result + (m_controlling ? 1231 : 1237);
        result = PRIME * result + m_foundation.hashCode();
        result = PRIME * result + (int) (m_priority ^ (m_priority >>> 32));
        result = PRIME * result + (m_transport.hashCode());
        return result;
        }

    @Override
    public boolean equals(Object obj)
        {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        final AbstractIceCandidate other = (AbstractIceCandidate) obj;
        if (!m_address.equals(other.m_address))
            return false;
        if (!m_candidateType.equals(other.m_candidateType))
            return false;
        if (m_controlling != other.m_controlling)
            return false;
        if (!m_foundation.equals(other.m_foundation))
            return false;
        if (m_priority != other.m_priority)
            return false;
        if (!m_transport.equals(other.m_transport))
            return false;
        return true;
        }
    

    public int compareTo(final Object obj)
        {
        final AbstractIceCandidate other = (AbstractIceCandidate) obj;
        final Long priority1 = new Long(m_priority);
        final Long priority2 = new Long(other.getPriority());
        final int priorityComparison = priority1.compareTo(priority2);
        if (priorityComparison != 0)
            {
            // We reverse this because we want to go from highest to lowest.
            return -priorityComparison;
            }
        
        // Otherwise, the two candidates have the same priority, but we now
        // need to check the other equality attributes for consistency with
        // equals.  

        if (!m_address.equals(other.m_address))
            return -1;
        if (!m_candidateType.equals(other.m_candidateType))
            return -1;
        if (m_controlling != other.m_controlling)
            return -1;
        if (!m_foundation.equals(other.m_foundation))
            return -1;
        if (!m_transport.equals(other.m_transport))
            return -1;
        
        // In this case, they really are the same.
        return 0;
        }
    }
