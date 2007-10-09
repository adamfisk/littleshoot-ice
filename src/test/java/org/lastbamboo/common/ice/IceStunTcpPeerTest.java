package org.lastbamboo.common.ice;

import static org.junit.Assert.*;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketAddress;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import org.apache.commons.lang.StringUtils;
import org.apache.mina.common.ByteBuffer;
import org.apache.mina.common.IoHandler;
import org.apache.mina.common.IoService;
import org.apache.mina.common.IoServiceConfig;
import org.apache.mina.common.IoServiceListener;
import org.apache.mina.common.IoSession;
import org.junit.Test;
import org.lastbamboo.common.ice.stubs.IceAgentStub;
import org.lastbamboo.common.ice.stubs.TurnClientListenerStub;
import org.lastbamboo.common.stun.client.StunClient;
import org.lastbamboo.common.stun.client.TcpStunClient;
import org.lastbamboo.common.stun.stack.message.StunMessage;
import org.lastbamboo.common.stun.stack.message.StunMessageVisitorFactory;
import org.lastbamboo.common.stun.stack.transaction.StunTransactionTracker;
import org.lastbamboo.common.stun.stack.transaction.StunTransactionTrackerImpl;
import org.lastbamboo.common.tcp.frame.TcpFrame;
import org.lastbamboo.common.tcp.frame.TcpFrameEncoder;
import org.lastbamboo.common.turn.client.TurnClientListener;
import org.lastbamboo.common.util.ShootConstants;
import org.lastbamboo.common.util.mina.MinaUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class IceStunTcpPeerTest
    {

    private final Logger m_log = LoggerFactory.getLogger(getClass());
    
    private final AtomicBoolean m_serverStarted = new AtomicBoolean(false);
    private final AtomicReference<String> m_serverMessageReceived = 
        new AtomicReference<String>();
    
    private static final String MESSAGE = "ITS 3AM AND I'M STILL CODING";
    
    @Test public void testSocketHandling() throws Exception
        {
        startThreadedHttpServer();
        synchronized (this.m_serverStarted)
            {
            if (!this.m_serverStarted.get())
                {
                this.m_serverStarted.wait(4000);
                }
            }
        final TurnClientListener turnClientListener = 
            new TurnClientListenerStub();
        final StunClient client = 
            new IceTcpTurnClient(turnClientListener);
        final StunTransactionTracker<StunMessage> transactionTracker =
            new StunTransactionTrackerImpl();
    
        final IceStunCheckerFactory checkerFactory =
            new IceStunCheckerFactoryImpl(transactionTracker);
        
        final IceAgent iceAgent = new IceAgentStub();
        final StunMessageVisitorFactory messageVisitorFactory =
            new IceStunConnectivityCheckerFactoryImpl<StunMessage>(iceAgent, 
                transactionTracker, checkerFactory);
        
        final IceStunTcpPeer peer = 
            new IceStunTcpPeer(client, messageVisitorFactory, true);
        peer.connect();
        final InetSocketAddress boundAddress = peer.getBoundAddress();
        
        final Socket sock = new Socket();
        sock.connect(boundAddress);
        final TcpFrame frame = new TcpFrame(MESSAGE.getBytes("US-ASCII"));
        final TcpFrameEncoder encoder = new TcpFrameEncoder();
        final ByteBuffer buf = encoder.encode(frame);
        sock.getOutputStream().write(MinaUtils.toByteArray(buf));
        sock.getOutputStream().flush();
        synchronized (this.m_serverMessageReceived)
            {
            if (StringUtils.isEmpty(this.m_serverMessageReceived.get()))
                {
                this.m_serverMessageReceived.wait(6000);
                }
            }
        
        assertFalse(StringUtils.isEmpty(this.m_serverMessageReceived.get()));
        assertEquals(MESSAGE, this.m_serverMessageReceived.get());
        
        final byte[] response = new byte[buf.capacity()];
        sock.getInputStream().read(response);
        final ByteBuffer responseBuf = ByteBuffer.wrap(response);
        final int length = responseBuf.getUnsignedShort();
        //responseBuf.skip(2);
        assertEquals(MESSAGE.length(), length);
        final String responseString = MinaUtils.toAsciiString(responseBuf);
        assertEquals(MESSAGE, responseString);
        }

    private void startThreadedHttpServer()
        {
        final Runnable runner = new Runnable()
            {
            public void run()
                {
                try
                    {
                    startHttpServer();
                    }
                catch (IOException e)
                    {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                    }
                }
            };
        final Thread turnThread = 
            new Thread(runner, "HTTP-Server-Thread");
        turnThread.setDaemon(true);
        turnThread.start();
        }


    private void startHttpServer() throws IOException
        {
        final ServerSocket server = 
            new ServerSocket(ShootConstants.HTTP_PORT);
        synchronized (this.m_serverStarted)
            {
            this.m_serverStarted.set(true);
            this.m_serverStarted.notifyAll();
            }
        while (true)
            {
            final Socket client = server.accept();
            m_log.debug("Accepted client!!");
            final InputStream is = client.getInputStream();
            final byte[] incoming = new byte[MESSAGE.length()];
            is.read(incoming);
            m_serverMessageReceived.set(new String(incoming, "US-ASCII"));
            synchronized (this.m_serverMessageReceived)
                {
                this.m_serverMessageReceived.notifyAll();
                }
            
            final OutputStream os = client.getOutputStream();
            os.write(MESSAGE.getBytes("US-ASCII"));
            os.flush();
            }
        }
    }
