package log;

import java.util.List;

/**
 * Created by xiaohe on 14-11-22.
 */
public class LogEntry {
    private String time;
    private List<Event> db;

    public LogEntry(String time, List<Event> db) {
        this.time = time;
        this.db = db;
    }

    public String getTime() {
        return time;
    }

    /**
     * Different log may have different event arg.
     * This part should be derived from analyzing sig file.
     * Event is like table, while an eventArg is like a tuple.
     */
    public static class Event {
        private String name;
        private Object[] fields;

        public Event(String name, Object[] fields) {
            this.name = name;
            this.fields = fields;
        }

        public Object[] getFields() {
            return fields;
        }

        public void print() {
            System.out.print(this.name + "(");
            for (int i = 0; i < fields.length - 1; i++) {
                System.out.print(fields[i] + ", ");
            }

            System.out.print(fields[fields.length - 1] + ")");
        }

        public String getName() {
            return name;
        }
    }
}
