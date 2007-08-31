package org.lastbamboo.common.ice;

import java.net.InetSocketAddress;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.id.uuid.UUID;
import org.apache.mina.common.IoHandler;
import org.apache.mina.common.IoSession;
import org.apache.mina.filter.codec.ProtocolCodecFactory;
import org.lastbamboo.common.ice.candidate.IceCandidate;
import org.lastbamboo.common.stun.stack.message.BindingRequest;
import org.lastbamboo.common.stun.stack.message.CanceledStunMessage;
import org.lastbamboo.common.stun.stack.message.NullStunMessage;
import org.lastbamboo.common.stun.stack.message.StunMessage;
import org.lastbamboo.common.stun.stack.message.StunMessageVisitorFactory;
import org.lastbamboo.common.stun.stack.transaction.StunTransactionListener;
import org.lastbamboo.common.stun.stack.transaction.StunTransactionTracker;
import org.lastbamboo.common.stun.stack.transaction.StunTransactionTrackerImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Abstract class for STUN connectivity checkers.  This performs STUN checks
 * and notifies the callers of responses.  Subclasses supply the transport.
 */
public abstract class AbstractIceStunChecker implements IceStunChecker,
    StunTransactionListener
    {

    private final Logger m_log = LoggerFactory.getLogger(getClass());
    
    private final IoSession m_ioSession;

    /**
     * TODO: Review if this works!!
     */
    private volatile boolean m_transactionCancelled = false;

    private volatile int m_writeCallsForPair = 0;
    
    private final Map<UUID, StunMessage> m_idsToResponses =
        new ConcurrentHashMap<UUID, StunMessage>();

    private final StunTransactionTracker<StunMessage> m_transactionTracker;

    private volatile boolean m_icmpError;
    
    private final Object m_requestLock = new Object();

    private StunMessageVisitorFactory<StunMessage> m_checkerVisitorFactory;

    /**
     * Creates a new ICE connectivity checker over any transport.
     * 
     * @param localCandidate The local address.
     * @param remoteCandidate The remote address.
     * @param messageVisitorFactory The factory for creating visitors for 
     * incoming messages.
     * @param iceAgent The top-level ICE agent.
     * @param demuxingCodecFactory The {@link ProtocolCodecFactory} for 
     * demultiplexing between STUN and another protocol.
     * @param clazz The top-level message class the protocol other than STUN.
     * @param ioHandler The {@link IoHandler} to use for the other protocol.
     */
    public AbstractIceStunChecker(
        final IceCandidate localCandidate, 
        final IceCandidate remoteCandidate, 
        final StunMessageVisitorFactory messageVisitorFactory, 
        final IceAgent iceAgent, 
        final ProtocolCodecFactory demuxingCodecFactory,
        final Class clazz, final IoHandler ioHandler)
        {
        this.m_transactionTracker = new StunTransactionTrackerImpl();
        this.m_checkerVisitorFactory = 
            new IceStunCheckerMessageVisitorFactory(messageVisitorFactory, 
                this.m_transactionTracker);
        this.m_ioSession = createClientSession(localCandidate, remoteCandidate, 
            iceAgent.isControlling(), 
            this.m_checkerVisitorFactory,
            demuxingCodecFactory, clazz, ioHandler);

        }
    
    protected abstract IoSession createClientSession(
        IceCandidate localCandidate, IceCandidate remoteCandidate, 
        boolean controlling, 
        StunMessageVisitorFactory<StunMessage> visitorFactory,
        ProtocolCodecFactory demuxingCodecFactory, Class clazz, 
        IoHandler ioHandler);
    
    public StunMessage write(final BindingRequest bindingRequest, 
        final long rto)
        {
        m_log.debug("Writing Binding Request...");
        this.m_writeCallsForPair++;
        try
            {
            return writeInternal(bindingRequest, rto);
            }
        catch (final Throwable t)
            {
            m_log.error("Could not write Binding Request", t);
            return new NullStunMessage();
            }
        }
    
    private StunMessage writeInternal(final BindingRequest bindingRequest, 
        final long rto)
        {
        if (bindingRequest == null)
            {
            throw new NullPointerException("Null Binding Request");
            }
        
        // This method will retransmit the same request multiple times because
        // it's being sent unreliably.  All of these requests will be 
        // identical, using the same transaction ID.
        final UUID id = bindingRequest.getTransactionId();
        final InetSocketAddress localAddress = 
            (InetSocketAddress) this.m_ioSession.getLocalAddress();
        final InetSocketAddress remoteAddress =
            (InetSocketAddress) this.m_ioSession.getRemoteAddress();
        
        this.m_transactionTracker.addTransaction(bindingRequest, this, 
            localAddress, remoteAddress);
        
        synchronized (m_requestLock)
            {   
            this.m_transactionCancelled = false;
            int requests = 0;
            
            long waitTime = 0L;

            while (!m_idsToResponses.containsKey(id) && requests < 7 &&
                !this.m_transactionCancelled && !this.m_icmpError)
                {
                waitIfNoResponse(bindingRequest, waitTime);
                if (this.m_icmpError)
                    {
                    m_log.debug("ICMP error -- breaking");
                    break;
                    }
                
                // See draft-ietf-behave-rfc3489bis-06.txt section 7.1.  We
                // continually send the same request until we receive a 
                // response, never sending more that 7 requests and using
                // an expanding interval between requests based on the 
                // estimated round-trip-time to the server.  This is because
                // some requests can be lost with UDP.
                this.m_ioSession.write(bindingRequest);
                
                // Wait a little longer with each send.
                waitTime = (2 * waitTime) + rto;
                
                requests++;
                }
            
            // Now we wait for 1.6 seconds after the last request was sent.
            // If we still don't receive a response, then the transaction 
            // has failed.  
            if (!this.m_transactionCancelled && !this.m_icmpError)
                {
                waitIfNoResponse(bindingRequest, 1600);
                }

            // Even if the transaction was canceled, we still may have 
            // received a successful response.  If we did, we process it as
            // usual.  This is specified in 7.2.1.4.
            if (m_idsToResponses.containsKey(id))
                {
                final StunMessage response = this.m_idsToResponses.remove(id);
                m_log.debug("Returning STUN response: {}", response);
                return response;
                }
            
            if (this.m_transactionCancelled)
                {
                m_log.debug("The transaction was cancelled!");
                return new CanceledStunMessage();
                }
            
            else
                {
                // This will happen quite often, such as when we haven't
                // yet successfully punched a hole in the firewall.
                m_log.debug("Did not get response on: {}", this.m_ioSession);
                return new NullStunMessage();
                }
            }
        }

    public void cancelTransaction()
        {
        m_log.debug("Cancelling transaction!!");
        this.m_transactionCancelled = true;
        }
    
    private void waitIfNoResponse(final BindingRequest request, 
        final long waitTime)
        {
        if (waitTime == 0L) return;
        if (!m_idsToResponses.containsKey(request.getTransactionId()))
            {
            try
                {
                m_requestLock.wait(waitTime);
                }
            catch (final InterruptedException e)
                {
                m_log.error("Unexpected interrupt", e);
                }
            }
        }

    public Object onTransactionFailed(final StunMessage request,
        final StunMessage response)
        {
        m_log.warn("Transaction failed");
        return notifyWaiters(request, response);
        }
    
    public Object onTransactionSucceeded(final StunMessage request, 
        final StunMessage response)
        {
        return notifyWaiters(request, response);
        }

    private Object notifyWaiters(final StunMessage request, 
        final StunMessage response)
        {
        synchronized (m_requestLock)
            {
            this.m_idsToResponses.put(request.getTransactionId(), response);
            m_requestLock.notify();
            }
        return null;
        }

    public IoSession getIoSession()
        {
        return this.m_ioSession;
        }
    }
