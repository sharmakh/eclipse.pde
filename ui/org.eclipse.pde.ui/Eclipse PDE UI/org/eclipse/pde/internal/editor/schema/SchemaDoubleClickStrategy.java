package org.eclipse.pde.internal.editor.schema;
/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import org.eclipse.jface.text.*;


public class SchemaDoubleClickStrategy implements ITextDoubleClickStrategy {
	protected ITextViewer fText;
	protected int fPos;
	protected int fStartPos;
	protected int fEndPos;
	protected static char[] fgBrackets = { '(', ')', '"', '"' };
   
	public SchemaDoubleClickStrategy() {
		super();
	}
public void doubleClicked(ITextViewer part) {
	fPos = part.getSelectedRange().x;

	if (fPos < 0)
		return;

	fText = part;

	if (!selectComment())
		selectWord();
}
protected boolean matchComment() {
	IDocument doc = fText.getDocument();

	try {
		int pos = fPos;
		char c = ' ';

		while (pos >= 0) {
			c = doc.getChar(pos);
			if (Character.isWhitespace(c) || c == '\"')
				break;
			--pos;
		}

		if (c != '\"')
			return false;

		fStartPos = pos;

		pos = fPos;
		int length = doc.getLength();
		c = ' ';

		while (pos < length) {
			c = doc.getChar(pos);
			if (Character.isWhitespace(c) || c == '\"')
				break;
			++pos;
		}
		if (c != '\"')
			return false;

		fEndPos = pos;

		return true;

	} catch (BadLocationException x) {
	}

	return false;
}
	protected boolean matchWord() {

		IDocument doc = fText.getDocument();

		try {

			int pos = fPos;
			char c;

			while (pos >= 0) {
				c = doc.getChar(pos);
				if (!Character.isJavaIdentifierPart(c))
					break;
				--pos;
			}

			fStartPos = pos;

			pos = fPos;
			int length = doc.getLength();

			while (pos < length) {
				c = doc.getChar(pos);
				if (!Character.isJavaIdentifierPart(c))
					break;
				++pos;
			}

			fEndPos = pos;

			return true;

		} catch (BadLocationException x) {
		}

		return false;
	}
	protected boolean selectComment() {
		if (matchComment()) {
			fText.setSelectedRange(fStartPos + 1, fEndPos);
			return true;
		}
		return false;
	}
	protected void selectWord() {
		if (matchWord())
			fText.setSelectedRange(fStartPos + 1, fEndPos);
	}
}
