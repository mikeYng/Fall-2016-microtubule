import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Shape;
import java.awt.event.MouseEvent;
import java.awt.geom.GeneralPath;
import java.awt.geom.Line2D;
import java.awt.geom.PathIterator;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;

import javax.swing.JPanel;

public class MicroPanel extends JPanel {

	public Lines currentCont;
	public Junctions currentJunc;
	public Microtubules currentMicros;
	public boolean showMicro = false;
	public int dispLine = -1;
	public ArrayList<Integer> multiSelect = new ArrayList<Integer>();
	public boolean enableMulti;
	public boolean colSeg = false;
	public int dispMicro = -1;
	public ArrayList<PathID> shapeList = new ArrayList<PathID>();
	public int paint = -1; // 0 for singlefram, >0 multi frame, <0 dont paint
	public boolean gray = false;
	public double[] intensities;
	public boolean ignoreTop = false;
	public boolean verbose = false;
	public boolean hideUnmatch = false;
	public boolean hideAdded = false;
	// only used if grayscale
	public double alpha = 1;
	public double beta = 0;
	public double gamma = 0;

	// the weight on slope difference in loss calculation
	
	public MicroPanel () {
		super();
	}
	

	public MicroPanel(MicroPanel panel) {
		super();
		this.currentCont = panel.currentCont;
		this.currentJunc = panel.currentJunc;
		this.currentMicros = panel.currentMicros;
		this.showMicro = panel.showMicro;
		this.dispLine = panel.dispLine;
		this.multiSelect = panel.multiSelect;
		this.enableMulti = panel.enableMulti;
		this.colSeg = panel.colSeg;
		this.dispMicro = panel.dispMicro;
		this.shapeList = panel.shapeList;
		this.paint = panel.paint;
		this.gray = panel.gray;
		this.intensities = panel.intensities;
		this.ignoreTop = panel.ignoreTop;
		this.verbose = panel.verbose;
		this.hideUnmatch = panel.hideUnmatch;
		this.hideAdded = panel.hideAdded;
		this.alpha = panel.alpha;
		this.beta = panel.beta;
		this.gamma = panel.gamma;
	}

	public void paintComponent(Graphics g) {
		super.paintComponent(g);
		Graphics2D g2 = (Graphics2D) g;
		g2.setStroke(new BasicStroke(3));
		if (paint == 0) {
			// simple frame link
			shapeList.clear();
			float hue = 0.0f;
			if (colSeg) {
				// coloring by segments
				float increment = 1.0f / 43;
				for (Microtubule micro : currentMicros) {
					for (Line l : micro.lines) {
						hue += 13 * increment * Math.sqrt(2);
						// make the color contrasting
						if (hue > 1.0f) {
							hue = 1.0f - hue;
						}
						Color color = Color.getHSBColor(hue, 1.0f, 0.5f);
						g2.setColor(color);
						shapeList.add(drawLine(l, color, g2));
					}
				}
			} else {
				// color by microtubules
				float increment = 1.0f / currentMicros.size();
				for (Microtubule micro : currentMicros) {
					hue += increment;
					Color color = Color.getHSBColor(hue, 1.0f, 0.5f);
					g2.setColor(color);
					for (Line l : micro.lines) {
						shapeList.add(drawLine(l, color, g2));
					}
				}
			}
			for (Line l : currentCont) {
				if (l.microID.isEmpty()) {
					if (!hideUnmatch) {
						shapeList.add(drawLine(l, Color.BLACK, g2));
						// color the unmatched lines black.
					}
				}
			}
		} else if (paint > 0) {
			shapeList.clear();
			float hue = 0.0f;
			if (gray) {
				for (int i = 0; i < currentMicros.size(); ++i) {
					int inten = (int) intensities[i];
					if (inten != 255) {
						Color color = new Color(inten, inten, inten);
						g2.setColor(color);
						for (Line l : currentMicros.get(i).lines) {
							drawLine(l, color, g2);
						}
					}
				}
			} else {
				for (Line l : currentCont) {
					if (l.microID.isEmpty()) {
						if (!hideUnmatch) {
							shapeList.add(drawLine(l, Color.BLACK, g2));
							// color the unmatched lines black.
						}
					}
				}
				if (colSeg) {
					// coloring by segments
					float increment = 1.0f / 43;
					for (Microtubule micro : currentMicros) {
						for (Line l : micro.lines) {
							hue += 13 * increment * Math.sqrt(2);
							// make the color contrasting
							if (hue > 1.0f) {
								hue = 1.0f - hue;
							}
							Color color = Color.getHSBColor(hue, 1.0f, 0.5f);
							g2.setColor(color);
							if (l.getID() <= 0) {
								// if added lines are not wanted, hide it
								if (!hideAdded) {
									shapeList.add(drawLine(l, color, g2));
								}
							} else {
								shapeList.add(drawLine(l, color, g2));
							}
						}
					}
				} else {
					for (Microtubule micro : currentMicros) {
						float increment = 1.0f / currentMicros.size();
						hue += increment;
						Color color = Color.getHSBColor(hue, 1.0f, 0.5f);
						g2.setColor(color);
						for (Line l : micro.lines) {
							if (l.getID() <= 0) {
								if (!hideAdded) {
									shapeList.add(drawLine(l, color, g2));
								}
							} else {
								shapeList.add(drawLine(l, color, g2));
							}
						}
					}
				}
			}

		}
		if (paint >= 0 && enableMulti) {
			for (int i : multiSelect) {
				Line l = currentCont.get(i);
				drawLine(l, new Color(255, 215, 0), g2);
				// color it gold
			}
		}
		if (paint >= 0 && dispLine >= 0 && (!enableMulti)) {
			// color single line
			Line l = currentCont.get(dispLine);
			drawLine(l, new Color(255, 215, 0), g2);
			// color it gold

		}
		if (paint >= 0 && dispMicro >= 0) {
			Microtubule micro = currentMicros.get(dispMicro);
			for (Line l : micro.lines) {
				drawLine(l, new Color(230, 190, 138), g2);
			}
		}

	}

	private PathID drawLine(Line l, Color c, Graphics2D g2) {
		GeneralPath path = new GeneralPath(GeneralPath.WIND_EVEN_ODD, l.num);
		path.moveTo(l.col.get(0), l.row.get(0));
		for (int i = 1; i < l.num; ++i) {
			path.lineTo(l.col.get(i), l.row.get(i));
		}
		g2.setColor(c);// gold color
		g2.draw(path);
		return new PathID(path, l.getID());
	}

	public int clickLine(MouseEvent e) {
		Point p = e.getPoint();
		for (int i = 0; i < shapeList.size(); ++i) {
			if (shapeList.get(i).gp
					.intersects(p.getX() - 3, p.getY() - 3, 6, 6)) {
				// return the ID of the line such that it can be retrieved.
				return shapeList.get(i).ID;
			}
		}
		return -1;
	}

