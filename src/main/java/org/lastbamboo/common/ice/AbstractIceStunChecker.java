package org.lastbamboo.common.ice;

import java.net.InetSocketAddress;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.id.uuid.UUID;
import org.apache.mina.common.ExecutorThreadModel;
import org.apache.mina.common.IoConnector;
import org.apache.mina.common.IoHandler;
import org.apache.mina.common.IoSession;
import org.apache.mina.common.ThreadModel;
import org.apache.mina.filter.codec.ProtocolCodecFactory;
import org.apache.mina.filter.codec.ProtocolCodecFilter;
import org.lastbamboo.common.ice.candidate.IceCandidate;
import org.lastbamboo.common.stun.stack.StunDemuxingIoHandler;
import org.lastbamboo.common.stun.stack.StunIoHandler;
import org.lastbamboo.common.stun.stack.message.BindingRequest;
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
    
    protected IoSession m_ioSession;

    private volatile int m_writeCallsForPair = 0;
    
    protected final Map<UUID, StunMessage> m_idsToResponses =
        new ConcurrentHashMap<UUID, StunMessage>();

    protected final StunTransactionTracker<StunMessage> m_transactionTracker;

    protected final Object m_requestLock = new Object();
    
    /**
     * TODO: Review if this works!!
     */
    protected volatile boolean m_transactionCancelled = false;

    protected final StunDemuxingIoHandler m_demuxer;

    protected final InetSocketAddress m_remoteAddress;
    
    protected final IoConnector m_connector;
    
    /**
     * Creates a new ICE connectivity checker over any transport.
     * 
     * @param localCandidate The local address.
     * @param remoteCandidate The remote address.
     * @param serverMessageVisitorFactory The factory for creating visitors for 
     * incoming messages.
     * @param iceAgent The top-level ICE agent.
     * @param demuxingCodecFactory The {@link ProtocolCodecFactory} for 
     * demultiplexing between STUN and another protocol.
     * @param clazz The top-level message class for the protocol other than 
     * STUN.
     * @param protocolIoHandler The {@link IoHandler} to use for the other 
     * protocol.
     */
    public AbstractIceStunChecker(
        final IceCandidate localCandidate, 
        final IceCandidate remoteCandidate, 
        final StunMessageVisitorFactory serverMessageVisitorFactory, 
        final IceAgent iceAgent, 
        final ProtocolCodecFactory demuxingCodecFactory,
        final Class clazz, final IoHandler protocolIoHandler)
        {
        this(localCandidate, remoteCandidate, new StunTransactionTrackerImpl(),
            serverMessageVisitorFactory, iceAgent, demuxingCodecFactory,
            clazz, protocolIoHandler);
        }

    /**
     * Creates a new ICE connectivity checker over any transport.
     * 
     * @param localCandidate The local address.
     * @param remoteCandidate The remote address.
     * @param serverMessageVisitorFactory The factory for creating visitors for 
     * incoming messages.
     * @param iceAgent The top-level ICE agent.
     * @param demuxingCodecFactory The {@link ProtocolCodecFactory} for 
     * demultiplexing between STUN and another protocol.
     * @param clazz The top-level message class for the protocol other than 
     * STUN.
     * @param protocolIoHandler The {@link IoHandler} to use for the other 
     * protocol.
     */
    public AbstractIceStunChecker(
        final IceCandidate localCandidate, 
        final IceCandidate remoteCandidate, 
        final StunTransactionTracker<StunMessage> transactionTracker,
        final StunMessageVisitorFactory serverMessageVisitorFactory, 
        final IceAgent iceAgent, 
        final ProtocolCodecFactory demuxingCodecFactory,
        final Class clazz, final IoHandler protocolIoHandler)
        {
        this.m_transactionTracker = transactionTracker;
        final StunMessageVisitorFactory<StunMessage> checkerVisitorFactory = 
            new IceStunCheckerMessageVisitorFactory(serverMessageVisitorFactory, 
                this.m_transactionTracker);
        final IoHandler ioHandler = 
            new StunIoHandler<StunMessage>(checkerVisitorFactory);
        
        this.m_demuxer = new StunDemuxingIoHandler(clazz, 
            protocolIoHandler, ioHandler);
        

        final String controllingString;
        if (iceAgent.isControlling())
            {
            controllingString = "Controlling";
            }
        else
            {
            controllingString = "Not-Controlling";
            }
        
        final ThreadModel threadModel = ExecutorThreadModel.getInstance(
            getClass().getSimpleName()+"-"+controllingString);
        final ProtocolCodecFilter stunFilter = 
            new ProtocolCodecFilter(demuxingCodecFactory);
        this.m_remoteAddress = remoteCandidate.getSocketAddress();
        this.m_connector = createConnector(
            localCandidate.getSocketAddress(), 
            remoteCandidate.getSocketAddress(), 
            threadModel, stunFilter, m_demuxer);

        }
    
    protected abstract IoConnector createConnector(
        InetSocketAddress localAddress, InetSocketAddress remoteAddress, 
        ThreadModel threadModel, ProtocolCodecFilter stunFilter, 
        IoHandler demuxer);
    
    protected abstract boolean connect();
    
    public StunMessage write(final BindingRequest bindingRequest, 
        final long rto)
        {
        m_log.debug("Writing Binding Request...");
        
        // TCP implementations, for example, need to establish a connection
        // before performing sending STUN.  If we can't connect, it's a 
        // failure.
        if (!connect())
            {
            return new NullStunMessage();
            }
        
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
    
    protected abstract StunMessage writeInternal(BindingRequest bindingRequest, 
        long rto);
    
    protected final void waitIfNoResponse(final BindingRequest request, 
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

    public void cancelTransaction()
        {
        m_log.debug("Cancelling transaction!!");
        this.m_transactionCancelled = true;
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
