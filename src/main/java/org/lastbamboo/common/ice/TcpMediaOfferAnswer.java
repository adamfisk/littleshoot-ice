package org.lastbamboo.common.ice;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.lastbamboo.common.ice.candidate.IceCandidate;
import org.lastbamboo.common.ice.candidate.IceCandidateVisitor;
import org.lastbamboo.common.ice.candidate.IceTcpActiveCandidate;
import org.lastbamboo.common.ice.candidate.IceTcpHostPassiveCandidate;
import org.lastbamboo.common.ice.candidate.IceTcpPeerReflexiveCandidate;
import org.lastbamboo.common.ice.candidate.IceTcpRelayPassiveCandidate;
import org.lastbamboo.common.ice.candidate.IceTcpServerReflexiveSoCandidate;
import org.lastbamboo.common.ice.candidate.IceUdpHostCandidate;
import org.lastbamboo.common.ice.candidate.IceUdpPeerReflexiveCandidate;
import org.lastbamboo.common.ice.candidate.IceUdpRelayCandidate;
import org.lastbamboo.common.ice.candidate.IceUdpServerReflexiveCandidate;
import org.lastbamboo.common.ice.sdp.IceCandidateSdpDecoder;
import org.lastbamboo.common.ice.sdp.IceCandidateSdpDecoderImpl;
import org.lastbamboo.common.offer.answer.MediaOfferAnswer;
import org.lastbamboo.common.offer.answer.OfferAnswerListener;
import org.lastbamboo.common.offer.answer.OfferAnswerMediaListener;
import org.littleshoot.mina.common.ByteBuffer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TcpMediaOfferAnswer implements MediaOfferAnswer,
    IceCandidateVisitor<Object>
    {

    private final Logger log = LoggerFactory.getLogger(getClass());
    private OfferAnswerListener m_offerAnswerListener;
    
    public void close() 
        {
        // TODO Auto-generated method stub
        }

    public void startMedia(final OfferAnswerMediaListener mediaListener) 
        {
        // This is called when the offer answer has happened and we're ready
        // to create the socket.
        }

    public byte[] generateAnswer() 
        {
        // TODO: This is a little bit odd since the TCP side should 
        // theoretically generate the SDP for the TCP candidates.
        final String msg = 
            "We fallback to the old code for gathering this for now.";
        log.error("TCP implemenation can't generate offers or answers");
        throw new UnsupportedOperationException(msg);
        }

    public byte[] generateOffer() 
        {
        // TODO: This is a little bit odd since the TCP side should 
        // theoretically generate the SDP for the TCP candidates.
        final String msg = 
            "We fallback to the old code for gathering this for now.";
        log.error("TCP implemenation can't generate offers or answers");
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
            log.warn("Could not process remote candidates", e);
            return;
            }

        // This should result in the stream entering either the Completed or
        // the Failed state.
        /*
        try
            {
            this.m_mediaStream.establishStream(remoteCandidates);
            }
        catch (final RuntimeException e)
            {
            log.error("Error establishing stream", e);
            }
            */
        }

    public void visitCandidates(final Collection<IceCandidate> candidates)
        {
        final List<IceCandidate> sorted = new LinkedList<IceCandidate>(candidates);
        //Collections.sort(sorted);
        for (final IceCandidate candidate : sorted)
            {
            candidate.accept(this);
            }
        }

    public Object visitTcpActiveCandidate(final IceTcpActiveCandidate candidate)
        {
        // TODO Auto-generated method stub
        return null;
        }

    public Object visitTcpHostPassiveCandidate(
        final IceTcpHostPassiveCandidate candidate)
        {
        final InetSocketAddress sa = candidate.getSocketAddress();
        try
            {
            final Socket sock = new Socket(sa.getAddress(), sa.getPort());
            this.m_offerAnswerListener.onOfferAnswerComplete(this);
            }
        catch (final IOException e)
            {
            log.info("Could not connect to remote host on: {}", sa);
            }
        return sa;
        }

    public Object visitTcpRelayPassiveCandidate(
        final IceTcpRelayPassiveCandidate candidate)
        {
        // TODO Auto-generated method stub
        return null;
        }

    public Object visitTcpServerReflexiveSoCandidate(
        final IceTcpServerReflexiveSoCandidate candidate)
        {
        final String msg = 
            "We don't support server reflexive SO candidates with TCP!!";
        log.error(msg);
        throw new UnsupportedOperationException(msg);
        }
    
    public Object visitTcpPeerReflexiveCandidate(
        final IceTcpPeerReflexiveCandidate candidate)
        {
        final String msg = 
            "We don't support peer reflexive candidates with TCP!!";
        log.error(msg);
        throw new UnsupportedOperationException(msg);
        }

    public Object visitUdpHostCandidate(final IceUdpHostCandidate candidate)
        {
        throw new UnsupportedOperationException("This only handles TCP!!");
        }

    public Object visitUdpPeerReflexiveCandidate(
        final IceUdpPeerReflexiveCandidate candidate)
        {
        throw new UnsupportedOperationException("This only handles TCP!!");
        }

    public Object visitUdpRelayCandidate(final IceUdpRelayCandidate candidate)
        {
        throw new UnsupportedOperationException("This only handles TCP!!");
        }

    public Object visitUdpServerReflexiveCandidate(
        final IceUdpServerReflexiveCandidate candidate)
        {
        throw new UnsupportedOperationException("This only handles TCP!!");
        }
    }
