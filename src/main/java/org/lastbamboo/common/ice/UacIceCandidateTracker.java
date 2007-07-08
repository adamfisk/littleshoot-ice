package org.lastbamboo.common.ice;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.Comparator;
import java.util.Iterator;
import java.util.concurrent.PriorityBlockingQueue;

import org.apache.commons.lang.ObjectUtils.Null;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.lastbamboo.common.ice.candidate.IceCandidate;
import org.lastbamboo.common.ice.candidate.IceTcpActiveCandidate;
import org.lastbamboo.common.util.NetworkUtils;

/**
 * Determines the best ICE candidate socket to use for a TURN User-Agent
 * client, or "UAC".
 */
public final class UacIceCandidateTracker extends AbstractIceCandidateTracker
    {
    
    /**
     * Logger for this class.
     */
    private static final Log LOG = 
        LogFactory.getLog(UacIceCandidateTracker.class);
    
    private final PriorityBlockingQueue<IceCandidate> m_resolvedCandidates;
    
    private final Object m_resolvedCandidatesLock = new Object();
    private volatile int m_connectResults;

    /**
     * Creates a new tracker for ICE candidates for a SIP "user-agent client."
     */
    public UacIceCandidateTracker()
        {
        final Comparator<IceCandidate> tcpIceCandidateComparator = 
            new IceCandidateComparator();
        this.m_resolvedCandidates = 
            new PriorityBlockingQueue<IceCandidate>(4, 
                tcpIceCandidateComparator);
        }

    public Socket getBestSocket() throws IceException 
        {
        LOG.trace("Creating UAC sockets...");
        
        if (LOG.isTraceEnabled())
            {
            LOG.trace("Visiting " + this.m_udpCandidates.size() + 
                " UDP candidates...");
            }
        
        // We loop through the STUN candidates first.  If we can't connect to
        // any of them, we try the TURN candidates.
        synchronized (this.m_udpCandidates)
            {
            for (final Iterator iter = this.m_udpCandidates.iterator(); 
                iter.hasNext();)
                {
                final IceCandidate candidate = (IceCandidate) iter.next();
                final InetSocketAddress socketAddress = 
                    candidate.getSocketAddress();
                
                // TODO: Connect with some form of reliable UDP.
                /*
                try
                    {
                    final Socket socket = 
                        new UDPConnection(socketAddress.getAddress(), 
                            socketAddress.getPort());
                    LOG.trace("Connected to UDP socket!!!");
                    return socket;
                    }
                catch (final IOException e)
                    {
                    LOG.debug("Could not connect to STUN candidate!!");
                    }
                    */
                }
            }
        
        if (LOG.isDebugEnabled())
            {
            LOG.debug("Visiting " + this.m_tcpPassiveRemoteCandidates.size() + 
                " TCP candidates...");
            }
        
        synchronized (this.m_tcpPassiveRemoteCandidates)
            {
            for (final IceCandidate candidate : this.m_tcpPassiveRemoteCandidates)
                {
                connectToTcpCandidate(candidate);
                }
            }
        
        synchronized (this.m_tcpSoCandidates)
            {
            for (final IceCandidate candidate : this.m_tcpSoCandidates)
                {
                connectToTcpSoCandidate(candidate);
                }
            }
        
        synchronized (this.m_resolvedCandidatesLock)
            {
            final long startTime = System.currentTimeMillis();
            final long timeToWait = 8*1000;
            while (this.m_connectResults < this.m_tcpPassiveRemoteCandidates.size())
                {
                final IceCandidate ic = this.m_resolvedCandidates.peek();
                if (ic != null && ic.getPriority() == 1)
                    {
                    LOG.debug("We've got a top priority candidate...");
                    break;
                    }
                final long curTime = System.currentTimeMillis();
                final long elapsedTime = curTime - startTime;
                if (elapsedTime >= timeToWait)
                    {
                    LOG.debug("Too much time elapsed...");
                    break;
                    }
                
                final long waitTime = timeToWait-elapsedTime;
                LOG.debug("About to wait for "+waitTime+" ms...");
                try
                    {
                    this.m_resolvedCandidatesLock.wait(waitTime);       
                    }
                catch (final InterruptedException e)
                    {
                    LOG.error("Interrupted wait", e);
                    }
                    
                }
            }
        
        final IceCandidate bestCandidate = this.m_resolvedCandidates.peek();
        if (bestCandidate == null)
            {
            LOG.warn("Could not connect to any ICE candidates from: "+
                this.m_tcpPassiveRemoteCandidates + "\n" +
                this.m_tcpActiveRemoteCandidates + "\n" +
                this.m_tcpSoCandidates + "\n" +
                this.m_udpCandidates);
            throw new IceException("Could not connect to ICE candidates");
            }
        
        // Close any sockets aside from the best one.
        for (final IceCandidate ic : this.m_resolvedCandidates)
            {
            if (ic != bestCandidate) 
                {
                try
                    {
                    ic.getSocket().close();
                    }
                catch (final IOException e)
                    {
                    LOG.debug("Error closing socket...");
                    }
                }
            }
        final Socket sock = bestCandidate.getSocket();
        
        return sock;
        }

    private void connectToTcpSoCandidate(final IceCandidate candidate)
        {
        // TODO Use the STUNT code here?  Implement it ourselves? The STUNT 
        // code would look something like the code here:
        // https://gforge.cis.cornell.edu/plugins/scmcvs/cvsweb.php/stunt_java/src/EchoClient.java?rev=1.4;content-type=text%2Fplain;cvsroot=cvsroot%2Fnutss
        
        
        // Here's Saikat description of what needs to happen:
        /*
            The STUNT dance, however, is more complicated that just using bind() and
            reuseaddr() -- certain other actions need to be taken as we discussed in
            our IMC paper that I'll repeat here. After the public IP/port has been
            discovered for a given local port, one side calls
            bind()/setsockopt(so_reuseaddr)/connect(), from that local port, then
            that side calls close() a few hundred millisecons later, calls
            bind()/setsockopt() on that local port once again, and finally listen().
            The other side then calls bind()/setsockopt()/connect() to the public
            IP/port of the first side.
         */
        }

    /**
     * Creates a blocking connection to the ICE candidate.  We can't use
     * NIO for connects here because the HTTP requests are ultimately made
     * using the blocking {@link OutputStream}.
     * 
     * @param candidate The ICE candidate to connect to.
     */
    private void connectToTcpCandidate(final IceCandidate candidate)
        {
        final InetSocketAddress socketAddress = candidate.getSocketAddress();
        final int soTimeout;
        if (NetworkUtils.isPrivateAddress(socketAddress.getAddress()))
            {
            // We should be able to connect to local, private addresses really,
            // really quickly.  So don't wait around too long.
            soTimeout = 4000;
            }
        else
            {
            soTimeout = 10000;
            }
        final Runnable connectRunner = new Runnable()
            {

            public void run()
                {
                try
                    {
                    final Socket socket = new Socket();
                    socket.connect(socketAddress, soTimeout);
                    LOG.trace("Connected to socket: "+socketAddress);
                    candidate.setSocket(socket);
                    UacIceCandidateTracker.this.m_resolvedCandidates.add(
                        candidate);             
                    }
                catch (final IOException e)
                    {
                    LOG.debug("Could not connect to TCP candidate: "+
                        socketAddress, e);
                    } 
                finally
                    {
                    handleConnectResult();
                    }
                }
            };
            
        final Thread connectThread = 
            new Thread(connectRunner, "ICE-Connect-Thread");
        connectThread.setDaemon(true);
        connectThread.start();
        }

    private void handleConnectResult()
        {
        this.m_connectResults++;
        synchronized (this.m_resolvedCandidatesLock)
            {
            this.m_resolvedCandidatesLock.notify();
            }
        }
    }
