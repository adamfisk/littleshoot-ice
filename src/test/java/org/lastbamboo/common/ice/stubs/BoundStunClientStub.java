package org.lastbamboo.common.ice.stubs;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;

import org.lastbamboo.common.stun.client.BoundStunClient;
import org.lastbamboo.common.stun.stack.message.BindingRequest;
import org.lastbamboo.common.stun.stack.message.StunMessage;
import org.lastbamboo.common.util.NetworkUtils;

public class BoundStunClientStub implements BoundStunClient
    {

    private InetAddress m_stunServerAddress;
    private InetSocketAddress m_hostAddress;
    private InetSocketAddress m_serverReflexiveAddress =
        new InetSocketAddress("76.24.52.2", 4820);

    public BoundStunClientStub(final InetAddress stunServerAddress)
        {
        m_stunServerAddress = stunServerAddress;
        try
            {
            m_hostAddress = 
                new InetSocketAddress(NetworkUtils.getLocalHost(), 48302);
            }
        catch (final UnknownHostException e)
            {
            }
        }

    public BoundStunClientStub()
        {
        try
            {
            m_stunServerAddress = InetAddress.getByName("32.34.3.2");
            m_hostAddress = 
                new InetSocketAddress(NetworkUtils.getLocalHost(), 48302);
            }
        catch (final UnknownHostException e)
            {
            }
        }

    public BoundStunClientStub(final InetAddress stunServerAddress, 
        final InetSocketAddress hostAddress)
        {
        m_stunServerAddress = stunServerAddress;
        m_hostAddress = hostAddress;
        }
    
    public BoundStunClientStub(final InetSocketAddress serverReflexiveAddress,
        final int hostPort)
        {
        try
            {
            m_stunServerAddress = InetAddress.getByName("32.34.3.2");
            m_hostAddress = 
                new InetSocketAddress(NetworkUtils.getLocalHost(), hostPort);
            }
        catch (final UnknownHostException e)
            {
            }
        m_serverReflexiveAddress = serverReflexiveAddress;
        }

    public InetSocketAddress getHostAddress()
        {
        return this.m_hostAddress;
        }

    public InetSocketAddress getServerReflexiveAddress()
        {
        return m_serverReflexiveAddress;
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
    
    public InetSocketAddress getBoundAddress()
        {
        // TODO Auto-generated method stub
        return null;
        }

    }
