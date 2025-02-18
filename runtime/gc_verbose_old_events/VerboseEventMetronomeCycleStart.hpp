
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

#if !defined(EVENT_METRONOME_CYCLE_START_HPP_)
#define EVENT_METRONOME_CYCLE_START_HPP_

#include "j9.h"
#include "j9cfg.h"
#include "mmhook.h"

#include "VerboseEvent.hpp"

#if defined(J9VM_GC_REALTIME)

class MM_VerboseEventMetronomeCycleStart : public MM_VerboseEvent
{
private:
	UDATA _heapFree; /**< Heap free bytes at the start of a GC cycle */ 
public:	
	static MM_VerboseEvent *newInstance(MM_GCCycleStartEvent *event, J9HookInterface** hookInterface);

	virtual void consumeEvents(void);
	virtual void formattedOutput(MM_VerboseOutputAgent *agent);

	MMINLINE virtual bool definesOutputRoutine(void) { return true; }
	MMINLINE virtual bool endsEventChain(void) { return true; }
	
	MM_VerboseEventMetronomeCycleStart(MM_GCCycleStartEvent *event, J9HookInterface** hookInterface) :
		MM_VerboseEvent(event->omrVMThread, event->timestamp, event->eventid, hookInterface),
		_heapFree(event->commonData->tenureFreeBytes)
	{}
};

#endif /* J9VM_GC_REALTIME */

#endif /* EVENT_METRONOME_CYCLE_START_HPP_ */
