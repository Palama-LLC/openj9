/*******************************************************************************
 * Copyright (c) 1991, 2023 IBM Corp. and others
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

#include "GCExtensions.hpp"

#include "hookable_api.h"
#include "j9cfg.h"
#include "j2sever.h"
#include "j9memcategories.h"
#include "j9nongenerated.h"
#include "j9port.h"
#include "ModronAssertions.h"
#include "util_api.h"

#include "EnvironmentBase.hpp"
#include "Forge.hpp"
#if defined(OMR_GC_IDLE_HEAP_MANAGER)
 #include  "IdleGCManager.hpp"
#endif /* defined(OMR_GC_IDLE_HEAP_MANAGER) */
#include "MemorySpace.hpp"
#include "MemorySubSpace.hpp"
#include "StandardAccessBarrier.hpp"
#include "ObjectModel.hpp"
#include "ReferenceChainWalkerMarkMap.hpp"
#include "SublistPool.hpp"
#include "Wildcard.hpp"
#include "VMHelpers.hpp"

MM_GCExtensions *
MM_GCExtensions::newInstance(MM_EnvironmentBase *env)
{
	PORT_ACCESS_FROM_ENVIRONMENT(env);
	MM_GCExtensions *extensions;
	
	/* Avoid using MM_Forge to allocate memory for the extension, since MM_Forge itself has not been created! */
	extensions = static_cast<MM_GCExtensions*>(j9mem_allocate_memory(sizeof(MM_GCExtensions), OMRMEM_CATEGORY_MM));
	if (extensions) {
		/* Initialize all the fields to zero */
		memset((void *)extensions, 0, sizeof(*extensions));

		new(extensions) MM_GCExtensions();
		if (!extensions->initialize(env)) {
			extensions->kill(env);
			return NULL;
		}
	}
	return extensions;
}

void
MM_GCExtensions::kill(MM_EnvironmentBase *env)
{
	/* Avoid using MM_Forge to free memory for the extension, since MM_Forge was not used to allocate the memory */
	PORT_ACCESS_FROM_ENVIRONMENT(env);
	tearDown(env);
	j9mem_free_memory(this);
}

/**
 * Initialize the global GC extensions structure.
 * Clear all values within the extensions structure and call the appropriate initialization routines
 * on all substructures.  Upon completion of this call, the extensions structure is ready for use.
 *
 * @return true if the initialization was successful, false otherwise.
 */
bool
MM_GCExtensions::initialize(MM_EnvironmentBase *env)
{
	PORT_ACCESS_FROM_ENVIRONMENT(env);

	if (!MM_GCExtensionsBase::initialize(env)) {
		goto failed;
	}

#if defined(J9VM_GC_REALTIME)
	/* only ref slots, size in bytes: 2 * minObjectSize - header size */
	minArraySizeToSetAsScanned = 2 * (1 << J9VMGC_SIZECLASSES_LOG_SMALLEST) - J9JAVAVM_CONTIGUOUS_HEADER_SIZE(getJavaVM());
#endif /* J9VM_GC_REALTIME */

#if defined(J9VM_GC_JNI_ARRAY_CACHE)
	getJavaVM()->jniArrayCacheMaxSize = J9_GC_JNI_ARRAY_CACHE_SIZE;
#endif /* J9VM_GC_JNI_ARRAY_CACHE */

#if defined(J9VM_GC_THREAD_LOCAL_HEAP)
	getJavaVM()->gcInfo.tlhThreshold = J9_GC_TLH_THRESHOLD;
	getJavaVM()->gcInfo.tlhSize = J9_GC_TLH_SIZE;
#endif /* J9VM_GC_THREAD_LOCAL_HEAP */

	/* if tuned for virtualized environment, we compromise a bit of performance for lower footprint */
	if (getJavaVM()->runtimeFlags & J9_RUNTIME_TUNE_VIRTUALIZED) {
		heapFreeMinimumRatioMultiplier = 20;
	}

	padToPageSize = J9_ARE_ALL_BITS_SET(getJavaVM()->runtimeFlags, J9_RUNTIME_AGGRESSIVE);

	if (J9HookInitializeInterface(getHookInterface(), OMRPORT_FROM_J9PORT(PORTLIB), sizeof(hookInterface))) {
		goto failed;
	}
	
	initializeReferenceArrayCopyTable(&referenceArrayCopyTable);
	
	{
		J9InternalVMFunctions const * const vmFuncs = getJavaVM()->internalVMFunctions;
		_asyncCallbackKey = vmFuncs->J9RegisterAsyncEvent(getJavaVM(), memoryManagerAsyncCallbackHandler, getJavaVM());
		_TLHAsyncCallbackKey = vmFuncs->J9RegisterAsyncEvent(getJavaVM(), memoryManagerTLHAsyncCallbackHandler, getJavaVM());
		if ((_asyncCallbackKey < 0) || (_TLHAsyncCallbackKey < 0)) {
			goto failed;
		}
	}

#if defined(OMR_GC_IDLE_HEAP_MANAGER)
	/* absorbs GC specific idle tuning flags */
	if (J9_IDLE_TUNING_GC_ON_IDLE == (getJavaVM()->vmRuntimeStateListener.idleTuningFlags & J9_IDLE_TUNING_GC_ON_IDLE)) {
		gcOnIdle = true;
	}
	if (J9_IDLE_TUNING_COMPACT_ON_IDLE == (getJavaVM()->vmRuntimeStateListener.idleTuningFlags & J9_IDLE_TUNING_COMPACT_ON_IDLE)) {
		compactOnIdle = true;
	}
	decommitMinimumFree = getJavaVM()->vmRuntimeStateListener.idleMinFreeHeap;
#endif /* if defined(OMR_GC_IDLE_HEAP_MANAGER) */

	return true;

failed:
	tearDown(env);
	return false;
}

