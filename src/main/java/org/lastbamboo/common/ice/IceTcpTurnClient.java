package org.lastbamboo.common.ice;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.Collection;
import java.util.LinkedList;

import org.apache.mina.common.IoHandler;
import org.apache.mina.filter.codec.ProtocolCodecFactory;
import org.lastbamboo.common.stun.stack.StunDemuxableProtocolCodecFactory;
import org.lastbamboo.common.stun.stack.message.BindingRequest;
import org.lastbamboo.common.stun.stack.message.StunMessage;
import org.lastbamboo.common.tcp.frame.TcpFrameCodecFactory;
import org.lastbamboo.common.turn.client.TcpTurnClient;
import org.lastbamboo.common.turn.client.TurnClient;
import org.lastbamboo.common.turn.client.TurnClientListener;
import org.lastbamboo.common.turn.client.TurnConnectionEstablisher;
import org.lastbamboo.common.turn.client.TurnServerCandidateProvider;
import org.lastbamboo.common.util.CandidateProvider;
import org.lastbamboo.common.util.ConnectionMaintainer;
import org.lastbamboo.common.util.ConnectionMaintainerImpl;
import org.lastbamboo.common.util.ConnectionMaintainerListener;
import org.lastbamboo.common.util.RuntimeIoException;
import org.lastbamboo.common.util.ThreadUtils;
import org.lastbamboo.common.util.ThreadUtilsImpl;
import org.lastbamboo.common.util.mina.DemuxableProtocolCodecFactory;
import org.lastbamboo.common.util.mina.DemuxingProtocolCodecFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * TURN client for ICE.  This specifies the protocol encoders decoders and 
 * {@link IoHandler}s necessary for ICE processing.
 */
public class IceTcpTurnClient implements TurnClient
    {
    
    private final Logger m_log = LoggerFactory.getLogger(getClass());
    
    private final TcpTurnClient m_turnClient;

    /**
     * Creates a new ICE TCP TURN client.  This connects automatically and 
     * blocks until it receives a successful Allocate Response message from 
     * the server.
     * 
     * @param turnClientListener The class that listens for TURN client events,
     * including processing of incoming data.
     */
    public IceTcpTurnClient(final TurnClientListener turnClientListener)
        {
        this(turnClientListener, new TurnServerCandidateProvider());
        }

    /**
     * Convenience constructor that allows the caller to specifiy a single
     * TURN server to connect to.  This connects automatically and 
     * blocks until it receives a successful Allocate Response message from 
     * the server.
     * 
     * @param turnClientListener The listener for TURN client events,
     * including processing of incoming data.
     * @param turnServerAddress The address of the TURN server to connect to.
     */
    public IceTcpTurnClient(final TurnClientListener turnClientListener, 
        final InetSocketAddress turnServerAddress)
        {
        this(turnClientListener, 
            new CandidateProvider<InetSocketAddress>()
            {
            public Collection<InetSocketAddress> getCandidates()
                {
                final Collection<InetSocketAddress> candidates = 
                    new LinkedList<InetSocketAddress>();
                candidates.add(turnServerAddress);
                return candidates;
                }
            });
        }
    
    private IceTcpTurnClient(final TurnClientListener turnClientListener, 
        final CandidateProvider<InetSocketAddress> candidateProvider)
        {
        final DemuxableProtocolCodecFactory stunCodecFactory =
            new StunDemuxableProtocolCodecFactory();
        final DemuxableProtocolCodecFactory tcpFramingCodecFactory =
            new TcpFrameCodecFactory();
        final ProtocolCodecFactory dataCodecFactory = 
            new DemuxingProtocolCodecFactory(stunCodecFactory, 
                tcpFramingCodecFactory);
    
        this.m_turnClient = 
            new TcpTurnClient(turnClientListener, dataCodecFactory);
    
        // Take care of all the connection maintaining code here.
        final TurnConnectionEstablisher connectionEstablisher = 
            new TurnConnectionEstablisher(this.m_turnClient);
        
        final ThreadUtils threadUtils = new ThreadUtilsImpl();
        final ConnectionMaintainer<InetSocketAddress> connectionMaintainer =
            new ConnectionMaintainerImpl<InetSocketAddress, InetSocketAddress>(
                threadUtils, connectionEstablisher, candidateProvider, 1);
        
        connectionMaintainer.start();
        
        // Make sure we connect.
        int connectChecks = 0;
        while (!this.m_turnClient.isConnected() && connectChecks < 300)
            {
            try
                {
                Thread.sleep(100);
                }
            catch (InterruptedException e)
                {
                m_log.error("Thread interrupted??", e);
                }
            connectChecks++;
            }
        if (!this.m_turnClient.isConnected())
            {
            m_log.error("Could not connect to server");
            throw new RuntimeIoException("Could not connect!!");
            }
        }

    public InetSocketAddress getHostAddress()
        {
        return this.m_turnClient.getHostAddress();
        }

    public InetSocketAddress getRelayAddress()
        {
        return this.m_turnClient.getRelayAddress();
        }

    public InetSocketAddress getServerReflexiveAddress()
        {
        return this.m_turnClient.getServerReflexiveAddress();
        }

    public InetAddress getStunServerAddress()
        {
        return this.m_turnClient.getStunServerAddress();
        }

    public void close()
        {
        this.m_turnClient.close();
        }

    public void connect(
        final ConnectionMaintainerListener<InetSocketAddress> listener, 
        final InetSocketAddress stunServerAddress)
        {
        this.m_turnClient.connect(listener, stunServerAddress);
        }

    public void connect(
        final ConnectionMaintainerListener<InetSocketAddress> listener, 
        final InetSocketAddress stunServerAddress, 
        final InetSocketAddress localAddress)
        {
        this.m_turnClient.connect(listener, stunServerAddress, localAddress);
        }

    public InetSocketAddress getMappedAddress()
        {
        return this.m_turnClient.getMappedAddress();
        }

    public void sendConnectRequest(final InetSocketAddress remoteAddress)
        {
        this.m_turnClient.sendConnectRequest(remoteAddress);
        }

    public StunMessage write(final BindingRequest request, 
        final InetSocketAddress remoteAddress)
        {
        return this.m_turnClient.write(request, remoteAddress);
        }

    public StunMessage write(final BindingRequest request, 
        final InetSocketAddress remoteAddress, final long rto)
        {
        return this.m_turnClient.write(request, remoteAddress, rto);
        }

    public boolean isConnected()
        {
        return this.m_turnClient.isConnected();
        }

    }
