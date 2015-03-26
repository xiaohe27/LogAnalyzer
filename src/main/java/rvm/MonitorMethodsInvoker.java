package rvm;

import java.util.List;

/**
 * Created by xiaohe on 3/26/15.
 */
public class MonitorMethodsInvoker {


    public static void invoke(String eventName, Object[] data, List<Object[]> violationsInCurLogEntry) {
        PubMonitor.hasViolation = false;
        switch (eventName) {
            case "approve":
                PubRuntimeMonitor.approveEvent(((Integer) data[ 0 ]));
                break;
            case "publish":
                PubRuntimeMonitor.publishEvent(((Integer) data[ 0 ]));
                break;
        }
        if (PubMonitor.hasViolation) {
            violationsInCurLogEntry.add(data);
        }
    }

}