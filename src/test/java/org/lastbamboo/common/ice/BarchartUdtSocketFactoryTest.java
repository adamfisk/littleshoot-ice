package org.lastbamboo.common.ice;

import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.SocketAddress;

import org.junit.Test;
import org.lastbamboo.common.ice.stubs.IceAgentStub;
import org.lastbamboo.common.ice.transport.IceConnector;
import org.lastbamboo.common.ice.transport.IceUdpConnector;
import org.lastbamboo.common.stun.client.StunClientMessageVisitorFactory;
import org.littleshoot.mina.common.IoHandler;
import org.littleshoot.mina.common.IoHandlerAdapter;
import org.littleshoot.mina.common.IoSession;
import org.littleshoot.mina.filter.codec.ProtocolCodecFactory;
import org.littleshoot.stun.stack.StunConstants;
import org.littleshoot.stun.stack.StunDemuxableProtocolCodecFactory;
import org.littleshoot.stun.stack.StunIoHandler;
import org.littleshoot.stun.stack.StunProtocolCodecFactory;
import org.littleshoot.stun.stack.message.StunMessage;
import org.littleshoot.stun.stack.message.StunMessageVisitorFactory;
import org.littleshoot.stun.stack.transaction.StunTransactionTracker;
import org.littleshoot.stun.stack.transaction.StunTransactionTrackerImpl;
import org.littleshoot.util.CandidateProvider;
import org.littleshoot.util.CommonUtils;
import org.littleshoot.util.SrvCandidateProvider;
import org.littleshoot.util.SrvUtil;
import org.littleshoot.util.SrvUtilImpl;
import org.littleshoot.util.mina.DemuxableProtocolCodecFactory;
import org.littleshoot.util.mina.DemuxingIoHandler;
import org.littleshoot.util.mina.DemuxingProtocolCodecFactory;

import com.barchart.udt.ResourceUDT;
import com.barchart.udt.net.NetServerSocketUDT;


public class BarchartUdtSocketFactoryTest {

    {
    ResourceUDT.setLibraryExtractLocation(CommonUtils.getLittleShootDir().getAbsolutePath());
    }
    
    //@Test
    public void testUdtBinding() throws Exception {
        ResourceUDT.setLibraryExtractLocation(CommonUtils.getLittleShootDir().getAbsolutePath());
        System.setProperty(ResourceUDT.PROPERTY_LIBRARY_EXTRACT_LOCATION, 
                CommonUtils.getLittleShootDir().getAbsolutePath());
        
        final SocketAddress sa = new InetSocketAddress("127.0.0.1", 19302);
        final DatagramSocket sock = new DatagramSocket(sa);
        //sock.bind(sa);
        
        sock.close();
        
        System.out.println(ResourceUDT.getLibraryExtractLocation());
        final ServerSocket server = new NetServerSocketUDT();
        server.bind(sa);
        
        
        /*
        final BarchartUdtSocketFactory sf = new BarchartUdtSocketFactory();
        final IoSession session = newSession();
        final IceStunUdpPeer peer = peer();
        sf.newSocket(session, false, new OfferAnswerListener() {
            
            @Override
            public void onUdpSocket(Socket arg0) {
                // TODO Auto-generated method stub
                
            }
            
            @Override
            public void onTcpSocket(Socket arg0) {
                // TODO Auto-generated method stub
                
            }
            
            @Override
            public void onOfferAnswerFailed(OfferAnswer arg0) {
                // TODO Auto-generated method stub
                
            }
        }, peer);
        */
    }
    
    private IoSession newSession() {
        final DemuxableProtocolCodecFactory stunCodecFactory =
            new StunDemuxableProtocolCodecFactory();
        final DemuxableProtocolCodecFactory otherCodecFactory =
            new StunDemuxableProtocolCodecFactory();
        final ProtocolCodecFactory codecFactory =
            new DemuxingProtocolCodecFactory(stunCodecFactory, 
                otherCodecFactory);
        final IoHandler clientIoHandler = new IoHandlerAdapter();
        
        final InetSocketAddress remoteAddress =
            //new InetSocketAddress("stun01.sipphone.com", 3478);
            new InetSocketAddress("stun.l.google.com", 19302);
        final StunTransactionTracker<StunMessage> tracker = 
            new StunTransactionTrackerImpl();
        final StunMessageVisitorFactory visitorFactory = 
            new StunClientMessageVisitorFactory<StunMessage>(tracker);
        final StunIoHandler<StunMessage> stunIoHandler =
            new StunIoHandler<StunMessage>(visitorFactory);
        
        final IoHandler demuxingIoHandler =
            new DemuxingIoHandler<StunMessage, Object>(
                StunMessage.class, stunIoHandler, Object.class, 
                clientIoHandler);
        final IceConnector connector = 
            new IceUdpConnector(codecFactory, demuxingIoHandler, true);
        final IoSession ioSession = 
            connector.connect(new InetSocketAddress(4932), remoteAddress);
        return ioSession;
    }
    
    private IceStunUdpPeer peer() throws Exception {
        final IceAgent iceAgent = new IceAgentStub();
        final ProtocolCodecFactory demuxingCodecFactory =
            new StunProtocolCodecFactory();
        final StunTransactionTracker<StunMessage> transactionTracker =
            new StunTransactionTrackerImpl();
    
        final IceStunCheckerFactory checkerFactory =
            new IceStunCheckerFactoryImpl(transactionTracker);
        final StunMessageVisitorFactory<StunMessage> udpMessageVisitorFactory =
            new IceStunConnectivityCheckerFactoryImpl<StunMessage>(iceAgent, 
                transactionTracker, checkerFactory);
        final IoHandler stunIoHandler = 
            new StunIoHandler<StunMessage>(udpMessageVisitorFactory);
        final IoHandler udpIoHandler = 
            new DemuxingIoHandler<StunMessage, Object>(
                StunMessage.class, stunIoHandler, Object.class, 
                new IoHandlerAdapter());
        
        final SrvUtil srv = new SrvUtilImpl();
        final CandidateProvider<InetSocketAddress> stunCandidateProvider =
            new SrvCandidateProvider(srv, "_stun._udp.littleshoot.org", 
                new InetSocketAddress("stun.littleshoot.org", 
                    StunConstants.STUN_PORT));
        return new IceStunUdpPeer(demuxingCodecFactory, udpIoHandler, true, 
                transactionTracker, stunCandidateProvider);
    }
}
