/*******************************************************************************
 * Copyright (c) 1991, 2020 IBM Corp. and others
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

/**
 * @file
 * @ingroup GC_Structs
 */

#include "j9.h"
#include "j9cfg.h"

#include "MixedObjectDeclarationOrderIterator.hpp"

/**
 * Gets the next slot (by declaration order) in the object containing an object reference.
 * @return the next slot in the object containing an object reference
 * @return NULL if there are no more such slots
 */
GC_SlotObject *
GC_MixedObjectDeclarationOrderIterator::nextSlot()
{
	if (NULL == _fieldShape) {
		return NULL;
	}

	_slotObject.writeAddressToSlot((fomrobject_t*)((uintptr_t)_objectPtr + _walkState.fieldOffsetWalkState.result.offset + J9JAVAVM_OBJECT_HEADER_SIZE(_javaVM)));
	_index = _walkState.referenceIndexOffset + _walkState.classIndexAdjust + _walkState.fieldOffsetWalkState.result.index - 1;
	_fieldShape = _javaVM->internalVMFunctions->fullTraversalFieldOffsetsNextDo(&_walkState);
	return &_slotObject;
}
