package org.lastbamboo.common.ice;

import java.net.InetSocketAddress;

public interface UpnpInternetGatewayDeviceManager
    {

    void mapAddress(InetSocketAddress socketAddress);

    void unmapAddress(InetSocketAddress hostAddress);

    }
