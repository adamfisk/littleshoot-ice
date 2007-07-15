package org.lastbamboo.common.ice.stubs;

import java.net.InetAddress;
import java.net.InetSocketAddress;

import org.apache.mina.common.IoSession;
import org.lastbamboo.common.stun.client.StunClient;
import org.lastbamboo.common.stun.stack.message.SuccessfulBindingResponse;

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

    public StunClientStub(InetAddress stunServerAddress, InetSocketAddress hostAddress)
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

    public SuccessfulBindingResponse getBindingResponse()
        {
        // TODO Auto-generated method stub
        return null;
        }

    public IoSession getIoSession()
        {
        // TODO Auto-generated method stub
        return null;
        }

    }
