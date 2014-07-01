package com.elliottsj.ftw.utilities;

import android.util.Log;
import net.sf.nextbus.publicxmlfeed.impl.RPCImpl;
import net.sf.nextbus.publicxmlfeed.impl.RPCRequest;
import net.sf.nextbus.publicxmlfeed.service.ServiceConfigurationException;
import net.sf.nextbus.publicxmlfeed.service.ServiceException;
import net.sf.nextbus.publicxmlfeed.service.TransientServiceException;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

/**
 *
 */
public class AndroidRPCImpl implements RPCImpl {

    private long lastSuccessfulCallTimeUTC;
    private long totalRPCCalls;
    private long bytesReceived;

    private static final String TAG = "AndroidRPCImpl";

    // Enforces advisory warnings on bandwidth use - NextBus spec says 2MB/20sec Max
    private static final long BANDWIDTH_LIMIT_INTERVAL_MILLISECONDS = 20*1000;
    private static final long BANDWIDTH_LIMIT_INTERVAL_BYTES = (long) Math.pow(2, 21);

    // State machine values for Sliding bandwidth monitor
    private long bwLimitIntervalStartTime;
    private long bwLimitIntervalStartBytes;

    public AndroidRPCImpl() {
    }

    public String call(RPCRequest request) throws ServiceException {
        HttpURLConnection c = null;

        BufferedReader rd;
        StringBuilder sb;
        InputStream is;
        String line;
        try {
            URL url = new URL(request.getFullHttpRequest());

            c = (HttpURLConnection) url.openConnection();
            Log.i(TAG, "RPC handler opened HTTP connection");

            c.connect();
            int http_status = c.getResponseCode();
            if (http_status != HttpURLConnection.HTTP_OK) {
                String msg = String.format("Received HTTP Status Code %s : %s", http_status, c.getResponseMessage());
                Log.w(TAG, msg);
                totalRPCCalls++;
                throw new TransientServiceException(msg);
            }

            is = new BufferedInputStream(c.getInputStream());

            /* Read in the HTTP Response until end of buffer */
            rd = new BufferedReader(new InputStreamReader(is));
            sb = new StringBuilder();
            while ((line = rd.readLine()) != null) {
                bytesReceived += line.length();
                sb.append(line);
                Log.v(TAG, "Read " + line.length() + " bytes from the HTTP input buffer");
                checkBandwidthLimits();
            }

            /* Done! Cleanup and return to the caller */
            is.close();
            rd.close();
            Log.i(TAG, "RPC handler closed HTTP connection");
            lastSuccessfulCallTimeUTC = System.currentTimeMillis();
            return sb.toString();
        } catch (MalformedURLException mfu) {
            Log.e(TAG, "Invalid URL. Inspect: " + request.getFullHttpRequest(), mfu);
            throw new ServiceConfigurationException(mfu);
        } catch (IOException ioe) {
            Log.w(TAG, "During http rpc to nextbus ", ioe);
            throw new TransientServiceException(ioe);
        } finally {
            if (c != null) c.disconnect();
            totalRPCCalls++;
        }
    }

    /**
     * Diagnostic method
     * @return the last timestamp (in UTC time) when a Successful RPC was made
     */
    public long getLastSuccessfulCallTimeUTC() {
        return lastSuccessfulCallTimeUTC;
    }

    /**
     * Diagnostic method
     * @return total number of RPC calls (failed and successful) made
     */
    public long getTotalRPCCalls() {
        return totalRPCCalls;
    }

    /**
     * Implements a simple Bandwidth limit check.
     * When b/w is exceeded, a WARN Advisory is posted to the logger.
     * Tests whether more than N bytes are sent within an M second interval
     */
    private void checkBandwidthLimits() {
        // Reset the observation time window if needed
        if (bwLimitIntervalStartTime == 0 || (System.currentTimeMillis()-bwLimitIntervalStartTime) > BANDWIDTH_LIMIT_INTERVAL_MILLISECONDS) {
            bwLimitIntervalStartTime = System.currentTimeMillis();
            bwLimitIntervalStartBytes += bytesReceived;
        }

        // Check the total byte count inside the observation window
        long bytesRecvInLimitWindow = bytesReceived - bwLimitIntervalStartBytes;
        if ((bytesRecvInLimitWindow ) > BANDWIDTH_LIMIT_INTERVAL_BYTES) {
            // The bandwidth limit has been exceeded!
            Log.w(TAG, "Bandwidth advisory limit exceeded by "+bytesRecvInLimitWindow+" bytes!");
        }
    }

    public void activate() { }

    public void passivate() { }

}
