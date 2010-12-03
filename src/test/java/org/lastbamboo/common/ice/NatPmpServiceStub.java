package org.lastbamboo.common.ice;

import org.lastbamboo.common.portmapping.NatPmpService;
import org.lastbamboo.common.portmapping.PortMapListener;
import org.lastbamboo.common.portmapping.PortMappingProtocol;

public class NatPmpServiceStub implements NatPmpService {

    public NatPmpServiceStub() {
        
    }
    
    public int addNatPmpMapping(PortMappingProtocol protocolType, int localPort,
            int externalPortRequested) {
        // TODO Auto-generated method stub
        return 0;
    }

    public void addPortMapListener(PortMapListener portMapListener) {
        // TODO Auto-generated method stub

    }

    public void removeNatPmpMapping(int mappingIndex) {
        // TODO Auto-generated method stub

    }

}
