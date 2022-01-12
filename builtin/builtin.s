	.text
	.attribute	4, 16
	.attribute	5, "rv32i2p0"
	.file	"builtin.c"
	.globl	print                           # -- Begin function print
	.p2align	2
	.type	print,@function
print:                                  # @print
	.cfi_startproc
# %bb.0:
	addi	sp, sp, -16
	.cfi_def_cfa_offset 16
	sw	ra, 12(sp)                      # 4-byte Folded Spill
	.cfi_offset ra, -4
	mv	a1, a0
	sw	a0, 8(sp)
	lui	a0, %hi(.L.str)
	addi	a0, a0, %lo(.L.str)
	call	printf 
	lw	ra, 12(sp)                      # 4-byte Folded Reload
	addi	sp, sp, 16
	ret
.Lfunc_end0:
	.size	print, .Lfunc_end0-print
	.cfi_endproc
                                        # -- End function
	.globl	println                         # -- Begin function println
	.p2align	2
	.type	println,@function
println:                                # @println
	.cfi_startproc
# %bb.0:
	addi	sp, sp, -16
	.cfi_def_cfa_offset 16
	sw	ra, 12(sp)                      # 4-byte Folded Spill
	.cfi_offset ra, -4
	mv	a1, a0
	sw	a0, 8(sp)
	lui	a0, %hi(.L.str.1)
	addi	a0, a0, %lo(.L.str.1)
	call	printf 
	lw	ra, 12(sp)                      # 4-byte Folded Reload
	addi	sp, sp, 16
	ret
.Lfunc_end1:
	.size	println, .Lfunc_end1-println
	.cfi_endproc
                                        # -- End function
	.globl	printInt                        # -- Begin function printInt
	.p2align	2
	.type	printInt,@function
printInt:                               # @printInt
	.cfi_startproc
# %bb.0:
	addi	sp, sp, -16
	.cfi_def_cfa_offset 16
	sw	ra, 12(sp)                      # 4-byte Folded Spill
	.cfi_offset ra, -4
	mv	a1, a0
	sw	a0, 8(sp)
	lui	a0, %hi(.L.str.2)
	addi	a0, a0, %lo(.L.str.2)
	call	printf 
	lw	ra, 12(sp)                      # 4-byte Folded Reload
	addi	sp, sp, 16
	ret
.Lfunc_end2:
	.size	printInt, .Lfunc_end2-printInt
	.cfi_endproc
                                        # -- End function
	.globl	printlnInt                      # -- Begin function printlnInt
	.p2align	2
	.type	printlnInt,@function
printlnInt:                             # @printlnInt
	.cfi_startproc
# %bb.0:
	addi	sp, sp, -16
	.cfi_def_cfa_offset 16
	sw	ra, 12(sp)                      # 4-byte Folded Spill
	.cfi_offset ra, -4
	mv	a1, a0
	sw	a0, 8(sp)
	lui	a0, %hi(.L.str.3)
	addi	a0, a0, %lo(.L.str.3)
	call	printf 
	lw	ra, 12(sp)                      # 4-byte Folded Reload
	addi	sp, sp, 16
	ret
.Lfunc_end3:
	.size	printlnInt, .Lfunc_end3-printlnInt
	.cfi_endproc
                                        # -- End function
	.globl	getString                       # -- Begin function getString
	.p2align	2
	.type	getString,@function
getString:                              # @getString
	.cfi_startproc
# %bb.0:
	addi	sp, sp, -16
	.cfi_def_cfa_offset 16
	sw	ra, 12(sp)                      # 4-byte Folded Spill
	.cfi_offset ra, -4
	addi	a0, zero, 1024
	mv	a1, zero
	call	malloc 
	mv	a1, a0
	sw	a0, 8(sp)
	lui	a0, %hi(.L.str)
	addi	a0, a0, %lo(.L.str)
	call	scanf 
	lw	a0, 8(sp)
	lw	ra, 12(sp)                      # 4-byte Folded Reload
	addi	sp, sp, 16
	ret
.Lfunc_end4:
	.size	getString, .Lfunc_end4-getString
	.cfi_endproc
                                        # -- End function
	.globl	getInt                          # -- Begin function getInt
	.p2align	2
	.type	getInt,@function
getInt:                                 # @getInt
	.cfi_startproc
