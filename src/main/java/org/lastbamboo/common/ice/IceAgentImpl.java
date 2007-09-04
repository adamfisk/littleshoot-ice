package org.lastbamboo.common.ice;

import java.io.IOException;
import java.math.BigInteger;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Random;
import java.util.concurrent.atomic.AtomicReference;

import org.apache.mina.common.ByteBuffer;
import org.lastbamboo.common.ice.candidate.IceCandidate;
import org.lastbamboo.common.ice.candidate.IceCandidatePair;
import org.lastbamboo.common.ice.sdp.IceCandidateSdpDecoder;
import org.lastbamboo.common.ice.sdp.IceCandidateSdpDecoderImpl;
import org.lastbamboo.common.offer.answer.OfferAnswerListener;
import org.lastbamboo.common.offer.answer.OfferAnswerMediaListener;
import org.lastbamboo.common.stun.client.StunClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implementation of an ICE agent.  An agent can contain multiple media 
 * streams and manages the top level of an ICE exchange. 
 */
public class IceAgentImpl implements IceAgent
    {

    private final Logger m_log = LoggerFactory.getLogger(getClass());
    
    private volatile boolean m_controlling;
    
    /**
     * The state of overall ICE processing across all media streams.
     */
    private AtomicReference<IceState> m_iceState =
        new AtomicReference<IceState>(IceState.RUNNING);

    /**
     * TODO: This is just a placeholder for now for the most part, as we only
     * currently support a single media stream.
     */
    private final Collection<IceMediaStream> m_mediaStreams =
        new LinkedList<IceMediaStream>();

    /**
     * The tie breaker to use when both agents think they're controlling.
     */
    private final byte[] m_tieBreaker;

    private final IceMediaStream m_mediaStream;

    private OfferAnswerListener m_offerAnswerListener;

    private final IceMediaFactory m_iceMediaFactory;

    /**
     * Creates a new ICE agent for an answerer.  Passes the offer in the 
     * constructor.
     * 
     * @param tcpTurnClient The TCP TURN client for gathering TCP TURN
     * candidate.
     * @param mediaStreamFactory Factory for creating the media streams we're
     * using ICE to establish.
     * @param controlling Whether or not the agent will start out as 
     * controlling.
     * @param iceMediaFactory The factory for creating media that needs to 
     * know about ICE.
     */
    public IceAgentImpl(final StunClient tcpTurnClient, 
        final IceMediaStreamFactory mediaStreamFactory, 
        final boolean controlling, final IceMediaFactory iceMediaFactory) 
        {
        this.m_controlling = controlling;
        this.m_iceMediaFactory = iceMediaFactory;
        this.m_tieBreaker = new BigInteger(64, new Random()).toByteArray();
        
        // TODO: We only currently support a single media stream!!
        this.m_mediaStream = mediaStreamFactory.newStream(this, tcpTurnClient);
        this.m_mediaStreams.add(this.m_mediaStream);
        }

    private void setIceState(final IceState state)
        {
        this.m_iceState.set(state);
        this.m_offerAnswerListener.onOfferAnswerComplete(this);
        }
    
    public void onValidPairsForAllComponents(final IceMediaStream mediaStream)
        {
        // See ICE section 7.1.2.2.3.  This indicates the media stream has a
        // valid pair for all it's components.  That event can potentially 
        // unfreeze checks for other media streams.  
        
        // TODO: We only currently handle a single media stream, so we don't
        // perform these checks for now!!!
        }

    public void onUnfreezeCheckLists(final IceMediaStream mediaStream)
        {
        // Specified in ICE section 7.1.2.3.
        // TODO: We only currently handle a single media stream, so we don't
        // unfreeze any other streams for now!!
        }

    public long calculateDelay(final int Ta_i)
        {
        return IceTransactionDelayCalculator.calculateDelay(Ta_i, 
            this.m_mediaStreams.size());
        }

    public boolean isControlling()
        {
        return this.m_controlling;
        }

    public void setControlling(final boolean controlling)
        {
        m_log.warn("Setting controlling to: "+controlling);
        this.m_controlling = controlling;
        }
    
    public void recomputePairPriorities()
        {
        this.m_mediaStream.recomputePairPriorities(this.m_controlling);
        }
    
    public byte[] getTieBreaker()
        {
        return m_tieBreaker;
        }
    
    public byte[] generateAnswer()
        {
        return m_mediaStream.encodeCandidates();
        }
    
    public byte[] generateOffer()
        {
        return m_mediaStream.encodeCandidates();
        }

    public void processOffer(final ByteBuffer offer, 
        final OfferAnswerListener offerAnswerListener) throws IOException
        {
        this.m_offerAnswerListener = offerAnswerListener;
        processRemoteCandidates(offer);
        }

    public void processAnswer(final ByteBuffer answer, 
        final OfferAnswerListener offerAnswerListener) throws IOException
        {
        this.m_offerAnswerListener = offerAnswerListener;
        processRemoteCandidates(answer);
        }
        
    private void processRemoteCandidates(final ByteBuffer encodedCandidates) 
        throws IOException
        {
        // TODO: We should process all possible media streams.
        
        // Note we set the controlling status of remote candidates to 
        // whatever we are not!!
        final IceCandidateSdpDecoder decoder = new IceCandidateSdpDecoderImpl();
        final Collection<IceCandidate> remoteCandidates = 
            decoder.decode(encodedCandidates, !this.m_controlling);

        // This should result in the stream entering either the Completed or
        // the Failed state.
        this.m_mediaStream.establishStream(remoteCandidates);
        }

    public Collection<IceMediaStream> getMediaStreams()
        {
        return Collections.unmodifiableCollection(this.m_mediaStreams);
        }

    public void onNominatedPair(final IceCandidatePair pair,
        final IceMediaStream mediaStream)
        {
        if (m_log.isDebugEnabled())
            {
            m_log.debug("Received nominated pair on agent.  Controlling: " + 
                isControlling()+" pair:\n"+pair);
            }
        // We now need to set the state of the check list as specified in
        // 8.1.2. Updating States
        final IceCheckListState state = mediaStream.getCheckListState();
        if (state == IceCheckListState.RUNNING)
            {
            mediaStream.onNominated(pair);
            mediaStream.setCheckListState(IceCheckListState.COMPLETED);
            // Now handle the case where all check lists are completed.
            if (allCheckListsInState(IceCheckListState.COMPLETED))
                {
                setIceState(IceState.COMPLETED);
                
                if (this.isControlling())
                    {
                    // TODO: We need to update the default candidate in the 
                    // SDP if the one we're using differs!!  We have to send 
                    // the new offer.
                    }
                }
            }
        
        else if (state == IceCheckListState.FAILED)
            {
            if (allCheckListsInState(IceCheckListState.FAILED))
                {
                setIceState(IceState.FAILED);
                }
            else if (anyCheckListInState(IceCheckListState.COMPLETED))
                {
                // TODO: We SHOULD remove the failed media stream from the 
                // session in our updated offer.
                }
            // Otherwise, none are COMPLETED and at least one of them must 
            // be in the RUNNING state, so we just let ICE continue.
            }
        }
    
    private boolean anyCheckListInState(final IceCheckListState state)
        {
        synchronized (this.m_mediaStreams)
            {
            for (final IceMediaStream stream : this.m_mediaStreams)
                {
                final IceCheckListState curState = stream.getCheckListState();
                if (state == curState)
                    {
                    return true;
                    }
                }
            }
        return false;
        }

    private boolean allCheckListsInState(final IceCheckListState state)
        {
        synchronized (this.m_mediaStreams)
            {
            for (final IceMediaStream stream : this.m_mediaStreams)
                {
                final IceCheckListState curState = stream.getCheckListState();
                if (state != curState)
                    {
                    return false;
                    }
                }
            }
        return true;
        }

    public IceState getIceState()
        {
        return m_iceState.get();
        }

    public Queue<IceCandidatePair> getNominatedPairs()
        {
        return this.m_mediaStream.getNominatedPairs();
        }

    public void onValidPairs(final IceMediaStream mediaStream)
        {
        // 8.1.1.1. Regular Nomination -- we decide whether to continue our
        // checks or nominate now.  We can nominate now through adding the 
        // pair to the triggered check queue with the USE-CANDIDATE attribute.
        //
        // As stated in that section:
        // "The criteria for stopping the checks and for evaluating the 
        // valid pairs is entirely a matter of local optimization."
        //
        // In this case, we have few enough candidates that we can nominate
        // pairs once we've completed checks for all high priority pairs.
        m_log.debug("Processing valid pair...");
        
        if (!isControlling())
            {
            m_log.debug("Not the controlling agent, so not sending a message " +
                "to select the final pair.");
            }
        else
            {
            final Queue<IceCandidatePair> validPairs = 
                mediaStream.getValidPairs();
            final IceCandidatePair pair = validPairs.peek();
            
            /*
            if (mediaStream.hasHigherPriorityPendingPair(pair))
                {
                m_log.debug("We have higher priority pairs that haven't " +
                    "completed their checks");
                }
            else
            */
                {
                m_log.debug("Repeating check that produced the valid pair " +
                    "using USE-CANDIDATE");
                pair.useCandidate();
                mediaStream.addTriggeredCheck(pair);
                }
            }
        }

    public void startMedia(final OfferAnswerMediaListener mediaListener)
        {
        final IceCandidatePair pair = getNominatedPair();
        this.m_iceMediaFactory.newMedia(pair, isControlling(), mediaListener);
        }

    private IceCandidatePair getNominatedPair()
        {
        final Queue<IceCandidatePair> pairs = getNominatedPairs();
        
        final IceCandidatePair topPriorityPair = pairs.peek();
        if (topPriorityPair == null)
            {
            m_log.warn("No nominated pairs");
            return null;
            }
        return topPriorityPair;
        }
    }
