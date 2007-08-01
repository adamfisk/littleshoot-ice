package org.lastbamboo.common.ice;

import java.io.IOException;
import java.math.BigInteger;
import java.net.Socket;
import java.util.Collection;
import java.util.LinkedList;
import java.util.Random;

import org.apache.mina.common.ByteBuffer;
import org.lastbamboo.common.ice.candidate.IceCandidate;
import org.lastbamboo.common.ice.candidate.IceCandidatePair;
import org.lastbamboo.common.ice.candidate.IceCandidatePairVisitor;
import org.lastbamboo.common.ice.candidate.TcpIceCandidatePair;
import org.lastbamboo.common.ice.candidate.UdpIceCandidatePair;
import org.lastbamboo.common.ice.sdp.IceCandidateSdpDecoder;
import org.lastbamboo.common.ice.sdp.IceCandidateSdpDecoderImpl;
import org.lastbamboo.common.ice.sdp.IceCandidateSdpEncoder;
import org.lastbamboo.common.stun.client.StunClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implementation of an ICE agent.  An agent can contain multiple media 
 * streams and manages the top level of an ICE exchange. 
 */
public class IceAgentImpl implements IceAgent, IceCandidatePairVisitor<Socket>
    {

    private final Logger m_log = LoggerFactory.getLogger(getClass());
    
    private final Collection<IceCandidate> m_localCandidates;
    private boolean m_controlling;
    
    private final Collection<IceMediaStream> m_mediaStreams =
        new LinkedList<IceMediaStream>();

    /**
     * The tie breaker to use when both agents think they're controlling.
     */
    private final byte[] m_tieBreaker;

    private final StunClient m_udpStunClient;

    private final IceCandidateSdpDecoder m_iceCandidateDecoder;

    /**
     * Creates a new ICE agent.
     * 
     * @param tcpTurnClient The TCP TURN client.
     * @param controlling Whether or not this agent will start out as 
     * controlling.  This can change with role conflicts, although that
     * should rarely happen.
     */
    public IceAgentImpl(final StunClient tcpTurnClient, 
        final boolean controlling)
        {
        this.m_iceCandidateDecoder = new IceCandidateSdpDecoderImpl();
        
        // TODO: We need to create separate ICE STUN handlers for ***each***
        // media stream!!!
        this.m_udpStunClient = new IceStunUdpPeer(this);
        this.m_controlling = controlling;
        this.m_tieBreaker = 
            new BigInteger(64, new Random()).toByteArray();

        // TODO: This should actually create a Collection of media streams, 
        // each of which has gathered it's own candidates!!
        final IceCandidateGatherer gatherer =
            new IceCandidateGathererImpl(tcpTurnClient, this.m_udpStunClient, 
                controlling);
        this.m_localCandidates = gatherer.gatherCandidates();
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
        this.m_controlling = controlling;
        }
    
    public void recomputePairPriorities()
        {
        synchronized (this.m_mediaStreams)
            {
            for (final IceMediaStream stream : this.m_mediaStreams)
                {
                stream.recomputePairPriorities(this.m_controlling);
                }
            }
        }
    
    public byte[] getTieBreaker()
        {
        return m_tieBreaker;
        }
    
    public byte[] generateAnswer()
        {
        return encodeCandidates();
        }
    
    public byte[] generateOffer()
        {
        return encodeCandidates();
        }

    private byte[] encodeCandidates()
        {
        final IceCandidateSdpEncoder encoder = new IceCandidateSdpEncoder();
        encoder.visitCandidates(m_localCandidates);
        return encoder.getSdp();
        }

    public Socket createSocket(final ByteBuffer answer) throws IOException
        {
        // TODO: We should process all possible media streams.
        final Collection<IceCandidate> remoteCandidates = 
            this.m_iceCandidateDecoder.decode(answer, this.m_controlling);
        final IceMediaStream mediaStream = 
            new IceMediaStreamImpl(this, m_localCandidates, remoteCandidates);
        
        this.m_mediaStreams.add(mediaStream);
        mediaStream.connect();
        
        final Collection<IceCandidatePair> validPairs = 
            mediaStream.getValidPairs();
        
        synchronized (validPairs)
            {
            for (final IceCandidatePair pair : validPairs)
                {
                final Socket socket = pair.accept(this);
                if (socket != null)
                    {
                    return socket;
                    }
                }
            }
        
        m_log.debug("Could not create socket");
        throw new IOException("Could not create socket");
        }
    
    public Socket visitTcpIceCandidatePair(final TcpIceCandidatePair pair)
        {
        return pair.getSocket();
        }

    public Socket visitUdpIceCandidatePair(final UdpIceCandidatePair pair)
        {
        // TODO: Add processing for UDP pairs.
        return null;
        }

    }
