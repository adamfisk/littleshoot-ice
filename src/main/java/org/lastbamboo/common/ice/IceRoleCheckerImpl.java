package org.lastbamboo.common.ice;

import java.math.BigInteger;
import java.util.Map;

import org.apache.commons.id.uuid.UUID;
import org.lastbamboo.common.stun.stack.message.BindingErrorResponse;
import org.lastbamboo.common.stun.stack.message.BindingRequest;
import org.lastbamboo.common.stun.stack.message.attributes.StunAttribute;
import org.lastbamboo.common.stun.stack.message.attributes.StunAttributeType;
import org.lastbamboo.common.stun.stack.message.attributes.ice.IceControlledAttribute;
import org.lastbamboo.common.stun.stack.message.attributes.ice.IceControllingAttribute;
import org.lastbamboo.common.util.NumberUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class that verifies ICE roles, as specified in  ICE section 
 * 7.2.1.1.  "Detecting and Repairing Role Conflicts" 
 */
public class IceRoleCheckerImpl implements IceRoleChecker
    {

    private final Logger m_log = LoggerFactory.getLogger(getClass());
    
    public BindingErrorResponse checkAndRepairRoles(
        final BindingRequest request, final IceAgent agent)
        {
        m_log.debug("Checking roles...");
        if (fromOurselves(agent, request))
            {
            m_log.error("Received a request from ourselves..");
            throw new IllegalArgumentException("Request from ourselves!!!");
            }
        m_log.debug("Not from ourselves...");
        final Map<StunAttributeType, StunAttribute> remoteAttributes = 
            request.getAttributes();
        if (!remoteAttributes.containsKey(StunAttributeType.ICE_CONTROLLED) &&
            !remoteAttributes.containsKey(StunAttributeType.ICE_CONTROLLING))
            {
            m_log.warn("No control information.  Old ICE implementation?");
            // The agent may have implemented a previous version of ICE.
            // We have no way of detecting a role conflict if there is one.
            return null;
            }
        
        // Resolve the conflict if we think we're the controlling agent but
        // the remote host indicated in the request they also think they're 
        // controlling.
        else if (agent.isControlling() &&
            remoteAttributes.containsKey(StunAttributeType.ICE_CONTROLLING))
            {
            m_log.warn("We both think we're controlling...");
            final IceControllingAttribute attribute = 
                (IceControllingAttribute) remoteAttributes.get(
                    StunAttributeType.ICE_CONTROLLING);
            if (weWin(agent, attribute.getTieBreaker()))
                {
                // We retain our role and send an error response.
                return createErrorResponse(request);
                }
            else
                {
                agent.setControlling(false);
                agent.recomputePairPriorities();
                }
            }
        
        // Otherwise, handle the case where we both think we're controlled.
        else if (!agent.isControlling() && 
            remoteAttributes.containsKey(StunAttributeType.ICE_CONTROLLED))
            {
            m_log.warn("We both think we're controlled...");
            m_log.debug("Transaction ID: {}", request.getTransactionId());
            final IceControlledAttribute attribute = 
                (IceControlledAttribute) remoteAttributes.get(
                    StunAttributeType.ICE_CONTROLLED);
            if (weWin(agent, attribute.getTieBreaker()))
                {
                agent.setControlling(true);
                agent.recomputePairPriorities();
                }
            else
                {
                // We retain our role and send an error response.
                return createErrorResponse(request);
                }
            }
        
        // Otherwise, there was no conflict.  This is the normal case.
        return null;
        }

    private boolean fromOurselves(final IceAgent agent, 
        final BindingRequest request)
        {
        final Map<StunAttributeType, StunAttribute> remoteAttributes = 
            request.getAttributes();
        final byte[] tieBreaker;
        if (remoteAttributes.containsKey(StunAttributeType.ICE_CONTROLLED))
            {
            final IceControlledAttribute attribute = 
                (IceControlledAttribute) remoteAttributes.get(
                    StunAttributeType.ICE_CONTROLLED);
            tieBreaker = attribute.getTieBreaker();
            }
        else 
            {
            final IceControllingAttribute attribute = 
                (IceControllingAttribute) remoteAttributes.get(
                    StunAttributeType.ICE_CONTROLLING);
            if (attribute == null)
                {
                // This can often happen during tests.  If it happens in
                // production, though, this will get sent to our servers.
                m_log.error("No controlling attribute");
                return false;
                }
            tieBreaker = attribute.getTieBreaker();
            }
        
        final BigInteger localTieBreaker = 
            new BigInteger(agent.getTieBreaker());
        final BigInteger remoteTieBreaker = new BigInteger(tieBreaker);
        
        return localTieBreaker.equals(remoteTieBreaker);
        }

    private BindingErrorResponse createErrorResponse(
        final BindingRequest request)
        {
        // We need to send a Binding Error Response with a 
        // 487 Role Conflict ERROR CODE attribute.
        final UUID transactionId = request.getTransactionId();
        final BindingErrorResponse errorResponse = 
            new BindingErrorResponse(transactionId, 487, 
                "Role Conflict");
        return errorResponse;
        }

    private boolean weWin(final IceAgent agent, 
        final byte[] remoteTieBreakerBytes)
        {
        final BigInteger localTieBreaker = 
            new BigInteger(agent.getTieBreaker());
        final BigInteger remoteTieBreaker = 
            new BigInteger(remoteTieBreakerBytes);
    
        return NumberUtils.isBiggerOrEqual(localTieBreaker, remoteTieBreaker);
        }

    }
