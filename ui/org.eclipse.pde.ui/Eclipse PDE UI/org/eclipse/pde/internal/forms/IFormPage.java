package org.eclipse.pde.internal.forms;
/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import org.eclipse.swt.widgets.*;

public interface IFormPage {

boolean becomesInvisible(IFormPage newPage);
void becomesVisible(IFormPage previousPage);
void createControl(Composite parent);
Control getControl();
String getLabel();
String getTitle();
boolean isSource();
boolean isVisible();
}
