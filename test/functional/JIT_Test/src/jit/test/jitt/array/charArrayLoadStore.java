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
package jit.test.jitt.array;

import org.testng.annotations.Test;
import org.testng.log4testng.Logger;
import org.testng.Assert;

@Test(groups = { "level.sanity","component.jit" })
public class charArrayLoadStore extends jit.test.jitt.Test {
private static Logger logger = Logger.getLogger(charArrayLoadStore.class);
static char initArry[]= new char[6];
private boolean tstCheck(char arrayP[]){
int len = arrayP.length;
for (int i=0;i<len;i++) {;
  if (arrayP[i] != initArry[i]){
     logger.error("\ncheck Failed at index " + i + " leftValue =" + (int)arrayP[i] + " rightValue =" + (int)initArry[i]);
     return false;
    } 
 }
return true;
}

private char[] tstFoo(char[] arrayP,int zero) {
    int len = arrayP.length;
	char arryLocal[] = new char [len];
	for (int i=0;i<len;i++) {;
	  arryLocal[i] = arrayP[i]; 
	  }
	 if (!tstCheck(arryLocal)){
	  Assert.fail("char Array value bad in tstFoo");
	 }
	return arryLocal;
}

@Test
public void testcharArrayLoadStore() {
int zero = 0;
initArry[0] = 'A';
initArry[1] = (char) 0;
initArry[2] = (char) 1;
initArry[3] = (char) 65535;

		charArrayLoadStore x = new charArrayLoadStore();
		char ArryReturn[];    
		for (int j = 0; j < sJitThreshold+1; j++) {
			ArryReturn = x.tstFoo(initArry,0);
			if (!tstCheck(ArryReturn)){
	         Assert.fail("char Array value bad in interpreted");
			}
	    }
}

}
