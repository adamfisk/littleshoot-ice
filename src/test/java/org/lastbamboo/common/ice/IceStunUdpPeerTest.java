package org.lastbamboo.common.ice;

import java.net.InetSocketAddress;

import junit.framework.TestCase;

import org.lastbamboo.common.stun.stack.message.BindingRequest;
import org.lastbamboo.common.stun.stack.message.StunMessage;
import org.lastbamboo.common.stun.stack.message.StunMessageVisitor;
import org.lastbamboo.common.stun.stack.message.StunMessageVisitorAdapter;
import org.lastbamboo.common.stun.stack.message.SuccessfulBindingResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Test sending of STUN messages between ICE STUN UDP peers.
 */
public class IceStunUdpPeerTest extends TestCase
    {

    private final Logger m_log = LoggerFactory.getLogger(getClass());
    
    public void testIceStunUdpPeers() throws Exception
        {
        final IceStunUdpPeer peer1 = new IceStunUdpPeer();
        final IceStunUdpPeer peer2 = new IceStunUdpPeer();
        
        final InetSocketAddress address1 = peer1.getHostAddress();
        final InetSocketAddress address2 = peer2.getHostAddress();
        
        assertFalse(address1.equals(address2));
        
        m_log.debug("Sending STUN request to: "+address2);
        
        final StunMessageVisitor<InetSocketAddress> visitor = 
            new StunMessageVisitorAdapter<InetSocketAddress>()
            {
            
            @Override
            public InetSocketAddress visitSuccessfulBindingResponse(
                final SuccessfulBindingResponse response)
                {
                return response.getMappedAddress();
                }
            };

        for (int i = 0; i < 10; i++)
            {
            final StunMessage msg1 = peer1.write(new BindingRequest(), address2);
            final InetSocketAddress mappedAddress1 = msg1.accept(visitor);
            assertEquals("Mapped address should equal the local address", 
                address1, mappedAddress1);
            
            final StunMessage msg2 = peer2.write(new BindingRequest(), address1);
            final InetSocketAddress mappedAddress2 = msg2.accept(visitor);
            assertEquals("Mapped address should equal the local address", 
                address2, mappedAddress2);
            }
        }

    }