# %bb.0:
	addi	sp, sp, -16
	.cfi_def_cfa_offset 16
	sw	ra, 12(sp)                      # 4-byte Folded Spill
	.cfi_offset ra, -4
	lui	a0, %hi(.L.str.2)
	addi	a0, a0, %lo(.L.str.2)
	addi	a1, sp, 8
	call	scanf 
	lw	a0, 8(sp)
	lw	ra, 12(sp)                      # 4-byte Folded Reload
	addi	sp, sp, 16
	ret
.Lfunc_end5:
	.size	getInt, .Lfunc_end5-getInt
	.cfi_endproc
                                        # -- End function
	.globl	toString                        # -- Begin function toString
	.p2align	2
	.type	toString,@function
toString:                               # @toString
	.cfi_startproc
# %bb.0:
	addi	sp, sp, -16
	.cfi_def_cfa_offset 16
	sw	ra, 12(sp)                      # 4-byte Folded Spill
	.cfi_offset ra, -4
	sw	a0, 8(sp)
	addi	a0, zero, 20
	mv	a1, zero
	call	malloc 
	lw	a5, 8(sp)
	sw	a0, 0(sp)
	lui	a1, %hi(.L.str.2)
	addi	a4, a1, %lo(.L.str.2)
	addi	a2, zero, -1
	addi	a3, zero, -1
	mv	a1, zero
	call	sprintf
	lw	a0, 0(sp)
	lw	ra, 12(sp)                      # 4-byte Folded Reload
	addi	sp, sp, 16
	ret
.Lfunc_end6:
	.size	toString, .Lfunc_end6-toString
	.cfi_endproc
                                        # -- End function
	.globl	__mx_concatenateString          # -- Begin function __mx_concatenateString
	.p2align	2
	.type	__mx_concatenateString,@function
__mx_concatenateString:                 # @__mx_concatenateString
	.cfi_startproc
# %bb.0:
	addi	sp, sp, -32
	.cfi_def_cfa_offset 32
	sw	ra, 28(sp)                      # 4-byte Folded Spill
	sw	s0, 24(sp)                      # 4-byte Folded Spill
	.cfi_offset ra, -4
	.cfi_offset s0, -8
	sw	a0, 16(sp)
	sw	a1, 8(sp)
	call	strlen 
	lw	a1, 8(sp)
	mv	s0, a0
	mv	a0, a1
	call	strlen 
	add	a0, s0, a0
	addi	a0, a0, 1
	sw	a0, 4(sp)
	srai	a1, a0, 31
	call	malloc 
	sw	a0, 0(sp)
	sb	zero, 0(a0)
	lw	a0, 0(sp)
	lw	a1, 16(sp)
	addi	a2, zero, -1
	addi	a3, zero, -1
	call	strcat
	lw	a0, 0(sp)
	lw	a1, 8(sp)
	addi	a2, zero, -1
	addi	a3, zero, -1
	call	strcat
	lw	a0, 0(sp)
	lw	a1, 4(sp)
	add	a0, a1, a0
	sb	zero, -1(a0)
	lw	a0, 0(sp)
	lw	s0, 24(sp)                      # 4-byte Folded Reload
	lw	ra, 28(sp)                      # 4-byte Folded Reload
	addi	sp, sp, 32
	ret
.Lfunc_end7:
	.size	__mx_concatenateString, .Lfunc_end7-__mx_concatenateString
	.cfi_endproc
                                        # -- End function
	.globl	__mx_stringLt                   # -- Begin function __mx_stringLt
	.p2align	2
	.type	__mx_stringLt,@function
__mx_stringLt:                          # @__mx_stringLt
	.cfi_startproc
# %bb.0:
	addi	sp, sp, -16
	.cfi_def_cfa_offset 16
	sw	ra, 12(sp)                      # 4-byte Folded Spill
	.cfi_offset ra, -4
	sw	a0, 8(sp)
	sw	a1, 0(sp)
	call	strcmp 
	srli	a0, a0, 31
	lw	ra, 12(sp)                      # 4-byte Folded Reload
	addi	sp, sp, 16
	ret
.Lfunc_end8:
	.size	__mx_stringLt, .Lfunc_end8-__mx_stringLt
	.cfi_endproc
                                        # -- End function
	.globl	__mx_stringLe                   # -- Begin function __mx_stringLe
	.p2align	2
	.type	__mx_stringLe,@function
__mx_stringLe:                          # @__mx_stringLe
	.cfi_startproc
