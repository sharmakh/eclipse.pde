package org.eclipse.pde.internal.model;
/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import org.eclipse.pde.internal.base.model.plugin.*;

public class WorkspaceFragmentModel extends WorkspacePluginModelBase implements IFragmentModel {

public WorkspaceFragmentModel() {
	super();
}
public WorkspaceFragmentModel(org.eclipse.core.resources.IFile file) {
	super(file);
}
public IPluginBase createPluginBase() {
	Fragment fragment = new Fragment();
	fragment.setModel(this);
	return fragment;
}
public IFragment getFragment() {
	return (IFragment)getPluginBase();
}
public boolean isFragmentModel() {
	return true;
}
}
