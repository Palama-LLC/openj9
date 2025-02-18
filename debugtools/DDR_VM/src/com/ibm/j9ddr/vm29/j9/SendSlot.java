/*******************************************************************************
 * Copyright (c) 2009, 2023 IBM Corp. and others
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
package com.ibm.j9ddr.vm29.j9;

import com.ibm.j9ddr.CorruptDataException;
import com.ibm.j9ddr.vm29.pointer.generated.J9UTF8Pointer;
import com.ibm.j9ddr.vm29.pointer.helper.J9ClassHelper;
import com.ibm.j9ddr.vm29.pointer.helper.J9UTF8Helper;
import com.ibm.j9ddr.vm29.types.UDATA;

/**
 * Analogue of util/sendslot.c util/sendslot.asm
 * 
 * @author andhall
 *
 */
public class SendSlot
{
	public static UDATA getSendSlotsFromSignature(J9UTF8Pointer signature) throws CorruptDataException
	{
		UDATA sendArgs = new UDATA(0);
		int i = 1; /* 1 to skip the opening '(' */
	 
		for (; ; i++) {
			switch (J9UTF8Helper.stringValue(signature).charAt(i)) {
			case ')':
				return sendArgs;
			case '[':
				/* skip all '['s */
				for (i++; J9UTF8Helper.stringValue(signature).charAt(i) == '['; i++);
				char charAti = J9UTF8Helper.stringValue(signature).charAt(i);
				if (J9ClassHelper.isRefOrValSignature(charAti)) {
					/* FALL THRU */
				} else {
					sendArgs = sendArgs.add(1);
					break;
				}
			case 'L':
			/*[IF INLINE-TYPES]*/
			case 'Q':
			/*[ENDIF] INLINE-TYPES */
				for (i++; J9UTF8Helper.stringValue(signature).charAt(i) != ';'; i++);
				sendArgs = sendArgs.add(1);
				break;
			case 'D':
			case 'J':
				sendArgs = sendArgs.add(2);
				break;
			default:
				/* any other primitive type */
				sendArgs = sendArgs.add(1);
				break;
			}
		}
	}
}
