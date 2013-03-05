package org.lastbamboo.common.ice;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;

import org.lastbamboo.common.offer.answer.OfferAnswerListener;
import org.littleshoot.mina.common.IoSession;
import org.littleshoot.util.ByteBufferUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Factory for creating "sockets" that really just relay raw UDP data to the
 * other side unreliably but that give the callers the Socket interface they
 * require in certain cases. This is useful, for example, for VoIP apps that
 * send all the data for a call via the browser or an app written in another
 * language that sends all the voice data to LittleShoot through a local 
 * socket.
 */
public class RawUdpSocketFactory implements UdpSocketFactory<Socket> {

    private final Logger log = LoggerFactory.getLogger(getClass());

    @Override
    public void newEndpoint(final IoSession session, final boolean controlling,
            final OfferAnswerListener<Socket> socketListener,
            final IceStunUdpPeer stunUdpPeer, final IceAgent iceAgent) {
        log.info("Creating new raw UDP Socket");
        if (session == null) {
            log.error("Null session: {}", session);
            return;
        }
        stunUdpPeer.close();
        
        DatagramChannel dc;
        try {
            dc = DatagramChannel.open();
            dc.configureBlocking(true);
            dc.socket().bind(session.getLocalAddress());
            dc.connect(session.getRemoteAddress());
            
            final Socket sock = new DatagramSocketWrapper(dc);
            socketListener.onUdpSocket(sock);
        } catch (final IOException e) {
            log.info("Could not create raw UDP socket", e);
        }

    }
    
    private static final class DatagramSocketWrapper extends Socket {
        
        private final DatagramChannel dc;

        private DatagramSocketWrapper(final DatagramChannel dc) {
            this.dc = dc;
            
        }
        
        @Override 
        public InputStream getInputStream() {
            return new DatagramSocketInputStreamWrapper(this.dc);
        }
        @Override
        public OutputStream getOutputStream() {
            return new DatagramSocketOutputStreamWrapper(this.dc);
        }
        
        @Override 
        public synchronized void close() {
            try {
                this.dc.close();
            } catch (final IOException e) {
            }
        }
    }
    
    private static final class DatagramSocketInputStreamWrapper 
        extends InputStream {

        private final DatagramChannel dc;

        public DatagramSocketInputStreamWrapper(final DatagramChannel dc) {
            this.dc = dc;
        }

        @Override
        public int read() throws IOException {
            return read(new byte[1]);
        }
        
        @Override 
        public int read(final byte[] data) throws IOException {
            return read(data, 0, data.length);
        }
        
        @Override 
        public int read(final byte[] byteArray, final int offset, 
            final int length) throws IOException {
            return this.dc.read(ByteBuffer.wrap(byteArray, offset, length));
        }
        
    }
    
    private static final class DatagramSocketOutputStreamWrapper
        extends OutputStream {

        private final DatagramChannel dc;

        public DatagramSocketOutputStreamWrapper(final DatagramChannel dc) {
            this.dc = dc;
        }

        @Override
        public void write(final int b) throws IOException {
            write(new byte[] {(byte) b});
        }

        @Override
        public void write(final byte[] byteArray) throws IOException {
            write(byteArray, 0, byteArray.length);
        }
        
        @Override
        public void write(final byte[] byteArray, final int offset, 
            final int length) throws IOException {
            // Note that in blocking mode this call is guaranteed to write 
            // "length" bytes.
            final int mtu = 1450;
            final ByteBuffer[] srcs = ByteBufferUtils.toArray(byteArray, mtu);
            this.dc.write(srcs, offset, length);
        }
    }
}
