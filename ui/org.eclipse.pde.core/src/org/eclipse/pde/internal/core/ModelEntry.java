package org.eclipse.pde.internal.core;

import java.io.File;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.*;
import org.eclipse.core.runtime.PlatformObject;
import org.eclipse.jdt.core.*;
import org.eclipse.jdt.core.IClasspathContainer;
import org.eclipse.pde.core.plugin.*;
import org.eclipse.pde.core.plugin.IPluginModelBase;

public class ModelEntry extends PlatformObject {
	public static final int AUTOMATIC = 0;
	public static final int WORKSPACE = 1;
	public static final int EXTERNAL = 2;
	private String id;
	private IPluginModelBase workspaceModel;
	private IPluginModelBase externalModel;
	private int mode = AUTOMATIC;
	private RequiredPluginsClasspathContainer classpathContainer;
	private boolean inJavaSearch = false;
	private PluginModelManager manager;

	public ModelEntry(PluginModelManager manager, String id) {
		this.manager = manager;
		this.id = id;
	}

	public IPluginModelBase getActiveModel() {
		if (mode == AUTOMATIC) {
			if (workspaceModel != null)
				return workspaceModel;
			return externalModel;
		} else
			return (mode == WORKSPACE) ? workspaceModel : externalModel;
	}

	public String getId() {
		return id;
	}

	public Object[] getChildren() {
		if (workspaceModel == null && externalModel != null) {
			String location = externalModel.getInstallLocation();
			File file = new File(location);
			FileAdapter adapter =
				new EntryFileAdapter(
					this,
					file,
					manager.getFileAdapterFactory());
			return adapter.getChildren();
		}
		return new Object[0];
	}

	public boolean isInJavaSearch() {
		return inJavaSearch;
	}

	void setInJavaSearch(boolean value) {
		this.inJavaSearch = value;
	}

	public void setWorkspaceModel(IPluginModelBase model) {
		this.workspaceModel = model;
		classpathContainer = null;
	}

	public void setExternalModel(IPluginModelBase model) {
		this.externalModel = model;
		classpathContainer = null;
	}
	public IPluginModelBase getWorkspaceModel() {
		return workspaceModel;
	}

	public IPluginModelBase getExternalModel() {
		return externalModel;
	}
	public boolean isEmpty() {
		return workspaceModel == null && externalModel == null;
	}
	public RequiredPluginsClasspathContainer getClasspathContainer() {
		if (classpathContainer == null)
			classpathContainer =
				new RequiredPluginsClasspathContainer(workspaceModel);
		return classpathContainer;
	}
	public void updateClasspathContainer(boolean force) throws CoreException {
		if (workspaceModel == null)
			return;
		if (force)
			classpathContainer = null;
		RequiredPluginsClasspathContainer container = getClasspathContainer();
		container.reset();
		IProject project = workspaceModel.getUnderlyingResource().getProject();
		IJavaProject[] javaProjects =
			new IJavaProject[] { JavaCore.create(project)};
		IClasspathContainer[] containers =
			new IClasspathContainer[] { container };
		IPath path = new Path(PDECore.CLASSPATH_CONTAINER_ID);
		JavaCore.setClasspathContainer(path, javaProjects, containers, null);
	}

	public static void updateUnknownClasspathContainer(IJavaProject javaProject)
		throws CoreException {
		IPath path = new Path(PDECore.CLASSPATH_CONTAINER_ID);
		JavaCore.setClasspathContainer(
			path,
			new IJavaProject[] { javaProject },
			new IClasspathContainer[] {
				 new RequiredPluginsClasspathContainer(null)},
			null);
	}

	public boolean isAffected(IPluginBase[] changedPlugins) {
		if (workspaceModel == null)
			return false;
		IPluginBase plugin = workspaceModel.getPluginBase();
		for (int i = 0; i < changedPlugins.length; i++) {
			IPluginBase changedPlugin = changedPlugins[i];
			String id = changedPlugin.getId();
			if (plugin.getId().equals(id))
				return true;
			if (isRequired(plugin, changedPlugin))
				return true;
		}
		return false;
	}
	private boolean isRequired(IPluginBase plugin, IPluginBase changedPlugin) {
		String changedId = changedPlugin.getId();

		if (changedId.equalsIgnoreCase("org.eclipse.core.boot")
			|| changedId.equalsIgnoreCase("org.eclipse.core.runtime"))
			return true;
		IPluginImport[] imports = plugin.getImports();
		for (int i = 0; i < imports.length; i++) {
			IPluginImport iimport = imports[i];
			if (iimport.getId().equals(changedPlugin.getId()))
				return true;
		}
		return false;
	}
}