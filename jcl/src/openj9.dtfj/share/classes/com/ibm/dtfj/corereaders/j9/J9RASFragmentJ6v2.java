/*[INCLUDE-IF Sidecar18-SE]*/
/*******************************************************************************
 * Copyright (c) 1991, 2017 IBM Corp. and others
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
package com.ibm.dtfj.corereaders.j9;

import com.ibm.dtfj.corereaders.MemoryAccessException;

/**
 * Represents the J9RAS structure starting from Java 6 SR09 which contains the DDR blob pointer
 * Java 6 32 bit : length =  0x248, version = 0x20000		TID+PID+DDR
 * Java 6 64 bit : length =  0x280, version = 0x20000		TID+PID+DDR
 * 
 * @author Adam Pilkington
 *
 */
public class J9RASFragmentJ6v2 implements J9RASFragment {
	private long tid = 0;
	private long pid = 0;
	
	/**
	 * Create a Java 6, version 2 J9RAS fragment
	 * @param memory address space
	 * @param address address of the J9RAS structure
	 * @param is64bit true if a 64 bit core file
	 * @throws MemoryAccessException rethrow memory exceptions
	 */
	public J9RASFragmentJ6v2(Memory memory, long address, boolean is64bit) throws MemoryAccessException {
		if(is64bit) {
			pid = memory.getLongAt(address + 0x270);
			tid = memory.getLongAt(address + 0x278);
		} else {
			pid = memory.getIntAt(address + 0x240);
			tid = memory.getIntAt(address + 0x244);
		}
	}

	public boolean currentThreadSupported() {
		return true;
	}

	public long getPID() {
		return pid;
	}

	public long getTID() {
		return tid;
	}

}
