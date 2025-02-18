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
package com.ibm.j9ddr.vm29.tools.ddrinteractive.structureformat.base;

import java.io.PrintStream;
import java.math.BigInteger;
import java.util.logging.Level;

import com.ibm.j9ddr.vm29.pointer.AbstractPointer;
import com.ibm.j9ddr.vm29.types.Scalar;

/**
 * Specific formatter to handle U64 which is not natively supported by the java long.
 * @author adam
 *
 */
public class U64ScalarFormatter extends ScalarFormatter {

	public U64ScalarFormatter(int typeCode, Class<? extends AbstractPointer> pointerClass) {
		super(typeCode, pointerClass);
	}

	@Override
	protected void formatShortScalar(Scalar value, PrintStream out) 
	{
		String hex = value.getHexValue();
		out.print(hex);
		out.print(" (");
		try {
			if(hex.toLowerCase().startsWith("0x")) {
				hex = hex.substring(2);
			}
			BigInteger bi = new BigInteger(hex, 16);
			out.print(bi.toString());
		} catch (Exception e) {
			out.print("Error - see log");
			logger.log(Level.FINE, "Error displaying U64 as decimal", e);
		}
		out.print(")");
	}

	
	
}