	public void frameCleanUp(Microtubules micros, Lines lines) {
		ArrayList<Microtubule> rm = new ArrayList<Microtubule>();
		ArrayList<Line> rml = new ArrayList<Line>();
		for (int i = 0; i < micros.size(); ++i) {
			double sum = 0.0;
			int num = micros.get(i).lines.size();
			for (int j = 0; j < micros.size(); ++j) {
				if (i != j) {
					if (micros.get(i).lines.containsAll(micros.get(j).lines)) {
						// j is a subset of i
						rm.add(micros.get(j));
					}
				}
			}
			// end of microtubules clean up
			for (Line l : micros.get(i).lines) {
				sum += l.num;
			}
			double avg = sum / (num * 1.0);
			if (sum <= 10 && avg < 4) {
				rm.add(micros.get(i));
			}
		}
		// remove meaningless microtubules

		for (Microtubule m : rm) {
			for (Line l : m.lines) {
				// remove the record from the line's perspective
				l.microID.remove(Integer.valueOf(m.ID));
			}
		}
		micros.removeAll(rm);
		// now remove all short and unmatched line segments
		for (Line l : lines) {
			if (l.microID.isEmpty() && l.num <= 3) {
				// there is a bug here that if lines with duplicate ID exists,
				// would fail.
				if (lines.getLineByID(l.getID()).microID.isEmpty()) {
					// for the sake of not having bug, recheck
					// rml.add(l);
					removeLineCleanUp(l, currentJunc, lines);
				}
			}
		}
		// lines.removeAll(rml);
	}

	private boolean checkAdditionalJunctions(Junctions junc, Junction j,
			Line l, int pos) {
		if (pos != 0) {
			pos = l.num - 1;
		}
		for (Junction j1 : junc) {
			if ((!j1.equals(j)) && (j1.x == l.col.get(pos))
					&& (j1.y == l.row.get(pos))) {
				return true;
			}
		}
		return false;
	}

	public void removeLineCleanUp(Line rm, Junctions junc, Lines contours) {
		ArrayList<Junction> toRemove = new ArrayList<Junction>();
		for (Junction j : junc) {
			if (j.cont1 == rm.getID() || j.cont2 == rm.getID()) {
				toRemove.add(j);
				Line toModify = j.cont1 == rm.getID() ? contours
						.getLineByID(j.cont2) : contours.getLineByID(j.cont1);
				if (toModify.getContourClass() == Line.contour_class.cont_start_junc) {
					if (!checkAdditionalJunctions(junc, j, toModify, 0)) {
						toModify.setContourClass(Line.contour_class.cont_no_junc);
					}
				} else if (toModify.getContourClass() == Line.contour_class.cont_end_junc) {
					if (!checkAdditionalJunctions(junc, j, toModify, 1)) {
						toModify.setContourClass(Line.contour_class.cont_no_junc);
					}
				} else if (toModify.getContourClass() == Line.contour_class.cont_both_junc) {
					if ((toModify.col.get(0) == j.x)
							&& (toModify.row.get(0) == j.y)) {
						if (!checkAdditionalJunctions(junc, j, toModify, 0)) {
							toModify.setContourClass(Line.contour_class.cont_end_junc);
						}
					} else if ((toModify.col.get(toModify.num - 1) == j.x)
							&& (toModify.row.get(toModify.num - 1) == j.y)) {
						if (!checkAdditionalJunctions(junc, j, toModify, 0)) {
							toModify.setContourClass(Line.contour_class.cont_start_junc);
						}
					} else {
						System.out.println("PROBLEM " + toModify.getID());
					}
				} else {
					System.out.println("PROBLEM " + toModify.getContourClass()
							+ " " + toModify.getID());
				}
			}
		}
		junc.removeAll(toRemove);
	}

	// copied from the imagej project
	public ArrayList<Lines> singleFrameLink(Lines contours, Junctions junc) {
		for (Line line : contours) {
			line.processed = 0;
		}
		ArrayList<Lines> microtubules = new ArrayList<Lines>();
		for (Line line : contours) {
			if (verbose) {
				System.out.println("Starting a new microtubule at "
						+ line.getID());
			}
			if (line.processed != 0) {
				continue;
			}
			if (ignoreTop) {
				if ((line.row.get(0) < 15 && line.col.get(0) < 50)
						|| (line.row.get(line.num - 1) < 15 && line.col
								.get(line.num - 1) < 50)) {
					// there is a time stamp on the left corner which is sth we
					// dont
					// want
					// ignore all the lines referring to that area.
					continue;
				}
			}
			if (line.getContourClass() == Line.contour_class.cont_closed
					|| line.getContourClass() == Line.contour_class.cont_both_junc) {
				continue;
			} else if (line.getContourClass() == Line.contour_class.cont_start_junc) {
				Lines initial = new Lines(contours.getFrame());
				initial.add(line);
				microtubules.add(nextLine(line, junc, contours, initial, 0,
						false));
			} else if (line.getContourClass() == Line.contour_class.cont_end_junc) {
				Lines initial = new Lines(contours.getFrame());
				initial.add(line);
				microtubules.add(nextLine(line, junc, contours, initial, 1,
						false));
			} else {
				Lines singleton = new Lines(contours.getFrame());
				singleton.add(line);
				microtubules.add(singleton);
			}

		}
		for (Line line : contours) {
			// second pass
			// try to resolve short line problems
			if (line.num <= 3
					&& line.getContourClass() == Line.contour_class.cont_both_junc) {
				Lines initial = new Lines(contours.getFrame());
				initial.add(line);
				initial = nextLine(line, junc, contours, initial, 0, false);
				if (initial.size() > 1) {
					microtubules.add(initial);
				}
			}
		}
		return microtubules;
	}

