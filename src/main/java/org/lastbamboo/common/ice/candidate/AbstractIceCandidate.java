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
    
    private final int m_componentId = 1;

    private final int m_priority;

    private final int m_foundation;
    
    /**
     * The is the local interface preference for calculating ICE priorities.
     * This is set to the highest possible value because we currently
     * only use one interface.
     */
    private static final int LOCAL_PREFERENCE = 65535;

    public AbstractIceCandidate(final InetSocketAddress socketAddress, 
        final InetAddress baseAddress, 
        final IceCandidateType type, final IceTransportProtocol transport)
        {
        this.m_address = socketAddress;
        this.m_candidateType = type;
        this.m_transport = transport;
        this.m_priority = calculatePriority(type);
        this.m_foundation = 
            IceFoundationCalculator.calculateFoundation(type, baseAddress, transport);
        }
    
    public AbstractIceCandidate(final InetSocketAddress socketAddress, 
        final int foundation, final IceCandidateType type, 
        final IceTransportProtocol transport)
        {
        this.m_address = socketAddress;
        this.m_candidateType = type;
        this.m_transport = transport;
        this.m_priority = calculatePriority(type);
        this.m_foundation = foundation;
        }

    private int calculatePriority(final IceCandidateType type)
        {
        // See draft-ietf-mmusic-ice-16.txt section 4.1.2.1.
        return 
            (2 ^ 24) * type.getTypePreference() +
            (2 ^ 8)  * LOCAL_PREFERENCE  +
            (2 ^ 0)  * (256 - this.m_componentId);
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

    public final int getPriority()
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

    }
