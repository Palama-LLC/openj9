/*******************************************************************************
 * Copyright (c) 2000, 2023 IBM Corp. and others
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

#ifndef TR_S390J9CALLSNIPPET_INCL
#define TR_S390J9CALLSNIPPET_INCL

#include "z/codegen/CallSnippet.hpp"
#include "z/codegen/ConstantDataSnippet.hpp"
#include "z/codegen/S390Instruction.hpp"

class TR_MHJ2IThunk;
namespace TR { class CodeGenerator; }
namespace TR { class LabelSymbol; }
namespace TR { class Node; }

namespace TR {

class S390J9CallSnippet : public TR::S390CallSnippet
   {
   public:

   S390J9CallSnippet(
         TR::CodeGenerator *cg,
         TR::Node *n,
         TR::LabelSymbol *lab,
         int32_t s) :
      TR::S390CallSnippet(cg, n, lab, s)
      {}

   S390J9CallSnippet(
         TR::CodeGenerator *cg,
         TR::Node *n,
         TR::LabelSymbol *lab,
         TR::SymbolReference *symRef,
         int32_t s) :
      TR::S390CallSnippet(cg, n, lab, symRef, s) {}


   static uint8_t *generateVIThunk(TR::Node *callNode, int32_t argSize, TR::CodeGenerator *cg);
   static TR_MHJ2IThunk *generateInvokeExactJ2IThunk(TR::Node *callNode, int32_t argSize, char* signature, TR::CodeGenerator *cg);

   TR_RuntimeHelper getInterpretedDispatchHelper(TR::SymbolReference *methodSymRef, TR::DataType type);

   uint8_t *generatePICBinary(uint8_t *cursor, TR::SymbolReference *glueRef);
   uint32_t getPICBinaryLength();
   virtual uint32_t getLength(int32_t estimatedSnippetStart);

   virtual void print(TR::FILE *pOutFile, TR_Debug *debug);

   virtual uint8_t *emitSnippetBody();
   };


class S390UnresolvedCallSnippet : public TR::S390J9CallSnippet
   {

   public:

   S390UnresolvedCallSnippet(TR::CodeGenerator *cg, TR::Node *c, TR::LabelSymbol *lab, int32_t s)
      : TR::S390J9CallSnippet(cg, c, lab, s)
      {
      }

   virtual Kind getKind() { return IsUnresolvedCall; }

   virtual uint8_t *emitSnippetBody();

   virtual uint32_t getLength(int32_t estimatedSnippetStart);
   };


class S390VirtualSnippet : public TR::S390J9CallSnippet
   {
   public:

   S390VirtualSnippet(TR::CodeGenerator *cg, TR::Node *c, TR::LabelSymbol *lab, int32_t s)
      : TR::S390J9CallSnippet(cg, c, lab, s)
      {
      }

   virtual Kind getKind() { return IsVirtual; }

   virtual uint8_t *emitSnippetBody();

   virtual uint32_t getLength(int32_t estimatedSnippetStart);
   };


class S390VirtualUnresolvedSnippet : public TR::S390VirtualSnippet
   {
   void *thunkAddress;

   public:

   S390VirtualUnresolvedSnippet(TR::CodeGenerator *cg, TR::Node *c, TR::LabelSymbol *lab, int32_t s)
      : TR::S390VirtualSnippet(cg, c, lab, s), thunkAddress(NULL)
      {
      }

   S390VirtualUnresolvedSnippet(TR::CodeGenerator *cg, TR::Node *c, TR::LabelSymbol *lab, int32_t s, void *thunkPtr)
      : TR::S390VirtualSnippet(cg, c, lab, s), thunkAddress(thunkPtr)
      {
      }

   virtual Kind getKind() { return IsVirtualUnresolved; }

   virtual uint8_t *emitSnippetBody();

   virtual uint32_t getLength(int32_t estimatedSnippetStart);
   void* getJ2IThunkAddress() { return thunkAddress; }

   TR::Instruction *patchVftInstruction;
   TR::Instruction *indirectCallInstruction;    // the BASR in the virtual dispatch sequence

   TR::Instruction *setPatchVftInstruction(TR::Instruction *i) {return patchVftInstruction=i;}
   TR::Instruction *getPatchVftInstruction() {return patchVftInstruction;}

   TR::Instruction *setIndirectCallInstruction(TR::Instruction *i) {return indirectCallInstruction = i;}
   TR::Instruction *getIndirectCallInstruction() {return indirectCallInstruction;}
   };

class J9S390InterfaceCallDataSnippet : public TR::S390ConstantDataSnippet
   {
   TR::Instruction * _firstCLFI;
   uint8_t _numInterfaceCallCacheSlots;
   uint8_t* _codeRA;
   void *_thunkAddress;
   bool _useCLFIandBRCL;

   public:
   J9S390InterfaceCallDataSnippet(TR::CodeGenerator *,
                                  TR::Node *,
                                  uint8_t,
                                  void *,
                                  bool useCLFIandBRCL = false);

   virtual Kind getKind() { return IsInterfaceCallData; }
   virtual uint8_t *emitSnippetBody();
   virtual uint32_t getLength(int32_t estimatedSnippetStart);

   int8_t getNumInterfaceCallCacheSlots() {return _numInterfaceCallCacheSlots;}

   void setUseCLFIandBRCL(bool useCLFIandBRCL) {_useCLFIandBRCL = useCLFIandBRCL;}
   bool isUseCLFIandBRCL() {return _useCLFIandBRCL;}

   void setFirstCLFI(TR::Instruction* firstCLFI) { _firstCLFI = firstCLFI; }
   TR::Instruction* getFirstCLFI() { return _firstCLFI;}

   uint8_t* getCodeRA() { return _codeRA;}
   uint8_t* setCodeRA(uint8_t *codeRA)
      {
      return _codeRA = codeRA;
      }

   virtual uint32_t getCallReturnAddressOffset();
   virtual uint32_t getSingleDynamicSlotOffset();
   virtual uint32_t getLastCachedSlotFieldOffset();
   virtual uint32_t getFirstSlotFieldOffset();
   virtual uint32_t getLastSlotFieldOffset();
   virtual uint32_t getFirstSlotOffset();
   virtual uint32_t getLastSlotOffset();
   };

class S390JNICallDataSnippet : public TR::S390ConstantDataSnippet
   {
   /** Base register for this snippet */
   TR::Register *  _baseRegister;

   //for JNI Callout frame
   uintptr_t _ramMethod;
   uintptr_t _JNICallOutFrameFlags;
   TR::LabelSymbol * _returnFromJNICallLabel;  //for savedCP slot
   uintptr_t _savedPC; // This is unused, and hence zero
   uintptr_t _tagBits;

   // VMThread setup
   uintptr_t _pc;
   uintptr_t _literals;
   uintptr_t _jitStackFrameFlags;

   //for releaseVMaccess
   uintptr_t _constReleaseVMAccessMask;
   uintptr_t _constReleaseVMAccessOutOfLineMask;

   /** For CallNativeFunction */
   uintptr_t _targetAddress;


   public:

  S390JNICallDataSnippet(TR::CodeGenerator *,
                                  TR::Node *);

   virtual Kind getKind() { return IsJNICallData; }
   virtual uint8_t *emitSnippetBody();
   virtual void print(TR::FILE *, TR_Debug*);
   void setBaseRegister(TR::Register * aValue){ _baseRegister = aValue; }
   TR::Register * getBaseRegister() { return _baseRegister; }

   void setRAMMethod(uintptr_t aValue){ _ramMethod = aValue; }
   void setJNICallOutFrameFlags(uintptr_t aValue){ _JNICallOutFrameFlags = aValue; }
   void setReturnFromJNICall( TR::LabelSymbol * aValue){ _returnFromJNICallLabel = aValue; }
   void setSavedPC(uintptr_t aValue){ _savedPC = aValue; }
   void setTagBits(uintptr_t aValue){ _tagBits = aValue; }

   void setPC(uintptr_t aValue){ _pc = aValue; }
   void setLiterals(uintptr_t aValue){ _literals = aValue; }
   void setJitStackFrameFlags(uintptr_t aValue){ _jitStackFrameFlags = aValue; }

   void setConstReleaseVMAccessMask(uintptr_t aValue){ _constReleaseVMAccessMask = aValue; }
   void setConstReleaseVMAccessOutOfLineMask(uintptr_t aValue){ _constReleaseVMAccessOutOfLineMask = aValue; }
   void setTargetAddress(uintptr_t aValue){ _targetAddress = aValue; }

   uint32_t getJNICallOutFrameDataOffset(){ return 0; }
   uint32_t getRAMMethodOffset(){ return 0; }
   uint32_t getJNICallOutFrameFlagsOffset();
   uint32_t getReturnFromJNICallOffset();
   uint32_t getSavedPCOffset();
   uint32_t getTagBitsOffset();

   uint32_t getPCOffset();
   uint32_t getLiteralsOffset();
   uint32_t getJitStackFrameFlagsOffset();

   uint32_t getConstReleaseVMAccessMaskOffset();
   uint32_t getConstReleaseVMAccessOutOfLineMaskOffset();

   uint32_t getTargetAddressOffset();

   uint32_t getLength(int32_t estimatedSnippetStart);
   };


