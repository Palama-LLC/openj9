ZZ Copyright (c) 2000, 2020 IBM Corp. and others
ZZ
ZZ This program and the accompanying materials are made 
ZZ available under the terms of the Eclipse Public License 2.0 
ZZ which accompanies this distribution and is available at 
ZZ https://www.eclipse.org/legal/epl-2.0/ or the Apache License, 
ZZ Version 2.0 which accompanies this distribution and is available 
ZZ at https://www.apache.org/licenses/LICENSE-2.0.
ZZ
ZZ This Source Code may also be made available under the following
ZZ Secondary Licenses when the conditions for such availability set
ZZ forth in the Eclipse Public License, v. 2.0 are satisfied: GNU
ZZ General Public License, version 2 with the GNU Classpath 
ZZ Exception [1] and GNU General Public License, version 2 with the
ZZ OpenJDK Assembly Exception [2].
ZZ
ZZ [1] https://www.gnu.org/software/classpath/license.html
ZZ [2] https://openjdk.org/legal/assembly-exception.html
ZZ
ZZ SPDX-License-Identifier: EPL-2.0 OR Apache-2.0 OR
ZZ GPL-2.0 WITH Classpath-exception-2.0 OR
ZZ LicenseRef-GPL-2.0 WITH Assembly-exception

ifdef({J9ZOS390},{dnl

include({z/runtime/zOS_macros.inc})dnl

},{dnl

include({z/runtime/zLinux_macros.inc})dnl

})dnl

ZZ ============================================================

ZZ
ZZ General Registers
ZZ

EQUVAL(r0,0)
EQUVAL(r1,1)
EQUVAL(r2,2)
EQUVAL(r3,3)
EQUVAL(r4,4)
EQUVAL(r5,5)
EQUVAL(r6,6)
EQUVAL(r7,7)
EQUVAL(r8,8)
EQUVAL(r9,9)
EQUVAL(r10,10)
EQUVAL(r11,11)
EQUVAL(r12,12)
EQUVAL(r13,13)
EQUVAL(r14,14)
EQUVAL(r15,15)


ZZ ============================================================

ZZ Special register names
ZZ These are included because they make their use
ZZ as explicit base registers more apparent.

EQUVAL(J9VM_STRUCT,13)
EQUVAL(J9SP,5)


ZZ ============================================================

ZZ Floating-point registers.

EQUVAL(f0,0)
EQUVAL(f1,1)
EQUVAL(f2,2)
EQUVAL(f3,3)
EQUVAL(f4,4)
EQUVAL(f5,5)
EQUVAL(f6,6)
EQUVAL(f7,7)
EQUVAL(f8,8)
EQUVAL(f9,9)
EQUVAL(f10,10)
EQUVAL(f11,11)
EQUVAL(f12,12)
EQUVAL(f13,13)
EQUVAL(f14,14)
EQUVAL(f15,15)

ZZ ============================================================

