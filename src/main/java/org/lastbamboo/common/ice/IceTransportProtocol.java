package org.lastbamboo.common.ice;

/**
 * Enumeration of ICE transport protocols for encoding in SDP>
 */
public enum IceTransportProtocol
    {

    /**
     * Simultaneous open.
     */
    TCP_SO("tcp-so"),
    
    /**
     * Active.
     */
    TCP_ACT("tcp-act"),
    
    /**
     * Passive.
     */
    TCP_PASS("tcp-pass"), 
    
    /**
     * UDP protocol.
     */
    UDP("udp"), 
    
    /**
     * An unknown protocol.  We only include this so we can ideally continue
     * on with our parsing, if desired.
     */
    UNKNOWN("unknown");
    
    private final String m_name;

    private IceTransportProtocol(final String name)
        {
        m_name = name;
        }

    /**
     * Accessor for the name of the protocol for encoding in SDP.
     * 
     * @return The name of the protocol.
     */
    public String getName()
        {
        return m_name;
        }
    }
