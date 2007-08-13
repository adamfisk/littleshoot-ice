package org.lastbamboo.common.ice;

import java.util.Collection;
import java.util.Timer;
import java.util.TimerTask;

import org.lastbamboo.common.ice.candidate.IceCandidatePair;
import org.lastbamboo.common.ice.candidate.IceCandidatePairState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class that schedules and executes ICE checks.  This behavior is defined
 * in ICE section 5.8.
 */
public class IceCheckSchedulerImpl implements IceCheckScheduler
    {

    private final Logger m_log = LoggerFactory.getLogger(getClass());
    private final IceCheckList m_checkList;
    private final IceMediaStream m_mediaStream;
    private final IceAgent m_agent;

    /**
     * Creates a new scheduler for the specified pairs.
     * 
     * @param agent The top-level ICE agent.
     * @param stream The media stream.
     * @param checkList The check list.
     */
    public IceCheckSchedulerImpl(final IceAgent agent, 
        final IceMediaStream stream, final IceCheckList checkList)
        {
        m_agent = agent;
        m_mediaStream = stream;
        m_checkList = checkList;
        }

    public void scheduleChecks()
        {
        m_log.debug("Scheduling checks...");
        final String offererOrAnswerer;
        if (this.m_agent.isControlling())
            {
            offererOrAnswerer = "STUN-Offerer-Timer";
            }
        else
            {
            offererOrAnswerer = "STUN-Answerer-Timer";
            }
        final Timer timer = new Timer(offererOrAnswerer, true);
        final TimerTask task = createTimerTask(timer);
        
        //final int Ta = 1;
        timer.schedule(task, 0L);
        }

    private TimerTask createTimerTask(final Timer timer)
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
        final IceCandidatePair activePair = getNextPair();
        if (activePair == null)
            {
            // No more pairs to try.
            timer.cancel();
            m_log.debug("No more active pairs...");
            this.m_checkList.setState(IceCheckListState.FAILED);
            }
        else
            {
            m_log.debug("About to perform check on: {}", activePair);
            if (performCheck(activePair))
                {
                this.m_checkList.setState(IceCheckListState.COMPLETED);
                timer.cancel();
                }
            else
                {
                m_log.debug("Scheduling new timer task...");
                final TimerTask task = createTimerTask(timer);
                final int Ta_i = 20;
                
                // TODO: The recommended formula for this is:
                // (stunPacketSize / rtpPacketSize) * rtpPtime;
                // We'd have to allow this to be configurable for an arbitrary
                // protocol in use, not just RTP.  For now, we just use the 
                // relatively safe value of 20ms supported in most NATs.
                //
                // Note also that our goal isn't necessarily to keep the 
                // bandwidth in line with the ultimate protocol, as the folmula
                // above intends, but rather to make sure the NAT can handle
                // the number of mappings we're requesting.
                timer.schedule(task, this.m_agent.calculateDelay(Ta_i));
                }
            }
        }

    private boolean performCheck(final IceCandidatePair pair)
        {
        final IceConnectivityChecker checker = 
            new IceConnectivityCheckerImpl(this.m_agent, this.m_mediaStream, pair);
        pair.setState(IceCandidatePairState.IN_PROGRESS);
        return checker.check();
        }

    private IceCandidatePair getNextPair()
        {
        final IceCandidatePair triggeredPair = removeTopTriggeredPair();
        if (triggeredPair != null)
            {
            return triggeredPair;
            }
        else
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
        }

    private IceCandidatePair removeTopTriggeredPair()
        {
        return this.m_checkList.removeTopTriggeredPair();
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
        synchronized (pairs)
            {
            for (final IceCandidatePair pair : pairs)
                {
                if (pair.getState() == state)
                    {
                    return pair;
                    }
                }
            }
            
        return null;
        }

    }
