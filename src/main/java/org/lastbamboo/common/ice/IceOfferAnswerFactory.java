package org.lastbamboo.common.ice;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.Collection;
import java.util.HashSet;

import javax.net.SocketFactory;

import org.lastbamboo.common.ice.candidate.IceCandidate;
import org.lastbamboo.common.ice.sdp.IceCandidateSdpEncoder;
import org.lastbamboo.common.offer.answer.IceMediaStreamDesc;
import org.lastbamboo.common.offer.answer.OfferAnswer;
import org.lastbamboo.common.offer.answer.OfferAnswerConnectException;
import org.lastbamboo.common.offer.answer.OfferAnswerFactory;
import org.lastbamboo.common.offer.answer.OfferAnswerListener;
import org.lastbamboo.common.turn.client.TurnClientListener;
import org.littleshoot.mina.common.ByteBuffer;
import org.littleshoot.util.CandidateProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class for creating ICE agents that process ICE offers and answers.
 */
public class IceOfferAnswerFactory<T> implements OfferAnswerFactory<T> {

    private final Logger m_log = LoggerFactory.getLogger(getClass());
    
    private final IceMediaStreamFactory m_mediaStreamFactory;
    private final UdpSocketFactory<T> m_udpSocketFactory;
    private final CandidateProvider<InetSocketAddress> m_turnCandidateProvider;
    private final MappedServerSocket m_answererServer;

    private final TurnClientListener m_turnClientListener;

    private final CandidateProvider<InetSocketAddress> m_stunCandidateProvider;

    private final MappedTcpOffererServerPool m_offererServer;

    private final SocketFactory m_socketFactory;

    /**
     * Creates a new ICE agent factory. The factory maintains a reference to
     * the TCP TURN client because the client holds a persistent connection
     * to the TURN server and is used across all ICE sessions.
     * 
     * @param mediaStreamFactory The factory for creating ICE media streams.
     * @param udpSocketFactory Factory for creating reliable UDP sockets.
     * @param streamDesc Data for the type of stream to create, such as TCP,
     * UPD, or either.
     * @param answererServer The single router port-mapped server socket for
     * when we're the answerer.
     * @param stunCandidateProvider Provider for STUN servers.
     * @param offererServer The pool of mapped servers to send from the
     * offering side.
     */
    public IceOfferAnswerFactory(
            final IceMediaStreamFactory mediaStreamFactory,
            final UdpSocketFactory<T> udpSocketFactory,
            final CandidateProvider<InetSocketAddress> turnCandidateProvider,
            final MappedServerSocket answererServer,
            final TurnClientListener turnClientListener, 
            final CandidateProvider<InetSocketAddress> stunCandidateProvider, 
            final MappedTcpOffererServerPool offererServer,
            final SocketFactory socketFactory) {
        this.m_mediaStreamFactory = mediaStreamFactory;
        this.m_udpSocketFactory = udpSocketFactory;
        this.m_turnCandidateProvider = turnCandidateProvider;
        this.m_answererServer = answererServer;
        this.m_turnClientListener = turnClientListener;
        this.m_stunCandidateProvider = stunCandidateProvider;
        this.m_offererServer = offererServer;
        this.m_socketFactory = socketFactory;
    }

    @Override
    public OfferAnswer createAnswerer(
            final OfferAnswerListener<T> offerAnswerListener, 
            final boolean useRelay)
            throws OfferAnswerConnectException {
        return createOfferAnswer(false, offerAnswerListener, 
            new IceMediaStreamDesc(true, true, "message", "http", 1, useRelay, 
                true));
    }

    @Override
    public OfferAnswer createOfferer(
            final OfferAnswerListener<T> offerAnswerListener,
            final IceMediaStreamDesc desc)
            throws OfferAnswerConnectException {
        return createOfferAnswer(true, offerAnswerListener, desc);
    }

