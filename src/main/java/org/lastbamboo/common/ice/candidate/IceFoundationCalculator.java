package org.lastbamboo.common.ice.candidate;

import java.net.InetAddress;

import org.lastbamboo.common.ice.IceCandidateType;
import org.lastbamboo.common.ice.IceTransportProtocol;

/**
 * Class for calculating ICE foundations. 
 */
public class IceFoundationCalculator 
    {
    
    private IceFoundationCalculator()
        {
        // Make sure it's not constructed.
        }
    
    /**
     * Returns the foundation using the type and the base address.
     * 
     * @param type The ICE candidate type.
     * @param baseAddress The base address.
     * @param transport The transport protocol.
     * @return The calculated foundation.
     */
    public static int calculateFoundation(final IceCandidateType type, 
        final InetAddress baseAddress, final IceTransportProtocol transport)
        {
        // Offset makes sure type ordinal and transport ordinals don't add
        // up to the same number for different combinations.
        return (200 + type.ordinal()) * transport.ordinal() + 
            baseAddress.hashCode();
        }

    /**
     * Returns the foundation using the type, base address, and STUN server
     * address.
     * 
     * @param type The ICE candidate type.
     * @param baseAddress The base address.
     * @param transport The transport protocol.
     * @param stunServerAddress The STUN server address.
     * @return The calculated foundation.
     */
    public static int calculateFoundation(final IceCandidateType type, 
        final InetAddress baseAddress, final IceTransportProtocol transport,
        final InetAddress stunServerAddress)
        {
        return calculateFoundation(type, baseAddress, transport) + 
            stunServerAddress.hashCode();
        }

    }
