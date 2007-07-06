package org.lastbamboo.common.ice;

/**
 * Enumeration of ICE candidate types, such as host, relayed, server reflexive,
 * or peer reflexive. 
 */
public enum IceCandidateType
    {

    /**
     * Host candidates accessible on the local network.
     */
    HOST (126),
    
    /**
     * Candidates relayed through a STUN relay server.
     */
    RELAYED (0),

    /**
     * Candidates with public addresses determined using a STUN server.
     */
    SERVER_REFLEXIVE (100),
    
    /**
     * Candidated discovered from exchanging STUN messages with peers.
     */
    PEER_REFLEXIVE (110),
    ;
    
    private final int m_typePreference;

    private IceCandidateType(final int typePreference)
        {
        m_typePreference = typePreference;
        }

    /**
     * Accessor for the type preference used in the formula for calculating
     * candidate priorities.
     * 
     * @return The type preference for calculating candidate priorities.
     */
    public int getTypePreference()
        {
        return m_typePreference;
        }
    }
