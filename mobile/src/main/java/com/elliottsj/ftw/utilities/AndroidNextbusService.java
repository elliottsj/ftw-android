package com.elliottsj.ftw.utilities;

import com.elliottsj.nextbus.impl.NextbusService;

/**
 *
 */
public class AndroidNextbusService extends NextbusService {

    public AndroidNextbusService() {
        super(new AndroidRPCImpl());
    }

}
