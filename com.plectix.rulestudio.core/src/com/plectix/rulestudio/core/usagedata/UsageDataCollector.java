package com.plectix.rulestudio.core.usagedata;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;

import org.osgi.framework.Version;

import com.plectix.rulestudio.core.Activator;


public class UsageDataCollector {
	private static final boolean DISABLED = false;

	// Use a small number for tests, a large number for the product
	private static final long TIMER_INTERVAL = 5000;
	
	private StorageConnector storageConnector = StorageConnector.PREFERENCES_STORE;
	
	private ConcurrentLinkedQueue<UsageDataItem> actionQueue = new ConcurrentLinkedQueue<UsageDataItem>();
	
	private Timer timer = new Timer();
	
	private AtomicBoolean collectingData = new AtomicBoolean(false);
	
	private Object processQueueLock = new Object();
	
	private static UsageDataCollector INSTANCE;
	
	/**
	 * The constructor is private because this object is a single
	 */
	private UsageDataCollector() {
		super();
	}
	
	public static final UsageDataCollector getInstance() {
		if (INSTANCE == null) {
			INSTANCE = new UsageDataCollector();
		}
		return INSTANCE;
	}
	
	/**
	 * Called from the Activator when the plugins are loaded. 
	 * We don't expect any Action to be logged before calling this.
	 * 
	 */
	public void start() {
		if (DISABLED) {
			return;
		}

		// first let's start accepting usage data:
		// this doesn't mean that we are processing that data...
		collectingData.set(true);
		
		// then let's load the data...
		storageConnector.load();
		
		// add system related data:
		addSystemData();
		addTimeSpanAction(Action.PLUGINS_SESSION, true);
		
		// start the timer to process those:
		timer.schedule(new TimerTask() {
			@Override
			public void run() {
				processQueue();
			}
		}, TIMER_INTERVAL, TIMER_INTERVAL);
		
	}
	
	/**
	 * Stops the data collection process.
	 * Called from the Activator when the plugins are unloaded. 
	 * We don't expect any Action to be logged after calling this.
	 */
	public void stop() {
		if (DISABLED) {
			return;
		}

		// plugin session is ending
		addTimeSpanAction(Action.PLUGINS_SESSION, false);
		// so stop accepting new dataa
		collectingData.set(false);
		// and cancel the tread to process them
		timer.cancel();

		// After we cancel the tread above, it wouldn't interfere with a currently executing task
		// So we may be already executing processQueue() but it doesn't matter, we will call it
		// one more time below...

		//  we write the data into preferences inside processQueue()
		processQueue();
	}
	
	/**
	 * Prepares post data made up of action names, and their current values.
	 * The values are reset after they are reported.
	 * 
	 * @return
	 */
	public List<String> getPost() {
		Action[] actionList = Action.values();
		List<String> post = new ArrayList<String>(2*actionList.length);

		 // make sure we don't process the queue while preparing the post:
		synchronized (processQueueLock) { 
			for (Action action : actionList) {
				post.add(action.getName());
				post.add(action.getActionProcessor().toString());
				action.reset();
			}	
		}
		
		if (collectingData.get()) {
			// since the system data is also reset we have to recreate it here:
			addSystemData();
		}
		
		return post;
	}
	
	public boolean addOneTimeAction(Action action) {
		if (DISABLED) {
			return false;
		}
		if (!collectingData.get()) {
			throw new RuntimeException("UsageDataCollector is not working");
		}
		return actionQueue.add(new UsageDataItem(action, System.currentTimeMillis()));
	}
	
	public boolean addTimeSpanAction(Action action, boolean start) {
		if (DISABLED) {
			return false;
		}
		if (!collectingData.get()) {
			throw new RuntimeException("UsageDataCollector is not working");
		}
		return actionQueue.add(new UsageDataItem(action, System.currentTimeMillis(), start));
	}

	public boolean addOneTimeActionWithLabel(Action action, String label) {
		if (DISABLED) {
			return false;
		}
		if (!collectingData.get()) {
			throw new RuntimeException("UsageDataCollector is not working");
		}
		return actionQueue.add(new UsageDataItem(action, System.currentTimeMillis(), label));
	}
	