class S390InterfaceCallSnippet : public TR::S390VirtualSnippet
   {
   TR::J9S390InterfaceCallDataSnippet * _dataSnippet;
   int8_t _numInterfaceCallCacheSlots;
   bool _useCLFIandBRCL;

   public:

   S390InterfaceCallSnippet(TR::CodeGenerator *cg, TR::Node *c, TR::LabelSymbol *lab, int32_t s, int8_t n, void *thunkPtr, bool useCLFIandBRCL = false);

   virtual Kind getKind() { return IsInterfaceCall; }
   int8_t getNumInterfaceCallCacheSlots() {return _numInterfaceCallCacheSlots;}
   void setUseCLFIandBRCL(bool useCLFIandBRCL) {
      _useCLFIandBRCL = useCLFIandBRCL;
      if (getDataConstantSnippet() != NULL)
         {
         getDataConstantSnippet()->setUseCLFIandBRCL(useCLFIandBRCL);
         }
      }
   bool isUseCLFIandBRCL() {return _useCLFIandBRCL;}

   TR::J9S390InterfaceCallDataSnippet *getDataConstantSnippet() { return _dataSnippet; }
   TR::J9S390InterfaceCallDataSnippet *setDataConstantSnippet(TR::J9S390InterfaceCallDataSnippet *snippet)
      {
      return _dataSnippet = snippet;
      }

   virtual uint32_t getLength(int32_t estimatedSnippetStart);
   virtual uint8_t *emitSnippetBody();

   };
}

#endif
