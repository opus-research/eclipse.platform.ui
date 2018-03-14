/*******************************************************************************
 * Copyright (c) 2010, 2013 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     René Brandstetter - Bug 411821 - [QuickAccess] Contribute SearchField
 *                                      through a fragment or other means 
 *     René Brandstetter - Bug 404231 - resetPerspectiveModel() does not reset
 *                         the perspective
 *******************************************************************************/
package org.eclipse.e4.ui.model.internal;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.ui.model.application.MApplicationElement;
import org.eclipse.e4.ui.model.application.ui.MContext;
import org.eclipse.e4.ui.model.application.ui.MUIElement;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EClassifier;
import org.eclipse.emf.ecore.EGenericType;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.emf.ecore.EStructuralFeature.Setting;
import org.eclipse.emf.ecore.ETypeParameter;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.impl.BinaryResourceImpl;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.emf.ecore.util.EcoreUtil.UsageCrossReferencer;
import org.eclipse.emf.ecore.xml.type.internal.DataValue.Base64;

public class ModelUtils {
	//public static final String CONTAINING_CONTEXT = "ModelUtils.containingContext";
	public static final String CONTAINING_PARENT = "ModelUtils.containingParent";
	
	public static EClassifier getTypeArgument(EClass eClass,
			EGenericType eGenericType) {
		ETypeParameter eTypeParameter = eGenericType.getETypeParameter();

		if( eTypeParameter != null ) {
			for (EGenericType eGenericSuperType : eClass.getEAllGenericSuperTypes()) {
				EList<ETypeParameter> eTypeParameters = eGenericSuperType
						.getEClassifier().getETypeParameters();
				int index = eTypeParameters.indexOf(eTypeParameter);
				if (index != -1
						&& eGenericSuperType.getETypeArguments().size() > index) {
					return getTypeArgument(eClass, eGenericSuperType
							.getETypeArguments().get(index));
				}
			}
			return null;
		} else {
			return eGenericType.getEClassifier();
		}
	}

	public static MApplicationElement findElementById(MApplicationElement element, String id) {
		if (id == null || id.length() == 0)
			return null;
		// is it me?
		if (id.equals(element.getElementId()))
			return element;
		// Recurse if this is a container
		EList<EObject> elements = ((EObject) element).eContents();
		for (EObject childElement : elements) {
			if (!(childElement instanceof MApplicationElement))
				continue;
			MApplicationElement result = findElementById((MApplicationElement) childElement, id);
			if (result != null)
				return result;
		}
		return null;
	}
	
	@SuppressWarnings("unchecked")
	public static List<MApplicationElement> merge(MApplicationElement container, EStructuralFeature feature, List<MApplicationElement> elements, String positionInList) {
		EObject eContainer = (EObject) container;
		
		if( feature.isMany() ) {
			List<MApplicationElement> copy = new ArrayList<MApplicationElement>(elements);
			
			List list = (List)eContainer.eGet(feature);
			boolean flag = true;
			if( positionInList != null && positionInList.trim().length() != 0 ) {
				int index = -1;
				
				PositionInfo posInfo = PositionInfo.parse(positionInList);
				
				if( posInfo != null ){
				  switch (posInfo.getPosition()){
				    case FIRST:
				      index = 0;
				      break;
				      
				    case INDEX:
				      index = posInfo.getPositionReferenceAsInteger();
				      break;
				      
				    case BEFORE:
				    case AFTER:
				      int tmpIndex = -1;
				      String elementId = posInfo.getPositionReference();
				      
				      for( int i = 0; i < list.size(); i++ ) {
		            if( elementId.equals(((MApplicationElement)list.get(i)).getElementId()) ) {
		              tmpIndex = i;
		              break;
		            }
		          }
				      
				      if( tmpIndex != -1 ) {
		            if( posInfo.getPosition() == Position.BEFORE ) {
		              index = tmpIndex;
		            } else {
		              index = tmpIndex + 1;
		            }
		          } else {
		            System.err.println("Could not find element with Id '"+elementId+"'");
		          }
				      
				    case LAST:
				      default:
				        // both no special operation, because the default is adding it at the last position
				        break;
				  }
				} else {
					System.err.println("Not a valid list position.");
				}
				
				
				if( index >= 0 && list.size() > index ) {
					flag = false;
					mergeList(list,  elements, index);
				}
			}
			
			// If there was no match append it to the list
			if( flag ) {
				mergeList(list,  elements, -1);
			}
			
			return copy;
		} else {
			if( elements.size() >= 1 ) {
				if( elements.size() > 1 ) {
					//FIXME Pass the logger
					System.err.println("The feature is single valued but a list of values is passed in.");
				}
				MApplicationElement e = elements.get(0);
				eContainer.eSet(feature, e);
				return Collections.singletonList(e);
			}
		}
		
		return Collections.emptyList();
	}
	
