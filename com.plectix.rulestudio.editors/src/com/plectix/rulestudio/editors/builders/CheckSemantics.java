/**
 * 
 */
package com.plectix.rulestudio.editors.builders;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IMarker;

import com.plectix.rulestudio.editors.view.model.AgentObject;
import com.plectix.rulestudio.editors.view.model.KappaModelObject;
import com.plectix.rulestudio.editors.view.model.OutlineObject;
import com.plectix.rulestudio.editors.view.model.RuleObject;
import com.plectix.rulestudio.editors.view.model.SiteObject;

/**
 * This class has all of the semantic checking for agents and rules.
 * 
 * @author bill
 *
 */
public class CheckSemantics {
	
	private final static int ANY_BOND = -1;
	private final static int WILD_BOND = -2;
	
	private KappaSyntaxParser parser;
	
	public CheckSemantics(KappaSyntaxParser parser) {
		this.parser = parser;
	}

	public List<Integer> checkAgent(AgentObject agent) {
		HashMap<String, SiteObject> dup = new HashMap<String, SiteObject>();
		boolean hasDup = false;
		ArrayList<Integer> bond = new ArrayList<Integer>();
		OutlineObject[] list = agent.getChildren();
		for (OutlineObject obj : list) {
			if (obj instanceof SiteObject) {
				SiteObject site = (SiteObject) obj;
				String name = site.getName();
				if (!hasDup && dup.containsKey(name)) {
					parser.createMarker(agent.getStart(), agent.getEnd(),
						"Duplicate site " + name + " in " + agent.getName(),
						IMarker.SEVERITY_ERROR);
					hasDup = true;
				} else {
					dup.put(name, site);
				}
				if (site.isAny()) {
					bond.add(ANY_BOND);
				} else if (site.isWild()) {
					bond.add(WILD_BOND);
				} else if (site.bond() != 0) {
					int link = site.bond();
					if (bond.contains(link)) {
						parser.createMarker(agent.getStart(), agent.getEnd(), "Duplicate bond " + link + " in " + agent.getName(), IMarker.SEVERITY_ERROR);
					} else {
						bond.add(link);
					}
				}
			}
		}
		agent.setSiteMap(dup);
		return bond;
	}
	
	public void checkAgentList(OutlineObject[] list) {
		HashMap<Integer,AgentObject>bondCheck = new HashMap<Integer, AgentObject>();
		for (OutlineObject obj: list) {
			if (obj instanceof AgentObject) {
				AgentObject agent = (AgentObject)obj;
				List<Integer> bond = checkAgent(agent);
				for (Integer num: bond) {
					switch (num) {
					case ANY_BOND:
						agent.setAny();
						break;
					case WILD_BOND:
						agent.setWild();
						break;
					default:
						if (bondCheck.containsKey(num)) {
							AgentObject bag = bondCheck.get(num);
							if (bag == null) {
								parser.createMarker(agent.getStart(), agent.getEnd(), "Bond " + num + " appears more than twice.", IMarker.SEVERITY_ERROR);
							} else {
								bondCheck.put(num, null);
							}
						} else {
							bondCheck.put(num, agent);
						}
					}
				}
			}
		}
		for (Integer num: bondCheck.keySet()) {
			AgentObject agent = bondCheck.get(num);
			if (agent != null) {
				parser.createMarker(agent.getStart(), agent.getEnd(), "Unattached bond " + num + " in " + agent.getName(), IMarker.SEVERITY_ERROR);
			}
		}
	}

	public void checkRule(RuleObject rule, boolean singleDirection) {
		OutlineObject[] leftList = rule.getLeft().getChildren();
		OutlineObject[] rightList = rule.getRight().getChildren();
		checkAgentList(leftList);
		checkAgentList(rightList);
		ArrayList<AgentObject> added = new ArrayList<AgentObject>();
		ArrayList<AgentObject> removed = new ArrayList<AgentObject>();
		for (int l = 0, r = 0; l < leftList.length || r < rightList.length;) {
			AgentObject left = null;
			AgentObject right = null;
			if (l < leftList.length) {
				left = (AgentObject)leftList[l];
			}
			if (r < rightList.length) {
				right = (AgentObject)rightList[r];
			}
			if (left == null) {
				added.add(right);
				++r;
			} else if (right == null) {
				removed.add(left);
				++l;
			} else if (left.getName().equals(right.getName())) {
				++l;
				++r;
				checkChange(left, right, singleDirection);
			} else if (appears(left, rightList, r)) {
				added.add(right);
				++r;		// test after done to check states
			} else {
				removed.add(left);
				++l;
			}
		}
		checkAddOrRemove(added, removed);
	}