ifdef({TR_HOST_64BIT},{dnl
EQUVAL(PTR_SIZE,8)

ZZ Interface Snippet Layout (64-bit mode)
ZZ Values defined here so they can be included in PicBuilder and
ZZ ValueProf

SETVAL(eq_codeRA_inInterfaceSnippet,0)
SETVAL(eq_cp_inInterfaceSnippet,8)
SETVAL(eq_cpindex_inInterfaceSnippet,16)
SETVAL(eq_intfAddr_inInterfaceSnippet,24)
SETVAL(eq_intfMethodIndex_inInterfaceSnippet,32)
SETVAL(eq_thunk_inInterfaceSnippet,40)

ZZ For Zero or Multi slot case
SETVAL(eq_flag_inInterfaceSnippet,48)

ZZ For Multi slot case
SETVAL(eq_lastCachedSlotField_inInterfaceSnippet,56)
SETVAL(eq_firstSlotField_inInterfaceSnippet,64)
SETVAL(eq_lastSlotField_inInterfaceSnippet,72)
SETVAL(eq_firstSlot_inInterfaceSnippet,80)

ZZ For single dynamic slot case
SETVAL(eq_implementerClass_inInterfaceSnippet,48)
SETVAL(eq_implementerMethod_inInterfaceSnippet,56)
SETVAL(eq_flag_inInterfaceSnippetSingleDynamicSlot,64)
SETVAL(eq_picreg_inInterfaceSnippetSingleDynamicSlot,65)

},{dnl
EQUVAL(PTR_SIZE,4)

ZZ Interface Snippet Layout (31 bit mode)

SETVAL(eq_codeRA_inInterfaceSnippet,0)
SETVAL(eq_cp_inInterfaceSnippet,4)
SETVAL(eq_cpindex_inInterfaceSnippet,8)
SETVAL(eq_intfAddr_inInterfaceSnippet,12)
SETVAL(eq_intfMethodIndex_inInterfaceSnippet,16)
SETVAL(eq_thunk_inInterfaceSnippet,20)

ZZ For Zero or Multi slot case
SETVAL(eq_flag_inInterfaceSnippet,24)

ZZ For Multi slot case
SETVAL(eq_lastCachedSlotField_inInterfaceSnippet,28)
SETVAL(eq_firstSlotField_inInterfaceSnippet,32)
SETVAL(eq_lastSlotField_inInterfaceSnippet,36)
SETVAL(eq_firstSlot_inInterfaceSnippet,40)

ZZ For Single dynamic slot case
SETVAL(eq_implementerClass_inInterfaceSnippet,24)
SETVAL(eq_implementerMethod_inInterfaceSnippet,28)
SETVAL(eq_flag_inInterfaceSnippetSingleDynamicSlot,32)
SETVAL(eq_picreg_inInterfaceSnippetSingleDynamicSlot,33)

})dnl

ifdef({ASM_J9VM_JIT_FREE_SYSTEM_STACK_POINTER},{dnl
SETVAL(eq_j9vmthr_ssp,J9TR_VMThread_systemStackPointer)
})dnl

ZZ ============================================================

ifdef({J9ZOS390},{dnl
ZZ under xplink, need a 4 byte nop for 31 bit, 2 for 64
ZZ after each call to a C routine
define(RETURN_NOP,
{dnl
ifdef({TR_HOST_64BIT},{dnl
    LR r0,r0
},{dnl
    LR_GPR r0,r0
    LR_GPR r0,r0
})dnl
})dnl

},{dnl

define(RETURN_NOP,
{dnl

ZZ no nop required
})dnl
})dnl

ZZ ============================================================
define(SAVE_FP_REGS,
{dnl
ifdef({TR_HOST_64BIT},{dnl
        std     f0,0*PTR_SIZE($1)
        std     f1,1*PTR_SIZE($1)
        std     f2,2*PTR_SIZE($1)
        std     f3,3*PTR_SIZE($1)
        std     f4,4*PTR_SIZE($1)
        std     f5,5*PTR_SIZE($1)
        std     f6,6*PTR_SIZE($1)
        std     f7,7*PTR_SIZE($1)
        std     f8,8*PTR_SIZE($1)
        std     f9,9*PTR_SIZE($1)
        std     f10,10*PTR_SIZE($1)
        std     f11,11*PTR_SIZE($1)
        std     f12,12*PTR_SIZE($1)
        std     f13,13*PTR_SIZE($1)
        std     f14,14*PTR_SIZE($1)
        std     f15,15*PTR_SIZE($1)
},{dnl
        std      f0,0*PTR_SIZE($1)
        std      f1,2*PTR_SIZE($1)
        std      f2,4*PTR_SIZE($1)
        std      f3,6*PTR_SIZE($1)
        std      f4,8*PTR_SIZE($1)
        std      f5,10*PTR_SIZE($1)
        std      f6,12*PTR_SIZE($1)
        std      f7,14*PTR_SIZE($1)
        std      f8,16*PTR_SIZE($1)
        std      f9,18*PTR_SIZE($1)
        std      f10,20*PTR_SIZE($1)
        std      f11,22*PTR_SIZE($1)
        std      f12,24*PTR_SIZE($1)
        std      f13,26*PTR_SIZE($1)
        std      f14,28*PTR_SIZE($1)
        std      f15,30*PTR_SIZE($1)
})dnl
})dnl

