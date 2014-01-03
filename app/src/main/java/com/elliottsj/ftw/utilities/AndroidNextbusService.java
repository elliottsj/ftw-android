package com.elliottsj.ftw.utilities;

import net.sf.nextbus.publicxmlfeed.impl.NextbusService;

/**
 *
 */
public class AndroidNextbusService extends NextbusService {

    public AndroidNextbusService() {
        super(new AndroidRPCImpl());
    }

}
