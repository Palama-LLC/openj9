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
package com.ibm.j9ddr.corereaders.tdump.zebedee.le;

import javax.imageio.stream.ImageInputStream;
import java.io.IOException;

/* This class was generated automatically by com.ibm.zebedee.util.Xml2Java */

public final class CeexcibTemplate {

    public static int length() {
        return 268;
    }

    public static long getCib_sv1(ImageInputStream inputStream, long address) throws IOException {
        inputStream.seek(address + 204);
        return inputStream.readUnsignedInt() & 0xffffffffL;
    }
    public static int getCib_sv1$offset() {
        return 204;
    }
    public static int getCib_sv1$length() {
        return 32;
    }
    public static long getCib_int(ImageInputStream inputStream, long address) throws IOException {
        inputStream.seek(address + 208);
        return inputStream.readUnsignedInt() & 0xffffffffL;
    }
    public static int getCib_int$offset() {
        return 208;
    }
    public static int getCib_int$length() {
        return 32;
    }
}
