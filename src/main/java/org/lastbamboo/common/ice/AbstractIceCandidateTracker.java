package org.lastbamboo.common.ice;

import java.util.Collection;
import java.util.Queue;
import java.util.concurrent.PriorityBlockingQueue;

import org.apache.commons.collections.Closure;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.ObjectUtils.Null;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.lastbamboo.common.ice.candidate.IceCandidate;
import org.lastbamboo.common.ice.candidate.IceTcpHostPassiveCandidate;
import org.lastbamboo.common.ice.candidate.IceTcpRelayPassiveCandidate;
import org.lastbamboo.common.ice.candidate.IceTcpServerReflexiveSoCandidate;
import org.lastbamboo.common.ice.candidate.IceUdpHostCandidate;
import org.lastbamboo.common.ice.candidate.IceUdpPeerReflexiveCandidate;
import org.lastbamboo.common.ice.candidate.IceUdpRelayCandidate;
import org.lastbamboo.common.ice.candidate.IceUdpServerReflexiveCandidate;

/**
 * Abstract class for tracking ICE candidates.  This handles things like
 * visiting ICE candidates from SDP data and handling socket creation events.
 */
public abstract class AbstractIceCandidateTracker 
    implements IceCandidateTracker<Null>
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
        new PriorityBlockingQueue<IceCandidate>(4, new IceCandidateComparator());
    
    /**
     * Collection of active TCP candidates from the remote host.
     */
    protected final Queue<IceCandidate> m_tcpActiveRemoteCandidates = 
        new PriorityBlockingQueue<IceCandidate>(4, new IceCandidateComparator());
    
    /**
     * Collection of passive TCP candidates from the remote host.
     */
    protected final Queue<IceCandidate> m_tcpPassiveRemoteCandidates = 
        new PriorityBlockingQueue<IceCandidate>(4, new IceCandidateComparator());
    
    
    /**
     * Collection of TCP simultaneous open candidates from the remote host.
     */
    protected final Queue<IceCandidate> m_tcpSoCandidates = 
        new PriorityBlockingQueue<IceCandidate>(4,
            new IceCandidateComparator());

    public void visitCandidates(final Collection<IceCandidate> candidates)
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

    public Null visitTcpHostPassiveCandidate(
        final IceTcpHostPassiveCandidate candidate)
        {
        LOG.debug("Visiting ICE passive TCP host candidate...");
        this.m_tcpPassiveRemoteCandidates.add(candidate);
        return ObjectUtils.NULL;
        }

    public Null visitTcpRelayPassiveCandidate(
        final IceTcpRelayPassiveCandidate candidate)
        {
        LOG.debug("Visiting ICE passive TCP relay candidate...");
        this.m_tcpPassiveRemoteCandidates.add(candidate);
        return ObjectUtils.NULL;
        }

    public Null visitTcpServerReflexiveSoCandidate(
        final IceTcpServerReflexiveSoCandidate candidate)
        {
        return ObjectUtils.NULL;
        }

    public Null visitUdpHostCandidate(
        final IceUdpHostCandidate candidate)
        {
        LOG.debug("Visiting ICE STUN Candidate: "+candidate);
        this.m_udpCandidates.add(candidate);
        return ObjectUtils.NULL;
        }

    public Null visitUdpPeerReflexiveCandidate(
        final IceUdpPeerReflexiveCandidate candidate)
        {
        return ObjectUtils.NULL;
        }

    public Null visitUdpRelayCandidate(final IceUdpRelayCandidate candidate)
        {
        return ObjectUtils.NULL;
        }

    public Null visitUdpServerReflexiveCandidate(
        final IceUdpServerReflexiveCandidate candidate)
        {
        return ObjectUtils.NULL;
        }

    }
