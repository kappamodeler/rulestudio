package com.plectix.rulestudio.views.storyrenderer;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;

import org.xmlpull.mxp1.MXParser;

public class StoryReader {
	
	public StoryReader() {
		super();
	}
	
	private String model = "";
	private String log = "";

	public List<Story> read(final String xmlFilename) throws Exception {

		List<Story> storyList = new ArrayList<Story>();
		
		final MXParser mxParser = new MXParser();
		final BufferedReader reader = new BufferedReader(new FileReader(xmlFilename));
		mxParser.setInput(reader);
		StringBuffer buf = new StringBuffer();

		int event = mxParser.next();
		while (event != MXParser.END_DOCUMENT) {
			if (event == MXParser.START_TAG) {
				if (mxParser.getName().equalsIgnoreCase("STORY")) {
					storyList.add(readStory(mxParser));
				} else if (mxParser.getName().equalsIgnoreCase("NODE")) {
					// get last story:
					Story story = storyList.get(storyList.size()-1);
					story.addNode(readStoryNode(mxParser));
				} else if (mxParser.getName().equalsIgnoreCase("CONNECTION")) {
					// get last story:
					Story story = storyList.get(storyList.size()-1);
					story.addConnection(readStoryConnection(mxParser));
				} else if (mxParser.getName().equalsIgnoreCase("MODEL")) {
					mxParser.next();
					model = mxParser.getText();
				} else if (mxParser.getName().equalsIgnoreCase("ENTRY")) {
					String msg = null;
					boolean isInfo = false;
					for (int i= 0; i < mxParser.getAttributeCount(); i++) {
						String attributeName = mxParser.getAttributeName(i);
						if (attributeName.equalsIgnoreCase("MESSAGE")) {
							msg = mxParser.getAttributeValue(i);
						} else if (attributeName.equalsIgnoreCase("TYPE")) {
							isInfo = mxParser.getAttributeValue(i).equalsIgnoreCase("INFO");
						}
					}
					if (isInfo && msg != null) {
						buf.append(msg);
						buf.append("\n");
					}
					
				}
			}
			
			event = mxParser.next();
		}
		reader.close();
		
		for (Story story : storyList) {
			story.sortNodes();
		}
		
		log = buf.toString();
		return storyList;
	}
	
	private static final Story readStory(final MXParser mxParser) {
		Story story = new Story();
		for (int i= 0; i < mxParser.getAttributeCount(); i++) {
			String attributeName = mxParser.getAttributeName(i);
			if (attributeName.equalsIgnoreCase("OBSERVABLE")) {
				story.setObservable(mxParser.getAttributeValue(i));
			} else if (attributeName.equalsIgnoreCase("PERCENTAGE")) {
				story.setPercentage(mxParser.getAttributeValue(i));
			} else if (attributeName.equalsIgnoreCase("AVERAGE")) {
				story.setAverage(mxParser.getAttributeValue(i));
			} else {
				throw new RuntimeException("Unexpected attribute: " + attributeName);
			}
		}
		if (story.isValid() == false) {
			throw new RuntimeException("Story attribute()s are not found");
		}
		return story;
	}
	
	private static final StoryNode readStoryNode(final MXParser mxParser) {
		StoryNode storyNode = new StoryNode();
		for (int i= 0; i < mxParser.getAttributeCount(); i++) {
			String attributeName = mxParser.getAttributeName(i);
			if (attributeName.equalsIgnoreCase("ID")) {
				storyNode.setNodeID(mxParser.getAttributeValue(i));
			} else if (attributeName.equalsIgnoreCase("TYPE")) {
				storyNode.setType(mxParser.getAttributeValue(i));
			} else if (attributeName.equalsIgnoreCase("TEXT")) {
				storyNode.setText(mxParser.getAttributeValue(i));
			} else if (attributeName.equalsIgnoreCase("DATA")) {
				storyNode.setData(mxParser.getAttributeValue(i));
			} else if (attributeName.equalsIgnoreCase("DEPTH")) {
				storyNode.setDepth(mxParser.getAttributeValue(i));
			} else {
				throw new RuntimeException("Unexpected attribute: " + attributeName);
			}
		}
		if (storyNode.isValid() == false) {
			throw new RuntimeException("Story Node attribute(s) are not found");
		}
		return storyNode;
	}

	private static final StoryConnection readStoryConnection(final MXParser mxParser) {
		StoryConnection storyConnection = new StoryConnection();
		for (int i= 0; i < mxParser.getAttributeCount(); i++) {
			String attributeName = mxParser.getAttributeName(i);
			if (attributeName.equalsIgnoreCase("FROMNODE")) {
				storyConnection.setFromNode(mxParser.getAttributeValue(i));
			} else if (attributeName.equalsIgnoreCase("TONODE")) {
				storyConnection.setToNode(mxParser.getAttributeValue(i));
			} else if (attributeName.equalsIgnoreCase("RELATION")) {
				storyConnection.setRelation(mxParser.getAttributeValue(i));
			} else {
				throw new RuntimeException("Unexpected attribute: " + attributeName);
			}
		}
		if (storyConnection.isValid() == false) {
			throw new RuntimeException("Story Connection attribute(s) are not found");
		}
		return storyConnection;
	}
	
	public String getModel() {
		return model;
	}
	
	public String getLog() {
		return log;
	}
	
}
