package rvm;
import java.io.IOException;
import java.util.concurrent.*;
import java.util.concurrent.locks.*;
import java.util.*;
import java.lang.ref.*;
import com.runtimeverification.rvmonitor.java.rt.*;
import com.runtimeverification.rvmonitor.java.rt.ref.*;
import com.runtimeverification.rvmonitor.java.rt.table.*;
import com.runtimeverification.rvmonitor.java.rt.tablebase.AbstractIndexingTree;
import com.runtimeverification.rvmonitor.java.rt.tablebase.SetEventDelegator;
import com.runtimeverification.rvmonitor.java.rt.tablebase.TableAdopter.Tuple2;
import com.runtimeverification.rvmonitor.java.rt.tablebase.TableAdopter.Tuple3;
import com.runtimeverification.rvmonitor.java.rt.tablebase.IDisableHolder;
import com.runtimeverification.rvmonitor.java.rt.tablebase.IMonitor;
import com.runtimeverification.rvmonitor.java.rt.tablebase.DisableHolder;
import com.runtimeverification.rvmonitor.java.rt.tablebase.TerminatedMonitorCleaner;
import java.util.concurrent.atomic.AtomicInteger;

final class InsertRawMonitor_Set extends com.runtimeverification.rvmonitor.java.rt.tablebase.AbstractMonitorSet<InsertRawMonitor> {

	InsertRawMonitor_Set(){
		this.size = 0;
		this.elements = new InsertRawMonitor[4];
	}
	final void event_Insert(String user, String db, String p, String data) {
		int numAlive = 0 ;
		for(int i = 0; i < this.size; i++){
			InsertRawMonitor monitor = this.elements[i];
			if(!monitor.isTerminated()){
				elements[numAlive] = monitor;
				numAlive++;

				monitor.event_Insert(user, db, p, data);
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

	@Override
	public final int getState() {
		return -1;
	}

	final boolean event_Insert(String user, String db, String p, String data) {
		RVM_lastevent = 0;
		{

			if(db.equals("db2") && !user.equals("script1"))
			{
				throw new IOException();
			}
		}
		return true;
	}

	final void reset() {
		RVM_lastevent = -1;
	}

	// RVMRef_user was suppressed to reduce memory overhead
	// RVMRef_db was suppressed to reduce memory overhead

	@Override
	protected final void terminateInternal(int idnum) {
		switch(idnum){
			case 0:
			break;
			case 1:
			break;
		}
		switch(RVM_lastevent) {
			case -1:
			return;
			case 0:
			//Insert
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
	private static Object Insert_user_db_Map_cachekey_db;
	private static Object Insert_user_db_Map_cachekey_user;
	private static InsertRawMonitor Insert_user_db_Map_cachevalue;
	private static final MapOfMap<MapOfMonitor<InsertRawMonitor>> Insert_user_db_Map = new MapOfMap<MapOfMonitor<InsertRawMonitor>>(0) ;

	public static int cleanUp() {
		int collected = 0;
		// indexing trees
		collected += Insert_user_db_Map.cleanUpUnnecessaryMappings();
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

	public static final void InsertEvent(String user, String db, String p, String data) {
		Insert_activated = true;
		while (!insert_RVMLock.tryLock()) {
			Thread.yield();
		}

		CachedWeakReference wr_user = null;
		CachedWeakReference wr_db = null;
		MapOfMonitor<InsertRawMonitor> matchedLastMap = null;
		InsertRawMonitor matchedEntry = null;
		boolean cachehit = false;
		if (((db == Insert_user_db_Map_cachekey_db) && (user == Insert_user_db_Map_cachekey_user) ) ) {
			matchedEntry = Insert_user_db_Map_cachevalue;
			cachehit = true;
		}
		else {
			wr_user = new CachedWeakReference(user) ;
			wr_db = new CachedWeakReference(db) ;
			{
				// FindOrCreateEntry
				MapOfMonitor<InsertRawMonitor> node_user = Insert_user_db_Map.getNodeEquivalent(wr_user) ;
				if ((node_user == null) ) {
					node_user = new MapOfMonitor<InsertRawMonitor>(1) ;
					Insert_user_db_Map.putNode(wr_user, node_user) ;
				}
				matchedLastMap = node_user;
				InsertRawMonitor node_user_db = node_user.getNodeEquivalent(wr_db) ;
				matchedEntry = node_user_db;
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
			// D(X) main:4
			InsertRawMonitor created = new InsertRawMonitor() ;
			matchedEntry = created;
			matchedLastMap.putNode(wr_db, created) ;
		}
		// D(X) main:8--9
		matchedEntry.event_Insert(user, db, p, data);

		if ((cachehit == false) ) {
			Insert_user_db_Map_cachekey_db = db;
			Insert_user_db_Map_cachekey_user = user;
			Insert_user_db_Map_cachevalue = matchedEntry;
		}

		insert_RVMLock.unlock();
	}

}
