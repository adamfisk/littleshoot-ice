package org.lastbamboo.common.ice;

import java.net.Socket;
import java.util.Collection;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.lastbamboo.common.util.SocketHandler;

/**
 * Tracks ICE candidates for a SIP User-Agent Server.
 */
public final class UasIceCandidateTracker extends AbstractIceCandidateTracker
    {
    
    /**
     * Logger for this class.
     */
    private static final Log LOG = 
        LogFactory.getLog(UasIceCandidateTracker.class);

    private final SocketHandler m_socketHandler;

    /**
     * Creates a new tracker for TURN sockets created for a SIP User-Agent 
     * Server.  
     * 
     * @param socketHandler The class for handling the socket directly, 
     * typically passing it to an HTTP server.
     */
    public UasIceCandidateTracker(final SocketHandler socketHandler)
        {
        this.m_socketHandler = socketHandler;
        }
    
    public void visitCandidates(final Collection candidates)
        {
        super.visitCandidates(candidates);

        for (final TcpActiveIceCandidate candidate : super.m_tcpActiveRemoteCandidates)
            {
            // TODO: Send TURN connect requests here to passive and active 
            // candidates?
            }
        }

    // Note: This method is not used right now because the code is 
    // optimized for use with TURN servers.  This will likely be re-activated
    // in the future to support retrieving sockets from UDP or TCP across 
    // NATs.
    public Socket getBestSocket() throws IceException
        {
        LOG.warn("Getting best socket on UAS");
        // Ugly, I know.  In reality the UAC interface should be completely
        // separated from the UAS interface because they work so differently.
        throw new UnsupportedOperationException("Not used on UAS...");
        }
   
    }
