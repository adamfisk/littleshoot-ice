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
    private final IceCheckList m_checkList;
    private final IceMediaStream m_mediaStream;

    /**
     * Creates a new scheduler for the specified pairs.
     * 
     * @param stream The media stream.
     * @param checkList The check list.
     */
    public IceCheckSchedulerImpl(final IceMediaStream stream, 
        final IceCheckList checkList)
        {
        m_mediaStream = stream;
        m_checkList = checkList;
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
                m_taskLog.debug("About to check pair...");
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
            m_log.debug("No more active pairs...");
            this.m_checkList.setState(IceCheckListState.FAILED);
            }
        else
            {
            m_log.debug("About to perform check...");
            if (performCheck(activePair))
                {
                this.m_checkList.setState(IceCheckListState.COMPLETED);
                timer.cancel();
                }
            else
                {
                // TODO: The delay between checks should be calculated 
                // here.
                m_log.debug("Scheduling new timer task...");
                final TimerTask task = createTimerTask(timer);
                timer.schedule(task, 0L);
                }
            }
        }

    private boolean performCheck(final IceCandidatePair pair)
        {
        final IceConnectivityChecker checker = 
            new IceConnectivityCheckerImpl(this.m_mediaStream, pair);
        pair.setState(IceCandidatePairState.IN_PROGRESS);
        if (checker.check())
            {
            this.m_mediaStream.addValidPair(pair);
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

    /**
     * Accesses the top priority pair in the specified state.
     * 
     * @param state The state to look for.
     * @return The top priority pair in that state, or <code>null</code> if 
     * no pair in the desired state can be found.
     */
    private IceCandidatePair getPairInState(final IceCandidatePairState state)
        {
        // The pairs are already ordered.
        final Collection<IceCandidatePair> pairs = this.m_checkList.getPairs();
        for (final IceCandidatePair pair : pairs)
            {
            if (pair.getState() == state)
                {
                return pair;
                }
            }
            
        return null;
        }

    }
