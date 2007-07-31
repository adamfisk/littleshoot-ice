package org.lastbamboo.common.ice;

import org.lastbamboo.common.stun.stack.message.BindingErrorResponse;
import org.lastbamboo.common.stun.stack.message.BindingRequest;

/**
 * Class that checks ICE roles.
 */
public interface IceRoleChecker
    {

    BindingErrorResponse checkAndRepairRoles(BindingRequest request, 
        IceAgent agent);

    }
