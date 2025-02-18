/*******************************************************************************
 * Copyright (c) 2009, 2022 IBM Corp. and others
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
package com.ibm.j9ddr.corereaders.memory;

/**
 * Contains static helpers for working with addresses as long.
 *
 * Encapsulates 64-bit unsigned comparisons using 64-bit signed longs.
 *
 * @see http://www.javamex.com/java_equivalents/unsigned_arithmetic.shtml
 *
 * @author andhall
 */
public class Addresses {

	/**
	 * Compare two addresses, returning true if the first is greater than the second.
	 *
	 * @param a
	 * @param b
	 * @return true if a > b
	 */
	public static boolean greaterThan(long a, long b) {
		return Long.compareUnsigned(a, b) > 0;
	}

	/**
	 * Compare two addresses, returning true if the first is greater than or equal to the second.
	 *
	 * @param a
	 * @param b
	 * @return true if a >= b
	 */
	public static boolean greaterThanOrEqual(long a, long b) {
		return Long.compareUnsigned(a, b) >= 0;
	}

	/**
	 * Compare two addresses, returning true if the first is less than the second.
	 *
	 * @param a
	 * @param b
	 * @return True if a is < b
	 */
	public static boolean lessThan(long a, long b) {
		return Long.compareUnsigned(a, b) < 0;
	}

	/**
	 * Compare two addresses, returning true if the first is less than or equal to the second.
	 *
	 * @param a
	 * @param b
	 * @return true if a is <= b
	 */
	public static boolean lessThanOrEqual(long a, long b) {
		return Long.compareUnsigned(a, b) <= 0;
	}

}
