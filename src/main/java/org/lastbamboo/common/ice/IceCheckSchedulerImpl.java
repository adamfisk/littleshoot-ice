package org.lastbamboo.common.ice;

import java.net.Socket;
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

    /**
     * Creates a new scheduler for the specified pairs.
     * 
     * @param pairs The candidate pairs to schedule checks for.
     * @param listener The listener for check list events. 
     */
    public IceCheckSchedulerImpl(final Collection<IceCandidatePair> pairs, 
        final IceCheckListListener listener)
        {
        m_pairs = pairs;
        m_listener = listener;
        }

    public void scheduleChecks()
        {
        m_log.debug("Scheduling checks...");
        final Timer timer = new Timer(true);
        
        final TimerTask task = new TimerTask()
            {
            @Override
            public void run()
                {
                final IceCandidatePair activePair = getActivePair();
                if (activePair == null)
                    {
                    // No more pairs to try.
                    timer.cancel();
                    }
                else
                    {
                    if (performCheck(activePair))
                        {
                        timer.cancel();
                        }
                    }
                }
            };

        //final int Ta = 1;
        timer.schedule(task, 0L, 1L);
        }

    protected boolean performCheck(final IceCandidatePair pair)
        {
        final IceConnectivityChecker checker = 
            new IceConnectivityCheckerImpl(pair);
        pair.setState(IceCandidatePairState.IN_PROGRESS);
        final Socket sock = checker.check();
        if (sock != null)
            {
            pair.setSocket(sock);
            this.m_listener.onNominated(pair);
            return true;
            }
        return false;
        }

    protected IceCandidatePair getActivePair()
        {
        final IceCandidatePair waitingPair = 
            getPair(IceCandidatePairState.WAITING);
        if (waitingPair == null)
            {
            final IceCandidatePair frozen = 
                getPair(IceCandidatePairState.FROZEN);
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

    protected IceCandidatePair getPair(final IceCandidatePairState state)
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
