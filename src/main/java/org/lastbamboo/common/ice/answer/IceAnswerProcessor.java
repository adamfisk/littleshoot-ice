package org.lastbamboo.common.ice.answer;

import java.io.IOException;
import java.net.Socket;
import java.util.Collection;

import org.apache.mina.common.ByteBuffer;
import org.lastbamboo.common.answer.AnswerProcessor;
import org.lastbamboo.common.ice.IceCheckList;
import org.lastbamboo.common.ice.IceCheckListCreator;
import org.lastbamboo.common.ice.IceCheckListCreatorImpl;
import org.lastbamboo.common.ice.IceMediaStream;
import org.lastbamboo.common.ice.IceMediaStreamImpl;
import org.lastbamboo.common.ice.candidate.IceCandidate;
import org.lastbamboo.common.ice.candidate.IceCandidatePair;
import org.lastbamboo.common.ice.candidate.IceCandidatePairVisitor;
import org.lastbamboo.common.ice.candidate.TcpIceCandidatePair;
import org.lastbamboo.common.ice.candidate.UdpIceCandidatePair;
import org.lastbamboo.common.ice.sdp.IceCandidateSdpDecoder;
import org.lastbamboo.common.sdp.api.SdpException;
import org.lastbamboo.common.util.IoExceptionWithCause;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * ICE implementation of answer processing for an offer/answer protocol such
 * as SIP.
 */
public class IceAnswerProcessor implements AnswerProcessor, 
    IceCandidatePairVisitor
    {
    
    private final Logger LOG = LoggerFactory.getLogger(getClass());
    
    private final IceCandidateSdpDecoder m_iceCandidateDecoder;

    protected Socket m_socket;
    
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

    public Socket processAnswer(final ByteBuffer offer, 
        final ByteBuffer answer) throws IOException
        {
        // TODO: This code relies on there only being one media stream in the
        // SDP.  This is an incorrect assumption even if it works for our
        // purposes.
        final Collection<IceCandidate> localCandidates = 
            decodeCandidates(offer);
        final Collection<IceCandidate> remoteCandidates = 
            decodeCandidates(answer);
        return processAnswer(localCandidates, remoteCandidates);
        }

    public Socket processAnswer(final Collection<IceCandidate> localCandidates, 
        final ByteBuffer answer) throws IOException
        {
        final Collection<IceCandidate> remoteCandidates = 
            decodeCandidates(answer);
        return processAnswer(localCandidates, remoteCandidates);
        }
    
    private Socket processAnswer(final Collection<IceCandidate> localCandidates, 
        final Collection<IceCandidate> remoteCandidates) throws IOException
        {

        final IceCheckListCreator checkListCreator = 
            new IceCheckListCreatorImpl();
        
        final IceCheckList checkList = 
            checkListCreator.createCheckList(localCandidates, remoteCandidates);
            
        final IceMediaStream mediaStream = 
            new IceMediaStreamImpl(checkList, true);
        mediaStream.connect();
        
        final Collection<IceCandidatePair> validPairs = 
            mediaStream.getValidPairs();
        
        for (final IceCandidatePair pair : validPairs)
            {
            pair.accept(this);
            }
        
        if (m_socket == null)
            {
            LOG.debug("Could not create socket");
            throw new IOException("Could not create socket");
            }
        return m_socket;
        }

    private Collection<IceCandidate> decodeCandidates(final ByteBuffer buf) 
        throws IoExceptionWithCause
        {
        try
            {
            return m_iceCandidateDecoder.decode(buf, true);
            }
        catch (final SdpException e)
            {
            throw new IoExceptionWithCause("Could not handle SDP", e);
            }
        }

    public Object visitTcpIceCandidatePair(final TcpIceCandidatePair pair)
        {
        this.m_socket = pair.getSocket();
        return null;
        }

    public Object visitUdpIceCandidatePair(final UdpIceCandidatePair pair)
        {
        return null;
        }

    }
