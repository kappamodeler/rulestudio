package com.plectix.rulestudio.views.storyrenderer;

import java.util.Timer;
import java.util.TimerTask;

import javax.swing.JFrame;
import javax.swing.JPanel;

import com.plectix.rulestudio.views.storyrenderer.StoryVisualizer.MouseMode;

public class TestMain {

	public static void main(String[] args) throws Exception {

		final StoryVisualizer storyVisualizer = new StoryVisualizer(args[0],
				new GraphSettings());
		System.err.println("Number of stories: "
				+ storyVisualizer.getNumberOfStories());
		JFrame frame = new JFrame("Graph View");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		JPanel jPanel = storyVisualizer.displayStory(0);

		frame.pack();
		frame.getContentPane().add(jPanel);
		frame.pack();
		storyVisualizer.autoResize();
		frame.setVisible(true);

		Timer timer = new Timer();
		timer.scheduleAtFixedRate(new TimerTask() {
			boolean transforming = true;

			public void run() {
				if (transforming) {
					storyVisualizer.setMouseMode(MouseMode.TRANSFORMING);
					System.err.println("Transforming now");
				} else {
					storyVisualizer.setMouseMode(MouseMode.PICKING);
					System.err.println("Picking now");
				}
				transforming = !transforming;
			}
		}, 0, 10000);
	}
}