	private void processQueue() {
		// System.err.println("Processing queue");
		
		synchronized (processQueueLock) { // wait if a post is prepared	
			UsageDataItem usageDataItem = actionQueue.poll();
			while (usageDataItem != null) {
				usageDataItem.getAction().getActionProcessor().add(usageDataItem.getLabel(), usageDataItem.getTime(), usageDataItem.isStart());
				usageDataItem = actionQueue.poll(); 
			}
			storageConnector.save();
		}
 	}
	
	private void addSystemData() {
		addOneTimeActionWithLabel(Action.OS_NAME, System.getProperties().get("os.name") + ", "
                + System.getProperties().get("os.version") + ", "
                + System.getProperties().get("os.arch"));
		
		addOneTimeActionWithLabel(Action.JAVA, System.getProperties().get("java.version") + ", "
                + System.getProperties().get("java.vendor"));
                
		addOneTimeActionWithLabel(Action.JAVA_RUNTIME,System.getProperties().get("java.runtime.name") + ", "
                + System.getProperties().get("java.runtime.version"));
                
		addOneTimeActionWithLabel(Action.JAVA_VM, System.getProperties().get("java.vm.name") + ", "
                + System.getProperties().get("java.vm.version") + ", "
                + System.getProperties().get("java.vm.vendor") + ", "
                + System.getProperties().get("java.vm.info"));
                
		addOneTimeActionWithLabel(Action.DEFAULT_LOCALE, Locale.getDefault().toString());
		
		addOneTimeActionWithLabel(Action.TIMEZONE, TimeZone.getDefault().getID());
		
		Activator activator = Activator.getDefault();
		if (activator != null) {
			Version version = activator.getVersion();
			if (version != null) {
				addOneTimeActionWithLabel(Action.PLUGINS_VERSION, version.toString());
			}
		}
	}

	public String toString() {
		StringBuffer stringBuffer = new StringBuffer();
		Action[] actionList = Action.values();
		for (Action action : actionList) {
			stringBuffer.append(action + "\n");
		}
		return stringBuffer.toString();
	}
	
	public String toVerboseString() {
		StringBuffer stringBuffer = new StringBuffer();
		Action[] actionList = Action.values();
		for (Action action : actionList) {
			stringBuffer.append(action.toVerboseString() + "\n");
		}
		return stringBuffer.toString();
	}

	public final StorageConnector getStorageConnector() {
		return storageConnector;
	}

	public final void setStorageConnector(StorageConnector storageConnector) {
		this.storageConnector = storageConnector;
	}
	
	private static final class UsageDataItem {
		private String label;
		private long time;
		private Action action;
		private boolean start = false;

		public UsageDataItem(Action action, long time) {
			super();
			if (action.isTimeSpan()) {
				throw new RuntimeException("Action + " + action + " needs start or end flag to be specified...");
			}
			if (action.doesNeedLabel()) {
				throw new RuntimeException("Action + " + action + " needs a label to be specified...");
			}
			this.action = action;
			this.time = time;
		}

		public UsageDataItem(Action action, long time, boolean start) {
			super();			
			if (action.isTimeSpan() == false) {
				throw new RuntimeException("Action + " + action + " DOES NOT NEED start or end flag...");
			}
			if (action.doesNeedLabel()) {
				throw new RuntimeException("Action + " + action + " needs a label to be specified...");
			}
			this.action = action;
			this.time = time;
			this.start = start;
		}
		
		public UsageDataItem(Action action, long time, String label) {
			super();
			if (action.isTimeSpan()) {
				throw new RuntimeException("Action + " + action + " needs start or end flag to be specified...");
			}
			if (action.doesNeedLabel() == false) {
				throw new RuntimeException("Action + " + action + " DOES NOT NEED a label... Remove " + label);
			}
			this.action = action;
			this.time = time;
			this.label = label;
		}
		
		public final long getTime() {
			return time;
		}

		public final Action getAction() {
			return action;
		}

		public final boolean isStart() {
			return start;
		}

		public final String getLabel() {
			return label;
		}
	}

}
