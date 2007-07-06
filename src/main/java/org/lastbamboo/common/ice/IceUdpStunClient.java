package org.lastbamboo.common.ice;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.channels.DatagramChannel;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.id.uuid.UUID;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.mina.common.ConnectFuture;
import org.apache.mina.common.IoSession;
import org.apache.mina.filter.codec.ProtocolCodecFactory;
import org.apache.mina.filter.codec.ProtocolCodecFilter;
import org.apache.mina.transport.socket.nio.DatagramConnector;
import org.lastbamboo.common.stun.client.StunClientIoHandler;
import org.lastbamboo.common.stun.client.StunClientMessageVisitorFactory;
import org.lastbamboo.common.stun.stack.decoder.StunProtocolCodecFactory;
import org.lastbamboo.common.stun.stack.message.BindingRequest;
import org.lastbamboo.common.stun.stack.message.StunMessage;
import org.lastbamboo.common.stun.stack.message.StunMessageVisitorFactory;
import org.lastbamboo.common.stun.stack.message.SuccessfulBindingResponse;
import org.lastbamboo.common.stun.stack.transaction.StunTransactionFactory;
import org.lastbamboo.common.stun.stack.transaction.StunTransactionFactoryImpl;
import org.lastbamboo.common.stun.stack.transaction.StunTransactionListener;
import org.lastbamboo.common.stun.stack.transaction.StunTransactionTracker;
import org.lastbamboo.common.stun.stack.transaction.StunTransactionTrackerImpl;

/**
 * STUN client implementation for ICE UDP. 
 */
public class IceUdpStunClient implements IceStunClient, StunTransactionListener
    {

    /**
     * The default STUN port.
     */
    private static final int STUN_PORT = 3478;
    
    private final Log LOG = LogFactory.getLog(IceUdpStunClient.class);
    
    private final DatagramConnector m_connector;

    /**
     * This is the address of a public STUN server for obtaining the 
     * "server reflexive" ICE candidate.
     */
    private final InetSocketAddress m_stunServer;

    private final StunClientIoHandler m_ioHandler;

    private final StunTransactionFactory m_transactionFactory;
    
    private final Map<UUID, StunMessage> m_idsToResponses =
        new ConcurrentHashMap<UUID, StunMessage>();

    private final IoSession m_ioSession;

    private final InetSocketAddress m_localAddress;
    
    /**
     * Creates a new STUN client for ICE processing.  This client is capable
     * of obtaining "server reflexive" and "host" candidates.  We don't use
     * relaying for UDP, so this does not currently support generating
     * "relayed" candidates.
     */
    public IceUdpStunClient()
        {
        final StunTransactionTracker tracker = new StunTransactionTrackerImpl();
        m_transactionFactory = new StunTransactionFactoryImpl(tracker);
        
        final StunMessageVisitorFactory messageVisitorFactory =
            new StunClientMessageVisitorFactory(tracker);
        
        m_connector = new DatagramConnector();
        m_connector.getDefaultConfig().getSessionConfig().setReuseAddress(true);
        m_stunServer = new InetSocketAddress("stun01.sipphone.com", STUN_PORT);
        m_ioHandler = new StunClientIoHandler(messageVisitorFactory);
        final ProtocolCodecFactory codecFactory = 
            new StunProtocolCodecFactory();
        final ProtocolCodecFilter stunFilter = 
            new ProtocolCodecFilter(codecFactory);
        
        m_connector.getFilterChain().addLast("stunFilter", stunFilter);
        final ConnectFuture connectFuture = 
            m_connector.connect(m_stunServer, m_ioHandler);
        connectFuture.join();
        m_ioSession = connectFuture.getSession();
        this.m_localAddress = getLocalAddress(m_ioSession);
        }
    

    private InetSocketAddress getLocalAddress(final IoSession ioSession)
        {
        // This insanity is needed because IoSession.getLocalAddress does
        // not, in fact, return the local address!!
        try
            {
            final Method getChannel = 
                ioSession.getClass().getDeclaredMethod("getChannel", new Class[0]);
            getChannel.setAccessible(true);
            
            final DatagramChannel channel = 
                (DatagramChannel) getChannel.invoke(ioSession, new Object[0]);
            return (InetSocketAddress) channel.socket().getLocalSocketAddress();
            }
        catch (SecurityException e)
            {
            LOG.error("Error accessing local address", e);
            }
        catch (NoSuchMethodException e)
            {
            LOG.error("Error accessing local address", e);
            }
        catch (IllegalAccessException e)
            {
            LOG.error("Error accessing local address", e);
            }
        catch (InvocationTargetException e)
            {
            LOG.error("Error accessing local address", e);
            }

        return null;
        }


    public InetSocketAddress getHostAddress()
        {
        return m_localAddress;
        }
    
    public InetAddress getStunServerAddress()
        {
        return this.m_stunServer.getAddress();
        }

    public InetSocketAddress getBaseAddress()
        {
        return getHostAddress();
        }
    

    public InetSocketAddress getServerReflexiveAddress()
        {
        // This method will retransmit the same request multiple times because
        // it's being sent unreliably.  All of these requests will be 
        // identical, using the same transaction ID.
        final BindingRequest bindingRequest = new BindingRequest();
        
        final UUID id = bindingRequest.getTransactionId();
        
        this.m_transactionFactory.createClientTransaction(bindingRequest, this);
        
        int requests = 0;
        
        // Use an RTO of 100ms, as discussed in 
        // draft-ietf-behave-rfc3489bis-06.txt section 7.1.  Note we just 
        // use this value and don't cache previously discovered values for
        // the RTO.
        final long rto = 100L;
        long waitTime = 0L;
        synchronized (bindingRequest)
            {
            while (!m_idsToResponses.containsKey(id) && requests < 7)
                {
                waitIfNoResponse(bindingRequest, waitTime);
                
                // See draft-ietf-behave-rfc3489bis-06.txt section 7.1.  We
                // continually send the same request until we receive a 
                // response, never sending more that 7 requests and using
                // an expanding interval between requests based on the 
                // estimated round-trip-time to the server.  This is because
                // some requests can be lost with UDP.
                m_ioSession.write(bindingRequest);
                
                // Wait a little longer with each send.
                waitTime = (2 * waitTime) + rto;
                
                requests++;
                }
            
            // Now we wait for 1.6 seconds after the last request was sent.
            // If we still don't receive a response, then the transaction 
            // has failed.  
            waitIfNoResponse(bindingRequest, 1600);
            }
        
        
        if (m_idsToResponses.containsKey(id))
            {
            // TODO: This cast is unfortunate.  Anything better?  What can
            // we do here?  Any generics solution?
            final SuccessfulBindingResponse response = 
                (SuccessfulBindingResponse) this.m_idsToResponses.get(id);
            return response.getMappedAddress();
            }
        return null;
        }

    private void waitIfNoResponse(final StunMessage request, 
        final long waitTime)
        {
        LOG.debug("Waiting "+waitTime+" milliseconds...");
        if (waitTime == 0L) return;
        if (!m_idsToResponses.containsKey(request.getTransactionId()))
            {
            try
                {
                LOG.debug("Actually waiting...");
                request.wait(waitTime);
                }
            catch (final InterruptedException e)
                {
                LOG.error("Unexpected interrupt", e);
                }
            }
        }

    public void onTransactionFailed(final StunMessage request)
        {
        synchronized (request)
            {
            request.notify();
            }
        }

    public void onTransactionSucceeded(final StunMessage request, 
        final StunMessage response)
        {
        synchronized (request)
            {
            this.m_idsToResponses.put(request.getTransactionId(), response);
            request.notify();
            }
        }
    }
