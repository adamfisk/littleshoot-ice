package org.lastbamboo.common.ice;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.StringTokenizer;


import org.apache.commons.id.uuid.UUID;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.lastbamboo.common.sdp.api.Attribute;
import org.lastbamboo.common.sdp.api.MediaDescription;
import org.lastbamboo.common.sdp.api.SdpException;
import org.lastbamboo.common.sdp.api.SdpParseException;
import org.lastbamboo.common.sdp.api.SessionDescription;

/**
 * Factory class for creating ICE candidates from offer/answer data.
 */
public final class IceCandidateFactoryImpl implements IceCandidateFactory
    {
    
    /**
     * Logger for this class.
     */
    private static final Log LOG = 
        LogFactory.getLog(IceCandidateFactoryImpl.class);
    
    private static final String CANDIDATE_KEY = "candidate";
    
    public Collection createCandidates(final SessionDescription sdp) 
        throws SdpException
        {
        final Collection mediaDescriptions = sdp.getMediaDescriptions(true);
        LOG.trace("Creating candidates from media descs: "+mediaDescriptions);
        return createCandidatesFromMediaDescriptions(mediaDescriptions);
        }
    
    /**
     * Sends TURN "Send Request"s to the collection of candidate connections 
     * from the peer's SDP.
     * 
     * @param remoteMediaDescriptions The <code>Collection</code> of media
     * description's from the peer's SDP.  Each media description will contain
     * some number of candidates for exchanging media.
     */
    private Collection createCandidatesFromMediaDescriptions(
        final Collection remoteMediaDescriptions)
        {
        final Collection candidates = new LinkedList();
        for (final Iterator iter = remoteMediaDescriptions.iterator(); 
            iter.hasNext();)
            {
            final MediaDescription mediaDesc = (MediaDescription) iter.next();
            final Collection attributes = mediaDesc.getAttributes(true);
            for (final Iterator iterator = attributes.iterator(); 
                iterator.hasNext();)
                {
                final Attribute attribute = (Attribute) iterator.next();
                try
                    {
                    if (attribute.getName().equals(CANDIDATE_KEY))
                        {
                        final String attributeValue = attribute.getValue();                  
                        candidates.addAll(createCandidates(attributeValue));
                        }
                    }
                catch (final UnknownHostException e)
                    {
                    LOG.warn("Could not parse SDP", e);
                    // Go to the next candidate.
                    continue;
                    }
                catch (final SdpParseException e)
                    {
                    LOG.warn("Could not parse SDP", e);
                    // Go to the next candidate.
                    continue;
                    }
                }
            }
        return candidates;
        }


    /**
     * Sends a TURN send request to a TURN server to enable incoming data
     * for a specific candidate for a specific media.
     * 
     * @param tracker The tracker for the status of the available ICE 
     * connection candidates.
     * @param attribute The attribute string to parse in order to send a
     * TURN send request to the server in the candidate.
     * @throws UnknownHostException If we could not parse the host 
     */
    private Collection createCandidates(final String attribute) 
        throws UnknownHostException
        {
        LOG.trace("Parsing attribute: "+attribute);
        final List candidates = new LinkedList();
        final StringTokenizer st = new StringTokenizer(attribute, " ");
        while (st.hasMoreTokens())
            {
            final IceCandidate candidate = createIceCandidate(st);
            candidates.add(candidate);
            }
        return candidates;
        }
    
    private IceCandidate createIceCandidate(final StringTokenizer st) 
        throws UnknownHostException 
        {
        final int candidateId = Integer.parseInt(st.nextToken());
        final UUID transportId = UUID.fromString(st.nextToken());
        final String transportString = st.nextToken();
        final int qValue = Integer.parseInt(st.nextToken());
        final InetAddress address = InetAddress.getByName(st.nextToken());
        
        final int port = Integer.parseInt(st.nextToken());
        
        if (transportString.equalsIgnoreCase(IceConstants.TCP_PASS))
            {
            return new TcpPassiveIceCandidate(candidateId, transportId, qValue, 
                new InetSocketAddress(address, port));
            }
        else if (transportString.equalsIgnoreCase(IceConstants.TCP_SO))
            {
            return new TcpSoIceCandidate(candidateId, transportId, qValue,
                new InetSocketAddress(address, port));
            }
        else if (transportString.equalsIgnoreCase("udp"))
            {
            return new UdpIceCandidate(candidateId, transportId, qValue,
                new InetSocketAddress(address, port));
            }
        else 
            {
            return new UnknownIceCandidate(candidateId, transportId, qValue,
                new InetSocketAddress(address, port), transportString);
            }
        }

    }
