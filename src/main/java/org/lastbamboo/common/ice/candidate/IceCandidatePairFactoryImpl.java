package org.lastbamboo.common.ice.candidate;

import java.util.Collection;

import org.lastbamboo.common.ice.IceCandidateVisitor;


/**
 * Factory for creating ICE candidate pairs from {@link IceCandidate}s. 
 */
public class IceCandidatePairFactoryImpl implements IceCandidatePairFactory
    {

    public IceCandidatePair createPair(final IceCandidate localCandidate, 
        final IceCandidate remoteCandidate)
        {
        final FactoryVisitor visitor = new FactoryVisitor(remoteCandidate);
        return localCandidate.accept(visitor);
        }
    
    private static final class FactoryVisitor 
        implements IceCandidateVisitor<IceCandidatePair>
        {
        private final IceCandidate m_remoteCandidate;

        private FactoryVisitor(final IceCandidate remoteCandidate)
            {
            m_remoteCandidate = remoteCandidate;
            }

        public IceCandidatePair visitTcpActiveCandidate(
            final IceTcpActiveCandidate candidate)
            {
            return new TcpIceCandidatePair(candidate, this.m_remoteCandidate);
            }

        public IceCandidatePair visitTcpServerReflexiveSoCandidate(
            final IceTcpServerReflexiveSoCandidate candidate)
            {
            return new TcpIceCandidatePair(candidate, this.m_remoteCandidate);
            }
        
        public IceCandidatePair visitTcpHostPassiveCandidate(
            final IceTcpHostPassiveCandidate candidate)
            {
            // This will get pruned.
            return new TcpIceCandidatePair(candidate, this.m_remoteCandidate);
            }

        public IceCandidatePair visitTcpRelayPassiveCandidate(
            final IceTcpRelayPassiveCandidate candidate)
            {
            // This will get pruned.
            return new TcpIceCandidatePair(candidate, this.m_remoteCandidate);
            }

        public IceCandidatePair visitUdpHostCandidate(
            final IceUdpHostCandidate candidate)
            {
            return new UdpIceCandidatePair(candidate, this.m_remoteCandidate);
            }

        public IceCandidatePair visitUdpPeerReflexiveCandidate(
            final IceUdpPeerReflexiveCandidate candidate)
            {
            return new UdpIceCandidatePair(candidate, this.m_remoteCandidate);
            }

        public IceCandidatePair visitUdpRelayCandidate(
            final IceUdpRelayCandidate candidate)
            {
            return new UdpIceCandidatePair(candidate, this.m_remoteCandidate);
            }

        public IceCandidatePair visitUdpServerReflexiveCandidate(
            final IceUdpServerReflexiveCandidate candidate)
            {
            return new UdpIceCandidatePair(candidate, this.m_remoteCandidate);
            }

        public void visitCandidates(Collection<IceCandidate> candidates)
            {
            }
        }

    }
