package org.lastbamboo.common.ice.stubs;

import org.lastbamboo.common.ice.transport.IceTcpConnector;

public class IceTcpConnectorStub extends IceTcpConnector
    {

    public IceTcpConnectorStub()
        {
        super(new StunMessageVisitorFactoryStub(), true);
        }

    }
