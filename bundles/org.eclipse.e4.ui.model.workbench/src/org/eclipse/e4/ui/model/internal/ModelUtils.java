/*******************************************************************************
 * Copyright (c) 2010, 2013 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.e4.ui.model.internal;

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
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.emf.ecore.util.EcoreUtil.UsageCrossReferencer;

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
   * All the possible positioning values which can be used to contribute
   * elements into the wanted place of a list.
   * 
   * @author René Brandstetter
   */
  public static enum Position {
    /** Add an element to the end of a list (absolute positioning). */
    LAST("last"),

    /** Add an element at the beginning of a list (absolute positioning). */
    FIRST("first"),

    /** Add an element before another named element (relative positioning). */
    BEFORE("before:"),

    /** Add an element after a named element (relative positioning). */
    AFTER("after:"),

    /** Add an element at a specific index (absolute positioning). */
    INDEX("index:");

    /** The prefix of the enum which is used in the positioning string. */
    private final String prefix;

    private Position(String prefix) {
      assert prefix != null : "Prefix required!";
      this.prefix = prefix;
    }

    /**
     * Find the {@link Position} enum value used in the given positioning
     * string.
     * 
     * @param positionInfo
     *          the positioning string (can be <code>null</code>, which would
     *          result in <code>null</code>)
     * @return the {@link Position} which is mentioned in the positioning
     *         string, or <code>null</code> if none can be found
     */
    public static final Position find(String positionInfo) {
      if (positionInfo == null || positionInfo.isEmpty())
        return null;

      for (Position position : Position.values()) {
        if (positionInfo.startsWith(position.prefix))
          return position;
      }

      return null;
    }
  }

  /**
   * A holder class for the full information to position an element in a list.
   * 
   * @author René Brandstetter
   */
  public static final class PositionInfo {
    /** The position type to use. */
    private final Position position;

    /**
     * The additional positioning information which can be used to position an
     * element relative to another element.
     */
    private final String positionReference;

    /**
     * The {@link PositionInfo} which represent an insert at the beginning of
     * the list.
     */
    public static final PositionInfo FIRST = new PositionInfo(Position.FIRST, null);

    /**
     * The {@link PositionInfo} which represent an insert at the end of the
     * list.
     */
    public static final PositionInfo LAST = new PositionInfo(Position.LAST, null);

    /**
     * Creates an instance of the PositionInfo.
     * 
     * @param position
     *          the kind of the positioning
     * @param positionReference
     *          additional information which is need to position an element
     *          (e.g.: index, ID of another element)
     * @throws NullPointerException
     *           if the <code>position</code> is <code>null</code>
     */
    public PositionInfo(Position position, String positionReference) {
      if (position == null) {
        throw new NullPointerException("No position given!");
      }

      this.position = position;
      this.positionReference = positionReference;
    }

    /**
     * Returns the kind/type of positioning which should be used.
     * 
     * @return the position
     */
    public Position getPosition() {
      return position;
    }

    /**
     * Returns additional information which is needed to place an element.
     * 
     * @return the positionReference
     */
    public String getPositionReference() {
      return positionReference;
    }

    /**
     * Returns the additional information which is needed to place an element as
     * an int.
     * 
     * @return the positionReference as an int
     * @throws NumberFormatException
     *           if the {@link #positionReference} can't be parsed to an int
     * @throws NullPointerException
     *           if the {@link #positionReference} is <code>null</code>
     */
    public int getPositionReferenceAsInteger() {
      return Integer.parseInt(positionReference);
    }

    /**
     * Creates a {@link PositionInfo} object out of the given positioning
     * string.
     * 
     * <p>
     * <b>Examples for a positioning string:</b>
     * <ul>
     * <li><code>last</code> - place an element to the end of a list</li>
     * <li><code>first</code> - place an element to the beginning of a list</li>
     * <li><code>index:3</code> - place an element at the provided index 3 in a
     * list</li>
     * <li><code>before:org.eclipse.test.id</code> - place an element in a list
     * in front of the element with the ID "org.eclipse.test.id"</li>
     * <li><code>after:org.eclipse.test.id</code> - place an element in a list
     * after the element with the ID "org.eclipse.test.id"</li>
     * </ul>
     * </p>
     * 
     * @param positionInfo
     *          the positioning string
     * @return a {@link PositionInfo} which holds all the data mentioned in the
     *         positioning string, or <code>null</code> if the positioning
     *         string doesn't hold a positioning information
     */
    public static PositionInfo parse(String positionInfo) {
      Position position = Position.find(positionInfo);
      if (position != null) {
        switch (position) {
          case FIRST:
            return PositionInfo.FIRST;

          case LAST:
            return PositionInfo.LAST;

          default:
            return new PositionInfo(position, positionInfo.substring(position.prefix.length()));
        }
      }

      return null;
    }

    @Override
    public String toString() {
      StringBuilder back = new StringBuilder(position.prefix);
      if (positionReference != null) {
        back.append(positionReference);
      }
      return back.toString();
    }
  }
}
