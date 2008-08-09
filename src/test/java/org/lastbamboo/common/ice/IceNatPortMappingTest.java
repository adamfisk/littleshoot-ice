package org.lastbamboo.common.ice;

import static org.junit.Assert.assertNotNull;

import java.net.InetSocketAddress;

import org.junit.Test;
import org.lastbamboo.common.stun.client.UdpStunClient;

/**
 * Quick test to check how the local NAT is assigning ports to new external
 * hosts.
 */
public class IceNatPortMappingTest
    {

    @Test public void testAssignedPorts() throws Exception
        {
        
        // We do this a bunch of times because we try random servers.
        for (int i = 0; i < 10; i++)
            {
            final UdpStunClient client = new UdpStunClient();
            final InetSocketAddress srflx = client.getServerReflexiveAddress();
            //System.out.println("Got address: "+srflx);
            assertNotNull("Did not get server reflexive address", srflx);
            }
        }
    }
