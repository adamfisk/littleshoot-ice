package org.lastbamboo.common.ice;

import java.util.Collection;
import java.util.Timer;
import java.util.TimerTask;

import org.lastbamboo.common.ice.candidate.IceCandidatePair;
import org.lastbamboo.common.ice.candidate.IceCandidatePairState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class that schedules and executes ICE checks. 
 */
public class IceCheckSchedulerImpl implements IceCheckScheduler
    {

    private final Logger m_log = LoggerFactory.getLogger(getClass());
    private final Collection<IceCandidatePair> m_pairs;
    private final IceCheckListListener m_listener;
    private final IceCheckList m_checkList;

    /**
     * Creates a new scheduler for the specified pairs.
     * 
     * @param checkList The check list.
     * @param pairs The candidate pairs to schedule checks for.
     * @param listener The listener for check list events. 
     */
    public IceCheckSchedulerImpl(final IceCheckList checkList,
        final Collection<IceCandidatePair> pairs, 
        final IceCheckListListener listener)
        {
        m_checkList = checkList;
        m_pairs = pairs;
        m_listener = listener;
        }

    public void scheduleChecks()
        {
        m_log.debug("Scheduling checks...");
        final Timer timer = new Timer(true);
        final TimerTask task = createTimerTask(timer);
        //final int Ta = 1;
        timer.schedule(task, 0L);
        
        }

    protected TimerTask createTimerTask(final Timer timer)
        {
        return new TimerTask()
            {
            private final Logger m_taskLog = 
                LoggerFactory.getLogger(getClass());
            @Override
            public void run()
                {
                try
                    {
                    checkPair(timer);
                    }
                catch (final Throwable t)
                    {
                    m_taskLog.warn("Caught throwable in check", t);
                    }
                }
            };
        }

    protected void checkPair(final Timer timer)
        {
        final IceCandidatePair activePair = getActivePair();
        if (activePair == null)
            {
            // No more pairs to try.
            timer.cancel();
            this.m_checkList.setState(IceCheckListState.FAILED);
            }
        else
            {
            if (performCheck(activePair))
                {
                this.m_checkList.setState(IceCheckListState.COMPLETED);
                timer.cancel();
                }
            else
                {
                // TODO: The delay between checks should be calculated 
                // here.
                final TimerTask task = createTimerTask(timer);
                timer.schedule(task, 0L);
                }
            }
        }

    private boolean performCheck(final IceCandidatePair pair)
        {
        final IceConnectivityChecker checker = 
            new IceConnectivityCheckerImpl(pair);
        pair.setState(IceCandidatePairState.IN_PROGRESS);
        if (checker.check())
            {
            this.m_listener.onNominated(pair);
            return true;
            }
        
        return false;
        }

    private IceCandidatePair getActivePair()
        {
        final IceCandidatePair waitingPair = 
            getPairInState(IceCandidatePairState.WAITING);
        if (waitingPair == null)
            {
            final IceCandidatePair frozen = 
                getPairInState(IceCandidatePairState.FROZEN);
            if (frozen != null) 
                {
                frozen.setState(IceCandidatePairState.WAITING);
                return frozen;
                }
            return null;
            }
        else
            {
            return waitingPair;
            }
        }

    private IceCandidatePair getPairInState(final IceCandidatePairState state)
        {
        // The pairs are already ordered.
        for (final IceCandidatePair pair : this.m_pairs)
            {
            if (pair.getState() == state)
                {
                return pair;
                }
            }
        return null;
        }

    }
