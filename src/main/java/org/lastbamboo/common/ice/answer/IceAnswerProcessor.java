package org.lastbamboo.common.ice.answer;

import java.io.IOException;
import java.net.Socket;
import java.util.Collection;

import org.apache.mina.common.ByteBuffer;
import org.lastbamboo.common.answer.AnswerProcessor;
import org.lastbamboo.common.ice.IceCandidateTracker;
import org.lastbamboo.common.ice.IceException;
import org.lastbamboo.common.ice.UacIceCandidateTracker;
import org.lastbamboo.common.ice.candidate.IceCandidate;
import org.lastbamboo.common.ice.sdp.IceCandidateSdpDecoder;
import org.lastbamboo.common.sdp.api.SdpException;
import org.lastbamboo.common.util.IoExceptionWithCause;
import org.lastbamboo.common.util.mina.MinaUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * ICE implementation of answer processing for an offer/answer protocol such
 * as SIP.
 */
public class IceAnswerProcessor implements AnswerProcessor
    {
    
    private final Logger LOG = LoggerFactory.getLogger(getClass());
    
    private final IceCandidateSdpDecoder m_iceCandidateDecoder;
    
    /**
     * Creates a new offer processor.
     * 
     * @param iceCandidateDecoder The factory for creating ICE candidates.
     */
    public IceAnswerProcessor(
        final IceCandidateSdpDecoder iceCandidateDecoder)
        {
        m_iceCandidateDecoder = iceCandidateDecoder;
        }

    public Socket processAnswer(final ByteBuffer answer) throws IOException
        {
        final Collection<IceCandidate> iceCandidates;
        try
            {
            iceCandidates = m_iceCandidateDecoder.decode(answer, true);
            }
        catch (final SdpException e)
            {
            throw new IoExceptionWithCause("Could not handle SDP", e);
            }

        if (iceCandidates.isEmpty())
            {
            // Give up when there are no ICE candidates.
            final String sdp = MinaUtils.toAsciiString(answer);
            LOG.warn("No ICE candidates in SDP: "+sdp);
            throw new IOException("No candidates in SDP: " + sdp);
            }

        // We only currently process ICE "answers" on the UAC side.  This
        // could theoretically use an IceCandidateTrackerFactory type class
        // here though.
        final IceCandidateTracker tracker = new UacIceCandidateTracker();
        tracker.visitCandidates(iceCandidates);
        
        try
            {
            final Socket socket = tracker.getBestSocket();
            return socket;
            }
        catch (final IceException e)
            {
            LOG.debug("Could not create socket", e);
            throw new IoExceptionWithCause("Could not create socket", e);
            }
        }

    }
