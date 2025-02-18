/*******************************************************************************
 * Copyright (c) 1991, 2022 IBM Corp. and others
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
package com.ibm.j9ddr.vm29.j9.gc;

import static com.ibm.j9ddr.vm29.events.EventManager.raiseCorruptDataEvent;

import java.lang.reflect.Method;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.NoSuchElementException;

import com.ibm.j9ddr.CorruptDataException;
import com.ibm.j9ddr.vm29.j9.J9ConstantHelper;
import com.ibm.j9ddr.vm29.j9.ObjectModel;
import com.ibm.j9ddr.vm29.pointer.PointerPointer;
import com.ibm.j9ddr.vm29.pointer.U8Pointer;
import com.ibm.j9ddr.vm29.pointer.generated.J9BuildFlags;
import com.ibm.j9ddr.vm29.pointer.generated.J9ModronThreadLocalHeapPointer;
import com.ibm.j9ddr.vm29.pointer.generated.J9ObjectPointer;
import com.ibm.j9ddr.vm29.pointer.generated.J9VMThreadPointer;
import com.ibm.j9ddr.vm29.pointer.generated.MM_CopyScanCachePointer;
import com.ibm.j9ddr.vm29.pointer.generated.MM_EnvironmentStandardPointer;
import com.ibm.j9ddr.vm29.structure.J9ModronThreadLocalHeap;
import com.ibm.j9ddr.vm29.types.UDATA;

class GCObjectHeapIteratorAddressOrderedList_V1 extends GCObjectHeapIterator
{
	private static final int realHeapAllocOffset = J9ConstantHelper.getInt(J9ModronThreadLocalHeap.class, "_realHeapAllocOffset_", -1);

	protected J9ObjectPointer currentObject;
	protected U8Pointer  scanPtr;
	protected U8Pointer scanPtrTop;
	protected U8Pointer[][] excludedRanges;
	protected int currentExcludedRange;
	
	protected GCObjectHeapIteratorAddressOrderedList_V1(U8Pointer base, U8Pointer top, boolean includeLiveObjects, boolean includeDeadObjects) throws CorruptDataException
	{
		super(includeLiveObjects, includeDeadObjects);
		
		currentObject = null;
		scanPtr = base;
		scanPtrTop = top;
		
		ArrayList<U8Pointer[]> excludedRangeList = new ArrayList<U8Pointer[]>();
		GCVMThreadListIterator threadIterator = new GCVMThreadListIterator();
		boolean scavengerEnabled = false;
		if (J9BuildFlags.gc_modronScavenger) {
			if (getExtensions().scavengerEnabled()) {
				scavengerEnabled = true;
			}
		}
		while (threadIterator.hasNext()) {
			J9VMThreadPointer vmThread = threadIterator.next();
			
			/* Check for TLHes */
			if (J9BuildFlags.gc_inlinedAllocFields) {
				U8Pointer heapTop = adjustedToRange(vmThread.heapTop(), base, top);
				if (heapTop.notNull()) {
					U8Pointer heapAlloc = adjustedToRange(vmThread.heapAlloc(), base, top);
					if (isSomethingToAdd(heapAlloc, heapTop)) {
						excludedRangeList.add(new U8Pointer[] {heapAlloc, heapTop});
					} else {
						/* Might be an instrumented VM */
						/* realHeapAlloc = allocateThreadLocalHeap.realHeapAlloc in V1, = heapAlloc in V2 */
						U8Pointer realHeapAlloc = adjustedToRange(getRealHeapAlloc(vmThread.allocateThreadLocalHeap(), heapAlloc), base, top);
						/* realHeapTop = heapTop in V1, = allocateThreadLocalHeap.realHeapTop in V2 */
						U8Pointer realHeapTop = adjustedToRange(getRealHeapTop(vmThread.allocateThreadLocalHeap(), heapTop), base, top);
						if (realHeapAlloc.notNull() && realHeapTop.notNull() && isSomethingToAdd(realHeapAlloc, realHeapTop)) {
							excludedRangeList.add(new U8Pointer[] {realHeapAlloc, realHeapTop});
						}
					}
				}
				
				/* Check non-zeroed TLH as well if it is enabled */
				if (J9BuildFlags.gc_nonZeroTLH) {
					heapTop = adjustedToRange(vmThread.nonZeroHeapTop(), base, top);
					if (heapTop.notNull()) {
						U8Pointer heapAlloc = adjustedToRange(vmThread.nonZeroHeapAlloc(), base, top);
						if (isSomethingToAdd(heapAlloc, heapTop)) {
							excludedRangeList.add(new U8Pointer[] {heapAlloc, heapTop});
						} else {
							/* Might be an instrumented VM */
							U8Pointer realHeapAlloc = adjustedToRange(getRealHeapAlloc(vmThread.nonZeroAllocateThreadLocalHeap(), heapAlloc), base, top);
							U8Pointer realHeapTop = adjustedToRange(getRealHeapTop(vmThread.nonZeroAllocateThreadLocalHeap(), heapTop), base, top);
							if (realHeapAlloc.notNull() && realHeapTop.notNull() && isSomethingToAdd(realHeapAlloc, realHeapTop)) {
								excludedRangeList.add(new U8Pointer[] {realHeapAlloc, realHeapTop});
							}
						}
					}
				}
			} else {
				throw new UnsupportedOperationException("No support for non-gc_inlinedAllocFields VMs");
			}
			
			/* If we're in the middle of a scavenge, record the CopyScanCache data */
			if (scavengerEnabled) {
				MM_EnvironmentStandardPointer env = MM_EnvironmentStandardPointer.cast(vmThread.gcExtensions());

				MM_CopyScanCachePointer survivorCache = env._survivorCopyScanCache();
				if (survivorCache.notNull()) {
					U8Pointer cacheAlloc = adjustedToRange(U8Pointer.cast(survivorCache.cacheAlloc()), base, top);
					U8Pointer cacheTop = adjustedToRange(U8Pointer.cast(survivorCache.cacheTop()), base, top);
					if (isSomethingToAdd(cacheAlloc, cacheTop)) {
						excludedRangeList.add(new U8Pointer[] {cacheAlloc, cacheTop});
					}
				}

				MM_CopyScanCachePointer tenureCache = env._tenureCopyScanCache();
				if (tenureCache.notNull()) {
					U8Pointer cacheAlloc = adjustedToRange(U8Pointer.cast(tenureCache.cacheAlloc()), base, top);
					U8Pointer cacheTop = adjustedToRange(U8Pointer.cast(tenureCache.cacheTop()), base, top);
					if (isSomethingToAdd(cacheAlloc, cacheTop)) {
						excludedRangeList.add(new U8Pointer[] {cacheAlloc, cacheTop});
					}					
				}
			}
		}
		excludedRangeList.add(new U8Pointer[] {scanPtrTop, scanPtrTop}); 
		Collections.sort(excludedRangeList, new Comparator<U8Pointer[]>() 
			{
				public int compare(U8Pointer[] o1, U8Pointer[] o2)
				{
					return o1[0].compare(o2[0]);
				}
			});
		excludedRanges = new U8Pointer[excludedRangeList.size()][];
		excludedRangeList.toArray(excludedRanges);
		currentExcludedRange = 0;
	}

	protected U8Pointer getRealHeapTop(J9ModronThreadLocalHeapPointer threadLocalHeap, U8Pointer heapTop) throws CorruptDataException
	{
		return heapTop;
	}

	protected U8Pointer getRealHeapAlloc(J9ModronThreadLocalHeapPointer threadLocalHeap, U8Pointer heapAlloc) throws CorruptDataException
	{
		if (realHeapAllocOffset < 0) {
			throw new CorruptDataException("No such field: realHeapAlloc");
		}

		PointerPointer realHeapAllocEA = PointerPointer.cast(threadLocalHeap).addOffset(realHeapAllocOffset);

		return U8Pointer.cast(realHeapAllocEA.at(0));
	}

	private U8Pointer adjustedToRange(U8Pointer ptr, U8Pointer base, U8Pointer top)
	{
		U8Pointer result = ptr;
		if (result.notNull()) {
			if (result.lt(base)) {
				result = base;
			} else if (result.gt(top)) {
				result = top;
			}
		}
		return result;
	}

	private boolean isSomethingToAdd (U8Pointer start, U8Pointer end) throws CorruptDataException
	{
		boolean result = false;

		if (start.lt(end)) {
			result = true;
		} else if (start.gt(end)) {
			throw new CorruptDataException("Memory range: Start address is higher then end address");
		}
		
		return result;
	}

	protected void advanceScanPointer()
	{
		try {
			while (scanPtr.lt(scanPtrTop)) {
				// If there is a current object, advance past it
				if (null != currentObject) {
					if (ObjectModel.isHoleObject(currentObject)) {
						UDATA holeObjectSize = ObjectModel.getSizeInBytesHoleObject(currentObject);
						if (holeObjectSize.eq(0)) {
							/* The size of a hole should not be 0 */
							throw new CorruptDataException("Hole object at " + currentObject.getHexAddress() + " has an invalid size of 0");
						}
						scanPtr = scanPtr.add(holeObjectSize);
					} else {
						scanPtr = scanPtr.add(ObjectModel.getConsumedSizeInBytesWithHeader(currentObject));
					}		
					currentObject = null;
				}
			
				// Make sure that pointer is not set too high
				// taken size can be bogus because of data corruption
				// so we need to skip check for ranges in this case
				if (scanPtr.gte(scanPtrTop)) {
					return;
				}
				
				// Move past any TLH regions.
				while (scanPtr.gt(excludedRanges[currentExcludedRange][1])) {
					currentExcludedRange++;
				}
				if (scanPtr.gte(excludedRanges[currentExcludedRange][0])) {
					// We're in an unused TLH region. 
					// TODO : this should report as a hole
					scanPtr = U8Pointer.cast(excludedRanges[currentExcludedRange][1]);
					currentExcludedRange++;
					continue;
				}
			
				// Make sure we haven't run past the end
				if (scanPtr.gte(scanPtrTop)) {
					return;
				}
				
				// Found a candidate entity. 
				currentObject = J9ObjectPointer.cast(scanPtr);
				if (!(includeLiveObjects && includeDeadObjects)) {
					// If we're filtering by type make sure this one is suitable.
					boolean isDead = ObjectModel.isHoleObject(currentObject) || ObjectModel.isDarkMatterObject(currentObject);
					if (!includeLiveObjects && !isDead) {
						continue;
					}
					if (!includeDeadObjects && isDead) {
						continue;
					}
				}
				return;
			}
		} catch (CorruptDataException e) {
			raiseCorruptDataEvent("Error getting next item", e, false);		//can try to recover from this
			currentObject = null;
			scanPtr = scanPtrTop; // cause iteration to cease
		}
	}

	public boolean hasNext()
	{
		if (null == currentObject) {
			advanceScanPointer();
		}
		return (null != currentObject);
	}

	@Override
	public void advance(UDATA size)
	{
		U8Pointer newAddress = scanPtr.addOffset(size);
		if (newAddress.gte(scanPtr) && newAddress.lte(scanPtrTop)) {
			scanPtr = newAddress;
			currentObject = null;
		} else {
			throw new NoSuchElementException("An address to advance is out of range");
		}
	}
	
	@Override
	public J9ObjectPointer next()
	{
		if(hasNext()) {
			J9ObjectPointer next = currentObject;
			advanceScanPointer();
			return next;
		} else {
			throw new NoSuchElementException("There are no more items available through this iterator");
		}
	}

	@Override
	public J9ObjectPointer peek()
	{
		if(hasNext()) {
			return currentObject;
		} else {
			throw new NoSuchElementException("There are no more items available through this iterator");
		}		
	}
}
