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
package org.openj9.test.floatsanity.functions;

import java.util.ArrayList;

import org.openj9.test.floatsanity.*;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.testng.log4testng.Logger;

@Test(groups = { "level.sanity" })
public class CheckDoubleMinMaxFunctions {

	public static Logger logger = Logger.getLogger(CheckDoubleMinMaxFunctions.class);

	@BeforeClass
	public void groupName() {
		logger.debug("Check double min/max functions");
	}

	double[] ordered = {
		D.NINF, D.NMAX, D.n1000, D.n200, D.n100, D.n2_2, D.nTwo, D.n1_1, D.nOne,
		D.n0_01, D.n0_005, D.n0_0001, 
		D.nC1, D.NMIN, D.PZERO, D.PMIN, D.pC1, 
		D.p0_0001, D.p0_005, D.p0_01,
		D.pOne, D.p1_1, D.pTwo, D.p2_2, D.p100, D.p200, D.p1000, D.PMAX, D.PINF
	};

	public void min_double_double() {
		ArrayList<double[]> tests = new ArrayList<>();

		for (int i = 0; i < ordered.length - 1; i++) {
			tests.add(new double[] { ordered[i], ordered[i + 1], ordered[i] });
			tests.add(new double[] { ordered[i + 1], ordered[i], ordered[i] });
		}
		for (int i = 0; i < ordered.length; i++) {
			tests.add(new double[] { ordered[i], D.NAN, D.NAN });
			tests.add(new double[] { D.NAN, ordered[i], D.NAN });
			tests.add(new double[] { ordered[i], ordered[i], ordered[i] });
		}
		tests.add(new double[] { D.NAN, D.NAN, D.NAN });

		tests.add(new double[] { D.NZERO, D.PZERO, D.NZERO });
		tests.add(new double[] { D.PZERO, D.NZERO, D.NZERO });
		
		for (double[] test : tests) {
			String operation = "testing function: double Math.min( " + test[0] + " , " + test[1] + " ) == " + test[2];
			logger.debug(operation);
			double result = Math.min(test[0], test[1]);
			if (Double.isNaN(test[2])) {
				Assert.assertTrue(Double.isNaN(result), operation);
			} else {
				Assert.assertEquals(result, test[2], 0, operation);
			}
		}
	}

	public void max_double_double() {
		ArrayList<double[]> tests = new ArrayList<>();

		for (int i = 0; i < ordered.length - 1; i++) {
			tests.add(new double[] { ordered[i], ordered[i + 1], ordered[i + 1] });
			tests.add(new double[] { ordered[i + 1], ordered[i], ordered[i + 1] });
		}
		for (int i = 0; i < ordered.length; i++) {
			tests.add(new double[] { ordered[i], D.NAN, D.NAN });
			tests.add(new double[] { D.NAN, ordered[i], D.NAN });
			tests.add(new double[] { ordered[i], ordered[i], ordered[i] });
		}
		tests.add(new double[] { D.NAN, D.NAN, D.NAN });

		tests.add(new double[] { D.NZERO, D.PZERO, D.PZERO });
		tests.add(new double[] { D.PZERO, D.NZERO, D.PZERO });

		for (double[] test : tests) {
			String operation = "testing function: double Math.max( " + test[0] + " , " + test[1] + " ) == " + test[2];
			logger.debug(operation);
			double result = Math.max(test[0], test[1]);
			if (Double.isNaN(test[2])) {
				Assert.assertTrue(Double.isNaN(result), operation);
			} else {
				Assert.assertEquals(result, test[2], 0, operation);
			}
		}
	}
}
