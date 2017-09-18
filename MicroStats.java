import java.awt.BorderLayout;
import java.awt.EventQueue;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

import java.awt.Color;

import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JList;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.ListSelectionEvent;

import javafx.application.Platform;
import javafx.embed.swing.JFXPanel;
import javafx.scene.Scene;
import javafx.scene.chart.Chart;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;

import javax.swing.JTextField;

import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;

import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JButton;
import javax.swing.JScrollBar;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.AdjustmentListener;
import java.awt.event.AdjustmentEvent;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;

class Stats {
	int ID; // ID of the microtubule
	double[] rawData; // length data in pixels
	double[] endData; // relative length data of the ends in pixels.
	int[] events; // 0 - pause, -1 shrink, +1 grow
	ArrayList<Integer> growEvents = new ArrayList<Integer>(); // frames that
																// growEvents
																// start and end
	ArrayList<Integer> shrinkEvents = new ArrayList<Integer>();
	double rescue;
	double catastrophe;
	double dynamicity; // in micrometers
	int growCount = 0;
	double totGrowth = 0;
	double totShrink = 0;
	int shrinkCount = 0;
	boolean modified = false; // whether it has been mannually altered.
	int pause = 0; // pause times
	int grow = 0;
	int shrink = 0;

	Stats(int ID) {
		this.ID = ID;
	}

	void getRawData(double[][] stats, int pos) {
		// getting the length data for one microtubule over frames
		this.rawData = new double[stats.length];
		for (int i = 0; i < stats.length; ++i) {
			rawData[i] = stats[i][pos];
		}
	}

	void getEndsData(double[][] endStats, int pos) {
		this.endData = new double[endStats.length];
		for (int i = 0; i < endStats.length; ++i) {
			endData[i] = endStats[i][pos];
		}
	}

	void getRates(double[] rawData, int[] events, double interval) {
		// getting the averaged growth rate for this microtubule over the
		// period.
		int i = 0;
		int initial = i;
		boolean growing = events[0] == 1;
		boolean shrinking = events[0] == -1;
		double time;
		while (i < events.length) {
			if ((!growing) && (!shrinking) && events[i] != 0) {
				growing = events[i] == 1;
				shrinking = events[i] == -1;
				initial = i;
			}
			if (growing && events[i] == -1) {
				// from grow to shrink
				growing = false;
				shrinking = true;
				if (i - initial > 1) {
					// more than one frame for the phase
					time = (i - initial) * interval * 1.0 / 60;
					totGrowth += 0.08 * (rawData[i] - rawData[initial]) / time;
					growCount++;
					growEvents.add(initial);
					growEvents.add(i);
					// if (this.ID ==1) {
					// System.out.println("recording a growth event in middle");
					// System.out.println("growth is " + 0.08 * (rawData[i] -
					// rawData[initial]) / time);
					// System.out.println("grow count is " + growCount);
					// System.out.println("i is " + i);
					// System.out.println("initial is " + initial);
					// }
				}
				initial = i;
			}
			if (shrinking && events[i] == 1) {
				// from shrink to grow
				growing = true;
				shrinking = false;
				if (i - initial > 1) {
					time = (i - initial) * interval * 1.0 / 60;
					totShrink += 0.08 * (rawData[initial] - rawData[i]) / time;
					shrinkCount++;
					shrinkEvents.add(initial);
					shrinkEvents.add(i);
					// if (this.ID ==1) {
					// System.out.println("recording a shrinking event");
					// System.out.println("shrinkage is " + 0.08 *
					// (rawData[initial] - rawData[i]) / time);
					// System.out.println("shrink count is " + shrinkCount);
					// }
				}
				initial = i;
			}
			++i;
		}
		if (growing && (events[events.length - 1] >= 0)
				&& (events.length - initial > 1)) {
			// ended with growing
			growCount++;
			time = (events.length - initial) * interval * 1.0 / 60;
			totGrowth += 0.08 * (rawData[events.length] - rawData[initial])
					/ time;
			growEvents.add(initial);
			growEvents.add(events.length);
			// if (this.ID ==1) {
			// System.out.println("recording a growth event at end");
			// System.out.println("growth is " + 0.08 * (rawData[events.length]
			// - rawData[initial]) / time);
			// System.out.println("grow count is " + growCount);
			// }
		}
		if (shrinking && (events[events.length - 1] <= 0)
				&& (events.length - initial > 1)) {
			// ended with shrinking
			shrinkCount++;
			time = (events.length - initial) * interval * 1.0 / 60;
			totShrink += 0.08 * (rawData[initial] - rawData[events.length])
					/ time;
			shrinkEvents.add(initial);
			shrinkEvents.add(events.length);
			// if (this.ID ==1) {
			// System.out.println("recording a shrinking event");
			// System.out.println("shrinkage is " + 0.08 * (rawData[initial] -
			// rawData[events.length]) / time);
			// System.out.println("shrink count is " + shrinkCount);
			// }
		}
		if (shrinkCount == 0) {
			// to avoid 0/0
			shrinkCount++;
		}
		if (growCount == 0) {
			growCount++;
		}

	}

