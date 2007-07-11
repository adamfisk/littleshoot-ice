package org.lastbamboo.common.ice;

import org.lastbamboo.common.ice.candidate.IceCandidatePair;

/**
 * Listener for check list events.
 */
public interface IceCheckListListener
    {

    void onNominated(IceCandidatePair pair);

    }
