package org.lastbamboo.common.ice;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.channels.spi.SelectorProvider;
import java.util.concurrent.atomic.AtomicLong;

import org.junit.Test;
import org.lastbamboo.common.util.DaemonThread;
import org.lastbamboo.common.util.NetworkUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.barchart.udt.SocketUDT;
import com.barchart.udt.TypeUDT;
import com.barchart.udt.nio.SelectorProviderUDT;


public class UdtTest {
    
    private final Logger log = LoggerFactory.getLogger(getClass());

    private InetSocketAddress serverIsa;
    
    @Test public void testUdtClient() throws Exception {
        SelectorProvider provider = SelectorProviderUDT.DATAGRAM;
        SocketChannel clientChannel = provider.openSocketChannel();
        clientChannel.configureBlocking(true);
        Socket clientSocket = clientChannel.socket();
        InetSocketAddress clientAddress = new InetSocketAddress("localhost", 10000);
        clientSocket.bind(clientAddress);
        assert clientSocket.isBound();
        InetSocketAddress serverAddress = new InetSocketAddress("localhost", 12345);
        clientChannel.connect(serverAddress);
        assert clientSocket.isConnected();
    }

    @Test public void testUdtServer() throws Exception {
        final SelectorProvider provider = SelectorProviderUDT.DATAGRAM;
        final ServerSocketChannel acceptorChannel = provider.openServerSocketChannel();
        final ServerSocket acceptorSocket = acceptorChannel.socket();
        final InetSocketAddress acceptorAddress= new InetSocketAddress(12345);
        acceptorSocket.bind(acceptorAddress);
        
        assert acceptorSocket.isBound();
        System.out.println("Bound on: "+acceptorSocket.getLocalSocketAddress());
        SocketChannel connectorChannel = acceptorChannel.accept();
        assert connectorChannel.isConnected();
        
        final Socket sock = connectorChannel.socket();
        echo(sock);
    }
    
    public void testUdt() throws Exception {
        serverIsa = new InetSocketAddress(NetworkUtils.getLocalHost(), 4729);
        System.out.println("this example tests if barchart-udt maven dependency works");
            final SocketUDT sock = new SocketUDT(TypeUDT.DATAGRAM);
            System.out.println("made socketID={}"+ sock.getSocketId());
            System.out.println("socket status={}"+ sock.getStatus());
            System.out.println("socket isOpen={}"+ sock.isOpen());
            System.out.println("socket isBlocking={}"+ sock.isBlocking());
            System.out.println("socket options{}"+ sock.toStringOptions());
        
        startThreadedServer();
        Thread.sleep(1000);
        sock.connect(serverIsa);
        System.out.println("Connected: "+sock.isConnected());
    }

    private void startThreadedServer() {
        final Runnable runner = new Runnable() {
            
            public void run() {
                startServer();
            }
        };
        final Thread t = new Thread(runner, "test-thread");
        t.setDaemon(true);
        t.start();
    }

    private void startServer() {
        log.info("started SERVER");
        try {
            final SocketUDT acceptor = new SocketUDT(TypeUDT.DATAGRAM);
            //log.info("init; acceptor={}", acceptor.socketID);

            //InetSocketAddress localSocketAddress = new InetSocketAddress(
            //        serverIsa, localPort);
            acceptor.bind(serverIsa);
            serverIsa = acceptor.getLocalSocketAddress();
            log.info("bind; localSocketAddress={}", serverIsa);

            acceptor.listen(10);
            log.info("listen;");
            while (true) {
                final SocketUDT sock = acceptor.accept();
                //echo(sock);
            }
            
            

            /*
            //log.info("accept; receiver={}", receiver.socketID);

            //assert receiver.socketID != acceptor.socketID;

            final long timeStart = System.currentTimeMillis();

            //

            final InetSocketAddress remoteSocketAddress = receiver
                            .getRemoteSocketAddress();

            log.info("receiver; remoteSocketAddress={}", remoteSocketAddress);

            StringBuilder text = new StringBuilder(1024);
            OptionUDT.appendSnapshot(receiver, text);
            text.append("\t\n");
            log.info("receiver options; {}", text);

            final MonitorUDT monitor = receiver.monitor;

            while (true) {

                    final byte[] array = new byte[SIZE];

                    final int result = receiver.receive(array);

                    assert result == SIZE : "wrong size";

                    getSequenceNumber(array);

                    if (sequenceNumber % countMonitor == 0) {

                            receiver.updateMonitor(false);
                            text = new StringBuilder(1024);
                            monitor.appendSnapshot(text);
                            log.info("stats; {}", text);

                            final long timeFinish = System.currentTimeMillis();
                            final long timeDiff = 1 + (timeFinish - timeStart) / 1000;

                            final long byteCount = sequenceNumber * SIZE;
                            final long rate = byteCount / timeDiff;

                            log.info("receive rate, bytes/second: {}", rate);

                    }

            }
            */

            // log.info("result={}", result);

        } catch (Throwable e) {
                log.error("unexpected", e);
        }

    }
    
    private void echo(final Socket sock) {
        final Runnable runner = new Runnable() {

            public void run() {
                try {
                    final BufferedReader br = new BufferedReader(new InputStreamReader(sock.getInputStream()));
                    final String line = br.readLine();
                    final OutputStream os = sock.getOutputStream();
                    os.write(line.getBytes());
                    os.close();
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        };
        final DaemonThread dt = new DaemonThread(runner, "test-thread");
        dt.start();
    }

    static long sequenceNumber = 0;

    static void getSequenceNumber(final byte[] array) {

            final ByteBuffer buffer = ByteBuffer.wrap(array);

            final long currentNumber = buffer.getLong();

            if (currentNumber == sequenceNumber) {
                    sequenceNumber++;
            } else {
                    System.out.println("sequence error; currentNumber={} sequenceNumber={}"+
                                    currentNumber+ sequenceNumber);
                    System.exit(1);
            }

    }
    
    

    final static AtomicLong sequencNumber = new AtomicLong(0);

    private static final int SIZE = 1460;
}
