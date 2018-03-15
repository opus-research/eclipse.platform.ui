package org.eclipse.ui.internal.views.markers;

import java.util.LinkedList;

import org.eclipse.ui.views.markers.MarkerItem;

/**
 * @since 3.4
 *
 */
class MarkerProject extends MarkerSupportItem {
	private LinkedList<Integer> indexes;

	private MarkerEntry[] children;

	private String name;

	private String category;

	private MarkerEntry[] markers;

	/**
	 * Create a new instance of the receiver that has the markers between
	 * startIndex and endIndex showing.
	 *
	 * @param projectName
	 * @param markers
	 * @param categoryName
	 * @param markerEntryIndexes
	 */
	public MarkerProject(String projectName, MarkerEntry[] markers, String categoryName,
			LinkedList<Integer> markerEntryIndexes) {
		this.markers = markers;
		name = projectName;
		category = categoryName;
		indexes = markerEntryIndexes;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see
	 * org.eclipse.ui.internal.views.markers.MarkerSupportItem#getChildren()
	 */
	@Override
	MarkerSupportItem[] getChildren() {
		// considering that markers is sorted by category, for the time being,
		// lets
		// get the children by going through the category and finding ones that
		// match
		// the project name.
		if (children == null) {
			MarkerItem[] allMarkers = markers;
			int totalSize = indexes.size();
			for (int i = 0; i < totalSize; i++) {
				children[i] = (MarkerEntry) allMarkers[indexes.get(i)];
			}
		}
		return children;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see
	 * org.eclipse.ui.internal.views.markers.MarkerSupportItem#getDescription()
	 */
	@Override
	int getChildrenCount() {
		return 0;
	}

	@Override
	String getDescription() {
		// Project marker should not have any description, as it is used only
		// for indexing
		return null;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.eclipse.ui.internal.views.markers.MarkerSupportItem#getParent()
	 */
	@Override
	MarkerCategory getParent() {
		// TODO this will return the parent which is the the category it is
		// listed as
		// Possibilities: pick a marker entry, find its category, and print that
		// other possibility: actually do a getParent function

		return null;
	}

	String getProjectName() {
		return name;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.eclipse.ui.internal.views.markers.MarkerSupportItem#isConcrete()
	 */
	@Override
	boolean isConcrete() {
		// TODO Auto-generated method stub
		return false;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.eclipse.ui.internal.views.markers.MarkerSupportItem#clearCache()
	 */
	@Override
	void clearCache() {
		// TODO Auto-generated method stub

	}

}