ZZ ============================================================
define(RESTORE_FP_REGS,
{dnl
ifdef({TR_HOST_64BIT},{dnl
        ld      f0,0*PTR_SIZE($1)
        ld      f1,1*PTR_SIZE($1)
        ld      f2,2*PTR_SIZE($1)
        ld      f3,3*PTR_SIZE($1)
        ld      f4,4*PTR_SIZE($1)
        ld      f5,5*PTR_SIZE($1)
        ld      f6,6*PTR_SIZE($1)
        ld      f7,7*PTR_SIZE($1)
        ld      f8,8*PTR_SIZE($1)
        ld      f9,9*PTR_SIZE($1)
        ld      f10,10*PTR_SIZE($1)
        ld      f11,11*PTR_SIZE($1)
        ld      f12,12*PTR_SIZE($1)
        ld      f13,13*PTR_SIZE($1)
        ld      f14,14*PTR_SIZE($1)
        ld      f15,15*PTR_SIZE($1)
},{dnl
        ld       f0,0*PTR_SIZE($1)
        ld       f1,2*PTR_SIZE($1)
        ld       f2,4*PTR_SIZE($1)
        ld       f3,6*PTR_SIZE($1)
        ld       f4,8*PTR_SIZE($1)
        ld       f5,10*PTR_SIZE($1)
        ld       f6,12*PTR_SIZE($1)
        ld       f7,14*PTR_SIZE($1)
        ld       f8,16*PTR_SIZE($1)
        ld       f9,18*PTR_SIZE($1)
        ld       f10,20*PTR_SIZE($1)
        ld       f11,22*PTR_SIZE($1)
        ld       f12,24*PTR_SIZE($1)
        ld       f13,26*PTR_SIZE($1)
        ld       f14,28*PTR_SIZE($1)
        ld       f15,30*PTR_SIZE($1)
})dnl
})dnl

ZZ ============================================================

define(LOAD_ADDR_FROM_TOC,
{dnl
ZZ Load TOC address
   L_GPR $1,J9TR_VMThread_codertTOC(,r13)
ZZ Load callee address
   L_GPR $1,(($2-1)*PTR_SIZE)(,$1)
})dnl

ZZ ============================================================

define(LOAD_ADDR_FROM_TOC_IND_REG,
{dnl
ZZ Load TOC address
   L_GPR $1,J9TR_VMThread_codertTOC(,r13)
ZZ Load callee address
   L_GPR $1,(($2-1)*PTR_SIZE)($3,$1)
})dnl

ZZ ============================================================

ZZ Constant Area layout for PATCH
SETVAL(eq_lshmask1,0)
SETVAL(eq_lshmask2,4)
SETVAL(eq_lshmask3,8)
SETVAL(eq_mshmask1,12)
SETVAL(eq_mshmask2,16)
SETVAL(eq_mshmask3,20)

ZZ ============================================================
ZZ This macro is used to patch an 4-bytes address with a new value
ZZ No prolog or epilog is required.
ZZ  R1=patch value R2=patch address
ZZ Patch Kills breg(r10),r1,r2,r3, and r14
ZZ ============================================================

define(PATCH,
{dnl

ZZ  # derive base pointer into table
    BRAS    breg,$1PatchLbl0

    CONST_4BYTE(000000FF) # lshmask1 for patching
    CONST_4BYTE(0000FFFF) # lshmask2 for patching
    CONST_4BYTE(00FFFFFF) # lshmask3 for patching
    CONST_4BYTE(FF000000) # mshmask1 for patching
    CONST_4BYTE(FFFF0000) # mshmask2 for patching
    CONST_4BYTE(FFFFFF00) # mshmask3 for patching

LABEL($1PatchLbl0)

ZZ  # is the dest. word aligned?
    TML       r2,HEX(0003)

    JNZ       $1PatchLbl1 # no, process accordingly

ZZ
ZZ 4-bytes patching in word aligned address.
ZZ Doing so is straightforward.
ZZ
    ST        r1,0(r2)              # replace the value
    J         $1PatchLbl2

ZZ
ZZ This is patch
ZZ

LABEL($1PatchLbl1)

ZZ
ZZ Patch the MSH relative to the supplied halfword address.
ZZ
    AHI_GPR    J9SP,-PTR_SIZE
    ST_GPR     rEP,0(J9SP)
    LA         rEP,HEX(0003)            # r0 = 0x'0003'
    NR         rEP,r2           # r0 = last 2 bits of the address
    SR_GPR     r2,rEP           # align patch address
    LR_GPR     r0,r2           # copy patch address
    LM         r2,r3,0(r2)     # load 8 bytes from the patch address
    LR         r14,r1          # copy patch value
    CHI        rEP,2

    JE         $1PatchLbl3

    JH         $1PatchLbl4

    SRL        r14,8
    N          r2,eq_mshmask1(breg)

    J          $1PatchLbl5

LABEL($1PatchLbl3)

    SRL        r14,16
    N          r2,eq_mshmask2(breg)

    J          $1PatchLbl5

LABEL($1PatchLbl4)

    SRL        r14,24
    N          r2,eq_mshmask3(breg)

LABEL($1PatchLbl5)

    OR         r2,r14

ZZ
ZZ Patch the LSH relative to the supplied halfword address.
ZZ

    LR         r14,r1
    CHI        rEP,2

    JE         $1PatchLbl6

    JH         $1PatchLbl7

    SLL        r14,24
    N          r3,eq_lshmask3(breg)

    J          $1PatchLbl8

LABEL($1PatchLbl6)

    SLL        r14,16
    N          r3,eq_lshmask2(breg)

    J          $1PatchLbl8

LABEL($1PatchLbl7)

    SLL        r14,8
    N          r3,eq_lshmask1(breg)

LABEL($1PatchLbl8)

    OR         r3,r14
    LR_GPR     r14,r0
    STM        r2,r3,0(r14)
    L_GPR      rEP,0(J9SP)
    AHI_GPR    J9SP,PTR_SIZE

LABEL($1PatchLbl2)

})dnl

