package org.lastbamboo.common.ice;

import java.net.SocketException;

import com.barchart.udt.ExceptionUDT;
import com.barchart.udt.net.NetSocketUDT;

/**
 * Wrapper class for UDT code.
 */
public class NetSocketUDTWrapper extends NetSocketUDT {

    public NetSocketUDTWrapper() throws ExceptionUDT {
        super();
    }

    @Override
    public void setTcpNoDelay(final boolean on) throws SocketException {
        // We don't want to throw an error here!!
    }
}
