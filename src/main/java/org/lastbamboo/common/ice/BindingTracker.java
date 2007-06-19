package org.lastbamboo.common.ice;

import java.net.InetSocketAddress;
import java.util.Collection;

/**
 * Interface that keeps track of all available bindings for accessing this peer.
 * Bindings could have been determined using STUN, TURN, or some other source.
 */
public interface BindingTracker
    {

    /**
     * Accessor for the STUN-derived <code>InetSocketAddress</code> for 
     * contacting this client over UDP.  This can be used in SDP data in 
     * SIP messages, for example.
     * 
     * @return The <code>InetSocketAddress</code> for the available UDP 
     * binding for this client.
     */
    InetSocketAddress getStunUdpBinding();
    
    /**
     * Accessor for the "STUNT" binding, or the binding that use STUN for TCP.
     * @return The <code>InetSocketAddress</code> for the STUN-derived TCP 
     * binding.
     */
    InetSocketAddress getTcpSoBinding();
    
    
    /**
     * Accessor for the <code>Collection</code> of 
     * <code>InetSocketAddress</code>es for TURN-derived transport addresses
     * for contacting this client over TCP.  This can be used in SDP data in 
     * SIP messages, for example.
     * 
     * @return The <code>Collection</code> of <code>InetSocketAddress</code>es
     * for the available TURN-derived TCP bindings for this client.
     */
    Collection<InetSocketAddress> getTurnTcpBindings();

    }
