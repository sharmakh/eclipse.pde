/*******************************************************************************
 * Copyright (c) 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.pde.api.tools.internal.search;

import java.util.Set;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.Flags;
import org.eclipse.jdt.core.ISourceRange;
import org.eclipse.jdt.core.IType;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.Position;
import org.eclipse.pde.api.tools.internal.provisional.ApiPlugin;
import org.eclipse.pde.api.tools.internal.provisional.IApiAnnotations;
import org.eclipse.pde.api.tools.internal.provisional.VisibilityModifiers;
import org.eclipse.pde.api.tools.internal.provisional.descriptors.IElementDescriptor;
import org.eclipse.pde.api.tools.internal.provisional.model.IApiMember;
import org.eclipse.pde.api.tools.internal.provisional.model.IApiType;
import org.eclipse.pde.api.tools.internal.provisional.model.IReference;
import org.eclipse.pde.api.tools.internal.provisional.problems.IApiProblem;

/**
 * Detects leaked types.
 * 
 * @since 1.1
 */
public abstract class AbstractTypeLeakDetector extends AbstractLeakProblemDetector {

	/**
	 * @param nonApiPackageNames
	 */
	public AbstractTypeLeakDetector(Set nonApiPackageNames) {
		super(nonApiPackageNames);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.pde.api.tools.internal.provisional.search.IApiProblemDetector#considerReference(org.eclipse.pde.api.tools.internal.provisional.model.IReference)
	 */
	public boolean considerReference(IReference reference) {
		// consider the reference if the location the reference is made from is visible:
		// i.e. a public or protected class in an API package
		if (isNonAPIReference(reference)) {
			IApiMember member = reference.getMember();
			int modifiers = member.getModifiers();
			if (((Flags.AccPublic | Flags.AccProtected) & modifiers) > 0) {
				try {
					IApiAnnotations annotations = member.getApiComponent().getApiDescription().resolveAnnotations(member.getHandle());
					if (annotations != null) {
						if (isApplicable(annotations) && isEnclosingTypeVisible(member)) {
							retainReference(reference);
							return true;
						}
					} else {
						// TODO: can be null for top level non-public types
						retainReference(reference);
						return true;
					}
				} catch (CoreException e) {
					ApiPlugin.log(e.getStatus());
				}
			}
		}
		return false;
	}
	
	/**
	 * Returns whether all enclosing types of the given member are visible.
	 * 
	 * @param member member
	 * @return whether all enclosing types of the given member are visible
	 * @throws CoreException
	 */
	protected boolean isEnclosingTypeVisible(IApiMember member) throws CoreException {
		IApiType type = member.getEnclosingType();
		while (type != null) {
			if (((Flags.AccPublic | Flags.AccProtected) & type.getModifiers()) == 0) {
				// the type is private or default protection, do not retain the reference
				return false;
			}
			type = type.getEnclosingType();
		}
		return true;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.pde.api.tools.internal.search.AbstractProblemDetector#isProblem(org.eclipse.pde.api.tools.internal.provisional.model.IReference)
	 */
	protected boolean isProblem(IReference reference) {
		IApiMember member = reference.getResolvedReference();
		try {
			IApiAnnotations annotations = member.getApiComponent().getApiDescription().resolveAnnotations(member.getHandle());
			return VisibilityModifiers.isPrivate(annotations.getVisibility());
		} catch (CoreException e) {
			ApiPlugin.log(e);
		}
		return false;
	}

	/**
	 * Returns whether the given annotations should be considered.
	 */
	protected boolean isApplicable(IApiAnnotations annotations) {
		return VisibilityModifiers.isAPI(annotations.getVisibility());
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.pde.api.tools.internal.search.AbstractProblemDetector#getQualifiedMessageArgs(org.eclipse.pde.api.tools.internal.provisional.model.IReference)
	 */
	protected String[] getQualifiedMessageArgs(IReference reference) throws CoreException {
		return new String[] {getTypeName(reference.getResolvedReference()), getTypeName(reference.getMember())};
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.pde.api.tools.internal.search.AbstractProblemDetector#getMessageArgs(org.eclipse.pde.api.tools.internal.provisional.model.IReference)
	 */
	protected String[] getMessageArgs(IReference reference) throws CoreException {
		return new String[] {getSimpleTypeName(reference.getResolvedReference()), getSimpleTypeName(reference.getMember())};
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.pde.api.tools.internal.search.AbstractProblemDetector#getSourceRange(org.eclipse.jdt.core.IType, org.eclipse.jface.text.IDocument, org.eclipse.pde.api.tools.internal.provisional.model.IReference)
	 */
	protected Position getSourceRange(IType type, IDocument doc, IReference reference) throws CoreException {
		ISourceRange range = type.getNameRange();
		return new Position(range.getOffset(), range.getLength());
	}		
	
	/* (non-Javadoc)
	 * @see org.eclipse.pde.api.tools.internal.search.AbstractProblemDetector#getElementType(org.eclipse.pde.api.tools.internal.provisional.model.IReference)
	 */
	protected int getElementType(IReference reference) {
		return IElementDescriptor.T_REFERENCE_TYPE;
	}	
	
	/* (non-Javadoc)
	 * @see org.eclipse.pde.api.tools.internal.search.AbstractProblemDetector#getProblemKind()
	 */
	protected int getProblemKind() {
		return IApiProblem.API_LEAK;
	}	
	
}