/**
 * Tear down the global GC extensions structure and all sub structures.
 */
void
MM_GCExtensions::tearDown(MM_EnvironmentBase *env)
{
	J9InternalVMFunctions const * const vmFuncs = getJavaVM()->internalVMFunctions;
	vmFuncs->J9UnregisterAsyncEvent(getJavaVM(), _TLHAsyncCallbackKey);
	_TLHAsyncCallbackKey = -1;
	vmFuncs->J9UnregisterAsyncEvent(getJavaVM(), _asyncCallbackKey);
	_asyncCallbackKey = -1;

#if defined(J9VM_GC_MODRON_TRACE) && !defined(J9VM_GC_REALTIME)
	tgcTearDownExtensions(getJavaVM());
#endif /* J9VM_GC_MODRON_TRACE && !defined(J9VM_GC_REALTIME) */

	MM_Wildcard *wildcard = numaCommonThreadClassNamePatterns;
	while (NULL != wildcard) {
		MM_Wildcard *nextWildcard = wildcard->_next;
		wildcard->kill(this);
		wildcard = nextWildcard;
	}
	numaCommonThreadClassNamePatterns = NULL;
	
	J9HookInterface** tmpHookInterface = getHookInterface();
	if((NULL != tmpHookInterface) && (NULL != *tmpHookInterface)){
		(*tmpHookInterface)->J9HookShutdownInterface(tmpHookInterface);
		*tmpHookInterface = NULL; /* avoid issues with double teardowns */
	}

#if defined(OMR_GC_IDLE_HEAP_MANAGER)
	if (NULL != idleGCManager) {
		idleGCManager->kill(env);
		idleGCManager = NULL;
	}
#endif /* defined(OMR_GC_IDLE_HEAP_MANAGER) */

	MM_GCExtensionsBase::tearDown(env);
}

void
MM_GCExtensions::identityHashDataAddRange(MM_EnvironmentBase *env, MM_MemorySubSpace* subspace, UDATA size, void* lowAddress, void* highAddress)
{
	J9IdentityHashData* hashData = getJavaVM()->identityHashData;
	if (J9_IDENTITY_HASH_SALT_POLICY_STANDARD == hashData->hashSaltPolicy) {
		if (MEMORY_TYPE_NEW == (subspace->getTypeFlags() & MEMORY_TYPE_NEW)) {
			if (hashData->hashData1 == (UDATA)highAddress) {
				/* Expanding low bound */
				hashData->hashData1 = (UDATA)lowAddress;
			} else if (hashData->hashData2 == (UDATA)lowAddress) {
				/* Expanding high bound */
				hashData->hashData2 = (UDATA)highAddress;
			} else {
				/* First expand */
				Assert_MM_true(UDATA_MAX == hashData->hashData1);
				Assert_MM_true(0 == hashData->hashData2);
				hashData->hashData1 = (UDATA)lowAddress;
				hashData->hashData2 = (UDATA)highAddress;
			}
		}
	}
}

