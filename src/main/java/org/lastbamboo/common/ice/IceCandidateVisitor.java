package org.lastbamboo.common.ice;

import java.util.Collection;

import org.lastbamboo.common.ice.candidate.IceCandidate;
import org.lastbamboo.common.ice.candidate.IceTcpHostPassiveCandidate;
import org.lastbamboo.common.ice.candidate.IceTcpRelayPassiveCandidate;
import org.lastbamboo.common.ice.candidate.IceTcpServerReflexiveSoCandidate;
import org.lastbamboo.common.ice.candidate.IceUdpHostCandidate;
import org.lastbamboo.common.ice.candidate.IceUdpPeerReflexiveCandidate;
import org.lastbamboo.common.ice.candidate.IceUdpRelayCandidate;
import org.lastbamboo.common.ice.candidate.IceUdpServerReflexiveCandidate;


/**
 * Visitor for connection candidates in the "Interactive Connectivity 
 * Establishment (ICE)" protocol.
 * 
 * @param <T> The class returned by a single visit method.
 */
public interface IceCandidateVisitor<T>
    {
    
    /**
     * Visits the specified <code>Collection</code> of ICE candidates.
     * @param candidates The <code>Collection</code> of candidates to visit.
     */
    void visitCandidates(Collection<IceCandidate> candidates);

    T visitUdpHostCandidate(IceUdpHostCandidate candidate);

    T visitUdpServerReflexiveCandidate(IceUdpServerReflexiveCandidate candidate);

    T visitUdpPeerReflexiveCandidate(IceUdpPeerReflexiveCandidate candidate);

    T visitUdpRelayCandidate(IceUdpRelayCandidate candidate);

    T visitTcpHostPassiveCandidate(IceTcpHostPassiveCandidate candidate);

    T visitTcpRelayPassiveCandidate(IceTcpRelayPassiveCandidate candidate);

    T visitTcpServerReflexiveSoCandidate(IceTcpServerReflexiveSoCandidate candidate);

    }
