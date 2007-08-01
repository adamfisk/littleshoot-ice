package org.lastbamboo.common.ice.stubs;

import java.net.InetAddress;
import java.net.InetSocketAddress;

import org.lastbamboo.common.stun.client.StunClient;
import org.lastbamboo.common.stun.stack.message.BindingRequest;
import org.lastbamboo.common.stun.stack.message.StunMessage;

public class StunClientStub implements StunClient
    {

    private InetAddress m_stunServerAddress;
    private InetSocketAddress m_hostAddress;

    public StunClientStub(final InetAddress stunServerAddress)
        {
        m_stunServerAddress = stunServerAddress;
        }

    public StunClientStub()
        {
        // TODO Auto-generated constructor stub
        }

    public StunClientStub(final InetAddress stunServerAddress, 
        final InetSocketAddress hostAddress)
        {
        m_stunServerAddress = stunServerAddress;
        m_hostAddress = hostAddress;
        }

    public InetSocketAddress getHostAddress()
        {
        return this.m_hostAddress;
        }

    public InetSocketAddress getServerReflexiveAddress()
        {
        // TODO Auto-generated method stub
        return null;
        }

    public InetAddress getStunServerAddress()
        {
        return this.m_stunServerAddress;
        }

    public InetSocketAddress getRelayAddress()
        {
        // TODO Auto-generated method stub
        return null;
        }

    public StunMessage write(BindingRequest request, InetSocketAddress remoteAddress)
        {
        // TODO Auto-generated method stub
        return null;
        }

    public StunMessage write(BindingRequest request, InetSocketAddress remoteAddress, long rto)
        {
        // TODO Auto-generated method stub
        return null;
        }

    }