	private Lines nextLine(Line line, Junctions junc, Lines contours,
			Lines micro, int pos, boolean shortLine) {
		if (verbose) {
			System.out
					.println("processing line " + line.getID() + " at " + pos);
		}
		Tool tool = new Tool();
		if (line.processed == 0) {
			// allow some lines to be used multiple times
			line.processed++;
		}

		if (pos != 0) {
			// pos =0 -> start
			// pos =1 -> end
			pos = line.num - 1;
		}
		Line proceed = null;
		boolean success = false;
		for (Junction j : junc) {
			if ((Math.abs(j.x - line.col.get(pos)) == 0.0)
					&& (Math.abs(j.y - line.row.get(pos)) == 0.0)
					&& ((j.cont1 == line.getID()) || (j.cont2 == line.getID()))) {
				// first search for exact matches
				if (verbose) {
					System.out.println("Line " + line.getID() + " matched at "
							+ j.y + " , " + j.x);
					System.out.println("The two lines are " + j.cont1 + " and "
							+ j.cont2);
				}
				proceed = (j.cont1 == line.getID() ? contours
						.getLineByID(j.cont2) : contours.getLineByID(j.cont1));
				if (verbose) {
					System.out.println("Next line is " + proceed.getID()
							+ " ,status " + proceed.processed + " ,type is "
							+ proceed.getContourClass());
				}

				// choose the next connecting line to proceed.
				if (proceed.processed == 0
						|| (proceed.processed < 2 && proceed.num <= 15)) {
					double straight = 2.0;
					if (shortLine) {
						// if the current line is very short, one need to
						// consider the previous line in the matching because
						// straightness can be inaccurate
						ArrayList<float[]> points = new ArrayList<float[]>();
						float[] mid1 = { line.col.get(line.num - 1 - pos),
								line.row.get(line.num - 1 - pos) };
						float[] mid2 = { line.col.get(pos), line.row.get(pos) };
						float[] p2 = tool.getInterceptPoint(mid2, proceed);
						// the two end points of this short line.
						points.add(p2);
						points.add(mid2);
						points.add(mid1);
						if (micro.size() >= 2) {
							// must ensure that it has a previous line
							// sometimes the algo starts from a short line.
							int p = 2;
							while (micro.get(micro.size() - p).num <= 5) {
								// previous line is still short
								Line anotherShort = micro.get(micro.size() - p);
								if (anotherShort.col.get(0) == mid1[0]
										&& anotherShort.row.get(0) == mid1[1]) {
									float[] shortPoint = {
											anotherShort.col
													.get(anotherShort.num - 1),
											anotherShort.row
													.get(anotherShort.num - 1) };
									points.add(shortPoint);
								} else {
									float[] shortPoint = {
											anotherShort.col.get(0),
											anotherShort.row.get(0) };
									points.add(shortPoint);
								}
								if (p + 1 > micro.size())
									break;
								++p;

							}
							float[] p1 = tool.getInterceptPoint(
									points.get(points.size() - 1),
									micro.get(micro.size() - p));
							points.add(p1);
							// get the point from the previous line.
						}

						straight = tool.straightCalc(points
								.toArray(new float[points.size()][]));
					} else {
						float[] mid = { line.col.get(pos), line.row.get(pos) };
						float[] p1 = tool.getInterceptPoint(mid, line);
						float[] p2 = tool.getInterceptPoint(mid, proceed);
						straight = tool.straightCalc(p1, mid, p2);
					}
					// measure the straightness of this link
					if (verbose) {
						System.out.println("straightness is " + straight);
					}

					if (straight < 1.051
							|| (proceed.num <= 5 && line.num <= 5 && straight < 1.75)) {
						if (!micro.contains(proceed)) {
							// do not want duplicate lines
							micro.add(proceed);
							if (verbose) {
								System.out.println("new line added");
							}

							success = true;
							proceed.processed++;
							if (proceed.getContourClass() == Line.contour_class.cont_both_junc) {
								shortLine = (proceed.num <= 5);
								if (j.x == proceed.col.get(0)
										&& j.y == proceed.row.get(0)) {
									// the junction is start of next line, then
									// search from its end
									return nextLine(proceed, junc, contours,
											micro, 1, shortLine);
								} else {
									// vice versa.
									return nextLine(proceed, junc, contours,
											micro, 0, shortLine);
								}
							} else {
								// proceed has at least one open end, end of
								// search.
								return micro;
							}
						}
					}
				}
			}
		}
		if (!success) {
			// no exact match, search rough matches instead
			// same process as before
			for (Junction j : junc) {
				if ((Math.abs(j.x - line.col.get(pos)) <= 1.0)
						&& (Math.abs(j.y - line.row.get(pos)) <= 1.0)
						&& ((j.cont1 == line.getID()) || (j.cont2 == line
								.getID()))) {
					if (verbose) {
						System.out.println("Line " + line.getID()
								+ " matched at " + j.y + " , " + j.x);
						System.out.println("The two lines are " + j.cont1
								+ " and " + j.cont2);
					}
					proceed = (j.cont1 == line.getID() ? contours
							.getLineByID(j.cont2) : contours
							.getLineByID(j.cont1));
					if (verbose) {
						System.out.println("Next line is " + proceed.getID()
								+ " ,status " + proceed.processed
								+ " ,type is " + proceed.getContourClass());
					}
					if (proceed.processed == 0
							|| (proceed.processed < 2 && proceed.num <= 15)) {
						double straight = 2.0;
						if (shortLine) {
							ArrayList<float[]> points = new ArrayList<float[]>();
							float[] mid1 = { line.col.get(line.num - 1 - pos),
									line.row.get(line.num - 1 - pos) };
							float[] mid2 = { line.col.get(pos),
									line.row.get(pos) };
							float[] p2 = tool.getInterceptPoint(mid2, proceed);
							points.add(p2);
							points.add(mid2);
							points.add(mid1);
							if (micro.size() >= 2) {
								int p = 2;
								while (micro.get(micro.size() - p).num <= 5) {
									Line anotherShort = micro.get(micro.size()
											- p);
									if (anotherShort.col.get(0) == mid1[0]
											&& anotherShort.row.get(0) == mid1[1]) {
										float[] shortPoint = {
												anotherShort.col
														.get(anotherShort.num - 1),
												anotherShort.row
														.get(anotherShort.num - 1) };
										points.add(shortPoint);
									} else {
										float[] shortPoint = {
												anotherShort.col.get(0),
												anotherShort.row.get(0) };
										points.add(shortPoint);
									}
									if (p + 1 > micro.size())
										break;
									++p;

								}
								float[] p1 = tool.getInterceptPoint(
										points.get(points.size() - 1),
										micro.get(micro.size() - p));
								points.add(p1);
							}

							straight = tool.straightCalc(points
									.toArray(new float[points.size()][]));
						} else {
							float[] mid = { line.col.get(pos),
									line.row.get(pos) };
							float[] p1 = tool.getInterceptPoint(mid, line);
							float[] p2 = tool.getInterceptPoint(mid, proceed);
							straight = tool.straightCalc(p1, mid, p2);
						}
						if (straight < 1.051
								|| (proceed.num <= 5 && line.num <= 5 && straight < 1.75)) {
							if (!micro.contains(proceed)) {
								micro.add(proceed);
								if (verbose) {
									System.out.println("new line added");
								}
								proceed.processed++;
								if (proceed.getContourClass() == Line.contour_class.cont_both_junc) {
									shortLine = (proceed.num <= 5);
									if (j.x == proceed.col.get(0)
											&& j.y == proceed.row.get(0)) {
										return nextLine(proceed, junc,
												contours, micro, 1, shortLine);
									} else {
										return nextLine(proceed, junc,
												contours, micro, 0, shortLine);
									}
								} else {
									return micro;
								}
							}
						}
					}
				}
			}
		}
		return micro;
	}

