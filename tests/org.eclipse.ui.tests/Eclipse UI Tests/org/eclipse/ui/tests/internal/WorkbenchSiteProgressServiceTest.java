/*******************************************************************************
 * Copyright (c) 2013 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.ui.tests.internal;

import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.core.services.events.IEventBroker;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.e4.ui.model.internal.ModelUtils;
import org.eclipse.e4.ui.workbench.UIEvents;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.internal.PartSite;
import org.eclipse.ui.internal.progress.WorkbenchSiteProgressService;
import org.eclipse.ui.tests.api.workbenchpart.EmptyView;
import org.eclipse.ui.tests.harness.util.UITestCase;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventHandler;

/**
 * @since 3.5
 *
 */
public class WorkbenchSiteProgressServiceTest extends UITestCase {
    private IWorkbenchWindow window;

    private IWorkbenchPage page;

    private EmptyView view;
    
    private Event receivedEvent;
    
    private EventHandler eventHandler;
    
    private IEventBroker eventBroker;

    private PartSite site;
    
    private WorkbenchSiteProgressServiceTestable progressService;
    
    public WorkbenchSiteProgressServiceTest(String testName) {
        super(testName);
    }
    
    protected void doSetUp() throws Exception {
        super.doSetUp();
        window = openTestWindow();
        page = window.getActivePage();
        String viewId = "org.eclipse.ui.tests.workbenchpart.EmptyView";                                                      
        view = (EmptyView) page.showView(viewId);
       
        assertTrue(page.getActivePart().getSite() instanceof PartSite);
        site = (PartSite) page.getActivePart().getSite();
        
        progressService = new WorkbenchSiteProgressServiceTestable(site);
                                
    	IEclipseContext context = ModelUtils.getContainingContext(site.getModel());
    	assertNotNull(context);
    	
    	eventHandler = new EventHandler() {
        	public void handleEvent(Event event) {
        		receivedEvent = event;

    		}
        };
    	
        eventBroker = context.get(IEventBroker.class);
        eventBroker.subscribe(UIEvents.UILabel.TOPIC_BUSY, eventHandler); 
    }


    protected void doTearDown() throws Exception {
    	eventBroker.unsubscribe(eventHandler);
    	eventBroker = null;    	
        page.hideView(view);
        super.doTearDown();
    }

    public void testShowBusyWhenCurrentlyIdle() throws Exception {
		site.getModel().getTags().remove(UIEvents.UILifeCycle.BUSY); /* state idle */

		progressService.showBusy(true);			
		
		assertTrue(site.getModel().getTags().contains(UIEvents.UILifeCycle.BUSY));		
		assertNotNull(receivedEvent);
		assertBusyEvent(receivedEvent);
		assertEventPropertyEquals(receivedEvent, UIEvents.EventTags.NEW_VALUE, Boolean.TRUE);
	}

	public void testShowBusyWhenCurrentlyIdleAndNextIdleEvent()
			throws Exception {
		site.getModel().getTags().remove(UIEvents.UILifeCycle.BUSY); /* state idle */

		progressService.showBusy(false);
		
		assertFalse(site.getModel().getTags().contains(UIEvents.UILifeCycle.BUSY));
		assertNull(receivedEvent);
	}

	public void testShowBusyWhenCurrentlyBusy() throws Exception {
		site.getModel().getTags().add(UIEvents.UILifeCycle.BUSY); /* state busy */

		progressService.showBusy(false);

		assertFalse(site.getModel().getTags().contains(UIEvents.UILifeCycle.BUSY));
		assertNotNull(receivedEvent);
		assertBusyEvent(receivedEvent);
		assertEventPropertyEquals(receivedEvent, UIEvents.EventTags.NEW_VALUE, Boolean.FALSE);
	}

	public void testShowBusyWhenCurrentlyBusyAndNextBusyEvent()
			throws Exception {
		site.getModel().getTags().add(UIEvents.UILifeCycle.BUSY); /* state busy */

		progressService.showBusy(true);

		assertTrue(site.getModel().getTags().contains(UIEvents.UILifeCycle.BUSY));
		assertNull(receivedEvent);
	}
    
	
	//helper functions
    private static class WorkbenchSiteProgressServiceTestable extends WorkbenchSiteProgressService {
    	/**
		 * @param partSite
		 */
		public WorkbenchSiteProgressServiceTestable(PartSite partSite) {
			super(partSite);
		}

		@Override
    	public void showBusy(boolean busy) {
    		super.showBusy(busy);
    	}
    }
    
    //TODO: Move to the separate class
    private void assertBusyEvent(Event event) {
		assertNotNull(event);
    	assertEquals(4, event.getPropertyNames().length);
    	assertTrue(event.getProperty(UIEvents.EventTags.ELEMENT) instanceof MPart);
    	assertEquals(UIEvents.UILifeCycle.BUSY, event.getProperty(UIEvents.EventTags.ATTNAME));
    	assertNotNull(event.getProperty(UIEvents.EventTags.NEW_VALUE));
    }
    
    private void assertEventPropertyEquals(Event event, String name, Object expectedValue) {
    	Object value = event.getProperty(name);
    	assertNotNull("Expected property not found: " + name, value);
    	assertTrue("Not expected value for property: " + name, value.equals(expectedValue));
    }
}
