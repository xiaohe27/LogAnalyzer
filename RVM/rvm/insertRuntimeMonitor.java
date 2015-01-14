package rvm;

import com.runtimeverification.rvmonitor.java.rt.RuntimeOption;
import com.runtimeverification.rvmonitor.java.rt.tablebase.TerminatedMonitorCleaner;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

final class InsertRawMonitor_Set extends com.runtimeverification.rvmonitor.java.rt.tablebase.AbstractMonitorSet<InsertRawMonitor> {

    InsertRawMonitor_Set() {
        this.size = 0;
        this.elements = new InsertRawMonitor[4];
    }

    final void event_insert(String user, String db, String p, String data, long time) {
        int numAlive = 0;
        for (int i = 0; i < this.size; i++) {
            InsertRawMonitor monitor = this.elements[i];
            if (!monitor.isTerminated()) {
                elements[numAlive] = monitor;
                numAlive++;

                monitor.event_insert(user, db, p, data, time);
            }
        }
        for (int i = numAlive; i < this.size; i++) {
            this.elements[i] = null;
        }
        size = numAlive;
    }
}

class InsertRawMonitor extends com.runtimeverification.rvmonitor.java.rt.tablebase.AbstractSynchronizedMonitor implements Cloneable, com.runtimeverification.rvmonitor.java.rt.RVMObject {
    protected Object clone() {
        try {
            InsertRawMonitor ret = (InsertRawMonitor) super.clone();
            return ret;
        } catch (CloneNotSupportedException e) {
            throw new InternalError(e.toString());
        }
    }

    @Override
    public final int getState() {
        return -1;
    }

    final boolean event_insert(String user, String db, String p, String data, long time) {
        RVM_lastevent = 0;
        {

            if (db.equals("db2")) {
                if (user.equals("script1")) {
                } else {
                    System.out.println("@" + time + " User " + user + " insert to db2 the data: " + data);
                    return true;
                }
            } else {
            }
        }
        return true;
    }

    final void reset() {
        RVM_lastevent = -1;
    }

    @Override
    protected final void terminateInternal(int idnum) {
        switch (idnum) {
        }
        switch (RVM_lastevent) {
            case -1:
                return;
            case 0:
                //insert
                return;
        }
        return;
    }

}

public final class insertRuntimeMonitor implements com.runtimeverification.rvmonitor.java.rt.RVMObject {
    // Declarations for the Lock
    static final ReentrantLock insert_RVMLock = new ReentrantLock();
    static final Condition insert_RVMLock_cond = insert_RVMLock.newCondition();
    // Declarations for Indexing Trees
    private static final InsertRawMonitor Insert__Map = new InsertRawMonitor();
    private static com.runtimeverification.rvmonitor.java.rt.map.RVMMapManager insertMapManager;

    static {
        insertMapManager = new com.runtimeverification.rvmonitor.java.rt.map.RVMMapManager();
        insertMapManager.start();
    }

    private static boolean Insert_activated = false;

    public static int cleanUp() {
        int collected = 0;
        // indexing trees
        return collected;
    }

    // Removing terminated monitors from partitioned sets
    static {
        TerminatedMonitorCleaner.start();
    }

    // Setting the behavior of the runtime library according to the compile-time option
    static {
        RuntimeOption.enableFineGrainedLock(false);
    }

    public static final void insertEvent(String user, String db, String p, String data, long time) {
        Insert_activated = true;
        while (!insert_RVMLock.tryLock()) {
            Thread.yield();
        }

        InsertRawMonitor matchedEntry = null;
        {
            // FindOrCreateEntry
            matchedEntry = Insert__Map;
        }
        // D(X) main:1
        if ((matchedEntry == null)) {
            // D(X) main:4
            InsertRawMonitor created = new InsertRawMonitor();
            matchedEntry = created;
        }
        // D(X) main:8--9
        matchedEntry.event_insert(user, db, p, data, time);

        insert_RVMLock.unlock();
    }

}
