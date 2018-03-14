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

	int minSashHorizontal = 20;
	int minSashVertical = 30;

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

	// Option to convert to absolute when the user resizes a relative sash.
	// There will
	// always be 1 relative after the container is normalized, so this may have
	// no effect.
	private boolean absoluteOnResize = false;

	private boolean isValidating;

	boolean layoutUpdateInProgress = false;

	/**
	 * @param host
	 * @param root
	 */
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

					} catch (Exception e2) {
						e2.printStackTrace();
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

	/**
	 * Changes all relative weights so they add up to 100(%).
	 * 
	 */
	protected void normalizeRelativeWeights() {
		isValidating = true;
		double availableRelative;
		List<MUIElement> visibleChildren = SashUtil
				.getVisibleChildren((MGenericTile<?>) root);
		// availableRelative = getAvailableRelative(visibleChildren);
		availableRelative = SashUtil.getTotalWeight(visibleChildren);
		for (MUIElement ele : visibleChildren) {
			SizeInfo i2 = SizeInfo.parse(ele.getContainerData());
			if (!i2.isDefaultAbsolute()) {
				double weight = i2.getDefaultValue() * 100.0
						/ availableRelative;
				i2.setDefaultValue(weight);
				ele.setContainerData(i2.getEncodedParameters());
			}
		}
		isValidating = false;
	}

	@Override
	protected void layout(Composite composite, boolean flushCache) {
		// setting container data in normalizeRelativeWeights() and
		// validateContainerData() will call layout
		// again
		if (root == null || isValidating)
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

			Rectangle leftRect = getRectangle(sr.left);
			Rectangle rightRect = getRectangle(sr.right);

			double totalSize;

			if (leftRect == null || rightRect == null)
				continue;

			SizeInfo infoLeft = SizeInfo.parse(sr.left.getContainerData());
			SizeInfo infoRight = SizeInfo.parse(sr.right.getContainerData());

			double newLeft;
			double newRight;
			int minSize;

			if (sr.container.isHorizontal()) {
				double left = leftRect.x;
				double right = rightRect.x + rightRect.width;
				totalSize = right - left;
				newLeft = curX - leftRect.x;
				minSize = minSashHorizontal;
			} else {
				double top = leftRect.y;
				double bottom = rightRect.y + rightRect.height;
				totalSize = bottom - top;
				newLeft = curY - leftRect.y;
				minSize = minSashVertical;
			}

			Rectangle r = getRectangle(root);
			List<MUIElement> visibleChildren = SashUtil
					.getVisibleChildren(sr.container);
			double availableRelative = SashUtil.getAvailableRelative(
					sr.container.isHorizontal(),
					sr.container.isHorizontal() ? r.width : r.height,
					sashWidth, visibleChildren);
			double totalRelative = SashUtil.getTotalWeight(visibleChildren);

			// constrain to bounds
			double minLeft = infoLeft.getMinValueAsAbsolute(totalRelative,
					availableRelative);
			if (minLeft < minSize) {
				minLeft = minSize;
			}
			double maxLeft = infoLeft.getMaxValueAsAbsolute(totalRelative,
					availableRelative);
			double minRight = infoRight.getMinValueAsAbsolute(totalRelative,
					availableRelative);
			if (minRight < minSize) {
				minRight = minSize;
			}
			double maxRight = infoRight.getMaxValueAsAbsolute(totalRelative,
					availableRelative);

			if (newLeft < minLeft) {
				// return;
				newLeft = minLeft;
			} else if (newLeft > maxLeft) {
				// return;
				newLeft = maxLeft;
			}
			newRight = totalSize - sashWidth - newLeft;
			if (newRight < minRight) {
				// return;
				newRight = minRight;
				newLeft = totalSize - sashWidth - newRight;
			} else if (newRight > maxRight) {
				// return;
				newRight = maxRight;
				newLeft = totalSize - sashWidth - newRight;
			}

			// Convert back to relative if required.
			// Calculate relative value considering all relative space and all
			// relative values
			if (infoLeft.isDefaultAbsolute()) {
				infoLeft.setDefaultValue(newLeft);
			} else {
				if (absoluteOnResize) {
					infoLeft.setDefaultValue(newLeft);
					infoLeft.setDefaultAbsolute(true);
				} else {
					double newWeight = newLeft * totalRelative
							/ (availableRelative);
					infoLeft.setDefaultValue((newWeight));
				}
			}
			sr.left.setContainerData(infoLeft.getEncodedParameters());

			if (infoRight.isDefaultAbsolute()) {
				infoRight.setDefaultValue(newRight);
			} else {
				if (absoluteOnResize) {
					infoRight.setDefaultValue(newRight);
					infoRight.setDefaultAbsolute(true);
				} else {
					double newWeight = newRight * totalRelative
							/ (availableRelative);
					infoRight.setDefaultValue((newWeight));
				}
			}
			sr.right.setContainerData(infoRight.getEncodedParameters());
		}
	}

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

	private void tileSubNodes(Rectangle bounds, MUIElement node) {
		if (node != root)
			setRectangle(node, bounds);

		if (!(node instanceof MGenericTile<?>))
			return;

		MGenericTile<?> sashContainer = (MGenericTile<?>) node;

		isValidating = true;
		SashUtil.validateContainerData(sashContainer);
		isValidating = false;

		boolean isHorizontal = sashContainer.isHorizontal();
		List<MUIElement> visibleChildren = SashUtil
				.getVisibleChildren(sashContainer);

		double totalWeight = SashUtil.getTotalWeight(visibleChildren);
		double availableRelative = SashUtil.getAvailableRelative(isHorizontal,
				isHorizontal ? bounds.width : bounds.height, sashWidth,
				visibleChildren);

		int tilePos = isHorizontal ? bounds.x : bounds.y;
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

			double newSize;

			SizeInfo sizeInfo = SizeInfo.parse(subNode.getContainerData());
			newSize = sizeInfo.getValueConstrained(totalWeight,
					availableRelative);
			sizeInfo.setDefaultValue(newSize);

			double newPx = sizeInfo.getValueAsAbsolute(totalWeight,
					availableRelative);

			int rndSize = (int) (newPx + .5);
			Rectangle subBounds = sashContainer.isHorizontal() ? new Rectangle(
					tilePos, bounds.y, rndSize, bounds.height) : new Rectangle(
					bounds.x, tilePos, bounds.width, rndSize);
			tilePos += rndSize;

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
}
