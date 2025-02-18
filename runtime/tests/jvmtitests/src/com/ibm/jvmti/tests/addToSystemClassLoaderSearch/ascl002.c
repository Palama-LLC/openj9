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
#include "ibmjvmti.h"
#include "jvmti_test.h"

static agentEnv * _agentEnv;

jint JNICALL
ascl002(agentEnv * agent_env, char * args)
{
	JVMTI_ACCESS_FROM_AGENT(agent_env);
	char * jar = agent_env->testArgs;                         

	if (!ensureVersion(agent_env, JVMTI_VERSION_1_1)) {
		return JNI_ERR;
	}   
       
	_agentEnv = agent_env;

	if (jar == NULL) {
		error(agent_env, JVMTI_ERROR_NONE, "Must specify jar name in args");
		return JNI_ERR;
	}

	return JNI_OK;
}

jboolean JNICALL
Java_com_ibm_jvmti_tests_addToSystemClassLoaderSearch_ascl002_addJar(JNIEnv *jni_env, jclass clazz)
{
	JVMTI_ACCESS_FROM_AGENT(_agentEnv);
	jvmtiError err;       
	char * jar = _agentEnv->testArgs;                         

	err = (*jvmti_env)->AddToSystemClassLoaderSearch(jvmti_env, jar);
	if (err != JVMTI_ERROR_NONE) {
		error(_agentEnv, err, "Failed to add \"%s\" to the classpath", jar);
		return JNI_FALSE;
	}						

	return JNI_TRUE;
}
