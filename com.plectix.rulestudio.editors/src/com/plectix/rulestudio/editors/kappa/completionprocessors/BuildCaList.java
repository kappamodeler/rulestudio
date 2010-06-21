package com.plectix.rulestudio.editors.kappa.completionprocessors;

import java.util.ArrayList;

import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.contentassist.CompletionProposal;
import org.eclipse.jface.text.contentassist.ICompletionProposal;

import com.plectix.rulestudio.editors.builders.KappaSyntaxParser;
import com.plectix.rulestudio.editors.view.model.AgentObject;
import com.plectix.rulestudio.editors.view.model.KappaModelObject;
import com.plectix.rulestudio.editors.view.model.OutlineObject;

public class BuildCaList {
	private KappaModelObject model = null;
	private String line = null;
	private boolean inLabel;
	private int offset;
	private final static String[] percent = new String[] {"%init: ", "%mod: ", "%obs: ", "%story: "};
	private final static String[] percentLabel = new String[] {
		"%init: number * agent list", 
		"%mod: perturbation expression", 
		"%obs: rule or agent observable", 
		"%story: rule"};
	private final static String NON_AGENT_CHAR = ")@{}";

	
	public BuildCaList(ITextViewer viewer, int offset) {
		IDocument 		document = viewer.getDocument();
        KappaSyntaxParser parser = new KappaSyntaxParser(false);
        parser.validateString(document.get(), false);
        model = parser.getKappaModel();
        line = buildLine(document.get(), offset);
        this.offset = offset;
	}
	
	public String getLine() {
		return line;
	}
	
	public boolean isInLabel() {
		return inLabel;
	}
	
	public int getOffset() {
		return offset;
	}
	
	private String buildLine(String content, int offset) {
		if (offset == 0)
			return "";
		
		int to = getStart(content, offset);
		inLabel = false;
		StringBuffer buf = new StringBuffer(offset - to);
		for (; to < offset; ++to) {
			char ch = content.charAt(to);
			buf.append(ch);
			if (ch == '\'')
				inLabel = !inLabel;
		}
		return buf.toString();
	}
	
	public char getActivationCharacter(char[] list) {
		for (int i = line.length()-1; i >= 0; --i) {
			char lch = line.charAt(i);
			for (char ch: list) {
				if (ch == lch)
					return ch;
			}
		}
		return '\n';			// end of previous line
	}

	private int getStart(String content, int offset) {
		int to = offset - 1;
		while (to > 0) {
			char ch = content.charAt(to);
			if (ch == '\n') {
				if (to <= 1)
					return to+1;		// no room for continuation
				int delta = 1;
				if (content.charAt(to-delta) == '\r') 
					++delta;				// ignore \r
				if (to <= delta || content.charAt(to-delta) != '\\')
					return to+1;
				else 
					to -= delta;		
			} else {
				--to;
			}
		}
		return 0;					// must be at the start of the string
	}

	public void buildEmptyList(ArrayList<ICompletionProposal> result) {
		if (line.length() == 0) {
			addProposal(result, "", "'", "'rule label' agent list");
		}
		return;
	}
	
	public void buildPrecentCompletionProposals(ArrayList<ICompletionProposal> result) {
		
        for (int index = 0; index < percent.length; index++){
        	addProposal(result, line, percent[index], percentLabel[index]);
        }

		return;
	}

	public void buildLabelList(ArrayList<ICompletionProposal> result){
        ArrayList<String>labels = model.getLabelList();
        String prefix = getLabelPrefix();
		
		for (String label: labels) {
			addProposal(result, prefix, label, label);
		}
        return;
	}

	private String getLabelPrefix() {
		for (int n = line.length() - 1; n >= 0; n--) {
			char c = line.charAt(n);
			if (c == '\''){
				return line.substring(n, line.length());
			}
		}
		return "";
	}
	
	/**
	 * Get the agent list
	 * 
	 * @param document
	 * @param offset
	 * @param prefix
	 * @param agentsListed
	 * @return
	 */
	public void buildFullAgentList(ArrayList<ICompletionProposal> result, String prefix){

		ArrayList<String> repeat = getCurrentList(prefix);
		// Look through all the agent objects and add them to the completion
		// processor.  Remove repeats.
		OutlineObject agentsObject = model.findChildByLabel("Agents");
		if (agentsObject != null) {
			for (OutlineObject agent : agentsObject.getChildren()) {
				if (!repeat.contains(agent.getName())) {
					AgentEnum list = new AgentEnum(agent);
					while (list.hasNext()) {
						String label = list.nextAgent();
						addProposal(result, prefix, label, label);
					}
				}
			}
		}

        return;
	}

