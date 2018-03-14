/*******************************************************************************
 * Copyright (c) 2013 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Steven Spungin <steven@spungin.tv> - Bug 361731
 *******************************************************************************/
package org.eclipse.e4.ui.workbench.renderers.swt;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.eclipse.e4.ui.model.application.ui.MGenericTile;
import org.eclipse.e4.ui.model.application.ui.MUIElement;
import org.eclipse.e4.ui.workbench.IPresentationEngine;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.MouseMoveListener;
import org.eclipse.swt.events.MouseTrackListener;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Layout;
import org.eclipse.swt.widgets.Shell;

public class SashLayout extends Layout {
	// The minimum value (as a percentage) that a sash can be dragged to
	int minSashPercent = 10;

	int marginLeft = 0;
	int marginRight = 0;
	int marginTop = 0;
	int marginBottom = 0;
	int sashWidth = 4;

	MUIElement root;
	private Composite host;

	class SashRect {
		Rectangle rect;
		MGenericTile<?> container;
		MUIElement left;
		MUIElement right;

		public SashRect(Rectangle rect, MGenericTile<?> container,
				MUIElement left, MUIElement right) {
			this.container = container;
			this.rect = rect;
			this.left = left;
			this.right = right;
		}
	}

	List<SashRect> sashes = new ArrayList<SashRect>();

	boolean draggingSashes = false;
	List<SashRect> sashesToDrag;

	public boolean layoutUpdateInProgress = false;

	public SashLayout(final Composite host, MUIElement root) {
		this.root = root;
		this.host = host;

		host.addMouseTrackListener(new MouseTrackListener() {
			@Override
			public void mouseHover(MouseEvent e) {
			}

			@Override
			public void mouseExit(MouseEvent e) {
				host.setCursor(null);
			}

			@Override
			public void mouseEnter(MouseEvent e) {
			}
		});

		host.addMouseMoveListener(new MouseMoveListener() {
			@Override
			public void mouseMove(final MouseEvent e) {
				if (!draggingSashes) {
					// Set the cursor feedback
					List<SashRect> sashList = getSashRects(e.x, e.y);
					if (sashList.size() == 0) {
						host.setCursor(host.getDisplay().getSystemCursor(
								SWT.CURSOR_ARROW));
					} else if (sashList.size() == 1) {
						if (sashList.get(0).container.isHorizontal())
							host.setCursor(host.getDisplay().getSystemCursor(
									SWT.CURSOR_SIZEWE));
						else
							host.setCursor(host.getDisplay().getSystemCursor(
									SWT.CURSOR_SIZENS));
					} else {
						host.setCursor(host.getDisplay().getSystemCursor(
								SWT.CURSOR_SIZEALL));
					}
				} else {
					try {
						layoutUpdateInProgress = true;
						adjustWeights(sashesToDrag, e.x, e.y);
						host.layout();
						host.update();
					} finally {
						layoutUpdateInProgress = false;
					}
				}
			}
		});

		host.addMouseListener(new MouseListener() {
			@Override
			public void mouseUp(MouseEvent e) {
				host.setCapture(false);
				draggingSashes = false;
			}

			@Override
			public void mouseDown(MouseEvent e) {
				if (e.button != 1) {
					return;
				}

				sashesToDrag = getSashRects(e.x, e.y);
				if (sashesToDrag.size() > 0) {
					draggingSashes = true;
					host.setCapture(true);
				}
			}

			@Override
			public void mouseDoubleClick(MouseEvent e) {
			}
		});

		host.addPaintListener(new PaintListener() {
			@Override
			public void paintControl(PaintEvent e) {
				// for (SashRect sr : sashes) {
				// Color color;
				// if (sr.container.isHorizontal())
				// color = e.display.getSystemColor(SWT.COLOR_MAGENTA);
				// else
				// color = e.display.getSystemColor(SWT.COLOR_CYAN);
				// e.gc.setForeground(color);
				// e.gc.setBackground(color);
				// e.gc.fillRectangle(sr.rect);
				// }
			}
		});
	}

	@Override
	protected void layout(Composite composite, boolean flushCache) {
		if (root == null)
			return;

		Rectangle bounds = composite.getBounds();
		if (composite instanceof Shell)
			bounds = ((Shell) composite).getClientArea();
		else {
			bounds.x = 0;
			bounds.y = 0;
		}

		bounds.width -= (marginLeft + marginRight);
		bounds.height -= (marginTop + marginBottom);
		bounds.x += marginLeft;
		bounds.y += marginTop;

		sashes.clear();
		tileSubNodes(bounds, root);
	}

