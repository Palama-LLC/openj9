# Makefile for DDR blob generation

###############################################################################
# Copyright (c) 1991, 2019 IBM Corp. and others
#
# This program and the accompanying materials are made available under
# the terms of the Eclipse Public License 2.0 which accompanies this
# distribution and is available at https://www.eclipse.org/legal/epl-2.0/
# or the Apache License, Version 2.0 which accompanies this distribution and
# is available at https://www.apache.org/licenses/LICENSE-2.0.
#
# This Source Code may also be made available under the following
# Secondary Licenses when the conditions for such availability set
# forth in the Eclipse Public License, v. 2.0 are satisfied: GNU
# General Public License, version 2 with the GNU Classpath
# Exception [1] and GNU General Public License, version 2 with the
# OpenJDK Assembly Exception [2].
#
# [1] https://www.gnu.org/software/classpath/license.html
# [2] https://openjdk.org/legal/assembly-exception.html
#
# SPDX-License-Identifier: EPL-2.0 OR Apache-2.0 OR GPL-2.0 WITH Classpath-exception-2.0 OR LicenseRef-GPL-2.0 WITH Assembly-exception
###############################################################################

# Targets to generate DDR blob C code from C headers automatically.
# Included in other Makefiles - don't run it directly.

all : # Must be the default target.

# Recursively find header files in directory $1.
FindHeaderFiles = \
	$(wildcard $1/*.h) \
	$(wildcard $1/*.hpp) \
	$(foreach i,$(wildcard $1/*),$(call FindHeaderFiles,$i))

# Define a rule to adjust a header file for DDR.
define FilterHeader
UMA_OBJECTS_PREREQS += $1
UMA_BYPRODUCTS      += $1

generate_files : $1

$1 : $2
	@ mkdir -p $$(@D)
	sed -f gc_xlat.sed $$< > $$@
endef

# If you change these lists, make the corresponding change to the j9ddrgc include list.
J9VM_GC_DIRS := \
	gc_api \
	gc_base \
	gc_glue_java \
	gc_include \
	gc_modron_standard \
	gc_modron_startup \
	gc_realtime \
	gc_stats \
	gc_structs \
	gc_verbose_java \
	gc_verbose_old \
	gc_verbose_old_events \
	gc_vlhgc \
	#

OMR_GC_DIRS := \
	gc/base \
	gc/include \
	gc/startup \
	gc/stats \
	gc/structs \
	gc/verbose \
	#

# Filter J9VM GC header files.
J9VM_GC_FILES := $(strip $(sort $(foreach dir,$(J9VM_GC_DIRS),$(subst $(UMA_PATH_TO_ROOT),,$(call FindHeaderFiles,$(UMA_PATH_TO_ROOT)$(dir))))))
$(foreach file,$(J9VM_GC_FILES),$(eval $(call FilterHeader,$(file),$(UMA_PATH_TO_ROOT)$(file))))

# Filter OMR GC header files.
OMR_GC_FILES := $(strip $(sort $(foreach dir, $(OMR_GC_DIRS),$(subst $(OMR_DIR)/,,$(call FindHeaderFiles,$(OMR_DIR)/$(dir))))))
$(foreach file,$(OMR_GC_FILES),$(eval $(call FilterHeader,omr/$(file),$(OMR_DIR)/$(file))))
