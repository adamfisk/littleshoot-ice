package org.lastbamboo.common.ice;

/**
 * Describes an individual media stream.
 */
public final class IceMediaStreamDesc {

    private final boolean m_udp;
    private final boolean m_tcp;
    private final String m_mimeContentType;
    private final String m_mimeContentSubtype;
    private final int m_numComponents;
    private final boolean m_useRelay;
    private final boolean reliable;

    /**
     * Creates a new stream description for a raw UDP stream that's not 
     * reliable.
     * 
     * @return A new stream description for an unreliable UDP stream.
     */
    public static IceMediaStreamDesc newUnreliableUdpStream() {
        return new IceMediaStreamDesc(false, true, "message", "udp", 1, true, 
            false);
    }
    
    /**
     * Creates a new media stream description with all the information 
     * necessary for ICE to establish the stream.
     * 
     * @param tcp Whether or not the stream will use TCP.
     * @param udp Whether or not the stream will use UDP.
     * @param mimeContentType The MIME content type for SDP.
     * @param mimeContentSubtype The MIME content subtype.
     * @param numComponents The number of components in the media stream.
     * @param useRelay Whether or not to use relay (TURN) servers.
     */
    public IceMediaStreamDesc(final boolean tcp, final boolean udp,
            final String mimeContentType, final String mimeContentSubtype,
            final int numComponents, final boolean useRelay) {
        this(tcp, udp, mimeContentType, mimeContentSubtype, numComponents, 
            useRelay, true);
    }
    
    /**
     * Creates a new media stream description with all the information 
     * necessary for ICE to establish the stream.
     * 
     * @param tcp Whether or not the stream will use TCP.
     * @param udp Whether or not the stream will use UDP.
     * @param mimeContentType The MIME content type for SDP.
     * @param mimeContentSubtype The MIME content subtype.
     * @param numComponents The number of components in the media stream.
     * @param useRelay Whether or not to use relay (TURN) servers.
     */
    private IceMediaStreamDesc(final boolean tcp, final boolean udp,
            final String mimeContentType, final String mimeContentSubtype,
            final int numComponents, final boolean useRelay,
            final boolean reliable) {
        m_tcp = tcp;
        m_udp = udp;
        m_mimeContentType = mimeContentType;
        m_mimeContentSubtype = mimeContentSubtype;
        m_numComponents = numComponents;
        this.m_useRelay = useRelay;
        this.reliable = reliable;
    }

    public String getMimeContentSubtype() {
        return m_mimeContentSubtype;
    }

    public String getMimeContentType() {
        return m_mimeContentType;
    }

    public boolean isTcp() {
        return m_tcp;
    }

    public boolean isUdp() {
        return m_udp;
    }

    public int getNumComponents() {
        return m_numComponents;
    }

    public boolean isUseRelay() {
        return m_useRelay;
    }

    /**
     * Whether or not there's a reliability layer over this stream, which is
     * true for TCP and any reliable UDT streams.
     * 
     * @return Whether or not there's a reliability layer over this stream.
     */
    public boolean isReliable() {
        return reliable;
    }

}
