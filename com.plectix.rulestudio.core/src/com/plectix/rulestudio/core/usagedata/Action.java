package com.plectix.rulestudio.core.usagedata;

import java.util.Comparator;

public enum Action {

	// Special actions to be registered when the UsageDataCollector is initialized...
	OS_NAME ("os.name", new ListActionProcessor()),
	JAVA ("java", new ListActionProcessor()),
	JAVA_RUNTIME ("jvm.runtime", new ListActionProcessor()),
	JAVA_VM ("jvm.vm", new ListActionProcessor()),
	DEFAULT_LOCALE ("defaultlocale", new ListActionProcessor()),
	TIMEZONE ("timezone", new ListActionProcessor()),
	PLUGINS_VERSION ("plugins.version", new ListActionProcessor()),
	
	// One-time Actions used by Usage Data Collector:
	UDC_CALL_ERROR ("udc.call.error", new TallyActionProcessor()),
	UDC_READ_ERROR ("udc.read.error", new TallyActionProcessor()),
	
	//////////////// Other actions: //////////////////////////////////////
	
	// Timespan Actions:
	PLUGINS_SESSION ("plugins.session", new TimeSpanActionProcessor()),
	SIMULATOR_JSIM_RUN_SIMULATION ("simulator.jsim.run.simulation", new TimeSpanActionProcessor()),
	SIMULATOR_JSIM_RUN_COMPRESSION ("simulator.jsim.run.compression", new TimeSpanActionProcessor()),
	SIMULATOR_JSIM_GENERATEMAP_INFLUENCEMAP ("simulator.jsim.generatemap.influencemap", new TimeSpanActionProcessor()),
	SIMULATOR_JSIM_GENERATEMAP_CONTACTMAP ("simulator.jsim.generatemap.contactmap", new TimeSpanActionProcessor()),
	SIMULATOR_JSIM_GENERATEMAP_REACHABLE("simulator.jsim.generatemap.reachable", new TimeSpanActionProcessor()),
	SIMULATOR_JSIM_GENERATEMAP_COMPILE("simulator.jsim.generatemap.compile", new TimeSpanActionProcessor()),
	
	// One-time Actions:
	WORKBENCH_PERSPECTIVE_CHANGE ("workbench.perspective.change", new TallyActionProcessor()),
	
	WORKBENCH_VIEW_EDITOR_FOCUS ("workbench.view.editor.focus", new TallyActionProcessor()),
	WORKBENCH_VIEW_CONTACTMAP_FOCUS ("workbench.view.conctactmap.focus", new TallyActionProcessor()),
	WORKBENCH_VIEW_INFLUENCEMAP_FOCUS ("workbench.view.influencemap.focus", new TallyActionProcessor()),
	WORKBENCH_VIEW_SIMULATOR_FOCUS ("workbench.view.simulator.focus", new TallyActionProcessor()),
	
	WORKBENCH_BUTTON_UPDATE_TO_CELLUCIDATE ("workbench.button.update.to.cellucidate", new TallyActionProcessor()),
	
	EDITOR_KAPPA_FILE_OPEN ("editor.kappa.file.open", new TallyActionProcessor()),
	EDITOR_KAPPA_FILE_SAVE ("editor.kappa.file.save", new TallyActionProcessor()),
	EDITOR_KAPPA_FILE_CLOSE ("editor.kappa.file.close", new TallyActionProcessor()),
	EDITOR_KAPPA_SYNTAX_CHECK ("editor.kappa.syntax.check", new TallyActionProcessor()),
	EDITOR_KAPPA_AUTOCOMPLETE_AGENT ("editor.kappa.autocomplete.agent", new TallyActionProcessor()),
	EDITOR_KAPPA_AUTOCOMPLETE_SITE ("editor.kappa.autocomplete.site", new TallyActionProcessor()),
	
	SIMULATOR_JSIM_SIMULATION_STOP ("simulator.jsim.run.simulation", new TallyActionProcessor()),
	
	// Set Actions:
	WORKSPACE_KAPPA_FILE ("workspace.kappa.file", new SetActionProcessor()),   // Usage: usageDataCollector.addOneTimeActionWithLabel(Action.WORKSPACE_KAPPA_FILE, "file.ka");
	
	;
	
	private final String name;
	
	private final boolean oneTime;
	
	private final boolean needsLabel;
	
	private final ActionProcessor actionProcessor;
	
	private Action(String name, ActionProcessor actionProcessor) {
		this.name = name;
		this.actionProcessor = actionProcessor;
		this.oneTime = this.actionProcessor.isOneTime();
		this.needsLabel = this.actionProcessor.doesNeedLabel();
	}

	public String toString() {
		return name + " = \"" + actionProcessor.toString() + "\"";
	}

	public String toVerboseString() {
		return name + " = \"" + actionProcessor.toVerboseString() + "\"";
	}

	public void reset() {
		actionProcessor.reset();
	}
	
	public final boolean isOneTime() {
		return oneTime;
	}
	
	public final boolean isTimeSpan() {
		return !oneTime;
	}

	public final String getName() {
		return name;
	}

	public final ActionProcessor getActionProcessor() {
		return actionProcessor;
	}

	public final boolean doesNeedLabel() {
		return needsLabel;
	}

	public static final ActionComparatorByName ACTION_COMPARATOR_BY_NAME = new ActionComparatorByName();
	
	private static final class ActionComparatorByName implements Comparator<Action> {
		public int compare(Action a1, Action a2) {
			return a1.getName().compareTo(a2.getName());
		}
	}

}
