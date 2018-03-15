package org.eclipse.ui.tests.internal;

import static org.eclipse.ui.SaveablesLifecycleEvent.POST_CLOSE;
import static org.eclipse.ui.SaveablesLifecycleEvent.POST_OPEN;

import java.util.Collection;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IPropertyListener;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchPartSite;
import org.eclipse.ui.Saveable;
import org.eclipse.ui.SaveablesLifecycleEvent;
import org.eclipse.ui.internal.SaveablesList;
import org.eclipse.ui.tests.harness.util.UITestCase;

/**
 * @since 3.5
 */
public class SaveablesListTest extends UITestCase {

	static class GoodSaveable extends Saveable {

		Object source;

		public GoodSaveable(Object source) {
			this.source = source;
		}

		@Override
		public boolean isDirty() {
			return false;
		}

		@Override
		public int hashCode() {
			return 42;
		}

		@Override
		public String getToolTipText() {
			return null;
		}

		@Override
		public String getName() {
			return null;
		}

		@Override
		public ImageDescriptor getImageDescriptor() {
			return null;
		}

		@Override
		public boolean equals(Object object) {
			if (object == this) {
				return true;
			}
			if (!(object instanceof GoodSaveable)) {
				return false;
			}
			GoodSaveable other = (GoodSaveable) object;
			return Objects.equals(source, other.source);
		}

		@Override
		public void doSave(IProgressMonitor monitor) {
		}
	}

	class BadSaveable extends GoodSaveable {

		public BadSaveable(Object source) {
			super(source);
		}

		void dispose() {
			source = null;
		}

	}

	static class DummyPart implements IWorkbenchPart {

		/**
		 * @param source
		 */
		public DummyPart(Object source) {
		}

		@Override
		public <T> T getAdapter(Class<T> adapter) {
			return null;
		}

		@Override
		public void addPropertyListener(IPropertyListener listener) {
		}

		@Override
		public void createPartControl(Composite parent) {
		}

		@Override
		public void dispose() {
		}

		@Override
		public IWorkbenchPartSite getSite() {
			return null;
		}

		@Override
		public String getTitle() {
			return null;
		}

		@Override
		public Image getTitleImage() {
			return null;
		}

		@Override
		public String getTitleToolTip() {
			return null;
		}

		@Override
		public void removePropertyListener(IPropertyListener listener) {
		}

		@Override
		public void setFocus() {
		}

	}

	static class SaveablesListForTest extends SaveablesList {
		@Override
		protected Map<Saveable, Collection<Saveable>> getEqualKeys() {
			return super.getEqualKeys();
		}

		@Override
		protected Map<Object, Set<Saveable>> getModelMap() {
			return super.getModelMap();
		}

		@Override
		protected Map<Saveable, Integer> getModelRefCounts() {
			return super.getModelRefCounts();
		}
	}

	private SaveablesListForTest slist;
	private BadSaveable badSaveable;
	private GoodSaveable goodSaveable;
	private Object source;
	private DummyPart part1;
	private DummyPart part2;

	public SaveablesListTest(String testName) {
		super(testName);
	}
	@Override
	protected void doSetUp() throws Exception {
		super.doSetUp();
		slist = new SaveablesListForTest();
		source = new Object();
		part1 = new DummyPart(source);
		part2 = new DummyPart(source);
		badSaveable = new BadSaveable(source);
		goodSaveable = new GoodSaveable(source);

		emulateOpenPart(badSaveable, part1);
		emulateOpenPart(goodSaveable, part2);
	}

