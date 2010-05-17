package org.lastbamboo.common.ice;

import org.lastbamboo.common.stun.client.UdpStunClient;

import junit.framework.TestCase;

public class IceUdpStunClientTest extends TestCase
    {

    public void testClient() throws Exception
        {
        final UdpStunClient stunClient = new UdpStunClient("_stun._udp.littleshoot.org");
        stunClient.connect();
        assertNotNull("null host address", stunClient.getHostAddress());
        assertNotNull("null server reflexive address", 
            stunClient.getServerReflexiveAddress());
        assertNotNull("Null STUN server address", 
            stunClient.getStunServerAddress());
        }
    }
