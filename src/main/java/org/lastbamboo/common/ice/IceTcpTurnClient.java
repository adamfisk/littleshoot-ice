package org.lastbamboo.common.ice;

import java.net.InetAddress;
import java.net.InetSocketAddress;

import org.apache.mina.filter.codec.ProtocolCodecFactory;
import org.lastbamboo.common.stun.stack.StunDemuxableProtocolCodecFactory;
import org.lastbamboo.common.stun.stack.message.BindingRequest;
import org.lastbamboo.common.stun.stack.message.StunMessage;
import org.lastbamboo.common.stun.stack.message.StunMessageVisitorFactory;
import org.lastbamboo.common.tcp.frame.TcpFrameCodecFactory;
import org.lastbamboo.common.turn.client.TcpTurnClient;
import org.lastbamboo.common.turn.client.TurnClient;
import org.lastbamboo.common.turn.client.TurnClientListener;
import org.lastbamboo.common.util.ConnectionMaintainerListener;
import org.lastbamboo.common.util.mina.DemuxableProtocolCodecFactory;
import org.lastbamboo.common.util.mina.DemuxingProtocolCodecFactory;

public class IceTcpTurnClient implements TurnClient
    {
    
    private TcpTurnClient m_turnClient;

    public IceTcpTurnClient(
        final StunMessageVisitorFactory<StunMessage> messageVisitorFactory,
        final TurnClientListener turnClientListener,
        final InetSocketAddress turnServerAddress, 
        final ConnectionMaintainerListener<InetSocketAddress> connectionListener)
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

        connect(connectionListener, turnServerAddress);
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

    }
