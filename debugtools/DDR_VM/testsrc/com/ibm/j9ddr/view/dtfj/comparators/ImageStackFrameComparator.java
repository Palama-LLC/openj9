/*******************************************************************************
 * Copyright (c) 2001, 2014 IBM Corp. and others
 *
 * This program and the accompanying materials are made available under
 * the terms of the Eclipse Public License 2.0 which accompanies this
 * distribution and is available at https://www.eclipse.org/legal/epl-2.0/
 * or the Apache License, Version 2.0 which accompanies this distribution and
 * is available at https://www.apache.org/licenses/LICENSE-2.0.
 *
 * This Source Code may also be made available under the following
 * Secondary Licenses when the conditions for such availability set
 * forth in the Eclipse Public License, v. 2.0 are satisfied: GNU
 * General Public License, version 2 with the GNU Classpath
 * Exception [1] and GNU General Public License, version 2 with the
 * OpenJDK Assembly Exception [2].
 *
 * [1] https://www.gnu.org/software/classpath/license.html
 * [2] https://openjdk.org/legal/assembly-exception.html
 *
 * SPDX-License-Identifier: EPL-2.0 OR Apache-2.0 OR GPL-2.0 WITH Classpath-exception-2.0 OR LicenseRef-GPL-2.0 WITH Assembly-exception
 *******************************************************************************/
package com.ibm.j9ddr.view.dtfj.comparators;

import com.ibm.dtfj.image.ImageStackFrame;
import com.ibm.j9ddr.view.dtfj.test.DTFJComparator;

public class ImageStackFrameComparator extends DTFJComparator {

	public static final int BASE_POINTER = 1;
	public static final int PROCEDURE_ADDRESS = 2;
	public static final int PROCEDURE_NAME = 4;
	
	// getBasePointer()
	// getProcedureAddress()
	// getProcedureName()
	public void testEquals(Object ddrObject, Object jextractObject, int members) {
		ImageStackFrame ddrImageStackFrame = (ImageStackFrame) ddrObject;
		ImageStackFrame jextractStackFrame = (ImageStackFrame) jextractObject;
		
		ImagePointerComparator imagePointerComparator = new ImagePointerComparator();
		
		// getBasePointer()
		if ((BASE_POINTER & members) != 0)
			imagePointerComparator.testComparatorEquals(ddrImageStackFrame, jextractStackFrame, "getBasePointer");
		
		// getProcedureAddress()
		if ((PROCEDURE_ADDRESS & members) != 0)
			imagePointerComparator.testComparatorEquals(ddrImageStackFrame, jextractStackFrame, "getProcedureAddress");
		
		// getProcedureName()
		if ((PROCEDURE_NAME & members) != 0)
			testJavaEquals(ddrImageStackFrame, jextractStackFrame, "getProcedureName");
	}

	@Override
	public int getDefaultMask()
	{
		return BASE_POINTER | PROCEDURE_ADDRESS;
	}

}
