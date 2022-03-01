	.file	"builtin 2.c"
	.option nopic
	.attribute arch, "rv32i2p0"
	.attribute unaligned_access, 0
	.attribute stack_align, 16
	.text
	.section	.rodata.str1.4,"aMS",@progbits,1
	.align	2
.LC0:
	.string	"%s"
	.text
	.align	2
	.globl	print
	.type	print, @function
print:
	mv	a1,a0
	lui	a0,%hi(.LC0)
	addi	a0,a0,%lo(.LC0)
	tail	printf
	.size	print, .-print
	.align	2
	.globl	println
	.type	println, @function
println:
	tail	puts
	.size	println, .-println
	.section	.rodata.str1.4
	.align	2
.LC1:
	.string	"%d"
	.text
	.align	2
	.globl	printInt
	.type	printInt, @function
printInt:
	mv	a1,a0
	lui	a0,%hi(.LC1)
	addi	a0,a0,%lo(.LC1)
	tail	printf
	.size	printInt, .-printInt
	.section	.rodata.str1.4
	.align	2
.LC2:
	.string	"%d\n"
	.text
	.align	2
	.globl	printlnInt
	.type	printlnInt, @function
printlnInt:
	mv	a1,a0
	lui	a0,%hi(.LC2)
	addi	a0,a0,%lo(.LC2)
	tail	printf
	.size	printlnInt, .-printlnInt
	.align	2
	.globl	getString
	.type	getString, @function
getString:
	addi	sp,sp,-16
	li	a0,1024
	sw	ra,12(sp)
	sw	s0,8(sp)
	call	malloc
	mv	s0,a0
	mv	a1,a0
	lui	a0,%hi(.LC0)
	addi	a0,a0,%lo(.LC0)
	call	scanf
	lw	ra,12(sp)
	mv	a0,s0
	lw	s0,8(sp)
	addi	sp,sp,16
	jr	ra
	.size	getString, .-getString
	.align	2
	.globl	getInt
	.type	getInt, @function
getInt:
	addi	sp,sp,-32
	lui	a0,%hi(.LC1)
	addi	a1,sp,12
	addi	a0,a0,%lo(.LC1)
	sw	ra,28(sp)
	call	scanf
	lw	ra,28(sp)
	lw	a0,12(sp)
	addi	sp,sp,32
	jr	ra
	.size	getInt, .-getInt
	.align	2
	.globl	toString
	.type	toString, @function
toString:
	addi	sp,sp,-16
	sw	s1,4(sp)
	mv	s1,a0
	li	a0,20
	sw	ra,12(sp)
	sw	s0,8(sp)
	call	malloc
	lui	a1,%hi(.LC1)
	mv	a2,s1
	addi	a1,a1,%lo(.LC1)
	mv	s0,a0
	call	sprintf
	lw	ra,12(sp)
	mv	a0,s0
	lw	s0,8(sp)
	lw	s1,4(sp)
	addi	sp,sp,16
	jr	ra
	.size	toString, .-toString
	.align	2
	.globl	__mx_concatenateString
	.type	__mx_concatenateString, @function
__mx_concatenateString:
	addi	sp,sp,-32
	sw	ra,28(sp)
	sw	s0,24(sp)
	sw	s1,20(sp)
	sw	s2,16(sp)
	sw	s3,12(sp)
	sw	s4,8(sp)
	sw	s5,4(sp)
	mv	s3,a1
	mv	s5,a0
	call	strlen
	mv	s1,a0
	mv	a0,s3
	call	strlen
	add	s2,s1,a0
	mv	s4,a0
	addi	a0,s2,1
	call	malloc
	mv	a2,s1
	mv	a1,s5
	mv	s0,a0
	call	memcpy
	add	a0,s0,s1
	add	s2,s0,s2
	addi	a2,s4,1
	mv	a1,s3
	call	memcpy
	sb	zero,0(s2)
	lw	ra,28(sp)
	mv	a0,s0
	lw	s0,24(sp)
	lw	s1,20(sp)
	lw	s2,16(sp)
	lw	s3,12(sp)
	lw	s4,8(sp)
	lw	s5,4(sp)
	addi	sp,sp,32
	jr	ra
	.size	__mx_concatenateString, .-__mx_concatenateString
	.align	2
	.globl	__mx_stringLt
	.type	__mx_stringLt, @function