ZZ ============================================================
ZZ This macro is used to align the snippet base pointer.
ZZ Snippet base pointer points to the data area of a snippet.
ZZ In 32-bit, this area is word-aligned and in 64-bit, it is
ZZ double word aligned
ZZ ============================================================
ifdef({TR_HOST_64BIT},{dnl

ZZ  64-bit version
define(AlignSB,
{dnl
    TML       r14,HEX(0007)   # check alignment
    JZ        $1AlignSBExit   # exit if dbl word aligned

ZZ  advance r14 to next 8 multiple

    NILL      r14,HEX(FFF8)   # mask off last 3bits
    AGHI      r14,8           # add 8
LABEL($1AlignSBExit)
})dnl

},{dnl

ZZ  32-bit version
define(AlignSB,
{dnl
    TML       r14,HEX(0002)   # check alignment
    JZ        $1AlignSBExit   # exit if word aligned
    AHI       r14,2           # advance snippet base reg by 2-bytes
LABEL($1AlignSBExit)
})dnl

})dnl


ZZ ============================================================
ZZ The following m4 macro is used to save system stack register
ZZ after returning from native (C/C++) function
ZZ ============================================================

define(SaveSSP,
{dnl
ifdef({ASM_J9VM_JIT_FREE_SYSTEM_STACK_POINTER},{dnl
     ST_GPR  r4,eq_j9vmthr_ssp(r13)
},{dnl
})dnl
})dnl

ZZ ========================================================  #
ZZ Restore system stack register from save area
ZZ Clear the save area to indicate the SSP is now in register
ZZ ========================================================  #

define(RestoreSSP,
{dnl
ifdef({ASM_J9VM_JIT_FREE_SYSTEM_STACK_POINTER},{dnl
     L_GPR  r4,eq_j9vmthr_ssp(r13)
     xc  eq_j9vmthr_ssp(PTR_SIZE,r13),eq_j9vmthr_ssp(r13)
},{dnl
})dnl
})dnl


ZZ ============================================================
ZZ The following m4 macros are used to save/restore all registers
ZZ before and after call out to the data resolution helpers.
ZZ ============================================================

define(SaveRegs,
{dnl

ifdef({ASM_J9VM_JIT_32BIT_USES64BIT_REGISTERS},{dnl
     AHI_GPR  r5,-(32*PTR_SIZE)
     STM_GPR  r0,r15,0(r5)            # save all registers
     STMH_GPR  r0,r15,64(r5)  # save all hi regs (16*4)
},{dnl
     AHI_GPR  r5,-(16*PTR_SIZE)
     STM_GPR  r0,r15,0(r5)            # save all registers
})dnl

})dnl

ZZ ========================================================  #
ZZ In the J9 VM, the java stack starts out small and grows if
ZZ need be.  What this means is that across helper calls, the
ZZ value of the java SP can change radically. So we need to
ZZ update the java stack pointer value saved on stack after
ZZ the helper call and before restoring it.
ZZ ========================================================  #

