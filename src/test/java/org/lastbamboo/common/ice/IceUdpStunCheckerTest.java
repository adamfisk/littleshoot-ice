package org.lastbamboo.common.ice;

import java.net.InetSocketAddress;

import org.apache.mina.common.IoHandler;
import org.apache.mina.common.IoHandlerAdapter;
import org.apache.mina.common.IoSession;
import org.apache.mina.filter.codec.ProtocolCodecFactory;
import org.junit.Assert;
import org.junit.Test;
import org.lastbamboo.common.ice.candidate.IceCandidate;
import org.lastbamboo.common.ice.candidate.IceUdpHostCandidate;
import org.lastbamboo.common.ice.stubs.IceAgentStub;
import org.lastbamboo.common.stun.stack.StunDemuxableProtocolCodecFactory;
import org.lastbamboo.common.stun.stack.message.BindingRequest;
import org.lastbamboo.common.stun.stack.message.BindingSuccessResponse;
import org.lastbamboo.common.stun.stack.message.StunMessage;
import org.lastbamboo.common.util.mina.DemuxableProtocolCodecFactory;
import org.lastbamboo.common.util.mina.DemuxingProtocolCodecFactory;

/**
 * Test for the ICE connectivity checker. 
 */
public class IceUdpStunCheckerTest
    {

    /**
     * Basic test for the STUN checker.
     * 
     * @throws Exception If any unexpected error occurs.
     */
    @Test
    public void testStunChecker() throws Exception
        {
        final DemuxableProtocolCodecFactory stunCodecFactory =
            new StunDemuxableProtocolCodecFactory();
        final DemuxableProtocolCodecFactory otherCodecFactory =
            new StunDemuxableProtocolCodecFactory();
        final ProtocolCodecFactory codecFactory =
            new DemuxingProtocolCodecFactory(stunCodecFactory, 
                otherCodecFactory);
        final IoHandler clientIoHandler = new IoHandlerAdapter();
        
        final IceCandidate localCandidate =
            new IceUdpHostCandidate(new InetSocketAddress(4932), true);
        final InetSocketAddress remoteAddress =
            new InetSocketAddress("stun01.sipphone.com", 3478);
        final IceCandidate remoteCandidate =
            new IceUdpHostCandidate(remoteAddress, false);
        final IceStunServerConnectivityChecker brh = 
            new IceStunServerConnectivityChecker()
            {
            public void handleBindingRequest(final IoSession ioSession, 
                final BindingRequest binding)
                {
                }
            };
        final IceAgent iceAgent = new IceAgentStub();
        final IceUdpStunChecker checker = 
            new IceUdpStunChecker(localCandidate, remoteCandidate, brh, 
                iceAgent, codecFactory, Object.class, clientIoHandler);
        
        final BindingRequest bindingRequest = new BindingRequest();
        final long rto = 20;
        final StunMessage response = checker.write(bindingRequest, rto);
        
        Assert.assertTrue(response instanceof BindingSuccessResponse);
        }
    }
