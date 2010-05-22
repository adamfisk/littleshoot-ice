package org.lastbamboo.common.ice;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.Collection;
import java.util.concurrent.atomic.AtomicReference;

import org.lastbamboo.common.ice.candidate.IceCandidate;
import org.lastbamboo.common.ice.candidate.IceCandidateVisitor;
import org.lastbamboo.common.ice.candidate.IceCandidateVisitorAdapter;
import org.lastbamboo.common.ice.candidate.IceTcpHostPassiveCandidate;
import org.lastbamboo.common.ice.sdp.IceCandidateSdpDecoder;
import org.lastbamboo.common.ice.sdp.IceCandidateSdpDecoderImpl;
import org.lastbamboo.common.offer.answer.MediaOfferAnswer;
import org.lastbamboo.common.offer.answer.OfferAnswerListener;
import org.lastbamboo.common.offer.answer.OfferAnswerMediaListener;
import org.lastbamboo.common.offer.answer.OfferAnswerSocketMedia;
import org.littleshoot.mina.common.ByteBuffer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * {@link MediaOfferAnswer} handler for TCP connections.
 */
public class TcpMediaOfferAnswer implements MediaOfferAnswer
    {

    private final Logger m_log = LoggerFactory.getLogger(getClass());
    private OfferAnswerListener m_offerAnswerListener;
    private final AtomicReference<Socket> m_socketRef = 
        new AtomicReference<Socket>();
    
    
    public void close() 
        {
        final Socket sock = m_socketRef.get();
        if (sock != null)
            {
            try
                {
                sock.close();
                }
            catch (final IOException e)
                {
                m_log.info("Exception closing socket", e);
                }
            }
        }

    public void startMedia(final OfferAnswerMediaListener mediaListener) 
        {
        // This is called when the offer answer has happened and we're ready
        // to create the socket.
        synchronized (m_socketRef)
            {
            if (m_socketRef.get() == null)
                {
                try
                    {
                    m_socketRef.wait(30 * 1000);
                    }
                catch (final InterruptedException e)
                    {
                    }
                }
            }
        final Socket sock = m_socketRef.get();
        if (sock != null)
            {
            mediaListener.onMedia(new OfferAnswerSocketMedia(sock));
            }
        }

    public byte[] generateAnswer() 
        {
        // TODO: This is a little bit odd since the TCP side should 
        // theoretically generate the SDP for the TCP candidates.
        final String msg = 
            "We fallback to the old code for gathering this for now.";
        m_log.error("TCP implemenation can't generate offers or answers");
        throw new UnsupportedOperationException(msg);
        }

    public byte[] generateOffer() 
        {
        // TODO: This is a little bit odd since the TCP side should 
        // theoretically generate the SDP for the TCP candidates.
        final String msg = 
            "We fallback to the old code for gathering this for now.";
        m_log.error("TCP implemenation can't generate offers or answers");
        throw new UnsupportedOperationException(msg);
        }

    public void processOffer(final ByteBuffer offer, 
        final OfferAnswerListener offerAnswerListener)
        {
        this.m_offerAnswerListener = offerAnswerListener;
        processRemoteCandidates(offer);
        }

    public void processAnswer(final ByteBuffer answer, 
        final OfferAnswerListener offerAnswerListener)
        {
        this.m_offerAnswerListener = offerAnswerListener;
        processRemoteCandidates(answer);
        }
        
    private void processRemoteCandidates(final ByteBuffer encodedCandidates) 
        {
        final IceCandidateSdpDecoder decoder = new IceCandidateSdpDecoderImpl();
        final Collection<IceCandidate> remoteCandidates;
        try
            {
            // Note the second argument doesn't matter at all.
            remoteCandidates = decoder.decode(encodedCandidates, false);
            }
        catch (final IOException e)
            {
            m_log.warn("Could not process remote candidates", e);
            return;
            }

        // OK, we've got the candidates. We'll now parallelize connection 
        // attempts to all of them, taking the first to succeed.
        final IceCandidateVisitor<Object> visitor = 
            new IceCandidateVisitorAdapter<Object>()
                {
                @Override
                public Object visitTcpHostPassiveCandidate(
                    final IceTcpHostPassiveCandidate candidate)
                    {
                    final InetSocketAddress endpoint = 
                        candidate.getSocketAddress();
                    final Runnable threadRunner = new Runnable()
                        {
                        public void run()
                            {
                            try
                                {
                                final Socket sock = 
                                    new Socket(endpoint.getAddress(), 
                                        endpoint.getPort());
                                if (m_socketRef.compareAndSet(null, sock))
                                    {
                                    synchronized (m_socketRef)
                                        {
                                        m_socketRef.notifyAll();
                                        }
                                    }
                                else 
                                    {
                                    m_log.info("Socket already exists!");
                                    sock.close();
                                    }
                                }
                            catch (final IOException e)
                                {
                                }
                            }
                        };
                    final Thread connectorThread = 
                        new Thread(threadRunner, "ICE-TCP-Connect-"+endpoint);
                    connectorThread.setDaemon(true);
                    connectorThread.start();
                    return null;
                    }
                };
        for (final IceCandidate candidate : remoteCandidates)
            {
            candidate.accept(visitor);
            }
        }
    }