	void calculateGivenEvents(double interval) {
		// to calculate a mannually altered kymogram.
		totGrowth = 0;
		totShrink = 0;
		growCount = 0;
		shrinkCount = 0;
		Arrays.fill(events, 0);
		if (growEvents.size() > 0) {
			for (int i = 0; i < growEvents.size(); ++i) {
				if (i % 2 == 0) {
					int from = growEvents.get(i);
					int to = growEvents.get(i + 1);
					for (int j = from; j < to; ++j) {
						// redo events classification
						events[j] = 1;
					}
					double time = (to - from) * interval * 1.0 / 60;
					totGrowth += 0.08 * (endData[to] - endData[from]) / time;
//					if (ID==0) {
//						System.out.println("time is " + time);
//						System.out.println(");
//					}
					growCount++;
				}
			}
		} else {
			growCount++;
		}
		if (shrinkEvents.size() > 0) {
			for (int i = 0; i < shrinkEvents.size(); ++i) {
				if (i % 2 == 0) {
					int from = shrinkEvents.get(i);
					int to = shrinkEvents.get(i + 1);
					for (int j = from; j < to; ++j) {
						events[j] = -1;
					}
					double time = (to - from) * interval * 1.0 / 60;
					totShrink += 0.08 * (endData[from] - endData[to]) / time;
					shrinkCount++;
				}
			}
		} else {
			shrinkCount++;
		}
	}

	void classifyEvents(double[] rawData, double thresh) {
		this.events = new int[rawData.length - 1];
		for (int i = 0; i < rawData.length - 1; ++i) {
			double change = rawData[i + 1] - rawData[i];
			if (Math.abs(change) > thresh / 0.08) {
				// 0.08 microns per pixel
				if (change < 0) {
					// shrink event
					this.events[i] = -1;
				} else {
					// grow event
					this.events[i] = 1;
				}
			} else {
				// a pause event
				this.events[i] = 0;
			}
		}
	}

	void getTimes(int[] events) {
		pause = 0;
		grow = 0;
		shrink = 0;
		for (int i = 0; i < events.length; ++i) {
			if (events[i] == 0) {
				pause++;
			} else if (events[i] == 1) {
				grow++;
			} else {
				shrink++;
			}
		}
	}

	void getFrequencies(int[] events, double interval) {
		int growTimes = 0;
		int shrinkTimes = 0;
		for (int i = 0; i < events.length - 1; ++i) {
			if (events[i] == -1 && events[i + 1] > -1) {
				growTimes++;
			}
			if (events[i] > -1 && events[i + 1] == -1) {
				shrinkTimes++;
			}
		}
		if (pause == 0 && shrink == 0) {
			this.rescue = Double.POSITIVE_INFINITY;
		} else {
			this.rescue = growTimes * 1.0
					/ ((pause + shrink) * interval / 60.0);
		}
		if (pause == 0 && grow == 0) {
			this.catastrophe = Double.POSITIVE_INFINITY;
		} else {
			this.catastrophe = shrinkTimes * 1.0
					/ ((pause + grow) * interval / 60.0);
		}

	}

	void getDynamicity(int[] events, double[] rawData, double interval) {
		double change = 0;
		for (int i = 0; i < events.length; ++i) {
			if (events[i] != 0) {
				// it is growing or shrinking
				change += 0.08 * Math.abs(rawData[i + 1] - rawData[i]);
			}
		}
		this.dynamicity = change / (events.length * interval / 60.0);
	}

	@Override
	public String toString() {
		return "Microtubule " + ID;
	}
}

public class MicroStats extends JFrame {

	private JPanel contentPane;
	private Stats[] statPlus;
	private Stats[] statMinus;
	public double[][] lengthStats;
	private double[][] end1Stats;
	private double[][] end2Stats;
	public int[] discontinuity;
	public int[] sharpBend;
	public boolean[] tooFast;
	public int[] atEdge;
	private boolean filterDiscont;
	private boolean filterBend;
	private boolean filterFast;
	private boolean filterEdge;
	private boolean showPlus = true;
	private boolean showMinus = false;
	public int[] microIDs;
	private boolean[] blockList;
	private boolean[] forceAdd;
	private boolean[] forceRemove;
	private JTextField timeInterval;
	private JTextField diffThresh;
	private double totRes1, totCat1, totDynam1, totGrow1, totShrink1,
			totPause1, totGRate1, totSRate1;
	private double totRes2, totCat2, totDynam2, totGrow2, totShrink2,
			totPause2, totGRate2, totSRate2;
	private JTextField startFrame;
	private JTextField endFrame;
	private int validIDs;
	private JLabel label_1;
	private JLabel label_2;
	private JLabel label;
	private JLabel lblCatastrophe;
	private JLabel lblDynamicity_1;
	private JLabel lblGrowthTime_1;
	private JLabel lblShrinkTime_1;
	private JLabel lblPauseTime_1;
	private JLabel lblID;
	private JLabel lblGrowthRate;
	private JLabel lblShrinkingRate;
	private JLabel lblRescueRate;
	private JLabel lblCatastropheFrequency;
	private JLabel lblDynamicity;
	private JLabel lblGrowthTime;
	private JLabel lblShrinkTime;
	private JLabel lblPauseTime;
	private JTextField textFieldFrom;
	private JTextField textFieldTo;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					MicroStats frame = new MicroStats(null, null, null, null,
							null, null, null, null, null, null);
					frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the frame.
	 */
	public MicroStats(double[][] stat, int[] IDs, int[] discont, int[] bend,
			int[] edge, double[][] end1Stats, double[][] end2Stats,
			MicroPanel microPanel, ArrayList<Lines> contourSets,
			ArrayList<Microtubules> results) {
		this.lengthStats = stat;
		this.microIDs = IDs;
		validIDs = IDs.length;
		this.discontinuity = discont;
		this.sharpBend = bend;
		this.tooFast = new boolean[discont.length];
		this.atEdge = edge;
		this.end1Stats = end1Stats;
		this.end2Stats = end2Stats;
		this.blockList = new boolean[IDs.length];
		this.forceAdd = new boolean[IDs.length];
		this.forceRemove = new boolean[IDs.length];
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 1600, 750);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		contentPane.setLayout(null);
		setContentPane(contentPane);

		JScrollPane scrollPane = new JScrollPane();
		scrollPane.setBounds(620, 260, 206, 350);
		contentPane.add(scrollPane);