	public Microtubules lineMatching(Microtubules[] micros, Lines contours,
			int frameID) {
		// micros contain an buffer of N processed frames, such that
		// if a line cannot find a match from the last frame
		// it can search couple of frames ahead.
		Microtubules anotherFrame = new Microtubules(frameID);
		int ID = 0;
		for (Microtubule tube : micros[0]) {
			for (Line l : tube.lines) {
				// initialize the coverage list
				l.initCoverage(l.num);
			}
			ID = connectBrokenMicros(tube, ID);
			Microtubule newMicro = new Microtubule(new Lines(
					contours.getFrame()), tube.ID);
			anotherFrame.add(newMicro);
		}
		// sort the lines such that the longest would be processed first
		Collections.sort(contours, new Comparator<Line>() {

			@Override
			public int compare(Line o1, Line o2) {
				// TODO Auto-generated method stub
				return -(o1.num - o2.num);
			}

		});
		// Lines oldLines = new Lines(anotherFrame.getFrame());
		for (Line l : contours) {
			if (l.processed != 0) {
				continue;
			}
			if (ignoreTop) {
				if ((l.row.get(0) < 15 && l.col.get(0) < 50)
						|| (l.row.get(l.num - 1) < 15 && l.col.get(l.num - 1) < 50)) {
					continue;
				}
			}
			l.matched = false;
			l.splits = null;
			l.isAdded = false;
			Loss[] lossList = new Loss[micros[0].size()];
			for (int i = 0; i < micros[0].size(); ++i) {
				Microtubule micro = micros[0].get(i);
				double addLoss = computeLoss(micro, l);
				lossList[i] = new Loss(addLoss, micro.ID, 0);
			}
			Arrays.sort(lossList);
			// add to the microtubule with least loss.
			l.lossList = lossList;
			if (l.lossList[0].loss == Double.MAX_VALUE) {
				// there is no match from the last frame
				// try a couple more

				for (int time = 1; time <= 2; ++time) {
					if (micros[time] != null) {
						for (int i = 0; i < micros[time].size(); ++i) {
							Microtubule micro = micros[time].get(i);
							for (Line line : micro.lines) {
								line.initCoverage(line.num);
							}
							double addLoss = computeLoss(micro, l);
							if (addLoss < Double.MAX_VALUE) {
								for (int j = 0; j < lossList.length; ++j) {
									if (micro.ID == lossList[j].ID
											&& addLoss < lossList[j].loss) {
										lossList[j] = new Loss(addLoss,
												micro.ID, time);
									}
								}
							}
						}
					}
				}
				Arrays.sort(lossList);
				l.lossList = lossList;
			}
			if (alpha == 0 && beta == 0 && gamma == 0) {
				// use the old scheme if all values are 0.
				if (l.lossList[0].loss != Double.MAX_VALUE) {
					// only a valid match if the least loss is not infinity
					l.matched = true;
					anotherFrame.getMicroByID(lossList[0].ID).lines.add(l);
				}
			} else {
				// otherwise do the micro based loss to further filter.
				double smallestChange = 0;
				int matchindex = -1;
				for (int i = 0; i < l.lossList.length; ++i) {
					if (l.lossList[i].loss != Double.MAX_VALUE) {
						// get the candidate microtubule
						Microtubule test = micros[l.lossList[i].loc]
								.getMicroByID(l.lossList[i].ID);
						Microtubule current = anotherFrame
								.getMicroByID(lossList[i].ID);
						double useBeta = beta;
						if (l.lossList[i].loss / l.num > 10) {
							// tune some error given by matching to couple
							// frames back
							if (l.lossList[i].loc != 0) {
								useBeta = l.num;
							} else {
								useBeta = 1.5 * beta;
							}
						}
						double prevLoss = alpha * uncoveredLoss(test, false)
								+ useBeta * overlapLoss(test, false) + 0
								* discontinuityLoss(current);
						current.lines.add(l);
						double afterLoss = alpha * uncoveredLoss(test, true)
								+ useBeta * overlapLoss(test, true) + 0
								* discontinuityLoss(current);
						double diff = afterLoss - prevLoss;
						if (diff < smallestChange) {
							smallestChange = diff;
							matchindex = i;
							// if (l.getID() == 5754 && test.ID==9) {
							// System.out.println("index is " + matchindex);
							// }
							l.matched = true;
						}
					}
				}
				for (int i = 0; i < l.lossList.length; ++i) {
					Microtubule target = micros[l.lossList[i].loc]
							.getMicroByID(l.lossList[i].ID);
					Microtubule current = anotherFrame
							.getMicroByID(lossList[i].ID);
					if (i == matchindex) {
						// if (l.getID() == 5754 && target.ID==9) {
						// System.out.println("index is " + i);
						// System.out.println("Before ");
						// System.out.println(uncoveredLoss(target, false) +
						// " , " + uncoveredLoss(target, true));
						// System.out.println(overlapLoss(target, false) + " , "
						// + overlapLoss(target, true));
						// }
						target.updateCoverage();
						// if (l.getID() == 5754 && target.ID==9) {
						// System.out.println("After ");
						// System.out.println(uncoveredLoss(target, false) +
						// " , " + uncoveredLoss(target, true));
						// System.out.println(overlapLoss(target, false) + " , "
						// + overlapLoss(target, true));
						// }
					} else {
						// not a good match, remove attempts made
						target.revertCoverage();
						current.lines.remove(l);
					}
				}
			}
			if (!l.matched) {
				// still umatched after these steps.
				// Need to consider splitting the lines to match more
				// microtubules.
				if (l.num > 10) {
					// only check ones that are significant
					l.initCoverage(l.num);
					for (int i = 0; i < micros[0].size(); ++i) {
						Microtubule micro = micros[0].get(i);
						double deltaLoss = computeTwoWayLoss(micro, l,
								anotherFrame, true);
						l.lossList[i] = new Loss(deltaLoss, micro.ID, 0);
						// first run, clear matching records afterwards;
						// this is used to test candidates.
						l.revertCoverage();
						micro.revertCoverage();
					}
					Arrays.sort(l.lossList);
					// second run, start from the most probable one.
					for (int i = 0; i < l.lossList.length; ++i) {
						if (l.lossList[i].loss != 0) {
							Microtubule micro = micros[0]
									.getMicroByID(l.lossList[i].ID);
							double deltaLoss = computeTwoWayLoss(micro, l,
									anotherFrame, false);
							double percent = Math.abs(deltaLoss / l.num);
							if (deltaLoss < -1.0 && percent > 0.05
									&& l.splits != null) {
								// should use the line
								// anotherFrame.getMicroByID(micro.ID).lines.add(l);
								l.updateCoverage();
								micro.updateCoverage();
								l.matched = true;
							} else {
								// still bad fit, erase the fit
								l.revertCoverage();
								micro.revertCoverage();
							}
						}
					}
					if (l.matched) {
						if (l.splits.size() == 1) {
							// the line did not split; it is just itself
							int matchID = l.splits.get(0).lossList[0].ID;
							l.isAdded = true;
							// l.setContourClass(null);
							anotherFrame.getMicroByID(matchID).lines.add(l);
						} else {
							// more than one line, needs to split
							for (int i = 0; i < l.splits.size(); ++i) {
								Line split = l.splits.get(i);
								int matchID = split.lossList[0].ID;
								anotherFrame.getMicroByID(matchID).added
										.add(split);
								anotherFrame.getMicroByID(matchID).lines
										.add(split);
							}
							// oldLines.add(l);
						}
					}
					Arrays.sort(l.lossList);
					// if (!l.matched) {
					// l.initCoverage(l.num);
					// // still not matched in this frame!
					// int time = 1; // use last frame for testing
					// if (micros[time] != null) {
					// for (int i = 0; i < micros[time].size(); ++i) {
					// Microtubule micro = micros[time].get(i);
					// for (Line line : micro.lines) {
					// line.initCoverage(line.num);
					// }
					// double deltaLoss = computeTwoWayLoss(micro, l,
					// anotherFrame, false);
					// double percent = Math.abs(deltaLoss / l.num);
					// l.lossList[i] = new Loss(deltaLoss, micro.ID,
					// time);
					// if (deltaLoss < -1.0 && percent > 0.05
					// && l.splits != null) {
					// // anotherFrame.getMicroByID(micro.ID).lines.add(l);
					// l.updateCoverage();
					// micro.updateCoverage();
					// l.matched = true;
					// } else {
					// // still bad fit, erase the fit
					// l.revertCoverage();
					// micro.revertCoverage();
					// }
					// }
					// if (l.matched) {
					// if (l.splits.size() == 1) {
					// // the line did not split; it is just itself
					// int matchID = l.splits.get(0).lossList[0].ID;
					// // l.setContourClass(null);
					// l.isAdded = true;
					// anotherFrame.getMicroByID(matchID).lines
					// .add(l);
					// } else {
					// // more than one line, needs to split
					// for (int i = 0; i < l.splits.size(); ++i) {
					// Line split = l.splits.get(i);
					// int matchID = split.lossList[0].ID;
					// anotherFrame.getMicroByID(matchID).added
					// .add(split);
					// anotherFrame.getMicroByID(matchID).lines
					// .add(split);
					// }
					// // oldLines.add(l);
					// }
					// }
					// Arrays.sort(l.lossList);
					// }
					// }
				}
			}

		}
		// remove all the split lines.
		// contours.removeAll(oldLines);
		return anotherFrame;
	}

