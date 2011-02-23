package org.lastbamboo.common.ice.stubs;

import org.littleshoot.mina.common.IoSession;
import org.littleshoot.stun.stack.message.StunMessageVisitor;
import org.littleshoot.stun.stack.message.StunMessageVisitorAdapter;
import org.littleshoot.stun.stack.message.StunMessageVisitorFactory;

public class StunMessageVisitorFactoryStub implements StunMessageVisitorFactory
    {

    public StunMessageVisitor createVisitor(IoSession session)
        {
        return new StunMessageVisitorAdapter();
        }

    }