void
MM_GCExtensions::identityHashDataRemoveRange(MM_EnvironmentBase *env, MM_MemorySubSpace* subspace, UDATA size, void* lowAddress, void* highAddress)
{
	J9IdentityHashData* hashData = getJavaVM()->identityHashData;
	if (J9_IDENTITY_HASH_SALT_POLICY_STANDARD == hashData->hashSaltPolicy) {
		if (MEMORY_TYPE_NEW == (subspace->getTypeFlags() & MEMORY_TYPE_NEW)) {
			if (hashData->hashData1 == (UDATA)lowAddress) {
				/* Contracting low bound */
				Assert_MM_true(hashData->hashData1 <= (UDATA)highAddress);
				Assert_MM_true((UDATA)highAddress <= hashData->hashData2);
				hashData->hashData1 = (UDATA)highAddress;
			} else if (hashData->hashData2 == (UDATA)highAddress) {
				/* Contracting high bound */
				Assert_MM_true(hashData->hashData1 <= (UDATA)lowAddress);
				Assert_MM_true((UDATA)lowAddress <= hashData->hashData2);
				hashData->hashData2 = (UDATA)lowAddress;
			} else {
				Assert_MM_unreachable();
			}
		}
	}
}

void
MM_GCExtensions::updateIdentityHashDataForSaltIndex(UDATA index)
{
	getJavaVM()->identityHashData->hashSaltTable[index] = (U_32)convertValueToHash(getJavaVM(), getJavaVM()->identityHashData->hashSaltTable[index]);
}

/*
 * computeDefaultMaxHeapForJava is for Java only, it will be called during gcParseCommandLineAndInitializeWithValues(),
 *  computeDefaultMaxHeap() will still be called during MM_GCExtensionsBase::initialize(), computeDefaultMaxHeapForJava() can overwrite value of memoryMax.
 */
void
MM_GCExtensions::computeDefaultMaxHeapForJava(bool enableOriginalJDK8HeapSizeCompatibilityOption)
{
	OMRPORT_ACCESS_FROM_OMRVM(_omrVM);

	if (OMR_CGROUP_SUBSYSTEM_MEMORY == omrsysinfo_cgroup_are_subsystems_enabled(OMR_CGROUP_SUBSYSTEM_MEMORY)) {
		if (omrsysinfo_cgroup_is_memlimit_set()) {
			/* If running in a cgroup with memory limit > 1G, reserve at-least 512M for JVM's internal requirements
			 * like JIT compilation etc, and extend default max heap memory to at-most 75% of cgroup limit.
			 * The value reserved for JVM's internal requirements excludes heap. This value is a conservative
			 * estimate of the JVM's internal requirements, given that one compilation thread can use up to 256M.
			 */
#define OPENJ9_IN_CGROUP_NATIVE_FOOTPRINT_EXCLUDING_HEAP ((U_64)512 * 1024 * 1024)
			memoryMax = (uintptr_t)OMR_MAX((int64_t)(usablePhysicalMemory / 2), (int64_t)(usablePhysicalMemory - OPENJ9_IN_CGROUP_NATIVE_FOOTPRINT_EXCLUDING_HEAP));
			memoryMax = (uintptr_t)OMR_MIN(memoryMax, (usablePhysicalMemory / 4) * 3);
#undef OPENJ9_IN_CGROUP_NATIVE_FOOTPRINT_EXCLUDING_HEAP
		}
	}

#if defined(OMR_ENV_DATA64)
	if (!enableOriginalJDK8HeapSizeCompatibilityOption) {
		/* extend java default max memory to 25% of usable RAM */
		memoryMax = OMR_MAX(memoryMax, usablePhysicalMemory / 4);
	}

	/* limit maxheapsize up to MAXIMUM_HEAP_SIZE_RECOMMENDED_FOR_3BIT_SHIFT_COMPRESSEDREFS, then can set 3bit compressedrefs as the default */
	memoryMax = OMR_MIN(memoryMax, MAXIMUM_HEAP_SIZE_RECOMMENDED_FOR_3BIT_SHIFT_COMPRESSEDREFS);
#endif /* OMR_ENV_DATA64 */

	memoryMax = MM_Math::roundToFloor(heapAlignment, memoryMax);
	maxSizeDefaultMemorySpace = memoryMax;
}

