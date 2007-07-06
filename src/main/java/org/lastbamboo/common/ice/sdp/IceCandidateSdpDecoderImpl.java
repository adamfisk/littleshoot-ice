package org.lastbamboo.common.ice.sdp;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Scanner;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.lastbamboo.common.ice.IceCandidateType;
import org.lastbamboo.common.ice.IceTransportProtocol;
import org.lastbamboo.common.ice.candidate.IceCandidate;
import org.lastbamboo.common.ice.candidate.IceTcpHostPassiveCandidate;
import org.lastbamboo.common.ice.candidate.IceTcpRelayPassiveCandidate;
import org.lastbamboo.common.ice.candidate.IceUdpHostCandidate;
import org.lastbamboo.common.sdp.api.Attribute;
import org.lastbamboo.common.sdp.api.MediaDescription;
import org.lastbamboo.common.sdp.api.SdpException;
import org.lastbamboo.common.sdp.api.SdpParseException;
import org.lastbamboo.common.sdp.api.SessionDescription;

/**
 * Factory class for creating ICE candidates from offer/answer data.
 */
public final class IceCandidateSdpDecoderImpl implements IceCandidateSdpDecoder
    {
    
    /**
     * Logger for this class.
     */
    private static final Log LOG = 
        LogFactory.getLog(IceCandidateSdpDecoderImpl.class);
    
    private static final String CANDIDATE_KEY = "candidate";
    
    public Collection<IceCandidate> decode(
        final SessionDescription sdp) throws SdpException
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
    private Collection<IceCandidate> createCandidatesFromMediaDescriptions(
        final Collection remoteMediaDescriptions)
        {
        final Collection<IceCandidate> candidates = 
            new LinkedList<IceCandidate>();
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
    private Collection<IceCandidate> createCandidates(final String attribute) 
        throws UnknownHostException
        {
        LOG.trace("Parsing attribute: "+attribute);
        final List<IceCandidate> candidates = new LinkedList<IceCandidate>();
        final Scanner scanner = new Scanner(attribute);
        scanner.useDelimiter(" ");
        while (scanner.hasNext())
            {
            final IceCandidate candidate = createIceCandidate(scanner);
            candidates.add(candidate);
            }
        return candidates;
        }
    
    private IceCandidate createIceCandidate(final Scanner scanner) 
        throws UnknownHostException 
        {
        final int foundation = Integer.parseInt(scanner.next());
        final int componentId = Integer.parseInt(scanner.next());
        final String transportString = scanner.next();
        final IceTransportProtocol transportProtocol = 
            IceTransportProtocol.toTransport(transportString);
        final int priority = Integer.parseInt(scanner.next());
        final InetAddress address = InetAddress.getByName(scanner.next());
        final int port = Integer.parseInt(scanner.next());
        final InetSocketAddress socketAddress = 
            new InetSocketAddress(address, port);
        
        final String typeToken = scanner.next();
        if (!typeToken.equals("typ"))
            {
            LOG.error("Unexpected type token: "+typeToken);
            }
        
        final IceCandidateType type = IceCandidateType.toType(scanner.next());
        
        switch (transportProtocol)
            {
            case UDP:
                switch (type)
                    {
                    case HOST:
                        return new IceUdpHostCandidate(socketAddress);
                    case RELAYED:
                        break;
                    case PEER_REFLEXIVE:
                        break;
                    case SERVER_REFLEXIVE:
                        break;
                    }
                break;
            case TCP_PASS:
                switch (type)
                    {
                    case HOST:
                        return new IceTcpHostPassiveCandidate(socketAddress);
                    case RELAYED:
                        LOG.debug("Received a TCP relay passive candidate");
                        final String raddr = scanner.next();
                        if (!raddr.equals("raddr"))
                            {
                            LOG.error("Bad related address: "+raddr);
                            }
                        final InetAddress relatedAddress = 
                            InetAddress.getByName(scanner.next());
                        
                        final String rport = scanner.next();
                        if (!rport.equals("rport"))
                            {
                            LOG.error("Bad related port: "+rport);
                            }
                        final int relatedPort = 
                            Integer.parseInt(scanner.next());
                        return new IceTcpRelayPassiveCandidate(socketAddress, 
                            foundation, relatedAddress, relatedPort);
                    case PEER_REFLEXIVE:
                        break;
                    case SERVER_REFLEXIVE:
                        break;
                    }
                break;
            case TCP_SO:
                // Not currently used.  Awaiting progress on the ICE TCP
                // draft before implementing things that could change.
                switch (type)
                    {
                    case HOST:
                        break;
                    case RELAYED:
                        break;
                    case PEER_REFLEXIVE:
                        break;
                    case SERVER_REFLEXIVE:
                        break;
                    }
                break;
            case TCP_ACT:
                // Not currently used.  Awaiting progress on the ICE TCP
                // draft before implementing things that could change.
                switch (type)
                    {
                    case HOST:
                        break;
                    case RELAYED:
                        break;
                    case PEER_REFLEXIVE:
                        break;
                    case SERVER_REFLEXIVE:
                        break;
                    }
                break;
            case UNKNOWN:
                LOG.warn("Received unknown transport: "+transportString);
                break;
            }
        
        return null;
        }
    }
