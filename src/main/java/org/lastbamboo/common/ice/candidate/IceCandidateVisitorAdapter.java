package org.lastbamboo.common.ice.candidate;

import java.util.Collection;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Adaptor for {@link IceCandidateVisitor}s.
 * 
 * @param <T> The class to return from visit methods.
 */
public abstract class IceCandidateVisitorAdapter<T> 
    implements IceCandidateVisitor<T>
    {

    private final Logger m_log = LoggerFactory.getLogger(getClass());

    public void visitCandidates(final Collection<IceCandidate> candidates)
        {
        m_log.warn("Not handling visit all candidates");
        }

    public T visitTcpActiveCandidate(final IceTcpActiveCandidate candidate)
        {
        m_log.warn("Visiting unhandled candidate: {}", candidate);
        return null;
        }

    public T visitTcpHostPassiveCandidate(
        final IceTcpHostPassiveCandidate candidate)
        {
        m_log.warn("Visiting unhandled candidate: {}", candidate);
        return null;
        }

    public T visitTcpRelayPassiveCandidate(
        final IceTcpRelayPassiveCandidate candidate)
        {
        m_log.warn("Visiting unhandled candidate: {}", candidate);
        return null;
        }

    public T visitTcpServerReflexiveSoCandidate(
        final IceTcpServerReflexiveSoCandidate candidate)
        {
        m_log.warn("Visiting unhandled candidate: {}", candidate);
        return null;
        }
    
    public T visitTcpPeerReflexiveCandidate(
        final IceTcpPeerReflexiveCandidate candidate)
        {
        m_log.warn("Visiting unhandled candidate: {}", candidate);
        return null;
        }

    public T visitUdpHostCandidate(final IceUdpHostCandidate candidate)
        {
        m_log.warn("Visiting unhandled candidate: {}", candidate);
        return null;
        }

    public T visitUdpPeerReflexiveCandidate(
        final IceUdpPeerReflexiveCandidate candidate)
        {
        m_log.warn("Visiting unhandled candidate: {}", candidate);
        return null;
        }

    public T visitUdpRelayCandidate(final IceUdpRelayCandidate candidate)
        {
        m_log.warn("Visiting unhandled candidate: {}", candidate);
        return null;
        }

    public T visitUdpServerReflexiveCandidate(
        final IceUdpServerReflexiveCandidate candidate)
        {
        m_log.warn("Visiting unhandled candidate: {}", candidate);
        return null;
        }

    }