	private double computeLoss(Microtubule micro, Line l) {
		Tool tool = new Tool();
		double addLoss = 0;
		for (int it = 0; it < l.num; ++it) {
			// for all points on the line, find its closest points on a
			// microtubule.

			// Step 1: calculate distance based squared loss
			double closest = Double.MAX_VALUE;
			int mappedindex = -1;
			Line mappedLine = null;

			for (int j = 0; j < micro.lines.size(); ++j) {
				for (int k = 0; k < micro.lines.get(j).num; ++k) {
					Line matchLine = micro.lines.get(j);
					float[] linePoint = { l.col.get(it), l.row.get(it) };
					float[] matchPoint = { matchLine.col.get(k),
							matchLine.row.get(k) };
					double dist = tool.dist(linePoint, matchPoint);
					if (dist < 18.0 && dist < closest) {
						// only search in a radius of 25.
						closest = dist;
						mappedindex = k; // record the mapped position
						mappedLine = micro.lines.get(j); // record mapped line
					}
				}
			}
			if (closest == Double.MAX_VALUE) {
				addLoss = Double.MAX_VALUE;
				break;
			} else {
				// use sum squared loss
				++mappedLine.coverHelper[mappedindex];
				addLoss += Math.pow(closest, 2);
			}
		}
		if ((addLoss < Double.MAX_VALUE) && (l.num > 3)) {
			// Step 2: calculate slope based loss
			float[] vecLine = { (l.col.get(l.num - 1) - l.col.get(0)),
					(l.row.get(l.num - 1) - l.row.get(0)) };
			Collections.sort(micro.lines);
			// need to do 3 tests to avoid deleting curvy microtubules
			// test against head-tail, mid-tail, head-mid vectors.
			Line first = micro.lines.get(0);
			Line last = micro.lines.get(micro.lines.size() - 1);
			Line mid = micro.lines.get((micro.lines.size() - 1) / 2);
			float[] ht = calcVector(last, first); // head-tail
			float[] mt = calcVector(mid, last);
			float[] hm = calcVector(first, mid);
			double cosht = Math.abs(calcCos(vecLine, ht));
			double cosmt = Math.abs(calcCos(vecLine, mt));
			double coshm = Math.abs(calcCos(vecLine, hm));
			double cos;
			cos = Math.max(Math.max(cosht, cosmt), coshm);
			// if (micro.lines.size() >= 3) {
			// cos = Math.max(Math.max(cosht, cosmt), coshm);
			// } else {
			// cos = cosht;
			// }
			// if (l.getID() == 3358 && micro.ID==156) {
			// System.out.println("first is " + first.getID() + " mid is " +
			// mid.getID());
			// System.out.println("failed in angle" + "cos is " + cos);
			// System.out.println("ht is " + cosht);
			// System.out.println("mt is " + cosmt);
			// System.out.println("hm is " + coshm);
			// }
			if (Math.abs(cos) < 0.8) {
				// the two microtubules are more than 45 degrees apart
				// if (l.getID() == 2691 && micro.ID == 66) {
				// }
				addLoss += 10 * l.num;
			} else {
				double diff = l.num * (1 - Math.abs(cos));
				// finding the difference in slope, consistent in the
				// scale
				// by multiplying the number of points.
				addLoss = addLoss + 1 * diff;
			}
		}
		return addLoss;
	}

	private double computeTwoWayLoss(Microtubule micro, Line l,
			Microtubules anotherFrame, boolean test) {
		Tool tool = new Tool();
		// Step 1: get the microtubule coverage.
		for (int it = 0; it < l.num; ++it) {
			// for all points on the line, find its closest points on a
			// microtubule.
			double closest = Double.MAX_VALUE;
			int mappedindex = -1;
			Line mappedLine = null;

			for (int j = 0; j < micro.lines.size(); ++j) {
				for (int k = 0; k < micro.lines.get(j).num; ++k) {
					Line matchLine = micro.lines.get(j);
					float[] linePoint = { l.col.get(it), l.row.get(it) };
					float[] matchPoint = { matchLine.col.get(k),
							matchLine.row.get(k) };
					double dist = tool.dist(linePoint, matchPoint);
					if (dist < 15.0 && dist < closest) {
						closest = dist;
						mappedindex = k; // record the mapped position
						mappedLine = micro.lines.get(j); // record mapped line
					}
				}
			}
			if (mappedindex != -1
					&& mappedLine.coverHelper[mappedindex] == mappedLine.covered[mappedindex]) {
				++mappedLine.coverHelper[mappedindex];
			}

		}
		// Step 2: get the line coverage. Find closest points from the
		// microtubule's perspective.
		ArrayList<Integer> splitPoints = new ArrayList<Integer>();
		for (int i = 0; i < micro.lines.size(); ++i) {
			for (int j = 0; j < micro.lines.get(i).num; ++j) {
				double closest = Double.MAX_VALUE;
				int mappedindex = -1;
				for (int it = 0; it < l.num; ++it) {
					Line testLine = micro.lines.get(i);
					float[] linePoint = { l.col.get(it), l.row.get(it) };
					float[] testPoint = { testLine.col.get(j),
							testLine.row.get(j) };
					double dist = tool.dist(linePoint, testPoint);
					if (dist < 15.0 && dist < closest) {
						closest = dist;
						mappedindex = it; // record the mapped position on the
											// line
					}
				}
				if (mappedindex != -1
						&& l.covered[mappedindex] == l.coverHelper[mappedindex]) {
					++l.coverHelper[mappedindex];
					splitPoints.add(mappedindex);
				}
			}
		}
		// Step 3: store the change in loss as if these are matched.
		double oldMicroLoss = alpha * uncoveredLoss(micro, false) + beta
				* overlapLoss(micro, false) + gamma
				* discontinuityLoss(anotherFrame.getMicroByID(micro.ID));
		double oldLineLoss = alpha * uncoveredLoss(l, false) + beta
				* overlapLoss(l, false);
		double oldLoss = oldMicroLoss + oldLineLoss;
		// ignoring discontinuity for right now
		// if (l.getID() == 16962 && micro.ID==0) {
		// System.out.println("with micro: " + micro.ID);
		// System.out.println("before: ");
		// // for (Line li : micro.lines) {
		// // int[] cover = li.covered;
		// // System.out.println(li.getID() + ": ");
		// // for (int i = 0; i < cover.length; ++i) {
		// // System.out.print(cover[i]);
		// // }
		// // System.out.println();
		// // }
		// System.out.println(uncoveredLoss(micro, false));
		// System.out.println(uncoveredLoss(l, false));
		// System.out.println(overlapLoss(micro, false));
		// System.out.println(overlapLoss(l, false));
		// System.out.println(discontinuityLoss(anotherFrame.getMicroByID(micro.ID)));
		// }
		anotherFrame.getMicroByID(micro.ID).lines.add(l);
		double newMicroLoss = alpha * uncoveredLoss(micro, true) + beta
				* overlapLoss(micro, true) + gamma
				* discontinuityLoss(anotherFrame.getMicroByID(micro.ID));
		double newLineLoss = alpha * uncoveredLoss(l, true) + beta
				* overlapLoss(l, true);
		double newLoss = newMicroLoss + newLineLoss;
		anotherFrame.getMicroByID(micro.ID).lines.remove(l);
		// if (l.getID() == 16962 && micro.ID==0) {
		// System.out.println("after: ");
		// // for (Line li : micro.lines) {
		// // int[] cover = li.coverHelper;
		// // System.out.println(li.getID() + ": ");
		// // for (int i = 0; i < cover.length; ++i) {
		// // System.out.print(cover[i]);
		// // }
		// // System.out.println();
		// // }
		// System.out.println(uncoveredLoss(micro, true));
		// System.out.println(uncoveredLoss(l, true));
		// System.out.println(overlapLoss(micro, true));
		// System.out.println(overlapLoss(l, true));
		// System.out.println(discontinuityLoss(anotherFrame.getMicroByID(micro.ID)));
		// }
		// need to make sure that the line is beneficial to the microtubule.
		if (test) {
			// for testing run, dont create split lines. return value only
			if ((newMicroLoss - oldMicroLoss) < 0 && (newLoss - oldLoss) < -1.0
					&& Math.abs((newLoss - oldLoss) / l.num) > 0.05) {
				return newLoss - oldLoss;
			} else {
				return 0;
			}
		} else {
			if ((newMicroLoss - oldMicroLoss) < 0 && (newLoss - oldLoss) < -1.0
					&& splitPoints.size() > 0) {
				if (Math.abs((newLoss - oldLoss) / l.num) > 0.05) {
					Collections.sort(splitPoints);
					// split the line and add a new line to it.
					Line split = new Line(l.getID()); // same ID as the old line
					split.num = splitPoints.size();
					split.isAdded = true;
					for (int i = 0; i < splitPoints.size(); ++i) {
						split.col.add(l.col.get(splitPoints.get(i)));
						split.row.add(l.row.get(splitPoints.get(i)));
					}
					split.lossList = new Loss[anotherFrame.size()];
					for (int i = 0; i < anotherFrame.size(); ++i) {
						split.lossList[i] = new Loss(Double.MAX_VALUE, 0, 0);
					}
					split.lossList[0] = new Loss(newLoss - oldLoss, micro.ID, 0);
					if (l.splits == null) {
						l.splits = new Lines(anotherFrame.getFrame());
					}
					l.splits.add(split);
					// anotherFrame.getMicroByID(micro.ID).added.add(split);
					// anotherFrame.getMicroByID(micro.ID).lines.add(split);
				}
			}
			return newLoss - oldLoss;
		}

	}