	protected void adjustWeights(List<SashRect> sashes, int curX, int curY) {
		for (SashRect sr : sashes) {

			boolean leftFixed = sr.left.getContainerData().endsWith("px"); //$NON-NLS-1$
			boolean rightFixed = sr.right.getContainerData().endsWith("px"); //$NON-NLS-1$
			// convert to relative
			int leftWeight;
			int rightWeight;
			if (!leftFixed && !rightFixed) {
				leftWeight = getWeight(sr.left);
				rightWeight = getWeight(sr.right);
			} else if (leftFixed && rightFixed) {
				int leftSize = getFixed(sr.left);
				int rightSize = getFixed(sr.right);
				int total = leftSize + rightSize;
				if (total != 0) {
					leftWeight = leftSize * 100 / total;
				} else {
					leftWeight = 50;
				}
				rightWeight = 100 - leftWeight;
			} else if (leftFixed) {
				rightWeight = getWeight(sr.right);
				leftWeight = 100 - rightWeight;
			} else {
				leftWeight = getWeight(sr.left);
				rightWeight = 100 - leftWeight;

			}
			int totalWeight = leftWeight + rightWeight;
			int minSashValue = (int) (((totalWeight / 100.0) * minSashPercent) + 0.5);

			Rectangle leftRect = getRectangle(sr.left);
			Rectangle rightRect = getRectangle(sr.right);
			if (leftRect == null || rightRect == null)
				continue;

			double totalSize;

			if (sr.container.isHorizontal()) {
				double left = leftRect.x;
				double right = rightRect.x + rightRect.width;
				totalSize = right - left;
				double pct = (curX - left) / (totalSize);
				leftWeight = (int) ((totalWeight * pct) + 0.5);
				if (leftWeight < minSashValue)
					leftWeight = minSashValue;
				if (leftWeight > (totalWeight - minSashValue))
					leftWeight = totalWeight - minSashValue;
				rightWeight = totalWeight - leftWeight;
			} else {
				double top = leftRect.y;
				double bottom = rightRect.y + rightRect.height;
				totalSize = bottom - top;
				double pct = (curY - top) / (totalSize);
				leftWeight = (int) ((totalWeight * pct) + 0.5);
				if (leftWeight < minSashValue)
					leftWeight = minSashValue;
				if (leftWeight > (totalWeight - minSashValue))
					leftWeight = totalWeight - minSashValue;
				rightWeight = totalWeight - leftWeight;
			}

			if (leftFixed) {
				setWeight(sr.left,
						((int) (leftWeight * totalSize / totalWeight + 0.5))
								+ "px"); //$NON-NLS-1$
			} else {
				setWeight(sr.left, leftWeight + "%"); //$NON-NLS-1$
			}
			if (rightFixed) {
				setWeight(sr.right, ((int) (rightWeight * totalSize
						/ totalWeight + 0.5))
						+ "px"); //$NON-NLS-1$
			} else
				setWeight(sr.right, rightWeight + "%"); //$NON-NLS-1$
		}
	}

	private void setWeight(MUIElement element, String weight) {
		element.setContainerData(weight);
	}

	/**
	 * @param weight
	 * @return
	 */
	public static Pattern patternAny = Pattern.compile("(\\d+)\\s*(px|%)?"); //$NON-NLS-1$

	// private String checkWeight(String weight) {
	// if (patternPct.matcher(weight).matches()) {
	// return weight;
	// } else if (patternPct.matcher(weight).matches()) {
	// return weight;
	// } else if (patternDefault.matcher(weight).matches()) {
	//			return weight + "%"; //$NON-NLS-1$
	// } else {
	//			return "5000%"; //$NON-NLS-1$
	// }
	// }

	private Rectangle getRectangle(MUIElement element) {
		if (element.getWidget() instanceof Rectangle)
			return (Rectangle) element.getWidget();
		else if (element.getWidget() instanceof Control)
			return ((Control) (element.getWidget())).getBounds();
		return null;
	}

	protected List<SashRect> getSashRects(int x, int y) {
		List<SashRect> srs = new ArrayList<SashRect>();
		Rectangle target = new Rectangle(x - 5, y - 5, 10, 10);
		for (SashRect sr : sashes) {
			if (!sr.container.getTags().contains(IPresentationEngine.NO_MOVE)
					&& sr.rect.intersects(target))
				srs.add(sr);
		}
		return srs;
	}

	@Override
	protected Point computeSize(Composite composite, int wHint, int hHint,
			boolean flushCache) {
		return new Point(600, 400);
	}