	public void buildAgentNameList(ArrayList<ICompletionProposal> result, String prefix) {

		ArrayList<String> repeat = getCurrentList(prefix);
		// Look through all the agent objects and add them to the completion
		// processor.  Remove repeats.
		OutlineObject agentsObject = model.findChildByLabel("Agents");
		if (agentsObject != null) {
			for (OutlineObject agent : agentsObject.getChildren()) {
				if (!repeat.contains(agent.getName())) {
					String label = agent.getName();
					addProposal(result, prefix, label, label);
				}
			}
		}

		return;
	}

	private ArrayList<String> getCurrentList(String prefix) {
		ArrayList<String> ret = new ArrayList<String>();
		
		int toEnd = line.length() - prefix.length();
		for (int i = 0; i < toEnd; ++i) {
			char lch = line.charAt(i);
			if (Character.isWhitespace(lch)) {
				continue;					// skip whitespace
			} else if (Character.isLetterOrDigit(lch)) {
				int from = i;
				while(++i < toEnd) {
					char ich = line.charAt(i);
					if (!isSiteAgentChar(ich))
						break;
				}
				int end = i;
				while (i < toEnd) {
					char ech = line.charAt(i);
					if (Character.isWhitespace(ech))
						++i;
					else 
						break;
				}
				if (i >= toEnd)
					break;						// at the end of the list
				if (line.charAt(i) == '(') {
					ret.add(line.substring(from, end));
					while (++i < toEnd && line.charAt(i) != ')') {
						// skip to the end of the agent;
					}
				}
			} else if (lch == '>') {			// ignore before -> or <->
				ret.clear();
			}
		}
		return ret;
	}

	public void addProposal(ArrayList<ICompletionProposal> result,
			String prefix, String label, String info) {
		if (label.startsWith(prefix) == true && label.length() > prefix.length()){
			String toInsert = label.substring(prefix.length());
			result.add(new CompletionProposal(toInsert, offset, 
					         0, toInsert.length(), null, info, null, null));
		}
	}
	
	public String getAgentPrefix() {
		int to = line.length();
		int from = to;
		while (--from >= 0) {
			char ch = line.charAt(from);
			if (!isSiteAgentChar(ch)) {
				return line.substring(from+1, to);
			}
		}
		
		return line;
	}

	public String getLabel() {
		if (inLabel)
			return "";
		int to = line.length();
		while (--to > 0) {
			if (line.charAt(to) == '\'')
				break;
		}
		if (to == 0)
			return "";
		
		int from = to;
		while (--from > 0) {
			if (line.charAt(from) == '\'') {
				return line.substring(from+1, to);
			}
		}
		return "";
	}

	public boolean isRuleLabel(String label) {
		return model.findRuleObject(label) != null;
	}

	public boolean inAgent() {
		return agentName(agentStart()) != null;
	}

	private String agentName(int to) {
		if (to == -1)
			return null;
		--to;							// skip over (
		for ( ; to >= 0 && Character.isWhitespace(line.charAt(to)); --to) {
			;
		}
		int from = to;
		for (; from >= 0; --from) {
			char ch = line.charAt(from);
			if (!isSiteAgentChar(ch)) {
				break;
			}
		}
		
		if (from == to)
			return null;
		else 
			return line.substring(from+1, to+1);
	}
	
	private int agentStart() {
		for (int to = line.length() -1; to >= 0; --to) {
			char ch = line.charAt(to);
			if (ch == '(') {
				return to;
			} else if (NON_AGENT_CHAR.indexOf(ch) != -1) {
				return -1;						// not an agent
			}
		}
		return -1;
	}

	public void buildSiteList(ArrayList<ICompletionProposal> ret) {
		int off = agentStart();
		String name = agentName(off);
		if (name == null)
			return;
		
		String prefix = sitePrefix();
		ArrayList<String> siteList = siteList(off);
		ArrayList<String> moreList = getNewSiteList(name, siteList);
		if (moreList.size() > 0) {
			StringBuffer buf = new StringBuffer();
			boolean first = true;
			for (String site: moreList) {
				if (first) 
					first = false;
				else 
					buf.append(',');
				buf.append(site);
			}
			buf.append(')');
			String all = buf.toString();
			addProposal(ret, prefix, all, all);
			if (moreList.size() == 1)
				return;							// only 1 to add
		}
		for (String site: moreList) {
			addProposal(ret, prefix, site, site);
		}
		return;	
	}

	private ArrayList<String> getNewSiteList(String name,
			ArrayList<String> siteList) {
		AgentObject agent = model.findAgentObject(name);
		ArrayList<String> ret = new ArrayList<String>();
		for (OutlineObject site: agent.getChildren()) {
			String sName = site.getName();
			if (!siteList.contains(sName))
				ret.add(sName);
		}
		return ret;
	}

