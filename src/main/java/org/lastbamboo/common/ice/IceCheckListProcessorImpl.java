package org.lastbamboo.common.ice;

import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.lastbamboo.common.ice.candidate.IceCandidatePair;
import org.lastbamboo.common.ice.candidate.IceCandidatePairState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class for processing check lists. 
 */
public class IceCheckListProcessorImpl implements IceCheckListProcessor
    {

    private final Logger LOG = LoggerFactory.getLogger(getClass());
    
    public void processCheckList(final IceCheckList checkList, 
        final IceCheckListListener listener)
        {
        LOG.debug("Processing check list");
        final Collection<IceCandidatePair> pairs = checkList.getPairs();
        final Collection<List<IceCandidatePair>> mediaGroups = 
            createGroups(pairs);
        
        LOG.debug("Created "+mediaGroups.size()+" groups");
        for (final List<IceCandidatePair> mediaPairs : mediaGroups)
            {
            LOG.debug("Looping through "+mediaPairs.size()+" pairs");
            final IceCheckScheduler scheduler = 
                new IceCheckSchedulerImpl(mediaPairs, listener);
            scheduler.scheduleChecks();
            }
        }

    private Collection<List<IceCandidatePair>> createGroups(
        final Collection<IceCandidatePair> pairs)
        {
        final Map<Integer, List<IceCandidatePair>> groupsMap = 
            new HashMap<Integer, List<IceCandidatePair>>();
        
        // Group together pairs with the same foundation.
        for (final IceCandidatePair pair : pairs)
            {
            final int foundation = pair.getFoundation();
            final List<IceCandidatePair> foundationPairs;
            if (groupsMap.containsKey(foundation))
                {
                foundationPairs = groupsMap.get(foundation);
                }
            else
                {
                foundationPairs = new LinkedList<IceCandidatePair>();
                groupsMap.put(foundation, foundationPairs);
                }
            foundationPairs.add(pair);
            }
        
        final Collection<List<IceCandidatePair>> groups = 
            groupsMap.values();
        
        LOG.debug(groups.size()+ " before sorting...");
        for (final List<IceCandidatePair> group : groups)
            {
            sortPairs(group);
            setLowestComponentIdToWaiting(group);
            }
        
        return groups;
        }
    
    private void setLowestComponentIdToWaiting(
        final List<IceCandidatePair> pairs)
        {
        IceCandidatePair pairToSet = null;
        for (final IceCandidatePair pair : pairs)
            {
            if (pairToSet == null)
                {
                pairToSet = pair;
                continue;
                }
            
            // Always use the lowest component ID.
            if (pair.getComponentId() < pairToSet.getComponentId())
                {
                pairToSet = pair;
                }
            
            // If the component IDs match, use the one with the highest
            // priority.
            else if (pair.getComponentId() == pairToSet.getComponentId())
                {
                if (pair.getPriority() > pairToSet.getPriority())
                    {
                    pairToSet = pair;
                    }
                }
            }
        
        if (pairToSet != null)
            {
            pairToSet.setState(IceCandidatePairState.WAITING);
            }
        else
            {
            LOG.warn("No pair to set!!!");
            }
        }

    private Collection<IceCandidatePair> sortPairs(
        final List<IceCandidatePair> pairs)
        {
        final Comparator<IceCandidatePair> comparator = 
            new IceCandidatePairComparator();
        
        Collections.sort(pairs, comparator);
        return pairs;
        }

    }
