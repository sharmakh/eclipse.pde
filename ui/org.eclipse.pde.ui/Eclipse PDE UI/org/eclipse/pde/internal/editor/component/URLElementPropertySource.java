package org.eclipse.pde.internal.editor.component;
/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import org.eclipse.core.runtime.*;
import java.net.*;
import org.eclipse.ui.*;
import java.util.*;
import org.eclipse.ui.views.properties.*;
import org.eclipse.jface.viewers.*;
import org.eclipse.swt.graphics.*;
import org.eclipse.pde.internal.*;
import org.eclipse.pde.internal.editor.*;
import org.eclipse.pde.internal.base.model.component.*;

public class URLElementPropertySource extends ComponentPropertySource {
	private Vector descriptors;
	private final static String P_URL = "url";
	public final static String KEY_TYPE = "ComponentEditor.URLProp.type";
	public final static String KEY_LABEL = "ComponentEditor.URLProp.label";
	public final static String KEY_URL = "ComponentEditor.URLProp.URL";
	private final static String P_TYPE = "type";
	private final static String P_LABEL = "label";
	private final static String [] elementTypes = { null, "Update URL", "Discovery URL" };


public URLElementPropertySource(IComponentURLElement element) {
	super(element);
}
public org.eclipse.pde.internal.base.model.component.IComponentURLElement getElement() {
	return (IComponentURLElement)object;
}
public IPropertyDescriptor[] getPropertyDescriptors() {
	if (descriptors == null) {
		descriptors = new Vector();
		PropertyDescriptor desc = new PropertyDescriptor(P_TYPE, PDEPlugin.getResourceString(KEY_TYPE));
		descriptors.addElement(desc);
		desc = createTextPropertyDescriptor(P_LABEL, PDEPlugin.getResourceString(KEY_LABEL));
		descriptors.addElement(desc);
		desc = createTextPropertyDescriptor(P_URL, PDEPlugin.getResourceString(KEY_URL));
		descriptors.addElement(desc);
	}
	return toDescriptorArray(descriptors);
}
public Object getPropertyValue(Object name) {
	if (name.equals(P_TYPE)) {
		return elementTypes[getElement().getElementType()];
	}
	if (name.equals(P_LABEL)) {
		return getElement().getLabel();
	}
	if (name.equals(P_URL)) {
		return getElement().getURL().toString();
	}
	return null;
}
public void setElement(IComponentURLElement newElement) {
	object = newElement;
}
public void setPropertyValue(Object name, Object value) {
	String svalue = value.toString();
	String realValue = svalue == null | svalue.length() == 0 ? null : svalue;
	try {
		if (name.equals(P_URL)) {
			try {
				URL url = null;
				if (realValue != null)
					url = new URL(realValue);
				getElement().setURL(url);
			} catch (MalformedURLException e) {
			}
		} else
			if (name.equals(P_LABEL)) {
				getElement().setLabel(realValue);
			}
	} catch (CoreException e) {
		PDEPlugin.logException(e);
	}
}
}
