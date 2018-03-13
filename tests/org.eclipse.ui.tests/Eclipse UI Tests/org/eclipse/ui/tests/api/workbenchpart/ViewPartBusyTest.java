/*******************************************************************************
 * Copyright (c) 2013 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.tests.api.workbenchpart;


import org.eclipse.e4.core.services.events.IEventBroker;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.e4.ui.workbench.UIEvents;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.tests.harness.util.UITestCase;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventHandler;


public class ViewPartBusyTest extends UITestCase {
    private IWorkbenchWindow window;

    private IWorkbenchPage page;

    private EmptyView view;
    
    private Event receivedEvent;
    
    private EventHandler eventHandler = new EventHandler() {
    	public void handleEvent(Event event) {
    		receivedEvent = event;
		}
    };
    
    private IEventBroker eventBroker;

    
    public ViewPartBusyTest(String testName) {
        super(testName);
    }
    
    protected void doSetUp() throws Exception {
        super.doSetUp();
        window = openTestWindow();
        page = window.getActivePage();
        String viewId = "org.eclipse.ui.tests.workbenchpart.EmptyView";
        view = (EmptyView) page.showView(viewId);
 
        eventBroker = (IEventBroker) PlatformUI.getWorkbench().getService(IEventBroker.class);        
        assertNotNull(eventBroker);
        eventBroker.subscribe(UIEvents.UILabel.TOPIC_BUSY, eventHandler);                
    }

    protected void doTearDown() throws Exception {
    	eventBroker.unsubscribe(eventHandler);
    	eventBroker = null;    	
        page.hideView(view);
        super.doTearDown();
    }

    public void testPartIsBusy() throws Throwable {
    	view.showBusy(true);    

    	assertNotNull(receivedEvent);
    	assertEquals(4, receivedEvent.getPropertyNames().length);
    	assertTrue(receivedEvent.getProperty(UIEvents.EventTags.ELEMENT) instanceof MPart);
    	assertEquals(UIEvents.UILifeCycle.BUSY, receivedEvent.getProperty(UIEvents.EventTags.ATTNAME));
    	assertEquals(Boolean.TRUE, receivedEvent.getProperty(UIEvents.EventTags.NEW_VALUE));
    }
    
    public void testPartIsIdle() throws Throwable {
    	view.showBusy(false);    

    	assertNotNull(receivedEvent);
    	assertEquals(4, receivedEvent.getPropertyNames().length);
    	assertTrue(receivedEvent.getProperty(UIEvents.EventTags.ELEMENT) instanceof MPart);
    	assertEquals(UIEvents.UILifeCycle.BUSY, receivedEvent.getProperty(UIEvents.EventTags.ATTNAME));
    	assertEquals(Boolean.FALSE, receivedEvent.getProperty(UIEvents.EventTags.NEW_VALUE));
    }
}
