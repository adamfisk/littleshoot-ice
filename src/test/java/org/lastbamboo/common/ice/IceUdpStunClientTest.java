package org.lastbamboo.common.ice;

import org.lastbamboo.common.stun.client.UdpStunClient;

import junit.framework.TestCase;

public class IceUdpStunClientTest extends TestCase
    {

    public void testClient() throws Exception
        {
        final UdpStunClient stunClient = new UdpStunClient();
        stunClient.connect();
        assertNotNull(stunClient.getHostAddress());
        assertNotNull(stunClient.getServerReflexiveAddress());
        assertNotNull(stunClient.getStunServerAddress());
        }
    }
