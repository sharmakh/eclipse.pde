package org.eclipse.pde.internal.model.build;
/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import org.eclipse.pde.internal.base.model.build.*;
import org.eclipse.pde.internal.base.model.plugin.*;

public class BuildModelFactory implements IBuildModelFactory {
	private IBuildModel model;

public BuildModelFactory(IBuildModel model) {
	this.model = model;
}
public IBuildEntry createEntry(String name) {
	BuildEntry entry = new BuildEntry(name);
	entry.setModel(model);
	return entry;
}
}
