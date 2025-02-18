
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
 
#if !defined(EVENT_TAROK_INCREMENT_START_HPP_)
#define EVENT_TAROK_INCREMENT_START_HPP_
 
#include "j9.h"
#include "j9cfg.h"
#include "mmhook.h"

#include "VerboseEventGCStart.hpp"


/**
 * Stores the data relating to the end of a Tarok incremental collection
 * @ingroup GC_verbose_events
 */
class MM_VerboseEventTarokIncrementStart : public MM_VerboseEventGCStart
{
private:
	UDATA _incrementID;	/**< The unique identifier for this increment */
	U_64 _lastIncrementEndTime;	/**< The time of the last increment end event */

public:
	virtual bool definesOutputRoutine();
	virtual bool endsEventChain();
	virtual void consumeEvents();
	virtual void formattedOutput(MM_VerboseOutputAgent *agent);
	
	static MM_VerboseEvent *newInstance(MM_TarokIncrementStartEvent *event, J9HookInterface** hookInterface);
	
	MM_VerboseEventTarokIncrementStart(MM_TarokIncrementStartEvent *event, J9HookInterface** hookInterface)
		: MM_VerboseEventGCStart(event->currentThread, event->timestamp, event->eventid, event->gcStartData, hookInterface)
		, _incrementID(event->incrementid)
		, _lastIncrementEndTime(0)
	{}
};

#endif /* EVENT_TAROK_INCREMENT_START_HPP_ */