	private ArrayList<String> siteList(int off) {
		ArrayList<String> ret = new ArrayList<String>();
		if (line.charAt(off) != '(')
			return ret;
		int from = -1;
		String toAdd = null;
		while (++off < line.length()) {
			char ch = line.charAt(off);
			if (isSiteAgentChar(ch)) {
				if (from == -1 && toAdd == null) {
					from = off;
				}
			} else if (ch == ',') {
				if (toAdd != null) {
					ret.add(toAdd);
					toAdd = null;
				} else if (from != -1) {
					ret.add(line.substring(from, off));
					from = -1;
				}
			} else if (from != -1) {
				toAdd = line.substring(from, off);
				from = -1;
			}
		}
		return ret;
	}

	private String sitePrefix() {
		int to = line.length();
		boolean haveChar = false;
		while (--to >= 0) {
			char ch = line.charAt(to);
			if (isSiteAgentChar(ch))
				haveChar = true;
			else if (Character.isWhitespace(ch)) {
				if (haveChar)
					break;
			} else {
				break;
			}
		}
		return line.substring(to+1, line.length());
	}
	
	private boolean isSiteAgentChar(char ch) {
		return Character.isLetterOrDigit(ch) || ch == '-' || ch == '^' || ch == '_';
	}

	public String getRightSide() {
		int to = 0;
		for ( ; to < line.length() && Character.isWhitespace(line.charAt(to)); ++to )
			;
		if (line.charAt(to) == '\'') {		// skip label
			while(++to < line.length() && line.charAt(to) != '\'')
				;
			while (++to < line.length() && Character.isWhitespace(line.charAt(to)))
				;
		}
		if (to >= line.length())
			return "";					// nothing
		
		int from = to;
		
		for (; to < line.length(); ++to) {
			char ch = line.charAt(to);
			if (ch == '{' || ch == '>') {
				if (ch == '>') {
					if (to > 2 && line.charAt(to-2) == '<' && line.charAt(to-1) == '-')
						to -= 2;
					else if (to > 1 && line.charAt(to-1) == '-')
						to -= 1;
					else 
						return "";			// not an arrow
				}
				--to;
				for (; to >= from && Character.isWhitespace(line.charAt(to)); --to)
					;
				
				return line.substring(from, to+1);
			}
		}
		
		return "";
	}

	public void buildStateList(ArrayList<ICompletionProposal> ret) {
		int off = agentStart();
		String name = agentName(off);
		if (name == null)
			return;
		
		String prefix = sitePrefix();				// also works for state
		String site = getSite();
		if (site == null)
			return;									// cannot find site, ignore
		ArrayList<String> moreList = getStateList(name, site);
		for (String state: moreList) {
			addProposal(ret, prefix, state, state);
		}
		return;	
	}

	private ArrayList<String> getStateList(String name, String siteName) {
		AgentObject agent = model.findAgentObject(name);
		ArrayList<String> ret = new ArrayList<String>();
		for (OutlineObject site: agent.getChildren()) {
			if (site.getName().equals(siteName)) {
				for (OutlineObject state: site.getChildren()) {
					ret.add(state.getName());
				}
				return ret;
			}
		}
		return ret;
	}

	private String getSite() {
		int from = line.length();
		int to = -1;
		int myFrom = -1;
		while (--from > 0) {
			char ch = line.charAt(from);
			if (isSiteAgentChar(ch)) {
				if (to == -1) {
					to = from;
					myFrom = -1;
				}
			} else if (ch == ',' || ch == '(') {
				if (to != -1) {
					if (myFrom != -1)
						from = myFrom;
					return line.substring(from+1, to+1);
				} else
					return null;
			} else if (Character.isWhitespace(ch)){
				myFrom = from;
			} else {
				to = -1;
				myFrom = -1;
			}
		}
		return null;
	}
	
	public static class AgentEnum {
		private OutlineObject agent = null;
		private OutlineObject[] sites;
		private int[] stateCount;
		private int[] nextAgent;
		private boolean more = true;
		
		public AgentEnum(OutlineObject agent) {
			this.agent = agent;
			sites = agent.getChildren();
			stateCount = new int[sites.length];
			nextAgent = new int[sites.length];
			for (int i = 0; i < sites.length; ++i) {
				stateCount[i] = sites[i].getChildren().length;
				nextAgent[i] = 0;
			}
		}
		
		public boolean hasNext() {
			return more;
		}
		
		public String nextAgent() {
			StringBuffer buf = new StringBuffer(agent.getName());
			buf.append('(');
			boolean iterate = true;
			for (int i = 0; i < sites.length; ++i) {
				if (i > 0)
					buf.append(',');
				buf.append(sites[i].getName());
				OutlineObject[] states = sites[i].getChildren();
				if (states.length > 0) {
					buf.append('~');
					buf.append(states[nextAgent[i]].getName());
					if (iterate) {
						if (++nextAgent[i] >= states.length) {
							nextAgent[i] = 0;
						} else {
							iterate = false;
						}
					}
				}
			}
			more = !iterate;
			buf.append(')');
			return buf.toString();
		}
	}
	
}
