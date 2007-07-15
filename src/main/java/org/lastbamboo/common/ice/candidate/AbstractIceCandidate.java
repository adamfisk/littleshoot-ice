package org.lastbamboo.common.ice.candidate;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;

import org.lastbamboo.common.ice.IceCandidateType;
import org.lastbamboo.common.ice.IceTransportProtocol;
import org.lastbamboo.common.stun.client.StunClient;

/**
 * Class that abstracts out general attributes of all ICE session candidates.
 */
public abstract class AbstractIceCandidate implements IceCandidate
    {

    private final InetSocketAddress m_address;
    
    private Socket m_socket;

    private final IceTransportProtocol m_transport;

    private final IceCandidateType m_candidateType;
    
    /**
     * We only have one component for our media streams for now.
     */
    private final static int s_componentId = 1;

    private final long m_priority;

    private final int m_foundation;
    
    private final boolean m_controlling;

    protected IceCandidate m_baseCandidate;
    
    private final int m_componentId;
    protected StunClient m_stunClient;
    
    /**
     * The is the local interface preference for calculating ICE priorities.
     * This is set to the highest possible value because we currently
     * only use one interface.
     */
    private static final int LOCAL_PREFERENCE = 65535;


    public AbstractIceCandidate(final StunClient stunClient, 
        final IceCandidateType type, final IceTransportProtocol transport, 
        final boolean controlling)
        {
        this (stunClient.getHostAddress(), 
            stunClient.getHostAddress().getAddress(), type, transport, 
            controlling);
        this.m_stunClient = stunClient;
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
        final IceTransportProtocol transport, final boolean controlling)
        {
        this(socketAddress, 
            IceFoundationCalculator.calculateFoundation(type, baseAddress, 
                transport), 
            type, transport, 
            calculatePriority(type), controlling, s_componentId, null);
        m_baseCandidate = this;
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
     */
    public AbstractIceCandidate(final InetSocketAddress socketAddress, 
        final int foundation, final IceCandidateType type, 
        final IceTransportProtocol transport, final boolean controlling,
        final IceCandidate baseCandidate)
        {
        this(socketAddress, foundation, type, transport, 
            calculatePriority(type), controlling, s_componentId, baseCandidate);
        }

    protected AbstractIceCandidate(final InetSocketAddress socketAddress, 
        final int foundation, final IceCandidateType type, 
        final IceTransportProtocol transport, final long priority,
        final boolean controlling, final int componentId,
        final IceCandidate baseCandidate)
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
        this.m_baseCandidate = baseCandidate;
        }

    private static long calculatePriority(final IceCandidateType type)
        {
        // See draft-ietf-mmusic-ice-16.txt section 4.1.2.1.
        return
            (long) (Math.pow(2, 24) * type.getTypePreference()) +
            (long) (Math.pow(2, 8) * LOCAL_PREFERENCE) +
            (int) (Math.pow(2, 0) * (256 - s_componentId));
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

    public int getFoundation()
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
        result = PRIME * result + m_foundation;
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
        if (m_foundation != other.m_foundation)
            return false;
        if (m_priority != other.m_priority)
            return false;
        if (!m_transport.equals(other.m_transport))
            return false;
        return true;
        }
    }
