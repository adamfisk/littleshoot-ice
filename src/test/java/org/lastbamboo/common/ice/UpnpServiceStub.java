package org.lastbamboo.common.ice;

import org.lastbamboo.common.portmapping.PortMapListener;
import org.lastbamboo.common.portmapping.PortMappingProtocol;
import org.lastbamboo.common.portmapping.UpnpService;

public class UpnpServiceStub implements UpnpService {

    public void addPortMapListener(PortMapListener portMapListener) {
    }

    public int addUpnpMapping(PortMappingProtocol protocolType, int localPort,
        int externalPortRequested) {
        // TODO Auto-generated method stub
        return 0;
    }

    public void removeUpnpMapping(int mappingIndex) {

    }

}
