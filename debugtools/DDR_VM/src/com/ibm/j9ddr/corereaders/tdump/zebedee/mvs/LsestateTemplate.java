/*******************************************************************************
 * Copyright (c) 2006, 2013 IBM Corp. and others
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
package com.ibm.j9ddr.corereaders.tdump.zebedee.mvs;

import javax.imageio.stream.ImageInputStream;
import java.io.IOException;

/* This class was generated automatically by com.ibm.zebedee.util.Xml2Java */

public final class LsestateTemplate {

    public static int length() {
        return 168;
    }

    public static long getLsesgrs(ImageInputStream inputStream, long address) throws IOException {
        inputStream.seek(address + 0);
        throw new Error("request for long value for field lsesgrs which has length of 64");
    }
    public static int getLsesgrs$offset() {
        return 0;
    }
    public static int getLsesgrs$length() {
        return 512;
    }
    public static long getLsespsw(ImageInputStream inputStream, long address) throws IOException {
        inputStream.seek(address + 136);
        return inputStream.readLong();
    }
    public static int getLsespsw$offset() {
        return 136;
    }
    public static int getLsespsw$length() {
        return 64;
    }
    public static long getLsestarg(ImageInputStream inputStream, long address) throws IOException {
        inputStream.seek(address + 148);
        return inputStream.readUnsignedInt() & 0xffffffffL;
    }
    public static int getLsestarg$offset() {
        return 148;
    }
    public static int getLsestarg$length() {
        return 32;
    }
    public static long getLses_ed(ImageInputStream inputStream, long address) throws IOException {
        inputStream.seek(address + 160);
        return inputStream.readLong();
    }
    public static int getLses_ed$offset() {
        return 160;
    }
    public static int getLses_ed$length() {
        return 64;
    }
}
