package rvm;
import com.runtimeverification.rvmonitor.java.rt.RuntimeOption;
import com.runtimeverification.rvmonitor.java.rt.ref.CachedWeakReference;
import com.runtimeverification.rvmonitor.java.rt.table.MapOfMap;
import com.runtimeverification.rvmonitor.java.rt.table.MapOfMonitor;
import com.runtimeverification.rvmonitor.java.rt.tablebase.TerminatedMonitorCleaner;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

final class InsertRawMonitor_Set extends com.runtimeverification.rvmonitor.java.rt.tablebase.AbstractMonitorSet<InsertRawMonitor> {

	InsertRawMonitor_Set(){
		this.size = 0;
		this.elements = new InsertRawMonitor[4];
	}
	final void event_insert(String user, String db, String p, String data, long time) {
		int numAlive = 0 ;
		for(int i = 0; i < this.size; i++){
			InsertRawMonitor monitor = this.elements[i];
			if(!monitor.isTerminated()){
				elements[numAlive] = monitor;
				numAlive++;

				monitor.event_insert(user, db, p, data, time);
			}
		}
		for(int i = numAlive; i < this.size; i++){
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
		}
		catch (CloneNotSupportedException e) {
			throw new InternalError(e.toString());
		}
	}

	String user;
	String db;
	String p;
	String data;
	long time;

	@Override
	public final int getState() {
		return -1;
	}

	final boolean event_insert(String user, String db, String p, String data, long time) {
		RVM_lastevent = 0;
		{

			if(db.equals("db2"))
			{
				if(user.equals("script1")) {}
				else{
					System.out.println("@"+time+" User "+user+" insert to db2 the data: "+data);
					return true;
				}
			}

			else{}
		}
		return true;
	}

	final void reset() {
		RVM_lastevent = -1;
	}

	// RVMRef_user was suppressed to reduce memory overhead
	// RVMRef_db was suppressed to reduce memory overhead
	// RVMRef_p was suppressed to reduce memory overhead
	// RVMRef_data was suppressed to reduce memory overhead

	@Override
	protected final void terminateInternal(int idnum) {
		switch(idnum){
			case 0:
			break;
			case 1:
			break;
			case 2:
			break;
			case 3:
			break;
		}
		switch(RVM_lastevent) {
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
	private static com.runtimeverification.rvmonitor.java.rt.map.RVMMapManager insertMapManager;
	static {
		insertMapManager = new com.runtimeverification.rvmonitor.java.rt.map.RVMMapManager();
		insertMapManager.start();
	}

	// Declarations for the Lock
	static final ReentrantLock insert_RVMLock = new ReentrantLock();
	static final Condition insert_RVMLock_cond = insert_RVMLock.newCondition();

	private static boolean Insert_activated = false;

	// Declarations for Indexing Trees
	private static Object Insert_user_db_p_data_Map_cachekey_data;
	private static Object Insert_user_db_p_data_Map_cachekey_db;
	private static Object Insert_user_db_p_data_Map_cachekey_p;
	private static Object Insert_user_db_p_data_Map_cachekey_user;
	private static InsertRawMonitor Insert_user_db_p_data_Map_cachevalue;
	private static final MapOfMap<MapOfMap<MapOfMap<MapOfMonitor<InsertRawMonitor>>>> Insert_user_db_p_data_Map = new MapOfMap<MapOfMap<MapOfMap<MapOfMonitor<InsertRawMonitor>>>>(0) ;

	public static int cleanUp() {
		int collected = 0;
		// indexing trees
		collected += Insert_user_db_p_data_Map.cleanUpUnnecessaryMappings();
		return collected;
	}

	// Removing terminated monitors from partitioned sets
	static {
		TerminatedMonitorCleaner.start() ;
	}
	// Setting the behavior of the runtime library according to the compile-time option
	static {
		RuntimeOption.enableFineGrainedLock(false) ;
	}

	public static final void insertEvent(String user, String db, String p, String data, long time) {
		Insert_activated = true;
		while (!insert_RVMLock.tryLock()) {
			Thread.yield();
		}

		CachedWeakReference wr_p = null;
		CachedWeakReference wr_data = null;
		CachedWeakReference wr_user = null;
		CachedWeakReference wr_db = null;
		MapOfMonitor<InsertRawMonitor> matchedLastMap = null;
		InsertRawMonitor matchedEntry = null;
		boolean cachehit = false;
		if (((((data == Insert_user_db_p_data_Map_cachekey_data) && (db == Insert_user_db_p_data_Map_cachekey_db) ) && (p == Insert_user_db_p_data_Map_cachekey_p) ) && (user == Insert_user_db_p_data_Map_cachekey_user) ) ) {
			matchedEntry = Insert_user_db_p_data_Map_cachevalue;
			cachehit = true;
		}
		else {
			wr_user = new CachedWeakReference(user) ;
			wr_db = new CachedWeakReference(db) ;
			wr_p = new CachedWeakReference(p) ;
			wr_data = new CachedWeakReference(data) ;
			{
				// FindOrCreateEntry
				MapOfMap<MapOfMap<MapOfMonitor<InsertRawMonitor>>> node_user = Insert_user_db_p_data_Map.getNodeEquivalent(wr_user) ;
				if ((node_user == null) ) {
					node_user = new MapOfMap<MapOfMap<MapOfMonitor<InsertRawMonitor>>>(1) ;
					Insert_user_db_p_data_Map.putNode(wr_user, node_user) ;
				}
				MapOfMap<MapOfMonitor<InsertRawMonitor>> node_user_db = node_user.getNodeEquivalent(wr_db) ;
				if ((node_user_db == null) ) {
					node_user_db = new MapOfMap<MapOfMonitor<InsertRawMonitor>>(2) ;
					node_user.putNode(wr_db, node_user_db) ;
				}
				MapOfMonitor<InsertRawMonitor> node_user_db_p = node_user_db.getNodeEquivalent(wr_p) ;
				if ((node_user_db_p == null) ) {
					node_user_db_p = new MapOfMonitor<InsertRawMonitor>(3) ;
					node_user_db.putNode(wr_p, node_user_db_p) ;
				}
				matchedLastMap = node_user_db_p;
				InsertRawMonitor node_user_db_p_data = node_user_db_p.getNodeEquivalent(wr_data) ;
				matchedEntry = node_user_db_p_data;
			}
		}
		// D(X) main:1
		if ((matchedEntry == null) ) {
			if ((wr_user == null) ) {
				wr_user = new CachedWeakReference(user) ;
			}
			if ((wr_db == null) ) {
				wr_db = new CachedWeakReference(db) ;
			}
			if ((wr_p == null) ) {
				wr_p = new CachedWeakReference(p) ;
			}
			if ((wr_data == null) ) {
				wr_data = new CachedWeakReference(data) ;
			}
			// D(X) main:4
			InsertRawMonitor created = new InsertRawMonitor() ;
			matchedEntry = created;
			matchedLastMap.putNode(wr_data, created) ;
		}
		// D(X) main:8--9
		matchedEntry.event_insert(user, db, p, data, time);

		if ((cachehit == false) ) {
			Insert_user_db_p_data_Map_cachekey_data = data;
			Insert_user_db_p_data_Map_cachekey_db = db;
			Insert_user_db_p_data_Map_cachekey_p = p;
			Insert_user_db_p_data_Map_cachekey_user = user;
			Insert_user_db_p_data_Map_cachevalue = matchedEntry;
		}

		insert_RVMLock.unlock();
	}

}