	private void checkAddOrRemove(ArrayList<AgentObject> added,
			ArrayList<AgentObject> removed) {
		if (added.size() == 0 || removed.size() == 0)
			return; // no dups

		for (AgentObject add : added) {
			for (AgentObject remove : removed) {
				if (remove.getName().equals(add.getName())) {
					parser
							.createMarker(
									add.getStart(),
									add.getEnd(),
									"Agent "
											+ add.getName()
											+ " is added and removed, but not updated by this rule.",
									IMarker.SEVERITY_WARNING);
				}

			}
		}

	}

	/**
	 * Check to see if the agent appears later in the list
	 * 
	 * @param left
	 * @param rightList
	 * @param r
	 * @return
	 */
	private boolean appears(AgentObject left, OutlineObject[] rightList, int r) {
		for (int i = r+1; i < rightList.length; ++i) {
			if (left.getName().equals(rightList[i].getName()))
				return true;
		}
		return false;
	}

	/**
	 * Check to see if the left agent can be transformed into the right agent.
	 * 
	 * @param left
	 * @param right
	 * @param checkSites if true check for site matches
	 */
	private void checkChange(AgentObject left, AgentObject right, boolean singleDirection) {
			if (left.size() != right.size()) {
				parser.createMarker(left.getStart(), left.getEnd(), 
						"Agent " + left.getName() + " has different number of sites on different sides of the rule.", 
						IMarker.SEVERITY_ERROR);
			} else {
				Map<String, SiteObject> rightSiteList = right.getSiteMap();
				for (OutlineObject obj: left.getChildren()) {
					SiteObject lSite = (SiteObject)obj;
					SiteObject rSite = rightSiteList.get(lSite.getName());
					if (rSite == null) {
						parser.createMarker(left.getStart(), left.getEnd(), 
								"Site " + lSite.getName() + " on " + left.getName() 
								+ " is missing from the agent on the other side.", 
								IMarker.SEVERITY_ERROR);
						break;		// only 1 per agent
					}
					if (lSite.size() > 0 && rSite.size() == 0) {
						parser.createMarker(right.getStart(), right.getEnd(), 
								"Rules cannot delete states.", IMarker.SEVERITY_ERROR);
					} else if (lSite.size() == 0 && rSite.size() > 0) {
						parser.createMarker(left.getStart(), left.getEnd(), 
								"Rules cannot add states.", IMarker.SEVERITY_ERROR);
					}
					if (lSite.isWild() && !rSite.isWild()) {
						parser.createMarker(left.getStart(), left.getEnd(), 
								"Wildcard bonds cannot be removed by a rule.", 
								IMarker.SEVERITY_ERROR);
					} else if (!lSite.isWild() && rSite.isWild()) {
						parser.createMarker(right.getStart(), right.getEnd(), 
								"Wildcard bonds cannot be added by a rule.", 
								IMarker.SEVERITY_ERROR);
					}
					if (lSite.isAny() != rSite.isAny()) {
						if (!singleDirection || rSite.isAny()) {
							parser.createMarker(right.getStart(), right.getEnd(), 
									"Any Bonds cannot be added by a rule.", IMarker.SEVERITY_ERROR);
						}
					}
				}
			}
		
		
	}

