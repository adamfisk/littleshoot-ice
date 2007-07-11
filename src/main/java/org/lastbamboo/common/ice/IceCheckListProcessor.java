package org.lastbamboo.common.ice;

/**
 * Processor for check lists. 
 */
public interface IceCheckListProcessor
    {

    /**
     * Processes the specified check list.
     * 
     * @param checkList The check list to process.
     * @param listener Listener for check list events. 
     */
    void processCheckList(IceCheckList checkList, 
        IceCheckListListener listener);

    }
