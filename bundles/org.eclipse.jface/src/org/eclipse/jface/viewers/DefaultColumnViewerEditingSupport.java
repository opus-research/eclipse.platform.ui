package org.eclipse.jface.viewers;

/**
 * This class is only used to support the "old" cell editor API before Eclipse
 * 3.3. New clients should should attach their own {@link EditingSupport} to
 * {@link ViewerColumn} objects themselves.
 * 
 * 
 * @noextend This class is not intended to be subclassed by clients.
 * @noinstantiate This class is not intended to be instantiated by clients.
 *
 * @see ViewerColumn#setEditingSupport(EditingSupport)
 *      {@link ViewerColumn#setEditingSupport(EditingSupport)}for a more
 *      flexible way of editing values in a column viewer.
 */
public class DefaultColumnViewerEditingSupport extends EditingSupport {

	private int columnIndex;

	/**
	 * @param viewer
	 *            {@link ColumnViewer}
	 * @param columnIndex
	 *            index of the target column
	 */
	public DefaultColumnViewerEditingSupport(ColumnViewer viewer, int columnIndex) {
		super(viewer);
		this.columnIndex = columnIndex;
	}

	@Override
	public boolean canEdit(Object element) {
		Object[] properties = getViewer().getColumnProperties();

		if (columnIndex < properties.length) {
			return getViewer().getCellModifier().canModify(element,
					(String) getViewer().getColumnProperties()[columnIndex]);
		}

		return false;
	}

	@Override
	public CellEditor getCellEditor(Object element) {
		CellEditor[] editors = getViewer().getCellEditors();
		if (columnIndex < editors.length) {
			return getViewer().getCellEditors()[columnIndex];
		}
		return null;
	}

	@Override
	public Object getValue(Object element) {
		Object[] properties = getViewer().getColumnProperties();

		if (columnIndex < properties.length) {
			return getViewer().getCellModifier().getValue(element,
					(String) getViewer().getColumnProperties()[columnIndex]);
		}

		return null;
	}

	@Override
	public void setValue(Object element, Object value) {
		Object[] properties = getViewer().getColumnProperties();

		if (columnIndex < properties.length) {
			getViewer().getCellModifier().modify(getViewer().findItem(element),
					(String) getViewer().getColumnProperties()[columnIndex],
					value);
		}
	}

	@Override
	boolean isLegacySupport() {
		return true;
	}

}
