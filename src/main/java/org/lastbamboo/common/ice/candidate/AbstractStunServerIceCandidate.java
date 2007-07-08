package org.lastbamboo.common.ice.candidate;

import java.net.InetAddress;
import java.net.InetSocketAddress;

import org.lastbamboo.common.ice.IceCandidateType;
import org.lastbamboo.common.ice.IceTransportProtocol;

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
     * @param baseAddress The address of the local interface.
     * @param type The type of candidate, such as server reflexive.
     * @param transport The transport used, such as UDP or passive TCP.
     * @param stunServerAddress The address of the STUN server used to 
     * determine the candidate address.
     * @param controlling Whether or not this candidate is the controlling
     * candidate.
     */
    public AbstractStunServerIceCandidate(final InetSocketAddress socketAddress, 
        final InetAddress baseAddress, final IceCandidateType type, 
        final IceTransportProtocol transport, 
        final InetAddress stunServerAddress, final InetAddress relatedAddress,
        final int relatedPort, final boolean controlling)
        {
        this(socketAddress, 
           IceFoundationCalculator.calculateFoundation(type, baseAddress, 
               transport, stunServerAddress), type, transport, relatedAddress,
           relatedPort, controlling);
        }

    public AbstractStunServerIceCandidate(
        final InetSocketAddress socketAddress, final int foundation, 
        final IceCandidateType type, final IceTransportProtocol transport, 
        final InetAddress relatedAddress, final int relatedPort, 
        final boolean controlling)
        {
        super(socketAddress, foundation, type, transport, controlling);
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