__mx_stringLt:
	addi	sp,sp,-16
	sw	ra,12(sp)
	call	strcmp
	lw	ra,12(sp)
	srli	a0,a0,31
	addi	sp,sp,16
	jr	ra
	.size	__mx_stringLt, .-__mx_stringLt
	.align	2
	.globl	__mx_stringLe
	.type	__mx_stringLe, @function
__mx_stringLe:
	addi	sp,sp,-16
	sw	ra,12(sp)
	call	strcmp
	lw	ra,12(sp)
	slti	a0,a0,1
	addi	sp,sp,16
	jr	ra
	.size	__mx_stringLe, .-__mx_stringLe
	.align	2
	.globl	__mx_stringGt
	.type	__mx_stringGt, @function
__mx_stringGt:
	addi	sp,sp,-16
	sw	ra,12(sp)
	call	strcmp
	lw	ra,12(sp)
	sgt	a0,a0,zero
	addi	sp,sp,16
	jr	ra
	.size	__mx_stringGt, .-__mx_stringGt
	.align	2
	.globl	__mx_stringGe
	.type	__mx_stringGe, @function
__mx_stringGe:
	addi	sp,sp,-16
	sw	ra,12(sp)
	call	strcmp
	lw	ra,12(sp)
	not	a0,a0
	srli	a0,a0,31
	addi	sp,sp,16
	jr	ra
	.size	__mx_stringGe, .-__mx_stringGe
	.align	2
	.globl	__mx_stringEq
	.type	__mx_stringEq, @function
__mx_stringEq:
	addi	sp,sp,-16
	sw	ra,12(sp)
	call	strcmp
	lw	ra,12(sp)
	seqz	a0,a0
	addi	sp,sp,16
	jr	ra
	.size	__mx_stringEq, .-__mx_stringEq
	.align	2
	.globl	__mx_stringNe
	.type	__mx_stringNe, @function
__mx_stringNe:
	addi	sp,sp,-16
	sw	ra,12(sp)
	call	strcmp
	lw	ra,12(sp)
	snez	a0,a0
	addi	sp,sp,16
	jr	ra
	.size	__mx_stringNe, .-__mx_stringNe
	.align	2
	.globl	__mx_stringLength
	.type	__mx_stringLength, @function
__mx_stringLength:
	tail	strlen
	.size	__mx_stringLength, .-__mx_stringLength
	.align	2
	.globl	__mx_stringSubstring
	.type	__mx_stringSubstring, @function
__mx_stringSubstring:
	addi	sp,sp,-32
	sw	s0,24(sp)
	sub	s0,a2,a1
	sw	s4,8(sp)
	addi	s4,s0,1
	sw	s3,12(sp)
	mv	s3,a0
	mv	a0,s4
	sw	ra,28(sp)
	sw	s1,20(sp)
	sw	s2,16(sp)
	mv	s2,a1
	call	malloc
	mv	s1,a0
	add	a1,s3,s2
	add	s0,s1,s0
	mv	a2,s4
	call	memcpy
	sb	zero,0(s0)
	lw	ra,28(sp)
	lw	s0,24(sp)
	lw	s2,16(sp)
	lw	s3,12(sp)
	lw	s4,8(sp)
	mv	a0,s1
	lw	s1,20(sp)
	addi	sp,sp,32
	jr	ra
	.size	__mx_stringSubstring, .-__mx_stringSubstring
	.align	2
	.globl	__mx_stringParseInt
	.type	__mx_stringParseInt, @function
__mx_stringParseInt:
	addi	sp,sp,-32
	lui	a1,%hi(.LC1)
	addi	a2,sp,12
	addi	a1,a1,%lo(.LC1)
	sw	ra,28(sp)
	call	sscanf
	lw	ra,28(sp)
	lw	a0,12(sp)
	addi	sp,sp,32
	jr	ra
	.size	__mx_stringParseInt, .-__mx_stringParseInt
	.align	2
	.globl	__mx_stringOrd
	.type	__mx_stringOrd, @function
__mx_stringOrd:
	add	a0,a0,a1
	lbu	a0,0(a0)
	ret
	.size	__mx_stringOrd, .-__mx_stringOrd
	.align	2
	.globl	__mx_malloc
	.type	__mx_malloc, @function
__mx_malloc:
	tail	malloc
	.size	__mx_malloc, .-__mx_malloc
	.ident	"GCC: (GNU) 11.1.0"
