
/*******************************************************************************
 * Copyright (c) 1991, 2014 IBM Corp. and others
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

#include "VerboseEventMetronomeNonMonotonicTime.hpp"
#include "GCExtensions.hpp"
#include "VerboseEventStream.hpp"
#include "VerboseManagerOld.hpp"

#include <string.h>

#if defined(J9VM_GC_REALTIME)

/**
 * Create an new instance of a MM_VerboseEventMetronomeOutOfMemory event.
 * @param event Pointer to a structure containing the data passed over the hookInterface
 */
MM_VerboseEvent *
MM_VerboseEventMetronomeNonMonotonicTime::newInstance(MM_NonMonotonicTimeEvent *event, J9HookInterface** hookInterface)
{
	MM_VerboseEventMetronomeNonMonotonicTime *eventObject = (MM_VerboseEventMetronomeNonMonotonicTime *)MM_VerboseEvent::create(event->currentThread, sizeof(MM_VerboseEventMetronomeNonMonotonicTime));
	if(NULL != eventObject) {
		new(eventObject) MM_VerboseEventMetronomeNonMonotonicTime(event, hookInterface);
		eventObject->initialize(event);
	}
	return eventObject;
}

void
MM_VerboseEventMetronomeNonMonotonicTime::initialize(MM_NonMonotonicTimeEvent *event)
{
	OMRPORT_ACCESS_FROM_OMRVMTHREAD(_omrThread);
	_timeInMilliSeconds = omrtime_hires_delta(0, _time, J9PORT_TIME_DELTA_IN_MILLISECONDS);
}

/**
 * Populate events data fields.
 * The event calls the event stream requesting the address of events it is interested in.
 * When an address is returned it populates itself with the data.
 */
void
MM_VerboseEventMetronomeNonMonotonicTime::consumeEvents()
{
}

/**
 * Passes a format string and data to the output routine defined in the passed output agent.
 * @param agent Pointer to an output agent.
 */
void
MM_VerboseEventMetronomeNonMonotonicTime::formattedOutput(MM_VerboseOutputAgent *agent)
{
	OMRPORT_ACCESS_FROM_OMRVMTHREAD(_omrThread);
	MM_GCExtensions *extensions = MM_GCExtensions::getExtensions(_omrThread->_vm);
	MM_VerboseManagerBase *manager = extensions->verboseGCManager;
	char timestamp[32];
	
	omrstr_ftime(timestamp, sizeof(timestamp), VERBOSEGC_DATE_FORMAT, _timeInMilliSeconds);
	
	agent->formatAndOutput(static_cast<J9VMThread*>(_omrThread->_language_vmthread), manager->getIndentLevel(), "<event details=\"non-monotonic time acknowledged\" timerDesc=\"%s\" timestamp=\"%s\" />", _timerDescription, timestamp);
	agent->endOfCycle(static_cast<J9VMThread*>(_omrThread->_language_vmthread));
}

#endif /* J9VM_GC_REALTIME */
