package com.plectix.rulestudio.core.usagedata;

import java.util.Random;

public class Test {

	private static final Random random = new Random();
	
	private static int numberOfActions = 1000;
	
	public static void main(String[] args)  {
		UsageDataCollector usageDataCollector = UsageDataCollector.getInstance();
		usageDataCollector.setStorageConnector(StorageConnector.PROPERTIES_FILE);
		usageDataCollector.start();
		
		Action[] actionList = Action.values();
		boolean start = true;
		
		for (int i= 0; i < numberOfActions; i++) {
			Action action = actionList[random.nextInt(actionList.length)];
			try {
				Thread.sleep(random.nextInt(20));
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			
			if (action == Action.SIMULATOR_JSIM_RUN_SIMULATION  // let's make sure that the count for simulator stays at zero
					|| action == Action.PLUGINS_SESSION   // plugin sessions are recorded automatically...
					|| action == Action.UDC_CALL_ERROR    // errors are also logged automatically
					|| action == Action.UDC_READ_ERROR
					|| action == Action.WORKSPACE_KAPPA_FILE) {
				continue;
			}
			if (action.isOneTime()) {
				if (action.getActionProcessor() instanceof ListActionProcessor) {
					// this is done automatically...
				} else {
					usageDataCollector.addOneTimeAction(action);
				}
			} else {
				usageDataCollector.addTimeSpanAction(action, start);
				start = !start;   // this logic would create some errors, but that's fine...
			}
			
			if (random.nextDouble() < 0.002) {
				System.err.println("Returning Post: \n[" + usageDataCollector.getPost() + "]\n");
				
				for (int files= 0; files< 1000; files++) {
					usageDataCollector.addOneTimeActionWithLabel(Action.WORKSPACE_KAPPA_FILE, "file" + random.nextInt(1000) + ".ka");
				}
			}
		}

		usageDataCollector.stop();
		// System.err.println("usageDataCollector: \n" + usageDataCollector);
		System.err.println("usageDataCollector: \n" + usageDataCollector.toVerboseString());
		
		System.err.println("Exiting...");
	}
}