MM_OwnableSynchronizerObjectList *
MM_GCExtensions::getOwnableSynchronizerObjectListsExternal(J9VMThread *vmThread)
{
	Assert_MM_true(!isConcurrentScavengerInProgress());

	return ownableSynchronizerObjectLists;
}

MM_ContinuationObjectList *
MM_GCExtensions::getContinuationObjectListsExternal(J9VMThread *vmThread)
{
	return continuationObjectLists;
}


void
MM_GCExtensions::releaseNativesForContinuationObject(MM_EnvironmentBase* env, j9object_t objectPtr)
{
#if JAVA_SPEC_VERSION >= 19
	J9VMThread *vmThread = (J9VMThread *)env->getLanguageVMThread();

	if (verify_continuation_list == continuationListOption) {
		J9VMContinuation *continuation = J9VMJDKINTERNALVMCONTINUATION_VMREF(vmThread, objectPtr);
		jboolean finished = J9VMJDKINTERNALVMCONTINUATION_FINISHED(vmThread, objectPtr);
		/* there might be potential case that GC would happen between JVM_VirtualThreadUnmountBegin() and JVM_VirtualThreadUnmountEnd() and
		 * last unmounted continuation Object is marked as "dead", but the related J9VMContinuation has not been freed up.
		 */
		if (!finished) {
			Assert_GC_true_with_message2(env, (NULL == continuation), "Continuation expected to be NULL, but it is %p, from Continuation object %p\n", continuation, objectPtr);
		}
	} else {
		getJavaVM()->internalVMFunctions->freeContinuation(vmThread, objectPtr);
	}
#endif /* JAVA_SPEC_VERSION >= 19 */
}

bool
MM_GCExtensions::needScanStacksForContinuationObject(J9VMThread *vmThread, j9object_t objectPtr, bool isGlobalGC)
{
	bool needScan = false;
#if JAVA_SPEC_VERSION >= 19
	jboolean started = J9VMJDKINTERNALVMCONTINUATION_STARTED(vmThread, objectPtr);
	jboolean finished = J9VMJDKINTERNALVMCONTINUATION_FINISHED(vmThread, objectPtr);
	J9VMContinuation *continuation = J9VMJDKINTERNALVMCONTINUATION_VMREF(vmThread, objectPtr);
	/**
	 * We don't scan mounted continuations:
	 *
	 * for concurrent GCs, since stack is actively changing. Instead, we scan them during preMount or during root scanning if already mounted at cycle start or during postUnmount (might be indirectly via card cleaning) or during final STW (via root re-scan) if still mounted at cycle end
	 * for sliding compacts to avoid double slot fixups
	 *
	 * For fully STW GCs, there is no harm to scan them, but it's a waste of time since they are scanned during root scanning already.
	 *
	 * We don't scan currently scanned for the same collector either - one scan is enough for the same collector, but there could be concurrent scavenger(local collector) and concurrent marking(global collector) overlapping,
	 * they are irrelevant and both are concurrent, we handle them independently and separately, they are not blocked or ignored each other.
	 *
	 * we don't scan the continuation object before started and after finished - java stack does not exist.
	 */
	if (started && !finished) {
		Assert_MM_true(NULL != continuation);
		needScan = !VM_VMHelpers::isContinuationMountedOrConcurrentlyScanned(continuation, isGlobalGC);
	}
#endif /* JAVA_SPEC_VERSION >= 19 */
	return needScan;
}
