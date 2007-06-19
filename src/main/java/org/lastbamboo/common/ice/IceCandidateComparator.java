package org.lastbamboo.common.ice;

import java.util.Comparator;

/**
 * Comparator for ICE candidates.  Candidates with a higher priority come
 * before candidates with a lower priority.
 */
public class IceCandidateComparator implements Comparator<IceCandidate>
    {

    public int compare(final IceCandidate candidate1, 
        final IceCandidate candidate2)
        {
        final int priority1 = candidate1.getPriority();
        final int priority2 = candidate2.getPriority();
        
        if (priority1 > priority2) return -1;
        if (priority1 < priority2) return 1;
        return 0;
        }

    }
