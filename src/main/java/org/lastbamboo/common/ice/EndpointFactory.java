package org.lastbamboo.common.ice;

import java.net.InetSocketAddress;
import java.nio.channels.DatagramChannel;

import org.lastbamboo.common.offer.answer.OfferAnswerListener;
import org.lastbamboo.common.stun.server.StunServer;
import org.littleshoot.mina.common.IoAcceptor;
import org.littleshoot.mina.common.IoService;
import org.littleshoot.mina.common.IoSession;
import org.littleshoot.mina.transport.socket.nio.support.DatagramSessionImpl;
import org.littleshoot.util.FiveTuple;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Factory for creating NAT/firewall traversed endpoint pairs for local and
 * remote hosts.
 */
public class EndpointFactory implements UdpSocketFactory<FiveTuple> {

    private final Logger log = LoggerFactory.getLogger(getClass());
    
    @Override
    public void newEndpoint(final IoSession session, final boolean controlling,
            final OfferAnswerListener<FiveTuple> endpointListener,
            final IceStunUdpPeer stunUdpPeer,
            final IceAgent iceAgent) {
        log.debug("Creating new endpoint");
        if (session == null) {
            log.error("Null session: {}", session);
            return;
        }
        
        // Wait for a bit before we clear the decoders and such on that port -
        // basically the client side may have sent a USE-CANDIDATE binding
        // request for a pair that's still in the in progress state on the
        // other end -- i.e. the server side hasn't verified the pair works
        // for it. So the server side could still be doing STUN checks at that
        // point, and we need to wait.
        //
        // We only do this on the controlling side due to an implementation
        // detail of how we're using this -- basically using HTTP the client
        // side always sends data before the server side (request -> response),
        // so there's no chance the server side could start sending media data
        // while we're still looking for STUN messages (the potential problem
        // on the server side that this sleep solves).
        if (controlling) {
            final long sleepTime = 1200;
            log.debug("Client side sleeping for {} milliseconds", sleepTime);
            try {
                Thread.sleep(sleepTime);
            } catch (final InterruptedException e) {
                log.warn("Sleep interrupted?", e);
            }
        }

        clear(session, stunUdpPeer, iceAgent);
        
        final InetSocketAddress local = 
            (InetSocketAddress) session.getLocalAddress();
        final InetSocketAddress remote = 
            (InetSocketAddress) session.getRemoteAddress();

        log.debug("Session local was: {}", local);
        final FiveTuple tuple = 
            new FiveTuple(local, remote, FiveTuple.Protocol.UDP);
        
        endpointListener.onUdpSocket(tuple);
    }
    
    private void clear(final IoSession session, 
        final IceStunUdpPeer stunUdpPeer, final IceAgent iceAgent) {
        log.debug("Closing ICE agent");
        iceAgent.close();
        log.debug("Clearing session: {}", session);
        final DatagramSessionImpl dgSession = (DatagramSessionImpl) session;
        final DatagramChannel dgChannel = dgSession.getChannel();
        session.close().join(10 * 1000);

        final StunServer stunServer = stunUdpPeer.getStunServer();
        stunServer.close();
        try {
            final IoService service = session.getService();
            log.debug("Service is: {}", service);
            if (IoAcceptor.class.isAssignableFrom(service.getClass())) {
                log.debug("Unbinding all!!");
                final IoAcceptor acceptor = (IoAcceptor) service;
                acceptor.unbindAll();
            }
            session.getService().getFilterChain().clear();
            dgChannel.disconnect();
            dgChannel.close();
            
            log.debug("Open: "+dgChannel.isOpen());
            log.debug("Connected: "+dgChannel.isConnected());
            log.debug("Sleeping on channel to make sure it unbinds");
            Thread.sleep(400);
            log.debug("Closed channel");
        } catch (final Exception e) {
            log.error("Error clearing session!!", e);
        } finally {
            stunUdpPeer.close();
        }
    }
}
