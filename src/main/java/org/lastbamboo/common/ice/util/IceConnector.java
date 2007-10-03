package org.lastbamboo.common.ice.util;

import java.net.InetSocketAddress;

import org.apache.mina.common.IoSession;

public interface IceConnector
    {

    IoSession connect(InetSocketAddress localAddress, InetSocketAddress remoteAddress);

    }
