package org.lastbamboo.common.ice;

import java.net.InetAddress;

import org.lastbamboo.common.stun.client.UdpStunClient;

import junit.framework.TestCase;

/**
 * Quick test to check how the local NAT is assigning ports to new external
 * hosts.
 */
public class IceNatPortMappingTest extends TestCase
    {

    public void testAssignedPorts() throws Exception
        {
        final String[] servers = 
            {
            "stun.fwdnet.net",   
            "stun01.sipphone.com",
            "stun.xten.net"
            };
        
        for (int i = 0; i < servers.length; i++)
            {
            final UdpStunClient client = 
                new UdpStunClient(InetAddress.getByName(servers[i]));
            //System.out.println(client.getServerReflexiveAddress());
            }
        }
    }
