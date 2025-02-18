/*******************************************************************************
 * Copyright (c) 1998, 2014 IBM Corp. and others
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

#include "jni.h"
#include "jcl.h"
#include "jclprots.h"
#include "jcl_internal.h"

void JNICALL
Java_java_lang_System_registerNatives(JNIEnv *env, jclass jlClass)
{
	/*
	 * If left alone the Oracle code would do this:
	 	register(currentTimeMillis()J) -> 0x6FC5C5C0
		register(nanoTime()J) -> 0x6FC5C5BA
		register(arraycopy(Ljava/lang/Object;ILjava/lang/Object;II)V) -> 0x6FC5C5B4
	 */
	PORT_ACCESS_FROM_ENV(env);
	j9tty_printf(PORTLIB,"HACK @ %s.%d: Oracle Java_java_lang_System_registerNatives() stubbed out.", __FILE__, __LINE__);
}
