package log;

import java.util.HashMap;
import java.util.List;

/**
 * Created by xiaohe on 14-11-22.
 */
public class LogEntry {
    private long time;
    private HashMap<String, List<EventArg>> tableMap;

    public LogEntry(long time, HashMap<String, List<EventArg>> tmap) {
        this.time = time;
        this.tableMap = tmap;
    }

    public long getTime() {
        return time;
    }

    public HashMap<String, List<EventArg>> getTableMap() {
        return tableMap;
    }

    /**
     * Different log may have different event arg.
     * This part should be derived from analyzing sig file.
     * Event is like table, while an eventArg is like a tuple.
     */
    public static class EventArg {
        private Object[] fields;

        public EventArg(Object[] fields) {
            this.fields = fields;
        }

        public Object[] getFields() {
            return fields;
        }

        public void print() {
            System.out.print("(");
            for (int i = 0; i < fields.length - 1; i++) {
                System.out.print(fields[i] + ", ");
            }

            System.out.print(fields[fields.length - 1] + ")");
        }
    }
}