	private double uncoveredLoss(Microtubule micro, boolean test) {
		// calculates the loss associated with not covering a microtubule
		// int totallength = 0;
		// for (Line l:micro.lines) {
		// totallength +=l.num;
		// }
		int uncovered = 0;
		for (Line l : micro.lines) {
			int[] cover = l.covered;
			if (test)
				cover = l.coverHelper;
			// for testing purposes, use the helper
			for (int i = 0; i < cover.length; ++i) {
				if (cover[i] == 0) {
					// 0 is for not covered/ not mapped
					++uncovered;
				}
			}
		}

		return uncovered;
	}

	private double uncoveredLoss(Line l, boolean test) {
		int uncovered = 0;
		int[] cover = l.covered;
		if (test) {
			cover = l.coverHelper;
		}
		for (int i = 0; i < l.num; ++i) {
			if (cover[i] == 0) {
				++uncovered;
			}
		}
		return uncovered;
	}

	private double overlapLoss(Microtubule micro, boolean test) {
		// calculate the overlapping loss
		// int totallength = 0;
		// for (Line l:micro.lines) {
		// totallength +=l.num;
		// }
		int overlapped = 0;
		for (Line l : micro.lines) {
			int[] cover = l.covered;
			if (test)
				cover = l.coverHelper;
			for (int i = 0; i < cover.length; ++i) {
				if (cover[i] > 1) {
					// >1 means being mapped to more than once.
					overlapped += cover[i] - 1;
				}
			}
		}
		return overlapped;
	}

	private double overlapLoss(Line l, boolean test) {
		int overlapped = 0;
		int[] cover = l.covered;
		if (test) {
			cover = l.coverHelper;
		}
		for (int i = 0; i < l.num; ++i) {
			if (cover[i] > 1) {
				overlapped += cover[i] - 1;
			}
		}
		return overlapped;
	}

	private double discontinuityLoss(Microtubule micro) {
		// measuring discontinuity using vector difference
		Collections.sort(micro.lines);
		// restore the IDS
		int lim = micro.lines.size() - 1;
		double loss = 0;
		for (int i = 0; i < lim; ++i) {
			if (!micro.lines.get(i).isConnected(micro.lines.get(i + 1))) {
				// interpolate a line between two disconnected
				float[][] closerEnd = Line.findCloserEnds(micro.lines.get(i),
						micro.lines.get(i + 1));
				ArrayList<float[]> points = Line.interpolate_line(closerEnd[0],
						closerEnd[1]);
				Line linked = new Line(0);
				linked.num = points.size();
				if (linked.num > 2) {
					for (int j = 0; j < linked.num; ++j) {
						linked.col.add(points.get(j)[0]);
						linked.row.add(points.get(j)[1]);
					}
					float[] vecLine = {
							(linked.col.get(linked.num - 1) - linked.col.get(0)),
							(linked.row.get(linked.num - 1) - linked.row.get(0)) };
					Line previous = micro.lines.get(i);
					Line next = micro.lines.get(i + 1);
					float[] vecpre = calcVector(previous);
					float[] vecnext = calcVector(next);
					double cospre = calcCos(vecLine, vecpre);
					double cosnext = calcCos(vecLine, vecnext);
					double cos = Math.min(Math.abs(cospre), Math.abs(cosnext));
					double cosLines = calcCos(vecpre, vecnext);
					// also needs to calculate the vector between the two lines
					// of interest
					if (closerEnd[0][0] == previous.col.get(0)
							&& closerEnd[1][0] == next.col.get(0)) {
						cosLines = -cosLines;
					}
					if (closerEnd[0][0] == previous.col.get(previous.num - 1)
							&& closerEnd[1][0] == next.col.get(next.num - 1)) {
						cosLines = -cosLines;
					}
					if (cos <= cosLines) {
						loss += linked.num * (1 - cos);
					} else {
						int penaltyLength = Math.min(previous.num, next.num);
						loss += penaltyLength * (1 - cosLines);
					}
					// normalize these vectors
					// float lineNorm = (float) Math.sqrt(Math.pow(vecLine[0],
					// 2)
					// + Math.pow(vecLine[1], 2));
					// float prevNorm = (float) Math.sqrt(Math.pow(vecpre[0], 2)
					// + Math.pow(vecpre[1], 2));
					// vecLine[0] = vecLine[0] / lineNorm;
					// vecLine[1] = vecLine[1] / lineNorm;
					// if (vecLine[1] <0) {
					// //put in 1st and 2nd quadrant
					// vecLine[0] = -vecLine[0];
					// vecLine[1] = -vecLine[1];
					// }
					// vecpre[0] = vecpre[0] / prevNorm;
					// vecpre[1] = vecpre[1] / prevNorm;
					// if (vecpre[1] <0) {
					// //put in 1st and 2nd quadrant
					// vecpre[0] = -vecpre[0];
					// vecpre[1] = -vecpre[1];
					// }
					// double diff = Math.sqrt(Math.pow(vecLine[0] - vecpre[0],
					// 2)
					// + Math.pow(vecLine[1] - vecpre[1], 2));
					// loss += linked.num*diff;
				}
			} else {
				// if connected, only care about the vector between two lines
				float[][] closerEnd = Line.findCloserEnds(micro.lines.get(i),
						micro.lines.get(i + 1));
				Line previous = micro.lines.get(i);
				Line next = micro.lines.get(i + 1);
				float[] vecpre = calcVector(previous);
				float[] vecnext = calcVector(next);
				double cosLines = calcCos(vecpre, vecnext);
				if (closerEnd[0][0] == previous.col.get(0)
						&& closerEnd[1][0] == next.col.get(0)) {
					cosLines = -cosLines;
				}
				if (closerEnd[0][0] == previous.col.get(previous.num - 1)
						&& closerEnd[1][0] == next.col.get(next.num - 1)) {
					cosLines = -cosLines;
				}
				int penaltyLength = Math.min(previous.num, next.num);
				loss += penaltyLength * (1 - cosLines);
			}
		}
		return loss;
	}