		JList<Stats> list = new JList<Stats>();
		list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		scrollPane.setViewportView(list);

		lblID = new JLabel("ID:");
		lblID.setBounds(931, 500, 177, 16);
		contentPane.add(lblID);

		lblGrowthRate = new JLabel("Growth Rate:");
		lblGrowthRate.setBounds(931, 520, 177, 16);
		contentPane.add(lblGrowthRate);

		lblShrinkingRate = new JLabel("Shrinking Rate:");
		lblShrinkingRate.setBounds(931, 540, 209, 16);
		contentPane.add(lblShrinkingRate);

		lblRescueRate = new JLabel("Rescue:");
		lblRescueRate.setBounds(931, 560, 209, 16);
		contentPane.add(lblRescueRate);

		lblCatastropheFrequency = new JLabel("Catastrophe:");
		lblCatastropheFrequency.setBounds(931, 580, 219, 16);
		contentPane.add(lblCatastropheFrequency);

		lblDynamicity = new JLabel("Dynamicity:");
		lblDynamicity.setBounds(931, 600, 177, 16);
		contentPane.add(lblDynamicity);

		lblGrowthTime = new JLabel("Growth Time:");
		lblGrowthTime.setBounds(931, 620, 177, 16);
		contentPane.add(lblGrowthTime);

		lblShrinkTime = new JLabel("Shrink Time:");
		lblShrinkTime.setBounds(931, 640, 177, 16);
		contentPane.add(lblShrinkTime);

		lblPauseTime = new JLabel("Pause Time:");
		lblPauseTime.setBounds(931, 660, 177, 16);
		contentPane.add(lblPauseTime);

		label = new JLabel("Rescue:");
		label.setForeground(Color.RED);
		label.setBounds(1136, 560, 209, 16);
		contentPane.add(label);

		lblCatastrophe = new JLabel("Catastrophe:");
		lblCatastrophe.setForeground(Color.RED);
		lblCatastrophe.setBounds(1136, 580, 209, 16);
		contentPane.add(lblCatastrophe);

		lblDynamicity_1 = new JLabel("Dynamicity:");
		lblDynamicity_1.setForeground(Color.RED);
		lblDynamicity_1.setBounds(1136, 600, 209, 16);
		contentPane.add(lblDynamicity_1);

		lblGrowthTime_1 = new JLabel("Growth Time:");
		lblGrowthTime_1.setForeground(Color.RED);
		lblGrowthTime_1.setBounds(1136, 620, 209, 16);
		contentPane.add(lblGrowthTime_1);

		lblShrinkTime_1 = new JLabel("Shrink TIme:");
		lblShrinkTime_1.setForeground(Color.RED);
		lblShrinkTime_1.setBounds(1136, 640, 209, 16);
		contentPane.add(lblShrinkTime_1);

		lblPauseTime_1 = new JLabel("Pause Time:");
		lblPauseTime_1.setForeground(Color.RED);
		lblPauseTime_1.setBounds(1136, 660, 209, 16);
		contentPane.add(lblPauseTime_1);

		JLabel lblTimeIntervalPer = new JLabel("Time Interval:");
		lblTimeIntervalPer.setBounds(622, 30, 97, 16);
		contentPane.add(lblTimeIntervalPer);

		timeInterval = new JTextField();
		timeInterval.setText("2");
		timeInterval.setBounds(712, 24, 84, 28);
		contentPane.add(timeInterval);
		timeInterval.setColumns(10);

		JLabel lblDifferenceThreshold = new JLabel("Difference Threshold: ");
		lblDifferenceThreshold.setBounds(622, 57, 140, 16);
		contentPane.add(lblDifferenceThreshold);

		diffThresh = new JTextField();
		diffThresh.setText("0.08");
		diffThresh.setBounds(772, 51, 84, 28);
		contentPane.add(diffThresh);
		diffThresh.setColumns(10);

		label_1 = new JLabel("Growth Rate:");
		label_1.setForeground(Color.RED);
		label_1.setBounds(1136, 520, 177, 16);
		contentPane.add(label_1);

		label_2 = new JLabel("Shrinking Rate:");
		label_2.setForeground(Color.RED);
		label_2.setBounds(1136, 540, 209, 16);
		contentPane.add(label_2);

		JLabel lblFilters = new JLabel("Filters for bad kymograms:");
		lblFilters.setBounds(622, 79, 206, 16);
		contentPane.add(lblFilters);

