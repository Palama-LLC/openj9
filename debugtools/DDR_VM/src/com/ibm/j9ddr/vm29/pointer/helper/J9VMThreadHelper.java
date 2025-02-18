/*******************************************************************************
 * Copyright (c) 1991, 2014 IBM Corp. and others
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
package com.ibm.j9ddr.vm29.pointer.helper;

import com.ibm.j9ddr.CorruptDataException;
import com.ibm.j9ddr.vm29.j9.J9VMThreadPointerUtil;
import com.ibm.j9ddr.vm29.pointer.generated.J9VMThreadPointer;

public class J9VMThreadHelper
{
	
	public static int getDTFJState(J9VMThreadPointer thread) throws CorruptDataException
	{
		return J9VMThreadPointerUtil.getDTFJState(thread);
	}
	
	public static String getName(J9VMThreadPointer thread)
	{
		String threadName = "<unnamed thread>";
		
		try {
			if (thread.omrVMThread().threadName().notNull()) {
				threadName = String.format("\"%s\"", thread.omrVMThread().threadName().getCStringAtOffset(0));
			}
		} catch (CorruptDataException e) {
			threadName = "<FAULT>";
		}
		
		return threadName;
	}
}
