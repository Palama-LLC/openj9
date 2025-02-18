/*******************************************************************************
 * Copyright (c) 2001, 2018 IBM Corp. and others
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
package jit.test.jitt.resolves;

import org.testng.annotations.Test;
import org.testng.Assert;

@Test(groups = { "level.sanity","component.jit" })
public class UnresolvedConstructor extends jit.test.jitt.Test {

	static class Y {
		Y() {
		}
		Y(int i) {
			if (i != 12345)
				Assert.fail("runtime execution error");
			ff++;
		}
		static int ff = 0;
	}

	private void tstFoo(int i) {
		new Y();
		if (i == 1) {
			if (null == new Y(12345)) {
				Assert.fail("could not instantiate Y");
			}
		}
	}

	@Test
	public void testUnresolvedConstructor() {
		Y.ff = 0;
		
		for (int j = 0; j < sJitThreshold; j++) {
			tstFoo(0);
		}
		tstFoo(1);
		tstFoo(1);
		tstFoo(1);
		if (Y.ff != 3)
			Assert.fail("bad execution");
	}
}
