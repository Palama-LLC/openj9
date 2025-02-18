/*[INCLUDE-IF Sidecar18-SE]*/
/*******************************************************************************
 * Copyright (c) 2007, 2016 IBM Corp. and others
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
package com.ibm.jvm.trace.format.api;

import java.math.BigInteger;

/**
 * Class to represent a tracepoint produced by IBM JVM's trace engine.
 * 
 * @author Simon Rowland
 * 
 */
public interface TracePoint {
	/* The types of tracepoints available */
	static final byte EVENT_TYPE = 0;
	static final byte EXCEPTION_TYPE = 1;
	static final byte ENTRY_TYPE = 2;
	static final byte ENTRY_EXCPT_TYPE = 3;
	static final byte EXIT_TYPE = 4;
	static final byte EXIT_EXCPT_TYPE = 5;
	static final byte MEM_TYPE = 6;
	static final byte MEM_EXCPT_TYPE = 7;
	static final byte DEBUG_TYPE = 8;
	static final byte DEBUG_EXCPT_TYPE = 9;
	static final byte PERF_TYPE = 10;
	static final byte PERF_EXCPT_TYPE = 11;
	static final byte ASSERT_TYPE = 12;
	static final byte APP_TYPE = 13;
	static final byte ERROR_TYPE = 14;

	static final String[] types = new String[] { "Event     ", "Exception ", "Entry     ", "Entry     ", "Exit      ", "Exit      ", "Mem       ", "Mem       ", "Debug     ", "Debug     ", "Perf      ", "Perf      ", "Assert    ", "AppTrace  ", "ERROR     " };

	/**
	 * @return the numeric sub-identifier of the current tracepoint within
	 *         the current component. A tracepoint is identified by
	 *         componentName](containerCompName)].numericID, for example,
	 *         comp1.14, or comp1(comp2).12.
	 */
	public int getID();

	/* return the actual time the tracepoint occurred (not a relative time) */
	/**
	 * @return the GMT time in milliseconds at which this TracePoint was
	 *         produced.
	 */
	public long getTimestampMillis();

	/*
	 * this time is intended to be appended to the getTimeStampMillis time,
	 * i.e. it adds resolution rather than being a formattable entity in its
	 * own right
	 */
	/**
	 * @return the high resolution timer value stored at the time this
	 *         tracepoint was generated.
	 */
	public int getMicrosecondsCount();

	/*
	 * return a String with the system date, time and microseconds at the
	 * time this tracepoint was written to trace buffer
	 */

	/**
	 * @return the raw timestamp
	 */
	public BigInteger getRawTime();

	/**
	 * @return the time in the format ????????
	 */
	public String getFormattedTime();

	/**
	 * @return the thread management object that this trace point belongs
	 *         to.
	 */
	public TraceThread getThread();

	/**
	 * @return the name of the component that produced this TracePoint. A
	 *         tracepoint is identified by
	 *         componentName](containerCompName)].numericID, for example,
	 *         comp1.14, or comp1(comp2).12.
	 */
	public String getComponent();

	/**
	 * @return the name of the container component that produced the current
	 *         TracePoint, or null if the TracePoint did not have a
	 *         container component. A tracepoint is identified by
	 *         componentName](containerCompName)].numericID, for example,
	 *         comp1.14, or comp1(comp2).12.
	 */
	public String getContainerComponent();

	/* the formatted data for the parameters */
	/**
	 * @return a String containing the parameters formatted and interpolated
	 *         into the TracePoint's formatting template.
	 */
	public String getFormattedParameters();

	/* the printf style formatting string for the tracepoint */
	/**
	 * @return a String containing the formatting template for this
	 *         TracePoint.
	 */
	public String getParameterFormattingTemplate();

	/* a representation of the raw parameters */
	/**
	 * @return an array of objects, with one object per parameter generated
	 *         by this TracePoint. Returns null if the TracePoint has no
	 *         parameters, or if parameters were not recorded for this
	 *         TracePoint.
	 */
	public Object[] getParameters();

	/**
	 * @return a list of groups to which this TracePoint belongs, or none if
	 *         it doesn't belong to any.
	 */
	public String[] getGroups();

	/**
	 * @return the level of this TracePoint.
	 */
	public int getLevel();

	/**
	 * @return the type of this TracePoint, for example, Entry, Exit or
	 *         Event.
	 */
	public String getType(); // Exit Entry etc
}
