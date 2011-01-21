package org.lastbamboo.common.ice;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.channels.spi.SelectorProvider;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.barchart.udt.nio.SelectorProviderUDT;


public class SimpleUdtTest {
    
    private final Logger log = LoggerFactory.getLogger(getClass());

    @Test public void testUdtClient() throws Exception {
        
        final InetSocketAddress serverAddress = 
            new InetSocketAddress("127.0.0.1", 12345);
        startThreadedServer(serverAddress);
        final SelectorProvider provider = SelectorProviderUDT.DATAGRAM;
        final SocketChannel clientChannel = provider.openSocketChannel();
        clientChannel.configureBlocking(true);
        final Socket sock = clientChannel.socket();
        final InetSocketAddress clientAddress = 
            new InetSocketAddress("127.0.0.1", 10000);
        sock.bind(clientAddress);
        assert sock.isBound();

        clientChannel.connect(serverAddress);
        assert sock.isConnected();
        
        System.out.println("Connected? "+sock.isConnected());
        
        final SocketChannel ch = sock.getChannel();
        ch.write(ByteBuffer.wrap("HELLO".getBytes()));
    }


    private void startThreadedServer(final InetSocketAddress serverAddress) {
        final Runnable runner = new Runnable() {
            
            public void run() {
                try {
                    startUdtServer(serverAddress);
                } catch (final IOException e) {
                    e.printStackTrace();
                }
            }
        };
        final Thread t = new Thread(runner, "test-thread");
        t.setDaemon(true);
        t.start();
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
    
    private void startUdtServer(final InetSocketAddress serverAddress) 
        throws IOException {
        final SelectorProvider provider = SelectorProviderUDT.DATAGRAM;
        final ServerSocketChannel acceptorChannel = 
            provider.openServerSocketChannel();
        final ServerSocket acceptorSocket = acceptorChannel.socket();
        acceptorSocket.bind(serverAddress);
        
        assert acceptorSocket.isBound();
        System.out.println("Bound on: "+acceptorSocket.getLocalSocketAddress());
        final SocketChannel connectorChannel = acceptorChannel.accept();
        assert connectorChannel.isConnected();
        echo(connectorChannel);
    }
    
    private void echo(final SocketChannel sc) {
        final Runnable runner = new Runnable() {

            public void run() {
                try {
                    System.out.println("About to read");
                    final byte[] data = new byte[8192];
                    final ByteBuffer dst = ByteBuffer.wrap(data);
                    sc.read(dst);
                    System.out.println("\n\n\n\nUDT Server Read: "+new String(data)+"\n\n\n\n");
                    //sc.write(ByteBuffer.wrap(data));
                } catch (final IOException e) {
                    e.printStackTrace();
                }
            }
        };
        final Thread dt = new Thread(runner, "test-thread");
        dt.setDaemon(true);
        dt.start();
    }
}
