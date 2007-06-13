package org.lastbamboo.common.ice;

import java.util.Collection;


/**
 * Visitor for connection candidates in the "Interactive Connectivity 
 * Establishment (ICE)" protocol.
 */
public interface IceCandidateVisitor
    {
    
    /**
     * Visits the specified <code>Collection</code> of ICE candidates.
     * @param candidates The <code>Collection</code> of candidates to visit.
     */
    void visitCandidates(Collection candidates);

    /**
     * Visits an ICE candidate that uses TCP for transport.  This candidate is
     * passive, so it waits for connection attempts from the remote host.
     * 
     * @param candidate The candidate to visit.
     */
    void visitTcpPassiveIceCandidate(TcpPassiveIceCandidate candidate);
    
    /**
     * Visits a TCP active candidate.
     * 
     * @param candidate The candidate.
     */
    void visitTcpActiveIceCandidate(TcpActiveIceCandidate candidate);
    
    /**
     * Visits a TCP simultaneous open candidate.
     * 
     * @param candidate The simultaneous open candidate.
     */
    void visitTcpSoIceCandidate(TcpSoIceCandidate candidate);

    /**
     * Visits a UDP ICE candidate.
     * @param candidate The candidate to visit.
     */
    void visitUdpIceCandidate(UdpIceCandidate candidate);

    /**
     * Visits an ICE candidate with an unknown protocol.
     * @param candidate The candidate to visit.
     */
    void visitUnknownIceCandidate(IceCandidate candidate);

    }