    private OfferAnswer createOfferAnswer(final boolean controlling,
            final OfferAnswerListener<T> offerAnswerListener,
            final IceMediaStreamDesc mediaDesc)
            throws OfferAnswerConnectException {
        final IceOfferAnswer turnOfferAnswer = newTurnOfferAnswer(controlling,
                offerAnswerListener, mediaDesc);
        final IceOfferAnswer udp = newUdpOfferAnswer(controlling,
                offerAnswerListener, mediaDesc);

        final IceOfferAnswer tcp = 
            newTcpOfferAnswer(offerAnswerListener, controlling,mediaDesc);

        // We create a high-level class that starts a race between the TCP
        // and UDP connections. The TCP approach does not use ICE, instead
        // simplifying things significantly through using straight sockets,
        // either via UPnP, directly over an internal network, or when one of
        // the peers is on the public Internet.
        return new OfferAnswer() {
            @Override
            public byte[] generateOffer() {
                return encodeCandidates(controlling, tcp, udp, turnOfferAnswer, 
                    mediaDesc);
            }

            @Override
            public byte[] generateAnswer() {
                return encodeCandidates(controlling, tcp, udp, turnOfferAnswer,
                        mediaDesc);
            }

            @Override
            public void close() {
                if (tcp != null)
                    tcp.close();
                if (turnOfferAnswer != null)
                    turnOfferAnswer.close();
                if (udp != null)
                    udp.close();
            }

            @Override
            public void processAnswer(final ByteBuffer answer) {
                m_log.info("Processing answer...");
                m_log.info("Turn offer answer: {}", turnOfferAnswer);
                if (mediaDesc.isUseRelay() && turnOfferAnswer != null) {
                    turnOfferAnswer.processAnswer(answer.duplicate());
                }
                if (mediaDesc.isTcp() && tcp != null) {
                    tcp.processAnswer(answer.duplicate());
                }
                if (mediaDesc.isUdp() && udp != null) {
                    udp.processAnswer(answer.duplicate());
                }
            }

            @Override
            public void processOffer(final ByteBuffer offer) {
                m_log.info("Processing offer...");
                if (mediaDesc.isTcp() && tcp != null) {
                    tcp.processOffer(offer);
                }
                if (mediaDesc.isUdp() && udp != null) {
                    udp.processOffer(offer);
                }
                if (mediaDesc.isUseRelay() && turnOfferAnswer != null) {
                    turnOfferAnswer.processOffer(offer);
                }
                m_log.info("Done processing offer...");
            }

            @Override
            public void closeTcp() {
                if (tcp != null)
                    tcp.close();
                if (turnOfferAnswer != null)
                    turnOfferAnswer.close();
            }

            @Override
            public void closeUdp() {
                if (udp != null)
                    udp.close();
            }

            @Override
            public void useRelay() {
                m_log.info("Sending use relay notification.");
                if (tcp != null)
                    tcp.useRelay();
                if (turnOfferAnswer != null)
                    turnOfferAnswer.useRelay();
            }
        };
    }
    
    private IceOfferAnswer newTcpOfferAnswer(
            final OfferAnswerListener<T> offerAnswerListener,
            final boolean controlling, final IceMediaStreamDesc mediaDesc) {
        if (mediaDesc.isTcp()) {
            m_log.info("Creating new TCP offer answer");
            return new TcpOfferAnswer<T>(offerAnswerListener,
                controlling, m_answererServer, this.m_stunCandidateProvider, 
                this.m_offererServer, m_socketFactory);
        } else {
            return null;
        }
    }

    private IceOfferAnswer newUdpOfferAnswer(final boolean controlling,
            final OfferAnswerListener<T> offerAnswerListener,
            final IceMediaStreamDesc mediaDesc)
            throws OfferAnswerConnectException {
        if (mediaDesc.isUdp()) {
            try {
                m_log.info("Creating UDP offer answer...");
                return new IceAgentImpl(this.m_mediaStreamFactory, controlling,
                        offerAnswerListener, this.m_udpSocketFactory,
                        new RawUdpSocketFactory(), mediaDesc);
            } catch (final IceUdpConnectException e) {
                throw new OfferAnswerConnectException(
                        "Could not create UDP connection", e);
            }
        }
        return null;
    }

    private byte[] encodeCandidates(final boolean controlling,
            final IceOfferAnswer tcp, final IceOfferAnswer udp,
            final IceOfferAnswer tcpTurn,
            final IceMediaStreamDesc mediaDesc) {
        final IceCandidateSdpEncoder encoder = new IceCandidateSdpEncoder(
                mediaDesc.getMimeContentType(),
                mediaDesc.getMimeContentSubtype());

        final Collection<IceCandidate> localCandidates = 
            new HashSet<IceCandidate>();
        if (tcp != null) {
            localCandidates.addAll(tcp.gatherCandidates());
        }
        if (udp != null) {
            localCandidates.addAll(udp.gatherCandidates());
        }
        if (!controlling && mediaDesc.isUseRelay() && tcpTurn != null) {
            localCandidates.addAll(tcpTurn.gatherCandidates());
        }
        encoder.visitCandidates(localCandidates);
        return encoder.getSdp();
    }

    /**
     * Creates a new TURN offer/answer.
     * 
     * @param controlling Whether or not this is the controlling ICE agent.
     * @param offerAnswerListener The listener for socket resolution.
     * @return The offer/answer for TURN.
     */
    private IceOfferAnswer newTurnOfferAnswer(final boolean controlling,
            final OfferAnswerListener<T> offerAnswerListener,
            final IceMediaStreamDesc mediaDesc) {
        if (!mediaDesc.isUseRelay()) {
            return null;
        }

        try {
            final TcpTurnOfferAnswer turn = new TcpTurnOfferAnswer(
                m_turnCandidateProvider, controlling, offerAnswerListener,
                m_turnClientListener);

            // We only actually connect to the TURN server on the answerer/
            // non-controlling client.
            if (!controlling) {
                turn.connect();
            }
            return turn;
        } catch (final IOException e) {
            m_log.error("Could not connect to TURN server!!", e);
            return null;
        }
    }

    @Override
    public boolean isAnswererPortMapped() {
        return this.m_answererServer.isPortMapped();
    }
    
    @Override
    public int getMappedPort() {
        return this.m_answererServer.getMappedPort();
    }
}
