package rvm;

import java.awt.*;
import java.util.List;

/**
 * Created by xiaohe on 14-11-22.
 */
public class LogEntry {
    private long time;
    private String eventName;
    private List<EventArg> argList;

    public LogEntry(long time, String eventName, List<EventArg> argList) {
        this.time = time;
        this.eventName = eventName;
        this.argList = argList;
    }

    public long getTime() {
        return time;
    }

    public String getEventName() {
        return eventName;
    }

    public List<EventArg> getArgList() {
        return argList;
    }

    /**
     * Different log may have different event arg.
     * This part should be derived from analyzing sig file.
     */
    public static class EventArg {
        private Object[] fields;

        public EventArg(Object[] fields) {
            this.fields = fields;
        }

        public Object[] getFields() {
            return fields;
        }
    }
}
