package org.lastbamboo.common.ice;

import java.io.IOException;
import java.math.BigInteger;
import java.net.Socket;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Random;

import org.apache.mina.common.ByteBuffer;
import org.lastbamboo.common.ice.candidate.IceCandidate;
import org.lastbamboo.common.ice.candidate.IceCandidatePair;
import org.lastbamboo.common.ice.candidate.IceCandidatePairVisitor;
import org.lastbamboo.common.ice.candidate.TcpIceCandidatePair;
import org.lastbamboo.common.ice.candidate.UdpIceCandidatePair;
import org.lastbamboo.common.ice.sdp.IceCandidateSdpDecoder;
import org.lastbamboo.common.ice.sdp.IceCandidateSdpDecoderImpl;
import org.lastbamboo.common.stun.client.StunClient;
import org.lastbamboo.common.stun.stack.message.BindingRequest;
import org.lastbamboo.common.stun.stack.message.BindingSuccessResponse;
import org.lastbamboo.common.stun.stack.message.StunMessage;
import org.lastbamboo.common.stun.stack.message.StunMessageVisitor;
import org.lastbamboo.common.stun.stack.message.StunMessageVisitorAdapter;
import org.lastbamboo.common.stun.stack.message.attributes.StunAttribute;
import org.lastbamboo.common.stun.stack.message.attributes.ice.IceControllingAttribute;
import org.lastbamboo.common.stun.stack.message.attributes.ice.IceUseCandidateAttribute;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implementation of an ICE agent.  An agent can contain multiple media 
 * streams and manages the top level of an ICE exchange. 
 */
public class IceAgentImpl implements IceAgent
    {

    private final Logger m_log = LoggerFactory.getLogger(getClass());
    
    private boolean m_controlling;

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
     */
    public IceAgentImpl(final StunClient tcpTurnClient, 
        final IceMediaStreamFactory mediaStreamFactory, 
        final boolean controlling) 
        {
        this.m_controlling = controlling;
        this.m_tieBreaker = new BigInteger(64, new Random()).toByteArray();
        
        // TODO: We only currently support a single media stream!!
        this.m_mediaStream = mediaStreamFactory.newStream(this, tcpTurnClient);
        this.m_mediaStreams.add(this.m_mediaStream);
        }
    
    public void processOffer(final ByteBuffer offer) throws IOException
        {
        final IceCandidateSdpDecoder decoder = new IceCandidateSdpDecoderImpl();
        final Collection<IceCandidate> remoteCandidates = 
            decoder.decode(offer, !this.m_controlling);
        this.m_mediaStream.establishStream(remoteCandidates);
        }

    public void onValidPairsForAllComponents(final IceMediaStream mediaStream)
        {
        // See ICE section 7.1.2.2.3.  This indicates the media stream has a
        // valid pair for all it's components.  That event can potentially 
        // unfreeze checks for other media streams.  
        
        // TODO: We only currently handle a single media stream, so we don't
        // perform these checks for now!!!
        
        // We can also potentially nominate the highest priority pair for
        // this stream.  
        
        // Since we only use one stream, we'll go ahead and nominate pairs for 
        // this stream.
        if (!isControlling())
            {
            m_log.debug("Not the controlling agent, so not sending a message " +
                " to select the final pair.");
            return;
            }
        
        m_log.debug("Nominating final pair...");
        final Queue<IceCandidatePair> validPairs = 
            mediaStream.getValidPairs();
        final IceCandidatePair pair = validPairs.peek();
        pair.nominate();
        final IceStunChecker checker = pair.getConnectivityChecker();
        
        final StunAttribute attribute = new IceUseCandidateAttribute();
        final StunAttribute controlling = 
            new IceControllingAttribute(this.m_tieBreaker);
        final BindingRequest request = 
            new BindingRequest(attribute, controlling);
        final StunMessage msg = checker.write(request, 20);
        
        final StunMessageVisitor<IceCandidatePair> visitor = 
            new StunMessageVisitorAdapter<IceCandidatePair>()
            {
            public IceCandidatePair visitBindingSuccessResponse(
                final BindingSuccessResponse response)
                {
                return pair;
                }
            };
            
        final IceCandidatePair verifiedPair = msg.accept(visitor);
        if (verifiedPair == null)
            {
            this.m_log.warn("Valid pair did not succeed after nomination");
            }
        else
            {
            this.m_log.debug("We've selected the final pair.");
            }
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

    public Socket createSocket(final ByteBuffer answer) throws IOException
        {
        // TODO: We should process all possible media streams.
        
        // Note we set the controlling status of remote candidates to 
        // whatever we are not!!
        final IceCandidateSdpDecoder decoder = new IceCandidateSdpDecoderImpl();
        final Collection<IceCandidate> remoteCandidates = 
            decoder.decode(answer, !this.m_controlling);

        this.m_mediaStream.establishStream(remoteCandidates);
        
        final Collection<IceCandidatePair> validPairs = 
            this.m_mediaStream.getValidPairs();
        
        final IceCandidatePairVisitor<Socket> visitor =
            new  IceCandidatePairVisitor<Socket>()
            {

            public Socket visitTcpIceCandidatePair(
                final TcpIceCandidatePair pair)
                {
                return pair.getSocket();
                }

            public Socket visitUdpIceCandidatePair(
                final UdpIceCandidatePair pair)
                {
                // TODO Auto-generated method stub
                return null;
                }
            
            };
        synchronized (validPairs)
            {
            for (final IceCandidatePair pair : validPairs)
                {
                final Socket socket = pair.accept(visitor);
                if (socket != null)
                    {
                    return socket;
                    }
                }
            }
        
        m_log.debug("Could not create socket");
        throw new IOException("Could not create socket");
        }

    public Collection<IceMediaStream> getMediaStreams()
        {
        return Collections.unmodifiableCollection(this.m_mediaStreams);
        }

    }