		JCheckBox chckbxDiscont = new JCheckBox("Discontinuous Microtubule");
		chckbxDiscont.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				filterDiscont = chckbxDiscont.isSelected();
				list.setCellRenderer(new FilterRenderer(discontinuity,
						sharpBend, tooFast, atEdge, forceAdd, forceRemove,
						filterDiscont, filterBend, filterFast, filterEdge));
				updateBlockList(discontinuity, sharpBend, tooFast, atEdge,
						filterDiscont, filterBend, filterFast, filterEdge);
				updateParam(IDs);
				updatePopulationStats();

			}
		});
		chckbxDiscont.setBounds(620, 103, 226, 23);
		contentPane.add(chckbxDiscont);

		JCheckBox chckbxSharpBending = new JCheckBox("Sharp Bending");
		chckbxSharpBending.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				filterBend = chckbxSharpBending.isSelected();
				// Arrays.fill(blockList, false);
				list.setCellRenderer(new FilterRenderer(discontinuity,
						sharpBend, tooFast, atEdge, forceAdd, forceRemove,
						filterDiscont, filterBend, filterFast, filterEdge));
				updateBlockList(discontinuity, sharpBend, tooFast, atEdge,
						filterDiscont, filterBend, filterFast, filterEdge);
				updateParam(IDs);
				updatePopulationStats();
			}
		});
		chckbxSharpBending.setBounds(620, 135, 128, 23);
		contentPane.add(chckbxSharpBending);

		JCheckBox chckbxGrowingTooFast = new JCheckBox("Growing too Fast");
		chckbxGrowingTooFast.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				filterFast = chckbxGrowingTooFast.isSelected();
				// Arrays.fill(blockList, false);
				list.setCellRenderer(new FilterRenderer(discontinuity,
						sharpBend, tooFast, atEdge, forceAdd, forceRemove,
						filterDiscont, filterBend, filterFast, filterEdge));
				updateBlockList(discontinuity, sharpBend, tooFast, atEdge,
						filterDiscont, filterBend, filterFast, filterEdge);
				updateParam(IDs);
				updatePopulationStats();
			}
		});
		chckbxGrowingTooFast.setBounds(620, 159, 173, 23);
		contentPane.add(chckbxGrowingTooFast);

		JCheckBox chckbxMicrotubulesOnEdge = new JCheckBox(
				"Microtubules on Edge");
		chckbxMicrotubulesOnEdge.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				filterEdge = chckbxMicrotubulesOnEdge.isSelected();
				// Arrays.fill(blockList, false);
				list.setCellRenderer(new FilterRenderer(discontinuity,
						sharpBend, tooFast, atEdge, forceAdd, forceRemove,
						filterDiscont, filterBend, filterFast, filterEdge));
				updateBlockList(discontinuity, sharpBend, tooFast, atEdge,
						filterDiscont, filterBend, filterFast, filterEdge);
				updateParam(IDs);
				updatePopulationStats();
			}
		});
		chckbxMicrotubulesOnEdge.setBounds(620, 187, 173, 23);
		contentPane.add(chckbxMicrotubulesOnEdge);

		String[] choices = { "Plus End", "Minus End" };
		JComboBox comboBox = new JComboBox(choices);
		comboBox.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				int select = comboBox.getSelectedIndex();
				if (select == 0) {
					showPlus = true;
					showMinus = false;
				} else {
					showPlus = false;
					showMinus = true;
				}
				// update what is shown
				if (list.getSelectedIndex()==-1) {
					list.setSelectedIndex(0);
				}
				int index = list.getSelectedIndex();
				// update population
				updatePopulationStats();
				updateIndividualStats(index);
				
