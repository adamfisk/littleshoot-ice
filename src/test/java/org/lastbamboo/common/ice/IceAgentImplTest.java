package org.lastbamboo.common.ice;

import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.Collection;
import java.util.concurrent.atomic.AtomicBoolean;

import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.lastbamboo.common.offer.answer.IceMediaStreamDesc;
import org.lastbamboo.common.offer.answer.OfferAnswer;
import org.lastbamboo.common.offer.answer.OfferAnswerListener;
import org.lastbamboo.common.turn.client.TurnClientListener;
import org.littleshoot.mina.common.ByteBuffer;
import org.littleshoot.mina.common.IoSession;
import org.littleshoot.util.CandidateProvider;
import org.littleshoot.util.DnsSrvCandidateProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Test connections between ICE agents.
 */

// NOTE: This is not used right now because it only tests for local UDP
// connections, and we no longer use those!
@Ignore
public class IceAgentImplTest {

    private final Logger m_log = LoggerFactory.getLogger(getClass());

    /**
     * Tests creating a local UDP connection using ICE.
     * 
     * @throws Exception
     *             If any unexpected error occurs.
     */
    @Test
    public void testLocalUdpConnection() throws Exception {
        final IceMediaStreamDesc desc = new IceMediaStreamDesc(false, true,
                "message", "http", 1, true);

        final CandidateProvider<InetSocketAddress> stunCandidateProvider = 
            new DnsSrvCandidateProvider("_stun._udp.littleshoot.org");

        final CandidateProvider<InetSocketAddress> turnCandidateProvider = 
            new DnsSrvCandidateProvider("_turn._tcp.littleshoot.org");

        final AtomicBoolean answererCompleted = new AtomicBoolean(false);
        final AtomicBoolean offererCompleted = new AtomicBoolean(false);
        final OfferAnswerListener<Socket> offererStateListener = new OfferAnswerListener<Socket>() {

            public void onOfferAnswerFailed(final OfferAnswer mediaOfferAnswer) {
                synchronized (offererCompleted) {
                    offererCompleted.notifyAll();
                }
            }

            public void onTcpSocket(Socket sock) {
                synchronized (offererCompleted) {
                    offererCompleted.set(true);
                    offererCompleted.notifyAll();
                }
            }

            public void onUdpSocket(Socket sock) {
                synchronized (offererCompleted) {
                    offererCompleted.set(true);
                    offererCompleted.notifyAll();
                }
            }
        };
        final OfferAnswerListener<Socket> answererStateListener = new OfferAnswerListener<Socket>() {
            public void onOfferAnswerFailed(final OfferAnswer mediaOfferAnswer) {
                synchronized (answererCompleted) {
                    answererCompleted.notifyAll();
                }
            }

            public void onTcpSocket(Socket sock) {
                synchronized (answererCompleted) {
                    answererCompleted.set(true);
                    answererCompleted.notifyAll();
                }
            }

            public void onUdpSocket(Socket sock) {
                synchronized (answererCompleted) {
                    answererCompleted.set(true);
                    answererCompleted.notifyAll();
                }
            }
        };

        /*
         * final InetAddress publicAddress =
         * InetAddress.getByName("77.77.77.77"); final NatPmpService
         * natPmpService = new NatPmpServiceStub(); final UpnpService
         * upnpService = new UpnpServiceStub();
         * 
         * final MappedTcpAnswererServer answererServer = new
         * MappedTcpAnswererServer(natPmpService, upnpService);
         * 
         * final TcpOfferAnswer tcp1 = new TcpOfferAnswer(publicAddress,
         * offererStateListener, true, natPmpService, upnpService,
         * answererServer);
         * 
         * final TcpOfferAnswer tcp2 = new TcpOfferAnswer(publicAddress,
         * answererStateListener, false, natPmpService, upnpService,
         * answererServer);
         */

        final GeneralIceMediaStreamFactory generalStreamFactory1 = new GeneralIceMediaStreamFactoryImpl(
                stunCandidateProvider);
        final GeneralIceMediaStreamFactory generalStreamFactory2 = new GeneralIceMediaStreamFactoryImpl(
                stunCandidateProvider);

        final IceMediaStreamFactory mediaStreamFactory1 = new IceMediaStreamFactory() {

            public IceMediaStream newStream(final IceAgent iceAgent,
                    final IceMediaStreamDesc streamDesc)
                    throws IceUdpConnectException {
                // final DemuxableProtocolCodecFactory otherCodecFactory =
                // new StunDemuxableProtocolCodecFactory();
                // final IoHandler clientIoHandler = new IoHandlerAdapter();
                final TurnClientListener delegateListener = null;
                // final IoServiceListener udpServiceListener =
                // new IoServiceListenerStub();
                return generalStreamFactory1.newIceMediaStream(desc, iceAgent,
                        delegateListener);
            }
        };

        final IceMediaStreamFactory mediaStreamFactory2 = new IceMediaStreamFactory() {

            public IceMediaStream newStream(final IceAgent iceAgent, 
                final IceMediaStreamDesc streamDesc)
                    throws IceUdpConnectException {
                // final DemuxableProtocolCodecFactory otherCodecFactory =
                // new StunDemuxableProtocolCodecFactory();
                // final IoHandler clientIoHandler = new IoHandlerAdapter();
                final TurnClientListener delegateListener = null;
                // final IoServiceListener udpServiceListener =
                // new IoServiceListenerStub();
                return generalStreamFactory2.newIceMediaStream(desc, iceAgent,
                        delegateListener);
            }
        };

        final UdpSocketFactory<Socket> udpSocketFactory = new UdpSocketFactory<Socket>() {

            public void newEndpoint(final IoSession session,
                    final boolean controlling,
                    final OfferAnswerListener<Socket> offerAnswerListener,
                    final IceStunUdpPeer stunUdpPeer, final IceAgent iceAgent) {
                offerAnswerListener.onUdpSocket(new Socket());
            }
        };

        final IceAgent offerer = new IceAgentImpl(mediaStreamFactory1, true,
                offererStateListener, udpSocketFactory, udpSocketFactory, desc);
        final byte[] offer = offerer.generateOffer();

        m_log.debug("Telling answerer to process offer: {}", new String(offer));

        final IceAgent answerer = new IceAgentImpl(mediaStreamFactory2, false,
                answererStateListener, udpSocketFactory, udpSocketFactory, desc);

        Assert.assertFalse(answerer.isControlling());

        m_log.debug("About to generate answer...");
        final byte[] answer = answerer.generateAnswer();

        m_log.debug("Generated answer: {}", new String(answer));

        final AtomicBoolean threadFailed = new AtomicBoolean(false);
        final Thread answerThread = new Thread(new Runnable() {
            public void run() {
                answerer.processOffer(ByteBuffer.wrap(offer));
            }
        });

        answerThread.setDaemon(true);
        answerThread.start();
        Thread.yield();

        final Collection<IceMediaStream> streams = answerer.getMediaStreams();
        Assert.assertEquals(1, streams.size());

        // We sleep here to simulate network latency. Otherwise checks are
        // constantly in the "In Progress" state and keep resetting themselves.
        // This sleep should make that happen less often.
        Thread.sleep(200);
        offerer.processAnswer(ByteBuffer.wrap(answer));

        // final Socket sock = offerer.createSocket();

        Assert.assertFalse(threadFailed.get());

        synchronized (offererCompleted) {
            if (!offererCompleted.get())
                offererCompleted.wait(16000);
        }

        synchronized (answererCompleted) {
            if (!answererCompleted.get())
                answererCompleted.wait(16000);
        }

        Assert.assertTrue("Did not complete offer", offererCompleted.get());
        Assert.assertTrue("Did not complete answer", answererCompleted.get());
    }
}
