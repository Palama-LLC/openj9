/*******************************************************************************
 * Copyright (c) 2009, 2014 IBM Corp. and others
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
package com.ibm.j9ddr.corereaders.osthread;

/**
 * @author andhall
 *
 */
public class OSStackFrame implements IOSStackFrame
{
	private final long instructionPointer;
	private final long basePointer;
	
	public OSStackFrame(long basePointer, long instructionPointer)
	{
		this.instructionPointer = instructionPointer;
		this.basePointer = basePointer;
	}
	
	public long getInstructionPointer()
	{
		return instructionPointer;
	}

	public long getBasePointer()
	{
		return basePointer;
	}


	public int hashCode()
	{
		final int prime = 31;
		int result = 1;
		result = prime * result + (int) (basePointer ^ (basePointer >>> 32));
		result = prime * result
				+ (int) (instructionPointer ^ (instructionPointer >>> 32));
		return result;
	}

	@Override
	public boolean equals(Object obj)
	{
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (!(obj instanceof OSStackFrame)) {
			return false;
		}
		OSStackFrame other = (OSStackFrame) obj;
		if (basePointer != other.basePointer) {
			return false;
		}
		if (instructionPointer != other.instructionPointer) {
			return false;
		}
		return true;
	}

	public long getStackPointer() {
		return 0;
	}

}
