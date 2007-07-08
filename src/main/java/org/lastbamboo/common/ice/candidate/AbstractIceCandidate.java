package org.lastbamboo.common.ice.candidate;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;

import org.lastbamboo.common.ice.IceCandidateType;
import org.lastbamboo.common.ice.IceTransportProtocol;

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
    
    /**
     * The is the local interface preference for calculating ICE priorities.
     * This is set to the highest possible value because we currently
     * only use one interface.
     */
    private static final int LOCAL_PREFERENCE = 65535;

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
            calculatePriority(type), controlling);
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
        final IceTransportProtocol transport, final boolean controlling)
        {
        this(socketAddress, foundation, type, transport, 
            calculatePriority(type), controlling);
        }

    private AbstractIceCandidate(final InetSocketAddress socketAddress, 
        final int foundation, final IceCandidateType type, 
        final IceTransportProtocol transport, final long priority,
        final boolean controlling)
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
        return s_componentId;
        }

    public int getFoundation()
        {
        return m_foundation;
        }
    
    public boolean isControlling()
        {
        return m_controlling;
        }
    }
