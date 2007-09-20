package org.lastbamboo.common.ice;

import java.net.InetSocketAddress;

import org.apache.commons.id.uuid.UUID;
import org.apache.mina.common.ConnectFuture;
import org.apache.mina.common.IoConnector;
import org.apache.mina.common.IoHandler;
import org.apache.mina.common.IoSession;
import org.apache.mina.common.ThreadModel;
import org.apache.mina.filter.codec.ProtocolCodecFactory;
import org.apache.mina.filter.codec.ProtocolCodecFilter;
import org.apache.mina.transport.socket.nio.DatagramConnector;
import org.apache.mina.transport.socket.nio.DatagramConnectorConfig;
import org.lastbamboo.common.ice.candidate.IceCandidate;
import org.lastbamboo.common.stun.stack.message.BindingRequest;
import org.lastbamboo.common.stun.stack.message.CanceledStunMessage;
import org.lastbamboo.common.stun.stack.message.NullStunMessage;
import org.lastbamboo.common.stun.stack.message.StunMessage;
import org.lastbamboo.common.stun.stack.transaction.StunTransactionTracker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class that performs STUN connectivity checks for ICE over UDP.  Each 
 * ICE candidate pair has its own connectivity checker. 
 */
public class IceUdpStunChecker extends AbstractIceStunChecker
    {

    private static final Logger m_log = 
        LoggerFactory.getLogger(IceUdpStunChecker.class);
    
    private volatile boolean m_icmpError;
    
    /**
     * Creates a new ICE connectivity checker over UDP.
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
     * @param transactionTracker The class that keeps track of STUN 
     * transactions.
     */
    public IceUdpStunChecker(final IceCandidate localCandidate, 
        final IceCandidate remoteCandidate, 
        final IoHandler stunIoHandler,
        final IceAgent iceAgent, 
        final ProtocolCodecFactory demuxingCodecFactory,
        final Class clazz, final IoHandler ioHandler, 
        final StunTransactionTracker<StunMessage> transactionTracker)
        {
        super(localCandidate, remoteCandidate, transactionTracker, 
            stunIoHandler, iceAgent, demuxingCodecFactory, clazz, 
            ioHandler);
        }

    @Override
    protected IoConnector createConnector(
        final InetSocketAddress localAddress, 
        final InetSocketAddress remoteAddress, 
        final ThreadModel threadModel, 
        final ProtocolCodecFilter stunFilter, 
        final IoHandler demuxer)
        {
        final DatagramConnector connector = new DatagramConnector();
        
        final DatagramConnectorConfig cfg = connector.getDefaultConfig();
        cfg.getSessionConfig().setReuseAddress(true);
        cfg.setThreadModel(threadModel);
        
        connector.getFilterChain().addLast("stunFilter", stunFilter);
        m_log.debug("Connecting from "+localAddress+" to "+remoteAddress);
        final ConnectFuture cf = 
            connector.connect(remoteAddress, localAddress, demuxer);
        cf.join();
        final IoSession session = cf.getSession();
        
        if (session == null)
            {
            m_log.error("Could not create session from "+
                localAddress +" to "+remoteAddress);
            }
        this.m_ioSession = session;
        return connector;
        }

    @Override
    protected boolean connect()
        {
        // This should never happen, but you never know.
        return this.m_ioSession != null;
        }
    
    protected StunMessage writeInternal(final BindingRequest bindingRequest, 
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
    
    }
