package org.lastbamboo.common.ice;

/**
 * Constants for ICE code.  Many of these constants are from the relevant 
 * RFCs or drafts on the topic.  At the time of the this writing, for example,
 * constants were taken from:<p> 
 * 
 * http://www.ietf.org/internet-drafts/draft-ietf-mmusic-ice-tcp-03.txt
 */
public class IceConstants
    {

    /**
     * Transport descriptor for passive TCP ICE candidates.
     */
    public static final String TCP_PASS = "tcp-pass";
    
    /**
     * Transport descriptor for active TCP ICE candidates.
     */
    public static final String TCP_ACT = "tcp-act";
    
    /**
     * Transport descriptor for TCP "simultaneous open" ICE candidates.
     */
    public static final String TCP_SO = "tcp-so";
    }
