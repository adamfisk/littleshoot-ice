package org.lastbamboo.common.ice;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;

import org.apache.commons.id.uuid.UUID;
import org.apache.mina.common.ConnectFuture;
import org.apache.mina.common.IoConnector;
import org.apache.mina.common.IoHandler;
import org.apache.mina.common.IoSession;
import org.apache.mina.common.RuntimeIOException;
import org.apache.mina.common.ThreadModel;
import org.apache.mina.filter.codec.ProtocolCodecFactory;
import org.apache.mina.filter.codec.ProtocolCodecFilter;
import org.apache.mina.transport.socket.nio.SocketConnector;
import org.apache.mina.transport.socket.nio.SocketConnectorConfig;
import org.lastbamboo.common.ice.candidate.IceCandidate;
import org.lastbamboo.common.stun.stack.StunDemuxableProtocolCodecFactory;
import org.lastbamboo.common.stun.stack.message.BindingRequest;
import org.lastbamboo.common.stun.stack.message.CanceledStunMessage;
import org.lastbamboo.common.stun.stack.message.NullStunMessage;
import org.lastbamboo.common.stun.stack.message.StunMessage;
import org.lastbamboo.common.stun.stack.transaction.StunTransactionTracker;
import org.lastbamboo.common.tcp.frame.TcpFrame;
import org.lastbamboo.common.tcp.frame.TcpFrameCodecFactory;
import org.lastbamboo.common.util.mina.DemuxableProtocolCodecFactory;
import org.lastbamboo.common.util.mina.DemuxingProtocolCodecFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class that performs STUN connectivity checks for ICE over TCP.  Each 
 * ICE candidate pair has its own connectivity checker. 
 */
public class IceTcpStunChecker extends AbstractIceStunChecker
    {

    private final Logger m_log = 
        LoggerFactory.getLogger(IceTcpStunChecker.class);

    /**
     * Creates a new ICE connectivity checker over TCP.  If the 
     * {@link IoSession} is <code>null</code>, this will connect to create a
     * new session.
     * 
     * @param localCandidate The local address.
     * @param remoteCandidate The remote address.
     * @param iceAgent The top-level ICE agent.
     * @param protocolIoHandler The {@link IoHandler} to use for the other protocol.
     */
    public IceTcpStunChecker(final IceCandidate localCandidate, 
        final IceCandidate remoteCandidate, 
        final IoHandler stunIoHandler,
        final IceAgent iceAgent, 
        final IoSession session,
        final StunTransactionTracker<StunMessage> transactionTracker,
        final IoHandler protocolIoHandler)
        {
        super(localCandidate, remoteCandidate, transactionTracker, 
            stunIoHandler, 
            iceAgent, createCodecFactory(), TcpFrame.class, 
            protocolIoHandler);
        this.m_ioSession = session;
        }
    
    
    private static ProtocolCodecFactory createCodecFactory()
        {
        final DemuxableProtocolCodecFactory stunCodecFactory =
            new StunDemuxableProtocolCodecFactory();
        final DemuxableProtocolCodecFactory tcpFramingCodecFactory =
            new TcpFrameCodecFactory();
        final ProtocolCodecFactory codecFactory = 
            new DemuxingProtocolCodecFactory(stunCodecFactory, 
                tcpFramingCodecFactory);
        return codecFactory;
        }
    
    @Override
    protected IoConnector createConnector(
        final InetSocketAddress localAddress, 
        final InetSocketAddress remoteAddress,
        final ThreadModel threadModel, final ProtocolCodecFilter stunFilter, 
        final IoHandler demuxer)
        {
        if (this.m_connector != null) 
            {
            m_log.debug("Already connected...");
            return this.m_connector;
            }
        
        final SocketConnector connector = new SocketConnector();
        
        final SocketConnectorConfig cfg = connector.getDefaultConfig();
        cfg.getSessionConfig().setReuseAddress(true);
        cfg.setThreadModel(threadModel);
        
        connector.getFilterChain().addLast("stunFilter", stunFilter);
        return connector;
        }

    @Override
    protected boolean connect()
        {
        m_log.debug("Establishing TCP connection...");
        if (this.m_ioSession != null)
            {
            // We might be performing a second check to verify a nominated
            // pair, for example.
            m_log.debug("Already connected");
            return true;
            }
        final InetAddress address = this.m_remoteAddress.getAddress();
        final int connectTimeout;
        // If the address is on the local network, we should be able to 
        // connect more quickly.  If we can't, that likely indicates the 
        // address is just from a different local network.
        if (address.isSiteLocalAddress())
            {
            try
                {
                if (!address.isReachable(400))
                    {
                    return false;
                    }
                }
            catch (final IOException e)
                {
                return false;
                }
            m_log.debug("Address is reachable. Connecting:{}", address);

            // We should be able to connect to local, private addresses 
            // really quickly.  So don't wait around too long.
            connectTimeout = 3000;
            }
        else
            {
            connectTimeout = 12000;
            }
        
        
        // TODO: We don't currently support TCP-SO, so we don't bind to the 
        // local port.
        final ConnectFuture cf = 
            m_connector.connect(this.m_remoteAddress, this.m_demuxingIoHandler);
        cf.join(connectTimeout);
        try
            {
            final IoSession session = cf.getSession();
            if (session == null)
                {
                return false;
                }
            this.m_ioSession = session;
            m_log.debug("TCP STUN checker connected on: "+session);
            return true;
            }
        catch (final RuntimeIOException e)
            {
            // This happens when we can't connect.
            m_log.debug("Could not connect to host: {}", this.m_remoteAddress);
            m_log.debug("Reason for no connection: ", e);
            return false;
            }
        }

    @Override
    protected StunMessage writeInternal(
        final BindingRequest bindingRequest, final long rto)
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
        
        m_log.debug("Waiting for lock...");
        synchronized (m_requestLock)
            {   
            this.m_transactionCancelled = false;
            m_log.debug("Sending Binding Request...");
            this.m_ioSession.write(bindingRequest);
            
            // Now we wait for 1.6 seconds after the last request was sent.
            // If we still don't receive a response, then the transaction 
            // has failed.  
            if (!this.m_transactionCancelled)
                {
                waitIfNoResponse(bindingRequest, 7900);
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