# %bb.0:
	addi	sp, sp, -16
	.cfi_def_cfa_offset 16
	sw	ra, 12(sp)                      # 4-byte Folded Spill
	.cfi_offset ra, -4
	sw	a0, 8(sp)
	sw	a1, 0(sp)
	call	strcmp 
	slti	a0, a0, 1
	lw	ra, 12(sp)                      # 4-byte Folded Reload
	addi	sp, sp, 16
	ret
.Lfunc_end9:
	.size	__mx_stringLe, .Lfunc_end9-__mx_stringLe
	.cfi_endproc
                                        # -- End function
	.globl	__mx_stringGt                   # -- Begin function __mx_stringGt
	.p2align	2
	.type	__mx_stringGt,@function
__mx_stringGt:                          # @__mx_stringGt
	.cfi_startproc
# %bb.0:
	addi	sp, sp, -16
	.cfi_def_cfa_offset 16
	sw	ra, 12(sp)                      # 4-byte Folded Spill
	.cfi_offset ra, -4
	sw	a0, 8(sp)
	sw	a1, 0(sp)
	call	strcmp 
	sgtz	a0, a0
	lw	ra, 12(sp)                      # 4-byte Folded Reload
	addi	sp, sp, 16
	ret
.Lfunc_end10:
	.size	__mx_stringGt, .Lfunc_end10-__mx_stringGt
	.cfi_endproc
                                        # -- End function
	.globl	__mx_stringGe                   # -- Begin function __mx_stringGe
	.p2align	2
	.type	__mx_stringGe,@function
__mx_stringGe:                          # @__mx_stringGe
	.cfi_startproc
# %bb.0:
	addi	sp, sp, -16
	.cfi_def_cfa_offset 16
	sw	ra, 12(sp)                      # 4-byte Folded Spill
	.cfi_offset ra, -4
	sw	a0, 8(sp)
	sw	a1, 0(sp)
	call	strcmp 
	not	a0, a0
	srli	a0, a0, 31
	lw	ra, 12(sp)                      # 4-byte Folded Reload
	addi	sp, sp, 16
	ret
.Lfunc_end11:
	.size	__mx_stringGe, .Lfunc_end11-__mx_stringGe
	.cfi_endproc
                                        # -- End function
	.globl	__mx_stringEq                   # -- Begin function __mx_stringEq
	.p2align	2
	.type	__mx_stringEq,@function
__mx_stringEq:                          # @__mx_stringEq
	.cfi_startproc
# %bb.0:
	addi	sp, sp, -16
	.cfi_def_cfa_offset 16
	sw	ra, 12(sp)                      # 4-byte Folded Spill
	.cfi_offset ra, -4
	sw	a0, 8(sp)
	sw	a1, 0(sp)
	call	strcmp 
	seqz	a0, a0
	lw	ra, 12(sp)                      # 4-byte Folded Reload
	addi	sp, sp, 16
	ret
.Lfunc_end12:
	.size	__mx_stringEq, .Lfunc_end12-__mx_stringEq
	.cfi_endproc
                                        # -- End function
	.globl	__mx_stringNe                   # -- Begin function __mx_stringNe
	.p2align	2
	.type	__mx_stringNe,@function
__mx_stringNe:                          # @__mx_stringNe
	.cfi_startproc
# %bb.0:
	addi	sp, sp, -16
	.cfi_def_cfa_offset 16
	sw	ra, 12(sp)                      # 4-byte Folded Spill
	.cfi_offset ra, -4
	sw	a0, 8(sp)
	sw	a1, 0(sp)
	call	strcmp 
	snez	a0, a0
	lw	ra, 12(sp)                      # 4-byte Folded Reload
	addi	sp, sp, 16
	ret
.Lfunc_end13:
	.size	__mx_stringNe, .Lfunc_end13-__mx_stringNe
	.cfi_endproc
                                        # -- End function
	.globl	__mx_stringLength               # -- Begin function __mx_stringLength
	.p2align	2
	.type	__mx_stringLength,@function
__mx_stringLength:                      # @__mx_stringLength
	.cfi_startproc
# %bb.0:
	addi	sp, sp, -16
	.cfi_def_cfa_offset 16
	sw	ra, 12(sp)                      # 4-byte Folded Spill
	.cfi_offset ra, -4
	sw	a0, 8(sp)
	call	strlen 
	lw	ra, 12(sp)                      # 4-byte Folded Reload
	addi	sp, sp, 16
	ret
.Lfunc_end14:
	.size	__mx_stringLength, .Lfunc_end14-__mx_stringLength
	.cfi_endproc
                                        # -- End function
	.globl	__mx_stringSubstring            # -- Begin function __mx_stringSubstring
	.p2align	2
	.type	__mx_stringSubstring,@function