	private static void mergeList(List list,  List<MApplicationElement> elements, int index) {
		MApplicationElement[] tmp = new MApplicationElement[elements.size()];
		elements.toArray(tmp);
		for(MApplicationElement element : tmp) {
			String elementID = element.getElementId();
			boolean found = false;
			if ((elementID != null) && (elementID.length() != 0)) {
				for(Object existingObject : list) {
					if (!(existingObject instanceof MApplicationElement))
						continue;
					MApplicationElement existingEObject = (MApplicationElement) existingObject;
					if (!elementID.equals(existingEObject.getElementId()))
						continue;
					if (EcoreUtil.equals((EObject)existingEObject, (EObject)element)) {
						found = true; // skip 
						break;
					} else { // replace
						EObject root = EcoreUtil.getRootContainer((EObject) existingEObject);
						// Replacing the object in the container
						EcoreUtil.replace((EObject)existingEObject, (EObject)element);
						// Replacing the object in other references than the container.
						Collection<Setting> settings = UsageCrossReferencer.find((EObject) existingEObject, root);
						for (Setting setting : settings) {
							setting.set(element);
						}
						found = true; 
					}
				}
			}
			if (!found) {
				if (index == -1)
					list.add(element);
				else
					list.add(index, element);
			}
		}
	}

	static MApplicationElement getParent(MApplicationElement element) {
		if ( (element instanceof MUIElement) && ((MUIElement)element).getCurSharedRef() != null) {
			return ((MUIElement)element).getCurSharedRef().getParent();
		} else if (element.getTransientData().get(CONTAINING_PARENT) instanceof MApplicationElement) {
			return (MApplicationElement) element.getTransientData().get(CONTAINING_PARENT);
		} else if (element instanceof EObject) {
			return (MApplicationElement) ((EObject) element).eContainer();
		}
		return null;
	}
	
	public static IEclipseContext getContainingContext(MApplicationElement element) {
		MApplicationElement curParent = getParent(element);

		while (curParent != null) {
			if (curParent instanceof MContext) {
				return ((MContext) curParent).getContext();
			}
			curParent = getParent(curParent);
		}

		return null;
	}
	
	/**
	 * Helper method which transforms the given {@link MApplicationElement} into a Base64 encoded string.
	 * 
	 * <p>
	 * This Base64 encoded string contains all the persistable data from the {@link MApplicationElement} and
	 * the element can later be restored via {@link #base64StringToModelElement(String)}.
	 * </p>
	 * 
	 * @param element the element to convert to a Base64 encoded string
	 * @return a Base64 encoded string containing the {@link MApplicationElement}, or <code>null</code> if
	 *         the given element is 
	 *         <ul>
	 *           <li><code>null</code> or</li>
	 *           <li>the given element is not an instance of {@link EObject} or</li>
	 *           <li>an error happened during the conversion</li>
	 *         </ul>
	 */
	public static String modelElementToBase64String(MApplicationElement element){
	  if( element instanceof EObject){
	    try {
	      // make a copy of the element so we can put it into a new resource,
	      // without removing it from the original one (which may cause some UIEvents
	      // to be fired)
	      EObject copy = EcoreUtil.copy((EObject) element);
	      
	      // BinaryResource because a XMI or XML one would be to chatty
	      Resource binaryResource = new BinaryResourceImpl();
	      binaryResource.getContents().add(copy);
	      
	      // keep the serialized from in memory
	      ByteArrayOutputStream data = new ByteArrayOutputStream(1024);
	      binaryResource.save(data, null);
	      data.close();  // just for safety
	      
	      String back = Base64.encode(data.toByteArray());
	      return back;
	    } catch (Exception e) {
	      // TODO: There is no Logger in this bundle, once there is one log this error via the Logger,
	      //       or maybe we a RuntimeException should be thrown!
	      e.printStackTrace();
	    }
	  }
	    
	  return null;
	}
	
	/**
	 * Takes a previously, with {@link #modelElementToBase64String(MApplicationElement)}, created Base64 string
	 * and recreates the {@link MApplicationElement} out of it.
	 * 
	 * @param base64encodedModelElement a Base64 representation of a {@link MApplicationElement}
	 * @return the {@link MApplicationElement} which was held in the Base64 string, or <code>null</code> if it couldn't be converted back
	 */
	public static MApplicationElement base64StringToModelElement(String base64encodedModelElement) {
	  if( base64encodedModelElement == null ) return null;
	  
    try {
      ByteArrayInputStream data = new ByteArrayInputStream(Base64.decode(base64encodedModelElement));
      Resource binaryResource = new BinaryResourceImpl();
      binaryResource.load(data, null);
      data.close();
      return (MApplicationElement) binaryResource.getContents().get(0);
    } catch (Exception e) {
      // TODO: There is no Logger in this bundle, once there is one log this error via the Logger,
      //       or maybe we a RuntimeException should be thrown!
      e.printStackTrace();
    }

    return null;
  }

}