	public void testNotBrokenSaveables() throws Exception {
		Map<Object, Set<Saveable>> modelMap = slist.getModelMap();
		assertEquals(2, modelMap.size());
		assertEquals(2, modelMap.values().size());

		Map<Saveable, Integer> modelRefCounts = slist.getModelRefCounts();
		assertEquals(1, modelRefCounts.keySet().size());
		assertSame(badSaveable, modelRefCounts.keySet().iterator().next());
		assertEquals(Integer.valueOf(2), modelRefCounts.get(badSaveable));
		assertEquals(Integer.valueOf(2), modelRefCounts.get(goodSaveable));

		Map<Saveable, Collection<Saveable>> equalKeys = slist.getEqualKeys();
		assertEquals(2, equalKeys.keySet().size());
		assertTrue(equalKeys.containsKey(badSaveable));
		assertTrue(equalKeys.containsKey(goodSaveable));

		Collection<Saveable> equalSaveables = equalKeys.get(badSaveable);
		assertEquals(2, equalSaveables.size());
		assertTrue(equalSaveables.stream().anyMatch(x -> x == badSaveable));
		assertTrue(equalSaveables.stream().anyMatch(x -> x == goodSaveable));
		assertSame(equalSaveables, equalKeys.get(goodSaveable));

		emulateClosePart(badSaveable, part1);

		assertEquals(1, modelMap.size());
		assertEquals(1, modelMap.values().size());

		assertEquals(1, modelRefCounts.keySet().size());
		assertSame(goodSaveable, modelRefCounts.keySet().iterator().next());
		assertEquals(Integer.valueOf(1), modelRefCounts.get(goodSaveable));

		assertEquals(1, equalKeys.keySet().size());
		assertTrue(equalKeys.containsKey(goodSaveable));

		assertEquals(1, equalSaveables.size());
		assertTrue(equalSaveables.stream().anyMatch(x -> x == goodSaveable));

		emulateClosePart(goodSaveable, part2);

		assertEquals(0, modelMap.size());
		assertEquals(0, modelMap.values().size());

		assertEquals(0, modelRefCounts.size());
		assertEquals(0, equalKeys.size());
	}

	public void testBrokenSaveables1() throws Exception {
		Map<Object, Set<Saveable>> modelMap = slist.getModelMap();
		Map<Saveable, Integer> modelRefCounts = slist.getModelRefCounts();
		Map<Saveable, Collection<Saveable>> equalKeys = slist.getEqualKeys();
		Collection<Saveable> equalSaveables = equalKeys.get(badSaveable);

		badSaveable.dispose();

		emulateClosePart(badSaveable, part1);

		assertEquals(1, modelMap.size());
		assertEquals(1, modelMap.values().size());

		assertEquals(1, modelRefCounts.keySet().size());
		assertSame(goodSaveable, modelRefCounts.keySet().iterator().next());
		assertEquals(Integer.valueOf(1), modelRefCounts.get(goodSaveable));
		assertNull(modelRefCounts.get(badSaveable));

		assertEquals(1, equalKeys.keySet().size());
		assertTrue(equalKeys.containsKey(goodSaveable));

		assertEquals(1, equalSaveables.size());
		assertTrue(equalSaveables.stream().anyMatch(x -> x == goodSaveable));

		emulateClosePart(goodSaveable, part2);

		assertEquals(0, modelMap.size());
		assertEquals(0, modelMap.values().size());

		assertEquals(0, modelRefCounts.size());
		assertEquals(0, equalKeys.size());
	}

	public void testBrokenSaveables2() throws Exception {
		Map<Object, Set<Saveable>> modelMap = slist.getModelMap();
		Map<Saveable, Integer> modelRefCounts = slist.getModelRefCounts();
		Map<Saveable, Collection<Saveable>> equalKeys = slist.getEqualKeys();
		Collection<Saveable> equalSaveables = equalKeys.get(badSaveable);

		badSaveable.dispose();

		emulateClosePart(goodSaveable, part2);

		assertEquals(1, modelMap.size());
		assertEquals(1, modelMap.values().size());

		assertEquals(1, modelRefCounts.keySet().size());
		assertSame(badSaveable, modelRefCounts.keySet().iterator().next());
		assertEquals(Integer.valueOf(1), modelRefCounts.get(badSaveable));
		assertNull(modelRefCounts.get(goodSaveable));

		assertEquals(1, equalKeys.keySet().size());
		assertTrue(equalKeys.containsKey(badSaveable));

		assertEquals(1, equalSaveables.size());
		assertTrue(equalSaveables.stream().anyMatch(x -> x == badSaveable));

		emulateClosePart(badSaveable, part1);

		assertEquals(0, modelMap.size());
		assertEquals(0, modelMap.values().size());

		assertEquals(0, modelRefCounts.size());
		assertEquals(0, equalKeys.size());
	}

	void emulateOpenPart(Saveable saveable, IWorkbenchPart part) {
		Saveable[] saveables = new Saveable[] { saveable };
		SaveablesLifecycleEvent event = new SaveablesLifecycleEvent(part, POST_OPEN, saveables, false);
		slist.handleLifecycleEvent(event);
	}

	void emulateClosePart(Saveable saveable, IWorkbenchPart part) {
		Saveable[] saveables = new Saveable[] { saveable };
		SaveablesLifecycleEvent event = new SaveablesLifecycleEvent(part, POST_CLOSE, saveables, false);
		slist.handleLifecycleEvent(event);
	}
}
