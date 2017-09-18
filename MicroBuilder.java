import java.awt.BorderLayout;
import java.awt.EventQueue;
import java.awt.Point;

import javax.swing.DefaultListSelectionModel;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollBar;
import javax.swing.ListModel;
import javax.swing.ListSelectionModel;
import javax.swing.border.EmptyBorder;

import java.awt.Color;

import javax.swing.JButton;
import javax.swing.JLabel;

import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;

import javax.swing.JScrollPane;
import javax.swing.JList;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.JCheckBox;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JTextField;
import javax.swing.event.AncestorListener;
import javax.swing.event.AncestorEvent;
import javax.swing.JComboBox;

public class MicroBuilder extends JFrame {

	private JPanel contentPane;
	private boolean lineParsed;
	private boolean juncParsed;
	private int numLineSets;
	private int numJuncSets;
	private JList<Microtubule> JmicroList;
	private boolean showInfo;
	private boolean[] negativeAdded;
	private boolean showMicro;
	private boolean boxSelect = false;
	private Point helper = null;
	private boolean multiSelect;
	private ArrayList<Lines> contourSets = new ArrayList<Lines>();
	// to store the unprocessed lines for every frame
	private ArrayList<Junctions> juncSets = new ArrayList<Junctions>();
	// junctions for every frame
	private ArrayList<Microtubules> results = new ArrayList<Microtubules>();
	private int[] microIDs;
	private double[][] lengthStats;
	private double[][] end1Stats;
	private double[][] end2Stats;
	private JTextField txtAlpha;
	private JTextField textBeta;
	private JTextField textGamma;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					MicroBuilder frame = new MicroBuilder();
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
	public MicroBuilder() {
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 1600, 630);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(null);

		MicroPanel firstFrame = new MicroPanel();
		firstFrame.setBounds(21, 18, 512, 512);
		firstFrame.setBackground(Color.WHITE);
		firstFrame.setVisible(false);
		contentPane.add(firstFrame);

		JScrollPane lossPane = new JScrollPane();
		lossPane.setBounds(1200, 315, 187, 170);
		contentPane.add(lossPane);

		JList<Loss> lossList = new JList<Loss>();
		lossList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		lossPane.setViewportView(lossList);

		JButton btnAddNew = new JButton("Merge");
		btnAddNew.setBounds(710, 515, 87, 29);
		btnAddNew.setVisible(false);
		btnAddNew.setEnabled(false);
		contentPane.add(btnAddNew);

		JButton btnReassign = new JButton("Re-assign");
		btnReassign.setBounds(603, 515, 117, 29);
		btnReassign.setVisible(false);
		btnReassign.setEnabled(false);
		contentPane.add(btnReassign);

		JButton btnRetrack = new JButton("Re-track");

		MicroPanel panel = new MicroPanel();

