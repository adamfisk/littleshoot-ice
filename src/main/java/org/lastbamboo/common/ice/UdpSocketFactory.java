package org.lastbamboo.common.ice;

import org.lastbamboo.common.offer.answer.OfferAnswerListener;
import org.littleshoot.mina.common.IoSession;

/**
 * Interface for creating sockets from reliable UDP "connections."
 */
public interface UdpSocketFactory<T> {

    /**
     * Creates a new socket, notifying the listener when the socket is 
     * available.
     * 
     * @param session The MINA session to create the socket from.
     * @param controlling Whether or not the caller is the controlling ICE
     * agent.
     * @param offerAnswerListener The listener for the socket.
     * @param stunUdpPeer The STUN UDP peer class.
     * @param iceAgent The high level ICE agent. 
     */
    //void newSocket(IoSession session, boolean controlling, 
    //    OfferAnswerListener offerAnswerListener, IceStunUdpPeer stunUdpPeer, 
    //    IceAgent iceAgent);
    
    /**
     * Creates a NAT/firewall traversed endpoint, notifying the listener when 
     * the channel is available.
     * 
     * @param session The MINA session to create the socket from.
     * @param controlling Whether or not the caller is the controlling ICE
     * agent.
     * @param offerAnswerListener The listener for the socket.
     * @param stunUdpPeer The STUN UDP peer class.
     * @param iceAgent The high level ICE agent. 
     */
    void newEndpoint(IoSession session, boolean controlling, 
        OfferAnswerListener<T> offerAnswerListener, IceStunUdpPeer stunUdpPeer, 
        IceAgent iceAgent);

}
