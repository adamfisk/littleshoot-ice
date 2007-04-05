package org.lastbamboo.common.ice;

import java.util.Collection;
import java.util.Queue;
import java.util.concurrent.PriorityBlockingQueue;

import org.apache.commons.collections.Closure;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Abstract class for tracking ICE candidates.  This handles things like
 * visiting ICE candidates from SDP data and handling socket creation events.
 */
public abstract class AbstractIceCandidateTracker 
    implements IceCandidateTracker
    {

    /**
     * Logger for this class.
     */
    private static final Log LOG = 
        LogFactory.getLog(AbstractIceCandidateTracker.class);
    
    /**
     * <code>Collection</code> of UDP candidates to try.
     */
    protected final Queue<IceCandidate> m_udpCandidates = 
        new PriorityBlockingQueue<IceCandidate>(4,new IceCandidateComparator());
    
    /**
     * Collection of TCP candidates from the remote host.
     */
    protected final Queue<IceCandidate> m_tcpPassiveRemoteCandidates = 
        new PriorityBlockingQueue<IceCandidate>(4,new IceCandidateComparator());
    
    
    /**
     * Collection of TCP simultaneous open candidates from the remote host.
     */
    protected final Queue<TcpSoIceCandidate> m_tcpSoCandidates = 
        new PriorityBlockingQueue<TcpSoIceCandidate>(4,
            new IceCandidateComparator());

    public final void visitCandidates(final Collection candidates)
        {
        final Closure trackerClosure = new Closure()
            {
            public void execute(final Object obj)
                {
                final IceCandidate candidate = (IceCandidate) obj;
                candidate.accept(AbstractIceCandidateTracker.this);
                }
            };
    
        CollectionUtils.forAllDo(candidates, trackerClosure);
        }
    
    public void visitTcpPassiveIceCandidate(final IceCandidate candidate)
        {
        LOG.trace("Visiting ICE TCP Candidate...");
        this.m_tcpPassiveRemoteCandidates.add(candidate);
        }
    
    public void visitTcpSoIceCandidate(final TcpSoIceCandidate candidate)
        {
        LOG.trace("Visiting TCP Simultaneous Open Candidate...");
        this.m_tcpSoCandidates.add(candidate);
        }

    public void visitUdpIceCandidate(final IceCandidate candidate)
        {
        LOG.trace("Visiting ICE STUN Candidate: "+candidate);
        
        this.m_udpCandidates.add(candidate);
        }

    public void visitUnknownIceCandidate(final IceCandidate candidate)
        {
        LOG.warn("Visiting unknown ICE candidate...");
        // Ignore unknown candidates.
        }

    }
