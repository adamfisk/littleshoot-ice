package org.lastbamboo.common.ice.candidate;

import java.net.InetAddress;
import java.net.InetSocketAddress;

import org.lastbamboo.common.ice.IceCandidateType;
import org.lastbamboo.common.ice.IceTransportProtocol;
import org.lastbamboo.common.stun.client.StunClient;

/**
 * Abstract class for ICE candidates that use STUN servers to determine their 
 * candidate addresses.
 */
public abstract class AbstractStunServerIceCandidate 
    extends AbstractIceCandidate
    {
    
    private final InetAddress m_relatedAddress;
    private final int m_relatedPort;

    /**
     * Creates a new ICE candidate where the candidate address was determined
     * with the assistance of a STUN server.
     * 
     * @param socketAddress The candidate address.
     * @param baseCandidate The base candidate.
     * @param type The type of candidate, such as server reflexive.
     * @param transport The transport used, such as UDP or passive TCP.
     * @param iceStunClient The address of the STUN server used to 
     * determine the candidate address.
     * @param relatedAddress The related address. 
     * @param relatedPort The related port.
     * @param controlling Whether or not this candidate is the controlling
     * candidate.
     */
    public AbstractStunServerIceCandidate(final InetSocketAddress socketAddress, 
        final IceCandidate baseCandidate, final IceCandidateType type, 
        final IceTransportProtocol transport, 
        final StunClient iceStunClient, final InetAddress relatedAddress,
        final int relatedPort, final boolean controlling)
        {
        super(socketAddress, 
            IceFoundationCalculator.calculateFoundation(type, 
               baseCandidate.getSocketAddress().getAddress(), 
               transport, iceStunClient.getStunServerAddress()), type, 
               transport, controlling, baseCandidate);
        
        m_relatedAddress = relatedAddress;
        m_relatedPort = relatedPort;
        m_stunClient = iceStunClient;
        }
    
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
    public AbstractStunServerIceCandidate(final InetSocketAddress socketAddress, 
        final IceCandidateType type, final IceTransportProtocol transport, 
        final StunClient stunClient, final InetAddress relatedAddress, 
        final int relatedPort, final boolean controlling)
        {
        super(socketAddress, 
            IceFoundationCalculator.calculateFoundation(type, 
               socketAddress.getAddress(), 
               transport, stunClient.getStunServerAddress()), type, 
               transport, controlling, null);
            
        m_relatedAddress = relatedAddress;
        m_relatedPort = relatedPort;
        m_stunClient = stunClient;
        m_baseCandidate = this;
        }

    /**
     * Creates a new ICE candidate where the candidate address was determined
     * with the assistance of a STUN server.
     * 
     * @param socketAddress The candidate address.
     * @param foundation The foundation for this candidate.
     * @param type The type of candidate, such as server reflexive.
     * @param transport The transport used, such as UDP or passive TCP.
     * determine the candidate address.
     * @param relatedAddress The related address. 
     * @param relatedPort The related port.
     * @param controlling Whether or not this candidate is the controlling
     * candidate.
     * @param priority The priority of the candidate.
     * @param componentId The component ID.
     */
    public AbstractStunServerIceCandidate(
        final InetSocketAddress socketAddress, final int foundation, 
        final IceCandidateType type, final IceTransportProtocol transport, 
        final InetAddress relatedAddress, final int relatedPort, 
        final boolean controlling, final long priority, final int componentId)
        {
        super(socketAddress, foundation, type, transport, priority, controlling, 
            componentId, null);
        m_relatedAddress = relatedAddress;
        m_relatedPort = relatedPort;
        }

    public InetAddress getRelatedAddress()
        {
        return m_relatedAddress;
        }

    public int getRelatedPort()
        {
        return m_relatedPort;
        }
    }