define(RestoreRegs,
{dnl

     ST_GPR   r5,(5*PTR_SIZE)(r5)     # store the new J9SP
ifdef({ASM_J9VM_JIT_32BIT_USES64BIT_REGISTERS},{dnl
     LMH_GPR  r0,r15,64(r5) # restore all hi regs (16*4)
     LM_GPR   r0,r15,0(r5)            # restore all registers
     AHI_GPR  r5,(32*PTR_SIZE)
},{dnl
     LM_GPR   r0,r15,0(r5)            # restore all registers
     AHI_GPR  r5,(16*PTR_SIZE)
})dnl

})dnl

ZZ ============================================================
ZZ For multi-code cache support
ZZ ============================================================
ifdef({TR_HOST_64BIT},{dnl
ifdef({J9ZOS390},{dnl
},{dnl
define({MCC_SUPPORTED},{SUPPORT_MCC})
})dnl
})dnl

ZZ ============================================================
ZZ For multi-tenancy support
ZZ inputs:
ZZ r0 = old r14
ZZ r1 = data size
ZZ r2 = resolved address
ZZ output:
ZZ r2 = address of the data
ZZ ============================================================
define(MTComputeStaticAddress,{dnl

    NILL    r2,HEX(FFFE)                      # mask off clinit
    TML     r2,HEX(0002)                      # if not isolated
    JZ      LMTComputeStaticAddress$1End

    LG      r3,J9TR_VMThread_tenantData$2(,r13)
    NILL    r2,HEX(FFFC)              # chop flags from address
    LG      r2,0(,r2)                 # load tenant data index
    LLGFR   r2,r2                     # clear high word
    SRLG    r6,r2,16                  # get row index
    NILH    r2,0                      # get col index
    SLLG    r2,r2,0(r1)               # col offset, data size

IfCompressedElse({dnl
    LGR     r14,r0                    # restore r14
    LG      r1,28(,r14)               # load CP word
    SRLG    r1,r1,8                   # shift amount
    SLLG    r6,r6,2                   # row offset, compressed
    LLGF    r3,8(r6,r3)               # tenantData[row]
    SLLG    r3,r3,0(r1)               # decompress
    LA      r2,8(r2,r3)               # tenantData[row][col]
},{dnl

    SLLG    r6,r6,3                   # row offset, 64-bit data
    LG      r3,16(r6,r3)              # tenantData[row]
    LA      r2,16(r2,r3)              # tenantData[row][col]
})dnl
LABEL(LMTComputeStaticAddress$1End)
})dnl

ZZ ============================================================
ZZ Perform either the compressed or non-compressed code path.
ZZ inputs:
ZZ r13 = current J9VMThread
ZZ $1  = compressed code block
ZZ $2  = non-compressed code block
ZZ
ZZ On compressed-only builds, this evaluates to $1
ZZ On full-only builds, this evaluates to $2
ZZ On mixed builds, a runtime test is inserted and both code paths
ZZ are evaluated.
ZZ ============================================================

define({CONCAT},$1$2)dnl
define({SYM_COUNT},0)dnl
define({GENSYM},{dnl
define({SYM_COUNT},eval(1+SYM_COUNT))dnl
define({CURRENT_FULL},CONCAT(Lfull,SYM_COUNT))dnl
define({CURRENT_DONE},CONCAT(Ldone,SYM_COUNT))dnl
})dnl

ZZ The ugly formatting in this macro is due to restrictions
ZZ in the assembler regarding line length and spacing before
ZZ comments.
ZZ
ZZ The +7 below is because TM operates on a byte. The compressed
ZZ flag is in the LSB of the word, which is byte 7 on 64-bit
ZZ big endian.

define({IfCompressedElse},{dnl
ifdef({ASM_OMR_GC_COMPRESSED_POINTERS},{dnl
ifdef({ASM_OMR_GC_FULL_POINTERS},{dnl
GENSYM
 TM eval(J9TR_VMThreadCompressObjectReferences+7)(r13),1
 JZ CURRENT_FULL
$1
 J CURRENT_DONE
LABEL(CURRENT_FULL)
$2
LABEL(CURRENT_DONE)
},{$1})dnl
},{$2})dnl
})dnl