	private int totalWeight(MGenericTile<?> node) {
		int total = 0;
		for (MUIElement subNode : node.getChildren()) {
			if (subNode.isToBeRendered() && subNode.isVisible())
				total += getWeight(subNode);
		}
		return total;
	}

	private int totalFixed(MGenericTile<?> node) {
		int total = 0;
		for (MUIElement subNode : node.getChildren()) {
			if (subNode.isToBeRendered() && subNode.isVisible())
				total += getFixed(subNode);
		}
		return total;
	}

	private void tileSubNodes(Rectangle bounds, MUIElement node) {
		if (node != root)
			setRectangle(node, bounds);

		if (!(node instanceof MGenericTile<?>))
			return;

		MGenericTile<?> sashContainer = (MGenericTile<?>) node;
		List<MUIElement> visibleChildren = getVisibleChildren(sashContainer);
		int childCount = visibleChildren.size();

		// How many pixels do we have?
		int availableWidth = sashContainer.isHorizontal() ? bounds.width
				: bounds.height;

		// Subtract off the room for the sashes
		availableWidth -= ((childCount - 1) * sashWidth);

		// Subtract off the fixed width
		double totalFixed = totalFixed(sashContainer);
		availableWidth -= totalFixed;

		// Get the total of the weights
		double totalWeight = totalWeight(sashContainer);
		int tilePos = sashContainer.isHorizontal() ? bounds.x : bounds.y;
		MUIElement prev = null;
		for (MUIElement subNode : visibleChildren) {
			// Add a 'sash' between this node and the 'prev'
			if (prev != null) {
				Rectangle sashRect = sashContainer.isHorizontal() ? new Rectangle(
						tilePos, bounds.y, sashWidth, bounds.height)
						: new Rectangle(bounds.x, tilePos, bounds.width,
								sashWidth);
				sashes.add(new SashRect(sashRect, sashContainer, prev, subNode));
				host.redraw(sashRect.x, sashRect.y, sashRect.width,
						sashRect.height, false);
				tilePos += sashWidth;
			}

			int newSize;
			Matcher matcher = patternAny.matcher(subNode.getContainerData());
			if (matcher.matches() && "px".equals(matcher.group(2))) { //$NON-NLS-1$
				newSize = Integer.parseInt(matcher.group(1));
			} else {
				// Calc the new size as a %'age of the total
				double ratio = getWeight(subNode) / totalWeight;
				newSize = (int) ((availableWidth * ratio) + 0.5);
			}

			Rectangle subBounds = sashContainer.isHorizontal() ? new Rectangle(
					tilePos, bounds.y, newSize, bounds.height) : new Rectangle(
					bounds.x, tilePos, bounds.width, newSize);
			tilePos += newSize;

			tileSubNodes(subBounds, subNode);
			prev = subNode;
		}
	}

	/**
	 * @param node
	 * @param bounds
	 */
	private void setRectangle(MUIElement node, Rectangle bounds) {
		if (node.getWidget() instanceof Control) {
			Control ctrl = (Control) node.getWidget();
			ctrl.setBounds(bounds);
		} else if (node.getWidget() instanceof Rectangle) {
			Rectangle theRect = (Rectangle) node.getWidget();
			theRect.x = bounds.x;
			theRect.y = bounds.y;
			theRect.width = bounds.width;
			theRect.height = bounds.height;
		}
	}

	private List<MUIElement> getVisibleChildren(MGenericTile<?> sashContainer) {
		List<MUIElement> visKids = new ArrayList<MUIElement>();
		for (MUIElement child : sashContainer.getChildren()) {
			if (child.isToBeRendered() && child.isVisible())
				visKids.add(child);
		}
		return visKids;
	}

	private static int getWeight(MUIElement element) {
		String info = element.getContainerData();
		if (info == null || info.length() == 0) {
			return 0;
		}
		Matcher matcher = patternAny.matcher(info);
		if (matcher.matches()) {
			if ("px".equals(matcher.group(2))) { //$NON-NLS-1$
				return 0;
			}
			try {
				int value = Integer.parseInt(matcher.group(1));
				return value;
			} catch (NumberFormatException e) {
				return 0;
			}
		} else {
			return 0;
		}
	}

	private static int getFixed(MUIElement element) {
		String info = element.getContainerData();
		if (info == null || info.length() == 0) {
			return 0;
		}
		Matcher matcher = patternAny.matcher(info);
		if (matcher.matches()) {
			if (!"px".equals(matcher.group(2))) { //$NON-NLS-1$
				return 0;
			}
			try {
				int value = Integer.parseInt(matcher.group(1));
				return value;
			} catch (NumberFormatException e) {
				return 0;
			}
		} else {
			return 0;
		}
	}
}