	public int[][] recordDiscontinuity(ArrayList<Microtubules> results) {
		int[] discont = new int[results.get(0).size()];
		int[] bend = new int[results.get(0).size()];
		int[] edge = new int[results.get(0).size()];
		for (Microtubules micros : results) {
			for (Microtubule m : micros) {
				if (m.lines.size() > 0) {
					//checking for micros on the edge 
					Line head = m.lines.get(0);
					Line tail = m.lines.get(m.lines.size() - 1);
					boolean headEdge = head.col.get(0) < 1
							|| head.col.get(0) >= 506
							|| head.col.get(head.num - 1) >= 506
							|| head.col.get(head.num - 1) < 1
							|| head.row.get(0) < 1
							|| head.row.get(head.num - 1) < 1;
					boolean tailEdge = tail.col.get(0) < 1
							|| tail.col.get(0) >= 506
							|| tail.col.get(tail.num - 1) >= 506
							|| tail.col.get(tail.num - 1) < 1
							|| tail.row.get(0) < 1
							|| tail.row.get(tail.num - 1) < 1;
					if (headEdge || tailEdge) {
						edge[micros.getIndexByID(m.ID)]++;
					}
				}
				int lim = m.lines.size() - 1;
				for (int i = 0; i < lim; ++i) {
					if (!m.lines.get(i).isConnected(m.lines.get(i + 1))) {
						// interpolate a line between two disconnected
						float[][] closerEnd = Line.findCloserEnds(
								m.lines.get(i), m.lines.get(i + 1));
						ArrayList<float[]> points = Line.interpolate_line(
								closerEnd[0], closerEnd[1]);
						Line linked = new Line(0);
						linked.num = points.size();
						if (linked.num > 5) {
							// record discontinuity
							if (m.lines.get(i).num > 5
									&& m.lines.get(i + 1).num > 5) {
								discont[micros.getIndexByID(m.ID)]++;
							}
						}
					} else {
						// if connected, only care about the vector between two
						// lines
						float[][] closerEnd = Line.findCloserEnds(
								m.lines.get(i), m.lines.get(i + 1));
						Line previous = m.lines.get(i);
						Line next = m.lines.get(i + 1);
						float[] vecpre = calcVector(previous);
						float[] vecnext = calcVector(next);
						double cosLines = calcCos(vecpre, vecnext);
						if (closerEnd[0][0] == previous.col.get(0)
								&& closerEnd[1][0] == next.col.get(0)) {
							cosLines = -cosLines;
						}
						if (closerEnd[0][0] == previous.col
								.get(previous.num - 1)
								&& closerEnd[1][0] == next.col
										.get(next.num - 1)) {
							cosLines = -cosLines;
						}
						if (cosLines < 0.85) {
							// record bending
							if (m.lines.get(i).num > 5
									&& m.lines.get(i + 1).num > 5) {
								bend[micros.getIndexByID(m.ID)]++;
							}
						}
					}
				}
			}
		}
		int[][] rtn = { discont, bend, edge };
		return rtn;

	}

	// which takes an int-- 0 for end1, 1 for end2.
	public double[][] getEndStats(ArrayList<Microtubules> results, int which) {
		int numMicros = results.get(0).size();
		double[][] rtn = new double[results.size()][numMicros];
		float[] originalEnd = { 0, 0 };
		float[] lastEnd = { 0, 0 };
		float[] endVector = { 0, 0 };
		for (int i = 0; i < numMicros; ++i) {
			// processing the ith micro
			for (int j = 0; j < results.size(); ++j) {
				// at the jth frame
				if (j == 0) {
					Microtubule first = results.get(j).get(i);
					float[][] ends = Line.findFarEnds(first.lines.get(0),
							first.lines.get(first.lines.size() - 1));
					originalEnd = ends[which];
					lastEnd = originalEnd;
					endVector[0] = ends[which][0] - ends[1 - which][0];
					endVector[1] = ends[which][1] - ends[1 - which][1];
					rtn[j][i] = 0.0;
				} else {
					Microtubule next = results.get(j).get(i);
					if (next.lines.size() > 0) {
						// the microtubule could have vanished
						float[][] ends = Line.findFarEnds(next.lines.get(0),
								next.lines.get(next.lines.size() - 1));
						float[] newEnd;
						Tool t = new Tool();
						if (t.dist(lastEnd, ends[0]) < t.dist(lastEnd, ends[1])) {
							// find which end that is
							newEnd = ends[0];
						} else {
							newEnd = ends[1];
						}
						rtn[j][i] = next.getEndDisplacement(newEnd,
								originalEnd, lastEnd, endVector);
						lastEnd = newEnd;
					} else {
						// just copy previous values if micro has vanished.
						rtn[j][i] = rtn[j - 1][i];
					}
				}

			}
		}
		return rtn;
	}

	private float[] calcVector(Line l1, Line l2) {
		// vector between the two extreme points of two lines
		float vecx, vecy;
		float test1 = l1.col.get(l1.num - 1) - l2.col.get(0);
		float test2 = l1.row.get(l1.num - 1) - l2.row.get(0);
		if ((test1 == 0) && (test2 == 0)) {
			vecx = l1.col.get(0) - l2.col.get(l2.num - 1);
			vecy = l1.row.get(0) - l2.row.get(l2.num - 1);
		} else {
			vecx = test1;
			vecy = test2;
		}
		float[] vecMicro = { vecx, vecy };
		return vecMicro;
	}

	private float[] calcVector(Line l) {
		// vector of a line
		float vecx = l.col.get(l.num - 1) - l.col.get(0);
		float vecy = l.row.get(l.num - 1) - l.row.get(0);
		float[] vecMicro = { vecx, vecy };
		return vecMicro;
	}

	private double calcCos(float[] vecLine, float[] vecMicro) {
		if (vecMicro[0] == 0) {
			return 0;
		}
		double normLine = Math.sqrt(Math.pow(vecLine[0], 2)
				+ Math.pow(vecLine[1], 2));
		double normMicro = Math.sqrt(Math.pow(vecMicro[0], 2)
				+ Math.pow(vecMicro[1], 2));
		double dot = vecLine[0] * vecMicro[0] + vecLine[1] * vecMicro[1];
		double cos = dot / (normLine * normMicro);
		return cos;
	}