	public void finalCheck(KappaModelObject kappaModel) {
		OutlineObject rules = kappaModel.getRules();
		if (rules != null) {
			OutlineObject[] list = rules.getChildren();
			for (OutlineObject obj:list) {
				postCheckRules((RuleObject)obj);
			}
		}
		
		OutlineObject inits = kappaModel.getInits();
		if (inits != null) {
			OutlineObject[] list = inits.getChildren();
			for (OutlineObject obj: list) {
				postCheckInit(obj);
			}
		}
		
		OutlineObject obs = kappaModel.getObs();
		if (obs != null) {
			OutlineObject[] list = obs.getChildren();
			for (OutlineObject obj: list) {
				OutlineObject[] children = obj.getChildren();
				if (children.length == 1 && !(children[0] instanceof AgentObject)) {
					if (kappaModel.findRuleObject(children[0].getLabel()) == null) {
						parser.createPostMarker(obj.getOffset(), obj.getLine(), 
								0, obj.getName().length()+1, "Must refer to a rule label", 
								IMarker.SEVERITY_ERROR);
					}
				}
			}
		}
		
		OutlineObject stories = kappaModel.getStories();
		if (stories != null) {
			OutlineObject[] list = stories.getChildren();
			for (OutlineObject obj: list) {
				OutlineObject[] children = obj.getChildren();
				if (children.length > 0) {
					if (kappaModel.findRuleObject(children[0].getName()) == null) {
						parser.createPostMarker(obj.getOffset(), obj.getLine(), 
								0, obj.getName().length()+1, "Story label should refer to a rule label", 
								IMarker.SEVERITY_WARNING);
					}
				}
			}
		}
		
	}

	private void postCheckInit(OutlineObject init) {
		OutlineObject[] list = init.getChildren();
		for (OutlineObject obj: list) {
			if (obj instanceof AgentObject) {
				AgentObject agent = (AgentObject)obj;
				Map<String, SiteObject> map = agent.getSiteMap();
				if (map == null) {
					continue;						// not relevant
				}
				AgentObject master = agent.getMaster();
				for (OutlineObject sObj: master.getChildren()) {
					SiteObject mSite = (SiteObject)sObj;
					SiteObject aSite = map.get(mSite.getName());
					if (aSite == null) {
						parser.createPostMarker(init.getOffset(), init.getLine(), agent.getStart(), 
								agent.getEnd(), "Site " + mSite.getName() + " missing from initial conditions.", 
								IMarker.SEVERITY_ERROR);
					} else if (mSite.size() > 0 && aSite.size() == 0) {
						parser.createPostMarker(init.getOffset(), init.getLine(), agent.getStart(), agent.getEnd(), 
								"Site " + aSite.getName() + " needs a state.", IMarker.SEVERITY_ERROR);
					}
				}
			}
		}
		
	}

	private void postCheckRules(RuleObject rule) {
		OutlineObject[] leftList = rule.getLeft().getChildren();
		OutlineObject[] rightList = rule.getRight().getChildren();
		String dir = rule.getDirection().getName();
		boolean singleDirection = dir.equals("->");
		for (int l = 0, r = 0; l < leftList.length || r < rightList.length;) {
			AgentObject left = null;
			AgentObject right = null;
			if (l < leftList.length) {
				left = (AgentObject)leftList[l];
			}
			if (r < rightList.length) {
				right = (AgentObject)rightList[r];
			}
			if (left != null && right != null && left.getName().equals(right.getName())) {
				++l;
				++r;
			} else if (right != null && (left == null || appears(left, rightList, r))) {
				++r;
				checkAdd(rule, right);
			} else if (left != null){
				++l;
				if (!singleDirection) {
					checkAdd(rule, left);
				}
			}
		}
	}

	private void checkAdd(RuleObject rule, AgentObject right) {
		Map<String, SiteObject> map = right.getSiteMap();
		if (map == null)
			return;				// ignore already has errors
		AgentObject master = right.getMaster();
		if (map != null && master != null) {
			for (OutlineObject obj: master.getChildren()) {
				SiteObject site = (SiteObject)obj;
				SiteObject ruleSite = map.get(site.getName());
				if (ruleSite == null) {
					parser.createPostMarker(rule.getOffset(), rule.getLine(), right.getStart(), right.getEnd(),
							"Missing " + site.getName() + " from synthesis rule.", IMarker.SEVERITY_ERROR);
				} else if (site.size() > 0 && ruleSite.size() == 0) {
					parser.createPostMarker(rule.getOffset(), rule.getLine(), right.getStart(), right.getEnd(),
							"State needed for synthesis rule.", IMarker.SEVERITY_ERROR);
				}
			}
		}
		
	}



}