		btnAddNew.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (multiSelect) {
					// multi add
					if (panel.multiSelect.isEmpty()) {
						// no valid line selected
						JOptionPane.showMessageDialog(null,
								"No line sets selected");
					} else {
						Lines newSet = new Lines(1);
						for (int i : panel.multiSelect) {
							newSet.add(panel.currentCont.get(i));
						}
						int ID = panel.currentMicros.get(panel.currentMicros
								.size() - 1).ID + 1;
						panel.currentMicros.add(new Microtubule(newSet, ID));
						for (int i : panel.multiSelect) {
							Line toAdd = panel.currentCont.get(i);
							if (toAdd.microID.isEmpty()) {
								// previously does not have a match at all
								toAdd.microID.add(ID);
							} else {
								// previously had a match, but was incorrectly
								// grouped
								for (int id : toAdd.microID) {
									Microtubule toModm = panel.currentMicros
											.getMicroByID(id);
									toModm.lines.remove(toAdd);
									if (toModm.lines.isEmpty()) {
										// if the microtubule is empty after the
										// change,
										// remove it
										panel.currentMicros.remove(toModm);
									}
								}
								toAdd.microID.clear();
								toAdd.microID.add(ID);
							}
						}
						// clear the set for next editing.
						panel.multiSelect.clear();
					}
				} else {
					if (panel.dispLine < 0) {
						// no valid line selected
						JOptionPane.showMessageDialog(null, "No line selected");
					} else {
						// has at leat one valid line.
						Line toAdd = panel.currentCont.get(panel.dispLine);
						Lines newSet = new Lines(1);
						newSet.add(toAdd);
						int ID = panel.currentMicros.get(panel.currentMicros
								.size() - 1).ID + 1;
						panel.currentMicros.add(new Microtubule(newSet, ID));
						if (toAdd.microID.isEmpty()) {
							// previously does not have a match at all
							toAdd.microID.add(ID);
						} else {
							// previously had a match, but was incorrectly
							// grouped
							for (int id : toAdd.microID) {
								Microtubule toModm = panel.currentMicros
										.getMicroByID(id);
								toModm.lines.remove(toAdd);
								if (toModm.lines.isEmpty()) {
									// if the microtubule is empty after the
									// change,
									// remove it
									panel.currentMicros.remove(toModm);
								}
							}
							toAdd.microID.clear();
							toAdd.microID.add(ID);
						}
					}
				}
				refreshMicroList(panel.currentMicros);
				panel.repaint();
				if (panel.paint == 1) {
					btnRetrack.setVisible(true);
				}
			}
		});

		btnReassign.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (panel.dispMicro < 0) {
					JOptionPane.showMessageDialog(null,
							"No target microtubule selected");
				} else {
					if (multiSelect) {
						// do multiple lines
						// code needs rework, too ugly now
						for (int i : panel.multiSelect) {
							// delete the old affiliations
							Line toMod = panel.currentCont.get(i);
							for (int ID : toMod.microID) {
								Microtubule toModm = panel.currentMicros
										.getMicroByID(ID);
								toModm.lines.remove(toMod);
								if (toModm.lines.isEmpty()) {
									// if the microtubule is empty after the
									// change,
									// remove it
									panel.currentMicros.remove(toModm);
								}
							}
							toMod.microID.clear();
							// establish new affiliations
							toMod.microID.add(panel.currentMicros
									.get(panel.dispMicro).ID);
							panel.currentMicros.get(panel.dispMicro).lines
									.add(toMod);
						}
					} else {
						// delete the old affiliations
						Line toMod = panel.currentCont.get(panel.dispLine);
						for (int ID : toMod.microID) {
							Microtubule toModm = panel.currentMicros
									.getMicroByID(ID);
							toModm.lines.remove(toMod);
							if (toModm.lines.isEmpty()) {
								// if the microtubule is empty after the change,
								// remove it
								panel.currentMicros.remove(toModm);
							}
						}
						toMod.microID.clear();
						// establish new affiliations
						toMod.microID.add(panel.currentMicros
								.get(panel.dispMicro).ID);
						panel.currentMicros.get(panel.dispMicro).lines
								.add(toMod);
					}
					refreshMicroList(panel.currentMicros);
					panel.repaint();
				}
			}
		});

		JList<Line> lineList = new JList<Line>();
		lineList.addListSelectionListener(new ListSelectionListener() {
			public void valueChanged(ListSelectionEvent e) {
				if (!e.getValueIsAdjusting()) {
					int index = lineList.getSelectedIndex();
					panel.dispLine = index;
					if (panel.paint > 1 && index >= 0
							&& panel.currentCont.get(index).getID() > 0) {
						Line l = panel.currentCont.get(index);
						if (l.lossList.length > 1) {
							lossList.setListData(l.lossList);
							colorLoss(firstFrame, l.lossList);
						}
					}
					panel.repaint();
					if (panel.paint == 1 && index >= 0) {
						btnReassign.setEnabled(true);
						btnReassign.setVisible(true);
						btnAddNew.setVisible(true);
						btnAddNew.setEnabled(true);
					} else {
						btnReassign.setEnabled(false);
						btnReassign.setVisible(false);
						btnAddNew.setVisible(false);
						btnAddNew.setEnabled(false);
					}

					if (showInfo && index >= 0) {
						Line l = panel.currentCont.get(index);
						System.out.println("Line "
								+ panel.currentCont.get(index).getID() + " : ");
						System.out.println("Type is "
								+ panel.currentCont.get(index)
										.getContourClass() + ". Length is "
								+ panel.currentCont.get(index).num);
						System.out.println("Coordinates are " + l.col.get(0)
								+ "," + l.row.get(0) + " and "
								+ l.col.get(l.num - 1) + ","
								+ l.row.get(l.num - 1));
						if (panel.currentCont.get(index).getID() <= 0) {
							// this is an added line
							System.out.println("Line trying to connect "
									+ panel.currentCont.get(index).connections[0]
									+ " and "
									+ panel.currentCont.get(index).connections[1]);
						}
						if (panel.currentCont.get(index).microID.isEmpty()) {
							System.out.println("Line "
									+ panel.currentCont.get(index).getID()
									+ " is unmatched");
						} else {
							for (int i : panel.currentCont.get(index).microID) {
								System.out.println("Microtubule " + i);
							}
						}
					}

				}
			}
		});
		lineList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		JList<Microtubule> microList = new JList<Microtubule>();
		microList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		JmicroList = microList;

		JScrollPane Boxpane = new JScrollPane();
		Boxpane.setBounds(830, 135, 180, 207);
		contentPane.add(Boxpane);

		JList<Line> BoxList = new JList<Line>();
		BoxList.setSelectionModel(new ToggleSelectionModel());
		Boxpane.setViewportView(BoxList);
		BoxList.addListSelectionListener(new ListSelectionListener() {
			public void valueChanged(ListSelectionEvent e) {
				if (!e.getValueIsAdjusting()) {
					int[] indices = BoxList.getSelectedIndices();
					panel.multiSelect.clear();
					ListModel<Line> model = BoxList.getModel();
					for (int i = 0; i < indices.length; ++i) {
						panel.multiSelect.add(panel.currentCont
								.getIndexByID(model.getElementAt(indices[i])
										.getID()));
					}
				}
				panel.repaint();
			}
		});

		panel.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				if (boxSelect) {
					if (helper == null) {
						// first click
						helper = e.getPoint();
					} else {
						// second click
						int startX = helper.x;
						int startY = helper.y;
						int endX = e.getX();
						int endY = e.getY();
						for (int i = 0; i < panel.currentCont.size(); ++i) {
							Line l = panel.currentCont.get(i);
							if (inRectangle(startX, startY, endX, endY,
									l.col.get(0), l.row.get(0))
									|| inRectangle(startX, startY, endX, endY,
											l.col.get(l.num - 1),
											l.row.get(l.num - 1))
									|| inRectangle(startX, startY, endX, endY,
											l.col.get((l.num - 1) / 2),
											l.row.get((l.num - 1) / 2))) {
								// either the start or end or mid should be in
								// the box
								if (l.microID.isEmpty()) {
									// for unmatched lines check whether it is
									// relevant
									if (!panel.hideUnmatch) {
										panel.multiSelect.add(i);
									}
								} else {
									panel.multiSelect.add(i);
								}
							}
						}
						if (!panel.multiSelect.isEmpty()) {
							// has data, must enable editing.
							btnReassign.setEnabled(true);
							btnReassign.setVisible(true);
							btnAddNew.setVisible(true);
							btnAddNew.setEnabled(true);
						}
						Line[] selected = new Line[panel.multiSelect.size()];
						for (int i = 0; i < panel.multiSelect.size(); ++i) {
							selected[i] = panel.currentCont
									.get(panel.multiSelect.get(i));
						}
						BoxList.setListData(selected);
						BoxList.setSelectionInterval(0, selected.length - 1);
						helper = null;
						panel.repaint();
					}
				} else {
					// not box selection
					panel.dispLine = panel.currentCont.getIndexByID(panel
							.clickLine(e));
					if (showMicro) {
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
								microList.setSelectedIndex(panel.dispMicro);
								microList.ensureIndexIsVisible(microList
										.getSelectedIndex());
							}
						}
					} else {
						panel.dispMicro = -1;
						if (multiSelect && panel.dispLine >= 0) {
							if (panel.multiSelect.contains(Integer
									.valueOf(panel.dispLine))) {
								// double click removes the line
								panel.multiSelect.remove(Integer
										.valueOf(panel.dispLine));
							} else {
								// otherwise add it
								panel.multiSelect.add(panel.dispLine);
							}
						}
						panel.repaint();
						if (panel.dispLine >= 0 && panel.paint >= 0) {
							// must have selected a valid line and that there is
							// loss.

							if (panel.currentCont.get(panel.dispLine).lossList.length > 1) {
								lossList.setListData(panel.currentCont
										.get(panel.dispLine).lossList);
								Loss[] list = panel.currentCont
										.get(panel.dispLine).lossList;
								colorLoss(firstFrame, list);
							}
							lineList.setSelectedIndex(panel.dispLine);
							lineList.ensureIndexIsVisible(lineList
									.getSelectedIndex());
						}
					}
				}
			}
		});
		panel.setBounds(21, 18, 512, 512);
		panel.setBackground(Color.GRAY);
		contentPane.add(panel);

		JScrollPane resultPane = new JScrollPane();
		resultPane.setBounds(1050, 135, 133, 349);
		contentPane.add(resultPane);
		resultPane.setViewportView(lineList);

		microList.addListSelectionListener(new ListSelectionListener() {
			public void valueChanged(ListSelectionEvent e) {
				if (!e.getValueIsAdjusting()) {
					int index = microList.getSelectedIndex();
					panel.dispMicro = index;
					panel.repaint();
					if (showInfo && index > -1) {
						Lines ls = panel.currentMicros.get(index).lines;
						System.out.println("With Microtubule "
								+ panel.currentMicros.get(index).ID + " : ");
						for (Line l : ls) {
							System.out.println(l.getID());
						}
					}
				}
			}
		});

		JButton btnImportLines = new JButton("Import Lines");
		btnImportLines.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				numLineSets = 0;
				numJuncSets = 0;
				lineParsed = false;
				juncParsed = false;
				contourSets.clear();
				juncSets.clear();
				JFileChooser chooser = new JFileChooser(
						"/Users/linxuanyang/Desktop/Research");
				chooser.setMultiSelectionEnabled(true);
				if (chooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
					File[] file = chooser.getSelectedFiles();
					for (File f : file) {
						if (f.getName().contains("lines")) {
							Lines tmp = new Lines(-100);
							lineParsed = parseLines(f, tmp);
							numLineSets++;
							contourSets.add(tmp);
						} else if (f.getName().contains("junctions")) {
							Junctions tmp = new Junctions(-100);
							juncParsed = parseJunctions(f, tmp);
							numJuncSets++;
							juncSets.add(tmp);
						} else {
							System.out.println("invalid file");
						}
					}
					Collections.sort(contourSets);
					Collections.sort(juncSets);
				}
			}
		});
		btnImportLines.setBounds(545, 6, 117, 29);
		contentPane.add(btnImportLines);

		JButton btnSave = new JButton("Save");
		btnSave.setBounds(545, 429, 117, 29);
		btnSave.setEnabled(false);
		btnSave.setVisible(false);
		contentPane.add(btnSave);

		JButton btnLoaf = new JButton("Load");
		btnLoaf.setBounds(674, 429, 117, 29);
		btnLoaf.setEnabled(false);
		btnLoaf.setVisible(false);
		contentPane.add(btnLoaf);

		JButton btnSingleFrameLink = new JButton("Single Frame Link");
		btnSingleFrameLink.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (numLineSets <= 1 && numJuncSets <= 1) {
					if (lineParsed && juncParsed) {
						panel.paint = 0;
						panel.currentCont = contourSets.get(0);
						panel.currentJunc = juncSets.get(0);
						panel.currentMicros = panel.constructMicroFromLines(
								panel.currentCont, panel.currentJunc);
						panel.frameCleanUp(panel.currentMicros,
								contourSets.get(0));
						panel.currentCont = contourSets.get(0);
						Collections.sort(panel.currentCont,
								new Comparator<Line>() {
									public int compare(Line l1, Line l2) {
										return l1.getID() - l2.getID();
									}
								});
						Line[] contArr = panel.currentCont
								.toArray(new Line[panel.currentCont.size()]);
						lineList.setListData(contArr);
						refreshMicroList(panel.currentMicros);
						panel.repaint();
					} else if (lineParsed && (!juncParsed)) {
						System.out.println("Junctions are not parsed");
					} else if ((!lineParsed) && juncParsed) {
						System.out.println("Lines are not parsed");
					} else {
						System.out.println("Nothing has been parsed");
					}
				} else {
					System.out.println("Too many lines sets or junction sets");
				}
				btnSave.setEnabled(true);
				btnSave.setVisible(true);

			}
		});
		btnSingleFrameLink.setBounds(545, 35, 141, 29);
		contentPane.add(btnSingleFrameLink);

		JLabel lblResultLines = new JLabel("Result Lines");
		lblResultLines.setBounds(169, 529, 208, 29);
		contentPane.add(lblResultLines);

		txtAlpha = new JTextField();
		txtAlpha.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (Double.parseDouble(txtAlpha.getText()) >= 0
						&& panel.paint > 0) {
					panel.alpha = Double.parseDouble(txtAlpha.getText());

					// if (results.isEmpty()) {
					// results = panel.constructMatch(contourSets, juncSets,
					// null);
					// } else {
					// results = panel.constructMatch(contourSets, juncSets,
					// results.get(0));
					// }
					// panel.currentMicros = results.get(panel.paint - 1);
					// panel.currentCont = contourSets.get(panel.paint - 1);
					// for (Microtubule micro : panel.currentMicros) {
					// panel.currentCont.addAll(micro.added);
					// }
					// panel.repaint();
					// if (panel.dispLine > 0) {
					// lossList.setListData(panel.currentCont
					// .get(panel.dispLine).lossList);
					// }
					btnRetrack.setVisible(true);

				}
			}
		});
		txtAlpha.setText("1.0");
		txtAlpha.setBounds(773, 5, 82, 28);
		contentPane.add(txtAlpha);
		txtAlpha.setColumns(10);
		
		JScrollBar scrollBar = new JScrollBar();
		scrollBar.addAdjustmentListener(new AdjustmentListener() {
			public void adjustmentValueChanged(AdjustmentEvent e) {
				// System.out.println("Value is " + scrollBar.getValue());
				if (scrollBar.getValue() > 0) {
					panel.dispLine = -1;
					panel.dispMicro = -1;
					panel.paint = scrollBar.getValue();
					panel.currentCont = contourSets.get(panel.paint - 1);
					panel.currentMicros = results.get(panel.paint - 1);
					if (!negativeAdded[panel.paint - 1]) {
						// add the linked lines if they have not been previously
						// added
						for (Microtubule micro : panel.currentMicros) {
							panel.currentCont.addAll(micro.added);
						}
						negativeAdded[panel.paint - 1] = true;
					}
					Collections.sort(panel.currentCont, new Comparator<Line>() {
						public int compare(Line l1, Line l2) {
							return l1.getID() - l2.getID();
						}
					});
					Line[] lineArr = panel.currentCont
							.toArray(new Line[panel.currentCont.size()]);
					lineList.setListData(lineArr);
					refreshMicroList(panel.currentMicros);
					panel.repaint();
					lblResultLines
							.setText("Result Lines frame: " + panel.paint);
				}
				if (panel.paint != 1) {
					btnRetrack.setVisible(false);
				}
			}
		});
		scrollBar.setOrientation(JScrollBar.HORIZONTAL);
		scrollBar.setBounds(31, 559, 495, 29);
		scrollBar.setUnitIncrement(1);

		contentPane.add(scrollBar);

		btnRetrack.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (panel.paint != 1) {
					// for now can only retrack from first frame
					JOptionPane.showMessageDialog(null,
							"Can only retrack from first frame");
				} else {
					retrackFrame(panel);
					Collections.sort(panel.currentCont, new Comparator<Line>() {
						public int compare(Line l1, Line l2) {
							return l1.getID() - l2.getID();
							// it can also return 0, and 1
						}
					});
					Line[] lineArr = panel.currentCont
							.toArray(new Line[panel.currentCont.size()]);
					refreshMicroList(panel.currentMicros);
					lineList.setListData(lineArr);
					lblResultLines
							.setText("Result Lines frame: " + panel.paint);
					scrollBar.setMinimum(1);
					scrollBar.setMaximum(results.size() + 10);
				}
			}
		});
		btnRetrack.setBounds(550, 104, 117, 29);
		contentPane.add(btnRetrack);
		btnRetrack.setVisible(false);


		JButton button = new JButton("<");
		button.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (panel.paint > 1) {
					panel.dispLine = -1;
					panel.dispMicro = -1;
					panel.currentCont = contourSets.get(panel.paint - 2);
					panel.currentMicros = results.get(panel.paint - 2);
					if (!negativeAdded[panel.paint - 2]) {
						// add the linked lines if they have not been previously
						// added
						for (Microtubule micro : panel.currentMicros) {
							panel.currentCont.addAll(micro.added);
						}
						negativeAdded[panel.paint - 2] = true;
					}
					Collections.sort(panel.currentCont, new Comparator<Line>() {
						public int compare(Line l1, Line l2) {
							return l1.getID() - l2.getID();
						}
					});
					Line[] lineArr = panel.currentCont
							.toArray(new Line[panel.currentCont.size()]);
					lineList.setListData(lineArr);
					refreshMicroList(panel.currentMicros);
					panel.paint--;
					panel.repaint();
					lblResultLines
							.setText("Result Lines frame: " + panel.paint);
					scrollBar.setValue(panel.paint);
				}
				if (panel.paint != 1) {
					btnRetrack.setVisible(false);
				}
			}
		});
		button.setBounds(21, 530, 58, 29);
		button.setVisible(false);
		contentPane.add(button);

		JButton button_1 = new JButton(">");
		button_1.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (panel.paint < results.size()) {
					panel.dispLine = -1;
					panel.dispMicro = -1;
					panel.currentCont = contourSets.get(panel.paint);
					panel.currentMicros = results.get(panel.paint);
					if (!negativeAdded[panel.paint]) {
						for (Microtubule micro : panel.currentMicros) {
							panel.currentCont.addAll(micro.added);
						}
						negativeAdded[panel.paint] = true;
					}

					Collections.sort(panel.currentCont, new Comparator<Line>() {
						public int compare(Line l1, Line l2) {
							return l1.getID() - l2.getID();
							// it can also return 0, and 1
						}
					});
					Line[] lineArr = panel.currentCont
							.toArray(new Line[panel.currentCont.size()]);
					refreshMicroList(panel.currentMicros);
					lineList.setListData(lineArr);
					panel.paint++;
					panel.repaint();
					lblResultLines
							.setText("Result Lines frame: " + panel.paint);
					scrollBar.setValue(panel.paint);
				}
				if (panel.paint != 1) {
					btnRetrack.setVisible(false);
				}
			}
		});
		button_1.setBounds(471, 530, 58, 29);
		button_1.setVisible(false);
		contentPane.add(button_1);

		JButton btnMultipleFrameTrack = new JButton("Multiple Frame Track");
		btnMultipleFrameTrack.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (numLineSets > 1 && numLineSets == numJuncSets) {
					negativeAdded = new boolean[numLineSets];
					panel.alpha = Double.parseDouble(txtAlpha.getText());
					panel.currentCont = contourSets.get(0);
					panel.currentJunc = juncSets.get(0);
					results = panel.constructMatch(contourSets, juncSets, null);
					panel.currentMicros = results.get(0);
					// get the length stats
					lengthStats = new double[results.size()][panel.currentMicros
							.size()];
					microIDs = new int[panel.currentMicros.size()];
					Tool t = new Tool();
					for (int i = 0; i < results.size(); ++i) {
						for (int j = 0; j < results.get(i).size(); ++j) {
							Microtubule m = results.get(i).get(j);
							double length = 0;
							for (Line l : m.lines) {
								float[] start = { l.col.get(0), l.row.get(0) };
								float[] end = { l.col.get(l.num - 1),
										l.row.get(l.num - 1) };
								length += t.dist(start, end);
							}
							lengthStats[i][j] = length;
							microIDs[j] = m.ID;
						}
					}
					Collections.sort(panel.currentCont, new Comparator<Line>() {
						public int compare(Line l1, Line l2) {
							return l1.getID() - l2.getID();
							// it can also return 0, and 1
						}
					});
					Line[] lineArr = panel.currentCont
							.toArray(new Line[panel.currentCont.size()]);
					lineList.setListData(lineArr);
					refreshMicroList(panel.currentMicros);
					panel.paint = 1;
					panel.repaint();
					button.setVisible(true);
					button_1.setVisible(true);
					lblResultLines
							.setText("Result Lines frame: " + panel.paint);
					scrollBar.setMinimum(1);
					scrollBar.setMaximum(results.size() + 10);
				} else {
					System.out
							.println("Too few line sets or cannot match lines and junctions");
				}
				btnSave.setEnabled(true);
				btnSave.setVisible(true);
				btnLoaf.setEnabled(true);
				btnLoaf.setVisible(true);
			}
		});
		btnMultipleFrameTrack.setBounds(536, 70, 173, 29);
		contentPane.add(btnMultipleFrameTrack);

		JLabel lblLines = new JLabel("Results");
		lblLines.setBounds(1060, 494, 82, 23);
		contentPane.add(lblLines);

		JLabel lblMatchLoss = new JLabel("Match Loss");
		lblMatchLoss.setBounds(1210, 497, 103, 16);
		contentPane.add(lblMatchLoss);

		JLabel lblLossColoring = new JLabel("Loss Coloring");
		lblLossColoring.setBounds(169, 529, 208, 29);
		lblLossColoring.setVisible(false);
		contentPane.add(lblLossColoring);

		JLabel lblAlpha = new JLabel("Alpha");
		lblAlpha.setBounds(700, 11, 61, 16);
		contentPane.add(lblAlpha);

		JButton btnDelete = new JButton("Split");
		btnDelete.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (multiSelect) {
					if (panel.paint <= 1) {
						int toDel = JOptionPane.showConfirmDialog(null,
								"Are you sure about splitting thess lines?",
								"Warning", JOptionPane.YES_NO_OPTION);
						if (toDel == JOptionPane.YES_OPTION) {
							for (int i : panel.multiSelect) {
								Line toRemove = panel.currentCont.get(i);
								panel.removeLineCleanUp(toRemove,
										juncSets.get(0), contourSets.get(0));
								panel.currentCont = contourSets.get(0);
								panel.currentJunc = juncSets.get(0);
								if (!toRemove.microID.isEmpty()) {
									for (int ID : toRemove.microID) {
										if (panel.currentMicros
												.getMicroByID(ID) != null) {
											if (panel.currentMicros
													.getMicroByID(ID).lines
													.size() == 1) {
												Microtubule rmm = panel.currentMicros
														.getMicroByID(ID);
												panel.currentMicros.remove(rmm);
											} else {
												panel.currentMicros
														.getMicroByID(ID).lines
														.remove(toRemove);
											}
										}
									}
									toRemove.microID.clear();
								}
							}
							if (panel.paint == 1) {
								btnRetrack.setVisible(true);
							}
							Collections.sort(panel.currentCont,
									new Comparator<Line>() {
										public int compare(Line l1, Line l2) {
											return l1.getID() - l2.getID();
										}
									});
							Line[] contArr = panel.currentCont
									.toArray(new Line[panel.currentCont.size()]);
							lineList.setListData(contArr);
							refreshMicroList(panel.currentMicros);
							panel.repaint();
						}
					} else {
						JOptionPane.showMessageDialog(null,
								"Can only modify the first frame");
					}

				} else {
					// single select
					if (panel.dispLine >= 0 && panel.paint <= 1) {
						// a line must be selected
						int toDel = JOptionPane.showConfirmDialog(null,
								"Are you sure about deleting this line?",
								"Warning", JOptionPane.YES_NO_OPTION);
						if (toDel == JOptionPane.YES_OPTION) {
							Line toRemove = panel.currentCont
									.get(panel.dispLine);
							// contourSets.get(0).remove(toRemove);
							// do not remove the line
							panel.removeLineCleanUp(toRemove, juncSets.get(0),
									contourSets.get(0));
							panel.currentCont = contourSets.get(0);
							panel.currentJunc = juncSets.get(0);
							if (toRemove.microID.isEmpty()) {
								// for unmatched lines, just remove it
								JOptionPane.showMessageDialog(null,
										"Line has already been removed");
							} else {
								// previously had a match, need to redo match
								for (int ID : toRemove.microID) {
									if (panel.currentMicros.getMicroByID(ID) != null) {
										if (panel.currentMicros
												.getMicroByID(ID).lines.size() == 1) {
											Microtubule rmm = panel.currentMicros
													.getMicroByID(ID);
											panel.currentMicros.remove(rmm);
										} else {
											panel.currentMicros
													.getMicroByID(ID).lines
													.remove(toRemove);
										}

										// try turning off these lines first
										// no need to make match again

										// else {
										// panel.currentMicros = panel
										// .constructMicroFromLines(
										// panel.currentCont,
										// panel.currentJunc);
										// panel.frameCleanUp(panel.currentMicros,
										// contourSets.get(0));
										// }

									}
								}
								toRemove.microID.clear();
							}
							if (panel.paint == 1) {
								btnRetrack.setVisible(true);
							}
						}
						Collections.sort(panel.currentCont,
								new Comparator<Line>() {
									public int compare(Line l1, Line l2) {
										return l1.getID() - l2.getID();
									}
								});
						Line[] contArr = panel.currentCont
								.toArray(new Line[panel.currentCont.size()]);
						lineList.setListData(contArr);
						refreshMicroList(panel.currentMicros);
						panel.repaint();
						// update the new microtubules

					} else {
						if (panel.dispLine == -1) {
							JOptionPane.showMessageDialog(null,
									"No valid line selected");
						} else {
							JOptionPane.showMessageDialog(null,
									"Can only modify the first frame");
						}

					}
				}

			}
		});
		btnDelete.setBounds(536, 512, 69, 35);
		contentPane.add(btnDelete);

		JScrollPane microPane = new JScrollPane();
		microPane.setBounds(1200, 132, 187, 171);
		contentPane.add(microPane);
		microPane.setViewportView(microList);

		JCheckBox chckbxIgnoreTopCorner = new JCheckBox("Ignore top corner");
		chckbxIgnoreTopCorner.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				panel.ignoreTop = chckbxIgnoreTopCorner.isSelected();
			}
		});
		chckbxIgnoreTopCorner.setBounds(545, 145, 163, 23);
		contentPane.add(chckbxIgnoreTopCorner);

		JCheckBox chckbxVerbose = new JCheckBox("Debug");
		chckbxVerbose.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				panel.verbose = chckbxVerbose.isSelected();
			}
		});
		chckbxVerbose.setBounds(710, 95, 82, 23);
		contentPane.add(chckbxVerbose);

		JCheckBox chckbxShowInfo = new JCheckBox("Show Info");
		chckbxShowInfo.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				showInfo = chckbxShowInfo.isSelected();
			}
		});
		chckbxShowInfo.setBounds(791, 95, 128, 23);
		contentPane.add(chckbxShowInfo);

		JCheckBox chckbxHideUnmatched = new JCheckBox("Hide Unmatched Segment");
		chckbxHideUnmatched.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				panel.hideUnmatch = chckbxHideUnmatched.isSelected();
				if (panel.dispLine != -1
						&& panel.currentCont.get(panel.dispLine).microID
								.isEmpty()) {
					panel.dispLine = -1;
				}
				panel.repaint();
			}
		});
		chckbxHideUnmatched.setBounds(545, 180, 208, 23);
		contentPane.add(chckbxHideUnmatched);

		JButton btnLossColoring = new JButton("Loss Coloring");
		btnLossColoring.setBounds(545, 274, 155, 29);
		contentPane.add(btnLossColoring);

		JButton btnNewButton = new JButton("Back to Results");
		btnNewButton.setBounds(554, 274, 155, 29);
		btnNewButton.setEnabled(false);
		btnNewButton.setVisible(false);
		contentPane.add(btnNewButton);

		JCheckBox chckbxMultipleSelect = new JCheckBox("Multiple Select");
		chckbxMultipleSelect.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (showMicro && chckbxMultipleSelect.isSelected()) {
					chckbxMultipleSelect.setSelected(false);
				}
				multiSelect = chckbxMultipleSelect.isSelected();
				if (!multiSelect) {
					// clear list if no long in multi select
					panel.multiSelect.clear();
				}
				panel.dispLine = -1;
				panel.dispMicro = -1;
				panel.repaint();
				panel.enableMulti = multiSelect;
			}
		});
		chckbxMultipleSelect.setBounds(545, 241, 128, 23);
		contentPane.add(chckbxMultipleSelect);

		String[] colorChoices = { "Microtubules", "Segments" };
		JComboBox comboBox = new JComboBox(colorChoices);
		comboBox.setSelectedIndex(0);
		comboBox.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				int index = comboBox.getSelectedIndex();
				if (index == 1) {
					panel.colSeg = true;
					panel.repaint();
				} else if (index == 0) {
					panel.colSeg = false;
					panel.repaint();
				} else {
					System.out.println("GOT AN ERROR AT COLOR CHOICR!");
				}
			}
		});
		comboBox.setBounds(536, 327, 128, 29);
		contentPane.add(comboBox);

		String[] selectChoices = { "Segment", "Microtubule", "Bounding Box" };
		JComboBox comboBox_1 = new JComboBox(selectChoices);
		comboBox.setSelectedIndex(0);
		comboBox_1.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				int index = comboBox_1.getSelectedIndex();
				if (index == 0) {
					boxSelect = false;
					showMicro = false;
				} else if (index == 1) {
					showMicro = true;
					boxSelect = false;
					chckbxMultipleSelect.setSelected(false);
					multiSelect = false;
					panel.enableMulti = false;
				} else if (index == 2) {
					showMicro = false;
					boxSelect = true;
					chckbxMultipleSelect.setSelected(true);
					multiSelect = true;
					panel.enableMulti = true;
				} else {
					System.out.println("ERROR IN SELECTIONS");
				}
				panel.multiSelect.clear();
				BoxList.setListData(new Line[0]);
				panel.dispLine = -1;
				panel.dispMicro = -1;
				panel.repaint();
			}
		});
		comboBox_1.setBounds(674, 327, 117, 29);
		contentPane.add(comboBox_1);

		JLabel lblColorBy = new JLabel("Color by:");
		lblColorBy.setBounds(545, 304, 103, 28);
		contentPane.add(lblColorBy);

		JLabel lblSelectBy = new JLabel("Select by:");
		lblSelectBy.setBounds(677, 304, 103, 28);
		contentPane.add(lblSelectBy);

		JButton btnUnselect = new JButton("Unselect");
		btnUnselect.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				panel.dispLine = -1;
				panel.dispMicro = -1;
				panel.multiSelect.clear();
				BoxList.setListData(new Line[0]);
				panel.repaint();
			}
		});
		btnUnselect.setBounds(705, 274, 87, 29);
		contentPane.add(btnUnselect);

		JLabel lblBoxResults = new JLabel("Box Results");
		lblBoxResults.setBounds(840, 354, 97, 16);
		contentPane.add(lblBoxResults);

		JCheckBox chckbxHideAddedLines = new JCheckBox("Hide Added Lines");
		chckbxHideAddedLines.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				panel.hideAdded = chckbxHideAddedLines.isSelected();
				if (panel.dispLine != -1
						&& panel.currentCont.get(panel.dispLine).getID() < 0) {
					panel.dispLine = -1;
				}
				panel.repaint();
			}
		});
		chckbxHideAddedLines.setBounds(545, 206, 175, 23);
		contentPane.add(chckbxHideAddedLines);

		JLabel lblBeta = new JLabel("Beta");
		lblBeta.setBounds(698, 40, 61, 16);
		contentPane.add(lblBeta);

		JLabel lblGamma = new JLabel("Gamma");
		lblGamma.setBounds(719, 70, 61, 16);
		contentPane.add(lblGamma);

		textBeta = new JTextField();
		textBeta.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				// System.out.println("here");
				if (Double.parseDouble(textBeta.getText()) >= 0
						&& panel.paint > 0) {
					panel.beta = Double.parseDouble(textBeta.getText());
					btnRetrack.setVisible(true);
				}
			}
		});
		textBeta.setText("1.0");
		textBeta.setBounds(745, 35, 69, 27);
		contentPane.add(textBeta);
		textBeta.setColumns(10);

		textGamma = new JTextField();
		textGamma.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (Double.parseDouble(textGamma.getText()) >= 0
						&& panel.paint > 0) {
					panel.gamma = Double.parseDouble(textGamma.getText());
					btnRetrack.setVisible(true);
				}
			}
		});
		textGamma.setText("1.0");
		textGamma.setBounds(773, 70, 69, 27);
		contentPane.add(textGamma);
		textGamma.setColumns(10);

		JButton btnShowStats = new JButton("Show Stats");
		btnShowStats.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				end1Stats = panel.getEndStats(results, 0);
				end2Stats = panel.getEndStats(results, 1);
				int[][] filters = panel.recordDiscontinuity(results);
				MicroStats frame = new MicroStats(lengthStats, microIDs,
						filters[0], filters[1], filters[2], end1Stats, end2Stats, panel, contourSets, results);
				frame.setVisible(true);
				// frame.repaint();
			}
		});
		btnShowStats.setBounds(545, 381, 117, 29);
		contentPane.add(btnShowStats);

		btnSave.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (panel.paint <= 1) {
					// can only save first frame
					saveFrame(panel.currentMicros, panel.currentCont);
				} else {
					JOptionPane.showMessageDialog(null,
							"Can only save the first frame");
				}
			}
		});

		btnLoaf.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (panel.paint <= 1) {
					JFileChooser chooser = new JFileChooser(
							"/Users/linxuanyang/Documents/workspace/SimpleFrame");
					chooser.setMultiSelectionEnabled(false);
					if (chooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
						File file = chooser.getSelectedFile();
						if (loadFrame(file, panel.currentCont,
								panel.currentMicros)) {
							if (panel.paint == 1) {
								btnRetrack.setVisible(true);
							}
							panel.repaint();
							refreshMicroList(panel.currentMicros);
						}
					}
				} else {
					JOptionPane.showMessageDialog(null,
							"Can only load the first frame");
				}
			}
		});

		btnLossColoring.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				btnNewButton.setEnabled(true);
				btnNewButton.setVisible(true);
				firstFrame.setVisible(true);
				panel.setVisible(false);
				lblLossColoring.setVisible(true);
				lblResultLines.setVisible(false);
				firstFrame.repaint();
				btnLossColoring.setEnabled(false);
				btnLossColoring.setVisible(false);
			}
		});

		btnNewButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				btnLossColoring.setEnabled(true);
				btnLossColoring.setVisible(true);
				firstFrame.setVisible(false);
				panel.setVisible(true);
				lblLossColoring.setVisible(false);
				lblResultLines.setVisible(true);
				panel.repaint();
				btnNewButton.setEnabled(false);
				btnNewButton.setVisible(false);

			}
		});

	}

	private void colorLoss(MicroPanel firstFrame, Loss[] list) {
		firstFrame.currentCont = contourSets.get(0);
		firstFrame.currentMicros = results.get(0);
		// only need the first frame results
		firstFrame.paint = 1;
		firstFrame.gray = true;
		firstFrame.intensities = new double[list.length];
		double intensity = 105.0;
		double increment = 30.0; // adjustable
		Microtubules micros = firstFrame.currentMicros;
		for (int i = 0; i < list.length; ++i) {
			if (list[i].loss == Double.MAX_VALUE || intensity >= 255.0) {
				firstFrame.intensities[micros.getIndexByID(list[i].ID)] = 255;
			} else {
				firstFrame.intensities[micros.getIndexByID(list[i].ID)] = intensity;
			}
			if (i == 0 && list[i].loss != Double.MAX_VALUE) {
				// make the first meaningful loss very dark
				firstFrame.intensities[micros.getIndexByID(list[i].ID)] = 0.0;
			}
			intensity += increment;
		}
		firstFrame.repaint();
	}

	public boolean saveFrame(Microtubules micros, Lines lines) {
		try (Writer writer = new BufferedWriter(new OutputStreamWriter(
				new FileOutputStream(micros.size() + "firstFrame.txt")))) {
			for (Microtubule m : micros) {
				writer.write(m.ID + ":");
				writer.write(System.lineSeparator());
				for (Line l : m.lines) {
					writer.write("" + l.getID());
					writer.write(System.lineSeparator());
				}
			}
			writer.write("Unmatched: ");
			writer.write(System.lineSeparator());
			for (Line l : lines) {
				if (l.microID.isEmpty()) {
					// add unmatched lines as well
					writer.write("" + l.getID());
					writer.write(System.lineSeparator());
				}
			}
			writer.close();
			JOptionPane.showMessageDialog(null,
					"First Frame Saved Successfully");
			return true;
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return false;
	}

	public boolean loadFrame(File file, Lines lines, Microtubules micros) {
		try {
			FileInputStream fis = new FileInputStream(file);
			BufferedReader br = new BufferedReader(new InputStreamReader(fis));
			Microtubule last = null;
			ArrayList<Integer> validMicro = new ArrayList<Integer>();
			Microtubules toRemove = new Microtubules(-100);
			while (br.ready()) {
				String lineinfo = br.readLine();
				if (lineinfo.contains(":")) {
					if (lineinfo.contains("Unmatched")) {
						// the unmatched lines are coming
						last = null;
					} else {
						int ID = Integer.parseInt(lineinfo.substring(0,
								lineinfo.length() - 1));
						validMicro.add(ID);
						if (ID > micros.get(micros.size() - 1).ID) {
							// this is a new one
							last = new Microtubule(new Lines(1), ID);
							micros.add(last);
						}
						// get the microtubule from the list
						last = micros.getMicroByID(ID);
						// clear its original lines
						last.lines.clear();
					}
				} else {
					// process the lines
					int lineID = Integer.parseInt(lineinfo);
					if (lineID > 0) {
						// ignore added lines
						Line toMod = lines.getLineByID(lineID);
						if (last != null) {
							// has a match, add to the microtubule
							last.lines.add(toMod);
							toMod.microID.clear();
						} else {
							// no match, clear the microID list
							toMod.microID.clear();
						}
					}

				}
			}
			for (Microtubule m : micros) {
				// reconstruct the line mapping to microtubules
				if (m.lines.isEmpty()
						|| (!validMicro.contains(Integer.valueOf(m.ID)))) {
					toRemove.add(m);
				} else {
					for (Line l : m.lines) {
						l.microID.add(m.ID);
					}
				}
			}
			micros.removeAll(toRemove);
			br.close();
			System.out.println("Load frame successfully");
			return true;
		} catch (IOException e) {
			System.out.println("failed to load frame");
			return false;
		}
	}

	public boolean parseLines(File file, Lines contours) {
		try {
			FileInputStream fis = new FileInputStream(file);
			BufferedReader br = new BufferedReader(new InputStreamReader(fis));
			int frame = 0;
			if (br.ready()) {
				// the first line is the frame number
				frame = Integer.parseInt(br.readLine());
				contours.frame = frame;
			}
			while (br.ready()) {
				String lineinfo = br.readLine();
				if (lineinfo.substring(0, 4).equals("line")) {
					Line l = new Line(Integer.parseInt(lineinfo.substring(5)));
					contours.add(l);
				} else if (lineinfo.contains("cont")) {
					Line l = contours.get(contours.size() - 1);
					if (lineinfo.equals("cont_no_junc")) {
						l.setContourClass(Line.contour_class.cont_no_junc);
					} else if (lineinfo.equals("cont_start_junc")) {
						l.setContourClass(Line.contour_class.cont_start_junc);
					} else if (lineinfo.equals("cont_end_junc")) {
						l.setContourClass(Line.contour_class.cont_end_junc);
					} else if (lineinfo.equals("cont_both_junc")) {
						l.setContourClass(Line.contour_class.cont_both_junc);
					} else if (lineinfo.equals("cont_closed")) {
						l.setContourClass(Line.contour_class.cont_closed);
					} else {
						System.out.println("should not happens");
					}
				} else {
					Line l = contours.get(contours.size() - 1);
					float col = Float.parseFloat(lineinfo.split(",")[0]);
					float row = Float.parseFloat(lineinfo.split(",")[1]);
					l.col.add(col);
					l.row.add(row);
					l.num++;
				}
			}
			if (contours.frame != 1) {
				// System.out.println(contours.size());
				HashSet<Line> uniqueLines = new HashSet<Line>(contours);
				// System.out.println(uniqueLines.size());
				contours.clear();
				contours.addAll(uniqueLines);
				Collections.sort(contours);
			}
			System.out.println("Done importing lines for frame" + frame);
			br.close();
			return true;
		} catch (IOException e) {
			System.out.println("failed to import lines");
			return false;
		}
	}

	public boolean parseJunctions(File file, Junctions junctions) {
		try {
			FileInputStream fis = new FileInputStream(file);
			BufferedReader br = new BufferedReader(new InputStreamReader(fis));
			int frame = 0;
			if (br.ready()) {
				frame = Integer.parseInt(br.readLine());
				junctions.frame = frame;
			}
			while (br.ready()) {
				String juncinfo = br.readLine();
				if (juncinfo.contains("and")) {
					Junction j = new Junction();
					j.cont1 = Integer.parseInt(juncinfo.split(" and ")[0]);
					j.cont2 = Integer.parseInt(juncinfo.split(" and ")[1]);
					junctions.add(j);
				} else {
					Junction j = junctions.get(junctions.size() - 1);
					j.x = Float.parseFloat(juncinfo.split(",")[0]);
					j.y = Float.parseFloat(juncinfo.split(",")[1]);
				}
			}
			System.out.println("Done importing junctions for frame" + frame);
			br.close();
			// clean up duplicate junctions.
			HashSet<Junction> unique = new HashSet<Junction>();
			unique.addAll(junctions);
			junctions.clear();
			junctions.addAll(unique);
			return true;
		} catch (IOException e) {
			System.out.println("failed to import junctions");
			return false;
		}
	}

	private void retrackFrame(MicroPanel panel) {
		negativeAdded = new boolean[numLineSets];
		results.clear();
		panel.alpha = Double.parseDouble(txtAlpha.getText());
		panel.beta = Double.parseDouble(textBeta.getText());
		panel.gamma = Double.parseDouble(textGamma.getText());
		for (Lines ls : contourSets) {
			panel.revertToDefaultLines(ls);
		}
		panel.currentCont = contourSets.get(0);
		panel.currentJunc = juncSets.get(0);
		panel.revertToDefaultMicro(panel.currentMicros);
		results = panel.constructMatch(contourSets, juncSets,
				panel.currentMicros);
		panel.currentMicros = results.get(0);
		lengthStats = new double[results.size()][panel.currentMicros.size()];
		microIDs = new int[panel.currentMicros.size()];
		Tool t = new Tool();
		for (int i = 0; i < results.size(); ++i) {
			for (int j = 0; j < results.get(i).size(); ++j) {
				Microtubule m = results.get(i).get(j);
				double length = 0;
				for (Line l : m.lines) {
					float[] start = { l.col.get(0), l.row.get(0) };
					float[] end = { l.col.get(l.num - 1), l.row.get(l.num - 1) };
					length += t.dist(start, end);
				}
				microIDs[j] = m.ID;
				lengthStats[i][j] = length;
			}
		}
		panel.frameCleanUp(panel.currentMicros, contourSets.get(0));
		panel.paint = 1;
		panel.repaint();
	}

	private boolean inRectangle(int startX, int startY, int endX, int endY,
			double X, double Y) {
		int left, right, up, down;
		if (startX > endX) {
			left = endX;
			right = startX;
		} else {
			left = startX;
			right = endX;
		}
		if (startY > endY) {
			up = endY;
			down = startY;
		} else {
			up = startY;
			down = endY;
		}
		return (X >= left) && (X <= right) && (Y >= up) && (Y <= down);
	}

	private void refreshMicroList(Microtubules micros) {
		JmicroList.setListData(micros.toArray(new Microtubule[micros.size()]));
	}
}

class ToggleSelectionModel extends DefaultListSelectionModel {
	boolean gestureStarted = false;

	public void setSelectionInterval(int index0, int index1) {
		if (isSelectedIndex(index0) && !gestureStarted) {
			super.removeSelectionInterval(index0, index1);
		} else {
			super.setSelectionInterval(index0, index1);
		}
		gestureStarted = true;
	}

	public void setValueIsAdjusting(boolean isAdjusting) {
		if (isAdjusting == false) {
			gestureStarted = false;
		}
	}
}