	public ArrayList<Microtubules> constructMatch(ArrayList<Lines> lines,
			ArrayList<Junctions> juncs, Microtubules firstFrame) {
		for (Lines ls : lines) {
			for (Line l : ls) {
				l.microID.clear();
			}
		}
		ArrayList<Microtubules> results = new ArrayList<Microtubules>();
		if (firstFrame == null) {
			// first frame not supplied, link from scratch.
			Microtubules micros = new Microtubules(lines.get(0).getFrame());
			micros = constructMicroFromLines(lines.get(0), juncs.get(0));
			// ArrayList<Lines> microLines = singleFrameLink(lines.get(0),
			// juncs.get(0));
			// int ID = 0;
			// for (Lines set : microLines) {
			// micros.add(new Microtubule(set, ID++));
			// }
			frameCleanUp(micros, lines.get(0));
			results.add(micros);
			// do single frame link for the first frame
		} else {
			// first frame supplied
			results.add(firstFrame);
		}
		// use a buffer to store the last N frames.
		Microtubules[] buffer = new Microtubules[3];
		buffer[0] = results.get(0);
		// do line matching for the rest
		for (int i = 0; i < lines.size() - 1; ++i) {
			results.add(lineMatching(buffer, lines.get(i + 1), lines.get(i + 1)
					.getFrame()));
			updateBuffer(results.get(results.size() - 1), buffer);
		}
		for (Microtubules ms : results) {
			for (Microtubule m : ms) {
				for (Line l : m.lines) {
					if (!l.microID.contains(m.ID)) {
						l.microID.add(m.ID);
					}
				}
			}
		}
		return results;

	}

	private void updateBuffer(Microtubules newAdded, Microtubules[] buffer) {
		for (int i = 1; i < buffer.length; ++i) {
			buffer[i] = buffer[i - 1];
		}
		buffer[0] = newAdded;
	}

	private int connectBrokenMicros(Microtubule micro, int startID) {
		Collections.sort(micro.lines);
		int ID = startID;
		if (micro.lines.size() > 1) {
			int lim = micro.lines.size() - 1;
			for (int i = 0; i < lim; ++i) {
				if (!micro.lines.get(i).isConnected(micro.lines.get(i + 1))) {
					boolean shortLineArtifact = false;
					if (i == 0) {
						if (micro.lines.get(i).num <= 3) {
							// starting line is a separated short line
							shortLineArtifact = true;
						} else {
							// starting line is not but the next one following
							// is
							if (lim > 1) {
								// more than 2 lines
								// check whether the next line is a separated
								// short line
								shortLineArtifact = (!micro.lines.get(i + 1)
										.isConnected(micro.lines.get(i + 2)))
										&& (micro.lines.get(i + 1).num <= 3);
							} else {
								// only two lines
								shortLineArtifact = micro.lines.get(i + 1).num <= 3;
							}
						}

					} else if (i == lim - 1) {
						if (micro.lines.get(i + 1).num <= 3) {
							// end line is a separated short line
							shortLineArtifact = true;
						} else {
							// check whether the previous line is a separated
							// short line
							shortLineArtifact = (!micro.lines.get(i - 1)
									.isConnected(micro.lines.get(i)))
									&& (micro.lines.get(i).num <= 3);
						}
					} else {
						// in the middle, checking both ways;
						shortLineArtifact = ((!micro.lines.get(i + 1)
								.isConnected(micro.lines.get(i + 2))) && (micro.lines
								.get(i + 1).num <= 3))
								|| ((!micro.lines.get(i - 1).isConnected(
										micro.lines.get(i))) && (micro.lines
										.get(i).num <= 3));
					}
					if (micro.lines.get(i).isAdded
							|| micro.lines.get(i + 1).isAdded) {
						// dont do this for splitted lines.
						shortLineArtifact = true;
					}
					if (!shortLineArtifact) {
						float[][] closerEnd = Line.findCloserEnds(
								micro.lines.get(i), micro.lines.get(i + 1));
						ArrayList<float[]> points = Line.interpolate_line(
								closerEnd[0], closerEnd[1]);
						Line linked = new Line(ID--);
						linked.num = points.size();
						if (linked.num < 50 && linked.num > 2) {
							// do the slope based screening as well.
							for (int j = 0; j < linked.num; ++j) {
								linked.col.add(points.get(j)[0]);
								linked.row.add(points.get(j)[1]);
							}
							float[] vecLine = {
									(linked.col.get(linked.num - 1) - linked.col
											.get(0)),
									(linked.row.get(linked.num - 1) - linked.row
											.get(0)) };
							Line previous = micro.lines.get(i);
							Line next = micro.lines.get(i + 1);
							float[] vecpre = calcVector(previous);
							float[] vecnext = calcVector(next);
							double cospre = calcCos(vecLine, vecpre);
							double cosnext = calcCos(vecLine, vecnext);
							double cos = Math.max(cospre, cosnext);
							double cosLines = calcCos(vecpre, vecnext);
							// also needs to calculate the vector between the
							// two lines of interest
							if (closerEnd[0][0] == previous.col.get(0)
									&& closerEnd[1][0] == next.col.get(0)) {
								cosLines = -cosLines;
							}
							if (closerEnd[0][0] == previous.col
									.get(previous.num - 1)
									&& closerEnd[1][0] == next.col
											.get(next.num - 1)) {
								cosLines = -cosLines;
							}
							if (Math.abs(cos) > 0.8 && cosLines > 0.8) {
								// throw away segments that are 45 degrees away.
								// consider the angles of the linked lines, as
								// well as the original lines
								linked.setContourClass(Line.contour_class.cont_both_junc);
								linked.setFrame(micro.lines.getFrame());
								linked.isAdded = true;
								linked.initCoverage(linked.num);
								linked.connections = new int[2];
								linked.connections[0] = micro.lines.get(i)
										.getID();
								linked.connections[1] = micro.lines.get(i + 1)
										.getID();
								micro.added.add(linked);
								micro.lines.add(linked);
							}
						}
					}
				}
			}
		}
		// micro.lines.addAll(micro.added);
		return ID;
	}

	public void revertToDefaultMicro(Microtubules micros) {
		ArrayList<Line> negs = new ArrayList<Line>();
		for (Microtubule m : micros) {
			m.added.clear(); // remove added lines
			for (Line l : m.lines) {
				if (l.getID() < 0) {
					// negative lines
					negs.add(l);
				}
			}
			m.lines.removeAll(negs);
		}
	}

	public void revertToDefaultLines(Lines ls) {
		ArrayList<Line> rml = new ArrayList<Line>();
		for (Line l : ls) {
			if (l.getID() < 0) {
				rml.add(l); // remove added lines
			}
		}
		ls.removeAll(rml);
	}

	public Microtubules constructMicroFromLines(Lines contours, Junctions juncs) {
		for (Line l : contours) {
			l.microID.clear();
		}
		int ID = 0;
		Microtubules micros = new Microtubules(contours.getFrame());
		ArrayList<Lines> microLines = singleFrameLink(contours, juncs);
		for (Lines set : microLines) {
			for (Line l : set) {
				if (!l.microID.contains(ID)) {
					l.microID.add(ID);
				}
			}
			micros.add(new Microtubule(set, ID++));
		}
		return micros;
	}

}
