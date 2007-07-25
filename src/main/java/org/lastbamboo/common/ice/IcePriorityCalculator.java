package org.lastbamboo.common.ice;

/**
 * Class for calculating ICE priorities.
 */
public class IcePriorityCalculator
    {
    
    /**
     * The component ID is 1 unless otherwise specified.
     */
    private final static int DEFAULT_COMPONENT_ID = 1;
    
    /**
     * The is the local interface preference for calculating ICE priorities.
     * This is set to the highest possible value because we currently
     * only use one interface.
     */
    private static final int LOCAL_PREFERENCE = 65535;

    /**
     * Calculates teh priority for the specified type using the default 
     * component ID of 1 and the default local preference.
     * 
     * @param type The type fo the candidate.
     * @return The priority.
     */
    public static long calculatePriority(final IceCandidateType type)
        {
        // See draft-ietf-mmusic-ice-17.txt section 4.1.2.1.
        return
            (long) (Math.pow(2, 24) * type.getTypePreference()) +
            (long) (Math.pow(2, 8) * LOCAL_PREFERENCE) +
            (int) (Math.pow(2, 0) * (256 - DEFAULT_COMPONENT_ID));
        }
    }