__mx_stringSubstring:                   # @__mx_stringSubstring
	.cfi_startproc
# %bb.0:
	addi	sp, sp, -32
	.cfi_def_cfa_offset 32
	sw	ra, 28(sp)                      # 4-byte Folded Spill
	.cfi_offset ra, -4
	sw	a0, 24(sp)
	sw	a1, 20(sp)
	sw	a2, 16(sp)
	sub	a0, a2, a1
	addi	a0, a0, 1
	sw	a0, 12(sp)
	srai	a1, a0, 31
	call	malloc 
	lw	a1, 24(sp)
	lw	a3, 20(sp)
	lw	a2, 12(sp)
	sw	a0, 8(sp)
	add	a1, a1, a3
	srai	a3, a2, 31
	addi	a4, zero, -1
	addi	a5, zero, -1
	call	memcpy
	lw	a0, 8(sp)
	lw	a1, 12(sp)
	add	a0, a1, a0
	sb	zero, -1(a0)
	lw	a0, 8(sp)
	lw	ra, 28(sp)                      # 4-byte Folded Reload
	addi	sp, sp, 32
	ret
.Lfunc_end15:
	.size	__mx_stringSubstring, .Lfunc_end15-__mx_stringSubstring
	.cfi_endproc
                                        # -- End function
	.globl	__mx_stringParseInt             # -- Begin function __mx_stringParseInt
	.p2align	2
	.type	__mx_stringParseInt,@function
__mx_stringParseInt:                    # @__mx_stringParseInt
	.cfi_startproc
# %bb.0:
	addi	sp, sp, -16
	.cfi_def_cfa_offset 16
	sw	ra, 12(sp)                      # 4-byte Folded Spill
	.cfi_offset ra, -4
	sw	a0, 8(sp)
	lui	a1, %hi(.L.str.2)
	addi	a1, a1, %lo(.L.str.2)
	addi	a2, sp, 4
	call	sscanf 
	lw	a0, 4(sp)
	lw	ra, 12(sp)                      # 4-byte Folded Reload
	addi	sp, sp, 16
	ret
.Lfunc_end16:
	.size	__mx_stringParseInt, .Lfunc_end16-__mx_stringParseInt
	.cfi_endproc
                                        # -- End function
	.globl	__mx_stringOrd                  # -- Begin function __mx_stringOrd
	.p2align	2
	.type	__mx_stringOrd,@function
__mx_stringOrd:                         # @__mx_stringOrd
	.cfi_startproc
# %bb.0:
	addi	sp, sp, -16
	.cfi_def_cfa_offset 16
	sw	a0, 8(sp)
	sw	a1, 4(sp)
	add	a0, a0, a1
	lb	a0, 0(a0)
	addi	sp, sp, 16
	ret
.Lfunc_end17:
	.size	__mx_stringOrd, .Lfunc_end17-__mx_stringOrd
	.cfi_endproc
                                        # -- End function
	.globl	__mx_malloc                     # -- Begin function __mx_malloc
	.p2align	2
	.type	__mx_malloc,@function
__mx_malloc:                            # @__mx_malloc
	.cfi_startproc
# %bb.0:
	addi	sp, sp, -16
	.cfi_def_cfa_offset 16
	sw	ra, 12(sp)                      # 4-byte Folded Spill
	.cfi_offset ra, -4
	sw	a0, 8(sp)
	srai	a1, a0, 31
	call	malloc 
	lw	ra, 12(sp)                      # 4-byte Folded Reload
	addi	sp, sp, 16
	ret
.Lfunc_end18:
	.size	__mx_malloc, .Lfunc_end18-__mx_malloc
	.cfi_endproc
                                        # -- End function
	.type	.L.str,@object                  # @.str
	.section	.rodata.str1.1,"aMS",@progbits,1
.L.str:
	.asciz	"%s"
	.size	.L.str, 3

	.type	.L.str.1,@object                # @.str.1
.L.str.1:
	.asciz	"%s\n"
	.size	.L.str.1, 4

	.type	.L.str.2,@object                # @.str.2
.L.str.2:
	.asciz	"%d"
	.size	.L.str.2, 3

	.type	.L.str.3,@object                # @.str.3
.L.str.3:
	.asciz	"%d\n"
	.size	.L.str.3, 4

	.ident	"clang version 10.0.0-4ubuntu1 "
	.section	".note.GNU-stack","",@progbits