//				Platform.runLater(new Runnable() {
//					@Override
//					public void run() {
//						System.out.println("before deleting");
//						contentPane.remove(contentPane.getComponentCount() - 1);
//						System.out.println("after deleting");
//						JFXPanel newPanel = new JFXPanel();
//						newPanel.setBackground(Color.WHITE);
//						newPanel.setBounds(900, 18, 512, 400);
//						contentPane.add(newPanel);
//						updateFX(newPanel, index);
//						revalidate();
//						repaint();
//					}
//				});
			}
		});
		comboBox.setBounds(686, 222, 140, 27);
		contentPane.add(comboBox);

		JLabel lblStatsFor = new JLabel("Stats for:");
		lblStatsFor.setBounds(622, 222, 72, 16);
		contentPane.add(lblStatsFor);

		MicroPanel panel = new MicroPanel(microPanel);
		panel.setBackground(Color.GRAY);
		panel.setBounds(40, 18, 512, 512);
		// panel = microPanel;
		contentPane.add(panel);
		panel.showMicro = true;
		panel.enableMulti = false;
		panel.repaint();

		panel.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				panel.dispLine = panel.currentCont.getIndexByID(panel
						.clickLine(e));
				if (panel.showMicro) {
					if (panel.dispLine >= 0) {
						ArrayList<Integer> mlist = panel.currentCont
								.get(panel.dispLine).microID;
						if (mlist.isEmpty()) {
							System.out.println("This segment is unmatched");
						} else {
							int microID = mlist.get(0);
							panel.dispMicro = panel.currentMicros
									.getIndexByID(microID);
							panel.repaint();
							list.setSelectedIndex(panel.dispMicro);
							list.ensureIndexIsVisible(list.getSelectedIndex());
						}
					}
				}
			}
		});

		JCheckBox checkBoxHideAdd = new JCheckBox("Hide Added Lines");
		checkBoxHideAdd.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				panel.hideAdded = checkBoxHideAdd.isSelected();
				if (panel.dispLine != -1
						&& panel.currentCont.get(panel.dispLine).getID() < 0) {
					panel.dispLine = -1;
				}
				panel.repaint();
			}
		});
		checkBoxHideAdd.setBounds(43, 658, 175, 23);
		contentPane.add(checkBoxHideAdd);

		JCheckBox checkBoxHideUmatch = new JCheckBox("Hide Unmatched Segment");
		checkBoxHideUmatch.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				panel.hideUnmatch = checkBoxHideUmatch.isSelected();
				if (panel.dispLine != -1
						&& panel.currentCont.get(panel.dispLine).microID
								.isEmpty()) {
					panel.dispLine = -1;
				}
				panel.repaint();
			}
		});
		checkBoxHideUmatch.setBounds(43, 632, 208, 23);
		contentPane.add(checkBoxHideUmatch);

		JButton btnUnselect = new JButton("Unselect");
		btnUnselect.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				panel.dispLine = -1;
				panel.dispMicro = -1;
				panel.multiSelect.clear();
				panel.repaint();
			}
		});
		btnUnselect.setBounds(300, 632, 87, 29);
		contentPane.add(btnUnselect);

		JLabel labelResult = new JLabel("Result Lines");
		labelResult.setBounds(225, 538, 208, 29);
		contentPane.add(labelResult);

		JScrollBar scrollBar = new JScrollBar();
		scrollBar.addAdjustmentListener(new AdjustmentListener() {
			public void adjustmentValueChanged(AdjustmentEvent e) {
				if (scrollBar.getValue() > 0) {
					panel.dispLine = -1;
					panel.dispMicro = -1;
					panel.paint = scrollBar.getValue();
					panel.currentCont = contourSets.get(panel.paint - 1);
					panel.currentMicros = results.get(panel.paint - 1);
					panel.repaint();
					labelResult.setText("Result Lines frame: " + panel.paint);
				}
			}
		});
		scrollBar.setUnitIncrement(1);
		scrollBar.setOrientation(JScrollBar.HORIZONTAL);
		scrollBar.setMinimum(1);
		scrollBar.setMaximum(results.size() + 10);
		scrollBar.setBounds(40, 572, 495, 29);
		contentPane.add(scrollBar);

		JButton buttonLeft = new JButton("<");
		buttonLeft.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (panel.paint > 1) {
					panel.dispLine = -1;
					panel.dispMicro = -1;
					panel.currentCont = contourSets.get(panel.paint - 2);
					panel.currentMicros = results.get(panel.paint - 2);
					panel.paint--;
					panel.repaint();
					labelResult.setText("Result Lines frame: " + panel.paint);
					scrollBar.setValue(panel.paint);
				}
			}
		});
		buttonLeft.setBounds(40, 539, 50, 29);
		contentPane.add(buttonLeft);

		JButton buttonRight = new JButton(">");
		buttonRight.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (panel.paint < results.size()) {
					panel.dispLine = -1;
					panel.dispMicro = -1;
					panel.currentCont = contourSets.get(panel.paint);
					panel.currentMicros = results.get(panel.paint);
					panel.paint++;
					panel.repaint();
					labelResult.setText("Result Lines frame: " + panel.paint);
					scrollBar.setValue(panel.paint);
				}
			}
		});
		buttonRight.setBounds(502, 540, 50, 29);
		contentPane.add(buttonRight);

		JButton btnInclude = new JButton("Include");
		btnInclude.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				int index = list.getSelectedIndex();
				if (index == -1) {
					JOptionPane.showMessageDialog(null,
							"Please select a valid microtubule");
				} else {
					forceAdd[index] = true;
					forceRemove[index] = false;
				}
				list.setCellRenderer(new FilterRenderer(discontinuity,
						sharpBend, tooFast, atEdge, forceAdd, forceRemove,
						filterDiscont, filterBend, filterFast, filterEdge));
				updateParam(IDs);
				updatePopulationStats();
			}
		});
		btnInclude.setBounds(620, 622, 97, 29);
		contentPane.add(btnInclude);

		JButton btnRemove = new JButton("Remove");
		btnRemove.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				int index = list.getSelectedIndex();
				if (index == -1) {
					JOptionPane.showMessageDialog(null,
							"Please select a valid microtubule");
				} else {
					forceAdd[index] = false;
					forceRemove[index] = true;
				}
				list.setCellRenderer(new FilterRenderer(discontinuity,
						sharpBend, tooFast, atEdge, forceAdd, forceRemove,
						filterDiscont, filterBend, filterFast, filterEdge));
				updateParam(IDs);
				updatePopulationStats();
			}
		});
		btnRemove.setBounds(729, 622, 97, 29);
		contentPane.add(btnRemove);

		JButton btnExportToCsv = new JButton("Export to CSV");
		btnExportToCsv.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				exportCSV(IDs);
				JOptionPane.showMessageDialog(null, "Exported to CSV");
			}
		});
		btnExportToCsv.setBounds(622, 655, 126, 29);
		contentPane.add(btnExportToCsv);

		JLabel lblPopulation = new JLabel("Population:");
		lblPopulation.setForeground(Color.RED);
		lblPopulation.setBounds(1136, 500, 104, 16);
		contentPane.add(lblPopulation);

		JButton btnClearAll = new JButton("Clear All");
		btnClearAll.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				 if (list.getSelectedIndex()==-1) {
				 list.setSelectedIndex(0);
				 }
				int index = list.getSelectedIndex();
				Stats current = statPlus[index];
				if (showMinus) {
					current = statMinus[index];
				}
				current.modified = true;
				current.growEvents.clear();
				current.shrinkEvents.clear();
				current.totGrowth = 0;
				current.totShrink = 0;
				updateParam(IDs);
				updateIndividualStats(index);
				updatePopulationStats();
				Platform.runLater(new Runnable() {
					@Override
					public void run() {
						contentPane.remove(contentPane.getComponentCount() - 1);
						JFXPanel newPanel = new JFXPanel();
						newPanel.setBackground(Color.WHITE);
						newPanel.setBounds(900, 18, 512, 400);
						contentPane.add(newPanel);
						updateFX(newPanel, index);
						revalidate();
						repaint();
					}
				});
			}
		});
		btnClearAll.setBounds(1313, 460, 117, 29);
		contentPane.add(btnClearAll);

		textFieldFrom = new JTextField();
		textFieldFrom.setBounds(919, 460, 97, 28);
		contentPane.add(textFieldFrom);
		textFieldFrom.setColumns(10);

		JLabel lblTo = new JLabel("to");
		lblTo.setBounds(1017, 465, 35, 16);
		contentPane.add(lblTo);

		textFieldTo = new JTextField();
		textFieldTo.setBounds(1039, 460, 97, 28);
		contentPane.add(textFieldTo);
		textFieldTo.setColumns(10);

		String[] options = { "Grow", "Shrink" };
		JComboBox comboBox_1 = new JComboBox(options);
		comboBox_1.setBounds(1140, 460, 90, 28);
		contentPane.add(comboBox_1);

		JButton btnAdd = new JButton("Add");
		btnAdd.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				 if (list.getSelectedIndex()==-1) {
				 list.setSelectedIndex(0);
				 }
				int index = list.getSelectedIndex();
				Stats current = statPlus[index];
				if (showMinus) {
					current = statMinus[index];
				}
				int from = Integer.parseInt(textFieldFrom.getText());
				int to = Integer.parseInt(textFieldTo.getText());
				if (from >= to || from < 1 || to > current.rawData.length) {
					JOptionPane.showMessageDialog(null,
							"Please enter valid intervals");
				} else {
					int direction = comboBox_1.getSelectedIndex();
					if (direction == 0) {
						// set to be growth
						current.growEvents.add(from-1);
						// -1 for 0/1 first position
						current.growEvents.add(to-1);
					} else {
						// set to be shrink
						current.shrinkEvents.add(from-1);
						current.shrinkEvents.add(to-1);
					}
					current.modified = true;
					updateParam(IDs);
					updateIndividualStats(index);
					updatePopulationStats();
					Platform.runLater(new Runnable() {
						@Override
						public void run() {
							contentPane.remove(contentPane.getComponentCount() - 1);
							JFXPanel newPanel = new JFXPanel();
							newPanel.setBackground(Color.WHITE);
							newPanel.setBounds(900, 18, 512, 400);
							contentPane.add(newPanel);
							updateFX(newPanel, index);
							revalidate();
							repaint();
						}
					});
				}
			}
		});
		btnAdd.setBounds(1225, 460, 76, 29);
		contentPane.add(btnAdd);

		JFXPanel chartPanel = new JFXPanel();
		chartPanel.setBackground(Color.WHITE);
		chartPanel.setBounds(880, 18, 550, 430);
		contentPane.add(chartPanel);

		Platform.runLater(new Runnable() {
			@Override
			public void run() {
				updateFX(chartPanel, 0);
			}
		});

		this.statPlus = new Stats[IDs.length];
		this.statMinus = new Stats[IDs.length];
		updateParam(IDs);
		updatePopulationStats();

		timeInterval.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				updateParam(IDs);
				// update the current microtubule
				int index = list.getSelectedIndex();
				updateIndividualStats(index);
				// update the population
				updatePopulationStats();
			}
		});

		diffThresh.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				updateParam(IDs);
				int index = list.getSelectedIndex();
				updateIndividualStats(index);
				updatePopulationStats();
			}
		});

		list.setListData(this.statPlus);
		list.addListSelectionListener(new ListSelectionListener() {
			public void valueChanged(ListSelectionEvent e) {
				if (!e.getValueIsAdjusting()) {
					int index = list.getSelectedIndex();
					panel.dispMicro = index;
					panel.repaint();
					updateIndividualStats(index);
					Platform.runLater(new Runnable() {
						@Override
						public void run() {
							contentPane.remove(contentPane.getComponentCount() - 1);
							JFXPanel newPanel = new JFXPanel();
							newPanel.setBackground(Color.WHITE);
							newPanel.setBounds(900, 18, 512, 400);
							contentPane.add(newPanel);
							updateFX(newPanel, index);
							revalidate();
							repaint();
						}
					});
				}
			}
		});

	}

	private void updateFX(JFXPanel fxPanel, int pos) {
		// This method is invoked on the JavaFX thread
		NumberAxis xAxis = new NumberAxis();
		NumberAxis yAxis = new NumberAxis();
		xAxis.setLabel("Relative Length");
		xAxis.setTickUnit(1);
		yAxis.setLabel("Frame");
		// creating the chart
		LineChart<Number, Number> lineChart = new LineChart<Number, Number>(
				xAxis, yAxis);
		// lineChart.setCreateSymbols(false);
		lineChart.setTitle("Microtubule Dynamics for " + statPlus[pos].ID);
		// defining a series
		XYChart.Series series = new XYChart.Series();
		series.setName("Dynamics");
		Stats current = statPlus[pos];
		if (showMinus) {
			current = statMinus[pos];
		}
		double initial = current.endData[0];
		// populating the series with data
		for (int i = 0; i < current.endData.length; ++i) {
			double state = current.endData[i];
			series.getData().add(new XYChart.Data(state - initial, i + 1));
		}
		Scene scene = new Scene(lineChart);
		lineChart.getData().add(series);
		//System.out.println("graphing for " + pos);
		//System.out.println(statPlus[pos].growEvents.size());
		if (current.growEvents.size() > 0) {
			ArrayList<Integer> events = current.growEvents;
			for (int i = 0; i < events.size(); ++i) {
				if (i % 2 == 0) {
					XYChart.Series grow = new XYChart.Series();
					grow.setName("G");
					for (int j = i; j <= i + 1; ++j) {
						double state = current.endData[events.get(j)];
						grow.getData().add(
								new XYChart.Data(state - initial,
										events.get(j) + 1));
					}
					lineChart.getData().add(grow);
				}
			}
		}
		if (current.shrinkEvents.size() > 0) {
			ArrayList<Integer> events = current.shrinkEvents;
			for (int i = 0; i < events.size(); ++i) {
				if (i % 2 == 0) {
					XYChart.Series shrink = new XYChart.Series();
					shrink.setName("S");
					for (int j = i; j <= i + 1; ++j) {
						double state = current.endData[events.get(j)];
						shrink.getData().add(
								new XYChart.Data(state - initial,
										events.get(j) + 1));
					}
					lineChart.getData().add(shrink);
				}
			}
		}
		fxPanel.setScene(scene);
	}

	private double round(double num, int precision) {
		double mult = 1.0 * Math.pow(10, precision);
		return (double) Math.round(num * mult) / mult;
	}

	private void updateBlockList(int[] discont, int[] bend, boolean[] fast,
			int[] edge, boolean discontFilter, boolean bendFilter,
			boolean fastFilter, boolean edgeFilter) {
		for (int i = 0; i < blockList.length; ++i) {
			boolean block = false;
			if (discontFilter) {
				if (discont[i] >= 5) {
					block = true;
				}
			}
			if (bendFilter) {
				if (bend[i] >= 5) {
					block = true;
				}
			}
			if (fastFilter) {
				if (fast[i]) {
					block = true;
				}
			}
			if (edgeFilter) {
				if (edge[i] >= 5) {
					block = true;
				}
			}
			if (block) {
				blockList[i] = true;
			} else {
				blockList[i] = false;
			}
		}
	}

	private void exportCSV(int[] IDs) {
		try {
			PrintWriter pw = new PrintWriter(new File("test.csv"));
			StringBuilder sb = new StringBuilder();
			sb.append("id");
			sb.append(',');
			sb.append("Position");
			sb.append(',');
			sb.append("Growth Rate");
			sb.append(',');
			sb.append("Shrinking Rate");
			sb.append(',');
			sb.append("Rescue Rate");
			sb.append(',');
			sb.append("Catastrophe Rate");
			sb.append(',');
			sb.append("Dynamicity");
			sb.append(',');
			sb.append("Growth Time");
			sb.append(',');
			sb.append("Shrink Time");
			sb.append(',');
			sb.append("Pause Time");
			sb.append('\n');
			for (int i = 0; i < IDs.length; ++i) {
				Stats current = statPlus[i];
				if ((!blockList[i] || forceAdd[i]) && !forceRemove[i]) {
				sb.append(current.ID);
				sb.append(',');
				sb.append("plus");
				sb.append(',');
				sb.append("" + round(current.totGrowth / current.growCount, 4));
				sb.append(',');
				sb.append(""
						+ round(current.totShrink / current.shrinkCount, 4));
				sb.append(',');
				sb.append("" + round(current.rescue, 4));
				sb.append(',');
				sb.append("" + round(current.catastrophe, 4));
				sb.append(',');
				sb.append("" + round(current.dynamicity, 4));
				sb.append(',');
				double growth = Math
						.round((current.grow * 1.0 / current.events.length) * 100);
				double shrink = Math
						.round((current.shrink * 1.0 / current.events.length) * 100);
				double pause = Math
						.round((current.pause * 1.0 / current.events.length) * 100);
				sb.append(growth + "%");
				sb.append(',');
				sb.append(shrink + "%");
				sb.append(',');
				sb.append(pause + "%");
				sb.append('\n');
				current = statMinus[i];
				sb.append(current.ID);
				sb.append(',');
				sb.append("minus");
				sb.append(',');
				sb.append("" + round(current.totGrowth / current.growCount, 4));
				sb.append(',');
				sb.append(""
						+ round(current.totShrink / current.shrinkCount, 4));
				sb.append(',');
				sb.append("" + round(current.rescue, 4));
				sb.append(',');
				sb.append("" + round(current.catastrophe, 4));
				sb.append(',');
				sb.append("" + round(current.dynamicity, 4));
				sb.append(',');
				double growth1 = Math
						.round((current.grow * 1.0 / current.events.length) * 100);
				double shrink1 = Math
						.round((current.shrink * 1.0 / current.events.length) * 100);
				double pause1 = Math
						.round((current.pause * 1.0 / current.events.length) * 100);
				sb.append(growth1 + "%");
				sb.append(',');
				sb.append(shrink1 + "%");
				sb.append(',');
				sb.append(pause1 + "%");
				sb.append('\n');
				}
			}

			pw.write(sb.toString());
			pw.close();

		} catch (FileNotFoundException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	}

	private void updateParam(int[] IDs) {
		totRes1 = totCat1 = totDynam1 = totGrow1 = totShrink1 = totPause1 = totGRate1 = totSRate1 = 0;
		totRes2 = totCat2 = totDynam2 = totGrow2 = totShrink2 = totPause2 = totGRate2 = totSRate2 = 0;
		validIDs = 0;
		for (int i = 0; i < IDs.length; ++i) {
			if (statPlus[i] != null && statPlus[i].modified) {
				// if modified, use the mannual function
				statPlus[i].calculateGivenEvents(Double
						.parseDouble(timeInterval.getText()));
			} else {
				statPlus[i] = new Stats(IDs[i]);
				statPlus[i].getRawData(this.lengthStats, i);
				statPlus[i].getEndsData(this.end1Stats, i);
				statPlus[i].classifyEvents(statPlus[i].endData,
						Double.parseDouble(diffThresh.getText()));
				statPlus[i].getRates(statPlus[i].endData, statPlus[i].events,
						Double.parseDouble(timeInterval.getText()));
			}
			if (statMinus[i] != null && statMinus[i].modified) {
				statMinus[i].calculateGivenEvents(Double
						.parseDouble(timeInterval.getText()));
			} else {
				statMinus[i] = new Stats(IDs[i]);
				statMinus[i].getRawData(this.lengthStats, i);
				statMinus[i].getEndsData(this.end2Stats, i);
				statMinus[i].classifyEvents(statMinus[i].endData,
						Double.parseDouble(diffThresh.getText()));
				statMinus[i].getRates(statMinus[i].endData,
						statMinus[i].events,
						Double.parseDouble(timeInterval.getText()));
			}
			statPlus[i].getTimes(statPlus[i].events);
			statMinus[i].getTimes(statMinus[i].events);
			double grate1 = statPlus[i].totGrowth / statPlus[i].growCount;
			if (grate1 > 10) {
				tooFast[i] = true;
			}
			double srate1 = statPlus[i].totShrink / statPlus[i].shrinkCount;
			double grate2 = statMinus[i].totGrowth / statMinus[i].growCount;
			if (grate2 > 10) {
				tooFast[i] = true;
			}
			double srate2 = statMinus[i].totShrink / statMinus[i].shrinkCount;
			if ((grate1 + srate1 > grate2 + srate2) || statPlus[i].modified || statMinus[i].modified) {
				if ((!blockList[i] || forceAdd[i]) && !forceRemove[i]) {
					totGRate1 += grate1;
					totSRate1 += srate1;
					totGRate2 += grate2;
					totSRate2 += srate2;
				}
			} else {
				if ((!blockList[i] || forceAdd[i]) && !forceRemove[i]) {
					totGRate1 += grate2;
					totSRate1 += srate2;
					totGRate2 += grate1;
					totSRate2 += srate1;
				}
				Stats tmp = statPlus[i];
				statPlus[i] = statMinus[i];
				statMinus[i] = tmp;
			}

			statPlus[i].getFrequencies(statPlus[i].events,
					Double.parseDouble(timeInterval.getText()));

			statMinus[i].getFrequencies(statMinus[i].events,
					Double.parseDouble(timeInterval.getText()));

			statPlus[i].getDynamicity(statPlus[i].events, statPlus[i].endData,
					Double.parseDouble(timeInterval.getText()));

			statMinus[i].getDynamicity(statMinus[i].events,
					statMinus[i].endData,
					Double.parseDouble(timeInterval.getText()));

			if ((!blockList[i] || forceAdd[i]) && !forceRemove[i]) {
				// add to the population if not blocked
				totGrow1 += statPlus[i].grow;
				totShrink1 += statPlus[i].shrink;
				totPause1 += statPlus[i].pause;

				totGrow2 += statMinus[i].grow;
				totShrink2 += statMinus[i].shrink;
				totPause2 += statMinus[i].pause;

				totRes1 += statPlus[i].rescue;
				totCat1 += statPlus[i].catastrophe;
				totDynam1 += statPlus[i].dynamicity;

				totRes2 += statMinus[i].rescue;
				totCat2 += statMinus[i].catastrophe;
				totDynam2 += statMinus[i].dynamicity;
				validIDs++;
			}

		}
	}

	private void updateIndividualStats(int index) {
		Stats current = statPlus[index];
		//System.out.println("updating " + index);
		if (showMinus) {
			current = statMinus[index];
		}
		lblID.setText("ID: " + current.ID);
		// if (current.totGrowth / current.growCount > 5) {
		// lblGrowthRate.setText("Growth Rate: "
		// + round(current.totGrowth / current.growCount / 1.8, 4));
		// } else {
		lblGrowthRate.setText("Growth Rate: "
				+ round(current.totGrowth / current.growCount, 4));
		// }
		// if (current.totShrink / current.shrinkCount > 5) {
		// lblShrinkingRate.setText("Shrinking Rate: "
		// + round(current.totShrink / current.shrinkCount / 1.1, 4));
		// } else {
		lblShrinkingRate.setText("Shrinking Rate: "
				+ round(current.totShrink / current.shrinkCount, 4));
		// }
		lblRescueRate.setText("Rescue: " + round(current.rescue, 4));
		lblCatastropheFrequency.setText("Catastrophe: "
				+ round(current.catastrophe, 4));
		lblDynamicity.setText("Dynamicity: " + round(current.dynamicity, 4));
		double growth = Math
				.round((current.grow * 1.0 / current.events.length) * 100);
		double shrink = Math
				.round((current.shrink * 1.0 / current.events.length) * 100);
		double pause = Math
				.round((current.pause * 1.0 / current.events.length) * 100);
		lblGrowthTime.setText("Growth Time: " + growth + "%");
		lblShrinkTime.setText("Shrink Time: " + shrink + "%");
		lblPauseTime.setText("Pause Time: " + pause + "%");
	}

	private void updatePopulationStats() {
		double totTime = statPlus[0].events.length;
		label_1.setText("Growth Rate: " + round(totGRate1 / validIDs, 4));
		label_2.setText("Shrinking Rate: " + round(totSRate1 / validIDs, 4));
		label.setText("Rescue: " + round(totRes1 / validIDs, 4));
		lblCatastrophe.setText("Catastrophe: " + round(totCat1 / validIDs, 4));
		lblDynamicity_1
				.setText("Dynamicity: " + round(totDynam1 / validIDs, 4));
		lblGrowthTime_1
				.setText("Growth Time: "
						+ Math.round(totGrow1 * 1.0 / (totTime * validIDs)
								* 100) + "%");
		lblShrinkTime_1.setText("Shrink Time: "
				+ Math.round(totShrink1 * 1.0 / (totTime * validIDs) * 100)
				+ "%");
		lblPauseTime_1.setText("Pause Time: "
				+ Math.round(totPause1 * 1.0 / (totTime * validIDs) * 100)
				+ "%");
		if (showMinus) {
			label_1.setText("Growth Rate: " + round(totGRate2 / validIDs, 4));
			label_2.setText("Shrinking Rate: " + round(totSRate2 / validIDs, 4));
			label.setText("Rescue: " + round(totRes2 / validIDs, 4));
			lblCatastrophe.setText("Catastrophe: "
					+ round(totCat2 / validIDs, 4));
			lblDynamicity_1.setText("Dynamicity: "
					+ round(totDynam2 / validIDs, 4));
			lblGrowthTime_1.setText("Growth Time: "
					+ Math.round(totGrow2 * 1.0 / (totTime * validIDs) * 100)
					+ "%");
			lblShrinkTime_1.setText("Shrink Time: "
					+ Math.round(totShrink2 * 1.0 / (totTime * validIDs) * 100)
					+ "%");
			lblPauseTime_1.setText("Pause Time: "
					+ Math.round(totPause2 * 1.0 / (totTime * validIDs) * 100)
					+ "%");
		}
	}
}
