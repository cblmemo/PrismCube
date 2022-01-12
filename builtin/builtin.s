	.file	"builtinq.c"
	.option nopic
	.attribute arch, "rv32i2p0"
	.attribute unaligned_access, 0
	.attribute stack_align, 16
	.text
	.section	.rodata
	.align	2
.LC0:
	.string	"%s"
	.text
	.align	2
	.globl	print
	.type	print, @function
print:
	addi	sp,sp,-32
	sw	ra,28(sp)
	sw	s0,24(sp)
	addi	s0,sp,32
	sw	a0,-20(s0)
	lw	a1,-20(s0)
	lui	a5,%hi(.LC0)
	addi	a0,a5,%lo(.LC0)
	call	printf
	nop
	lw	ra,28(sp)
	lw	s0,24(sp)
	addi	sp,sp,32
	jr	ra
	.size	print, .-print
	.align	2
	.globl	println
	.type	println, @function
println:
	addi	sp,sp,-32
	sw	ra,28(sp)
	sw	s0,24(sp)
	addi	s0,sp,32
	sw	a0,-20(s0)
	lw	a0,-20(s0)
	call	puts
	nop
	lw	ra,28(sp)
	lw	s0,24(sp)
	addi	sp,sp,32
	jr	ra
	.size	println, .-println
	.section	.rodata
	.align	2
.LC1:
	.string	"%d"
	.text
	.align	2
	.globl	printInt
	.type	printInt, @function
printInt:
	addi	sp,sp,-32
	sw	ra,28(sp)
	sw	s0,24(sp)
	addi	s0,sp,32
	sw	a0,-20(s0)
	lw	a1,-20(s0)
	lui	a5,%hi(.LC1)
	addi	a0,a5,%lo(.LC1)
	call	printf
	nop
	lw	ra,28(sp)
	lw	s0,24(sp)
	addi	sp,sp,32
	jr	ra
	.size	printInt, .-printInt
	.section	.rodata
	.align	2
.LC2:
	.string	"%d\n"
	.text
	.align	2
	.globl	printlnInt
	.type	printlnInt, @function
printlnInt:
	addi	sp,sp,-32
	sw	ra,28(sp)
	sw	s0,24(sp)
	addi	s0,sp,32
	sw	a0,-20(s0)
	lw	a1,-20(s0)
	lui	a5,%hi(.LC2)
	addi	a0,a5,%lo(.LC2)
	call	printf
	nop
	lw	ra,28(sp)
	lw	s0,24(sp)
	addi	sp,sp,32
	jr	ra
	.size	printlnInt, .-printlnInt
	.align	2
	.globl	getString
	.type	getString, @function
getString:
	addi	sp,sp,-32
	sw	ra,28(sp)
	sw	s0,24(sp)
	addi	s0,sp,32
	li	a0,1024
	call	malloc
	mv	a5,a0
	sw	a5,-20(s0)
	lw	a1,-20(s0)
	lui	a5,%hi(.LC0)
	addi	a0,a5,%lo(.LC0)
	call	scanf
	lw	a5,-20(s0)
	mv	a0,a5
	lw	ra,28(sp)
	lw	s0,24(sp)
	addi	sp,sp,32
	jr	ra
	.size	getString, .-getString
	.align	2
	.globl	getInt
	.type	getInt, @function
getInt:
	addi	sp,sp,-32
	sw	ra,28(sp)
	sw	s0,24(sp)
	addi	s0,sp,32
	addi	a5,s0,-20
	mv	a1,a5
	lui	a5,%hi(.LC1)
	addi	a0,a5,%lo(.LC1)
	call	scanf
	lw	a5,-20(s0)
	mv	a0,a5
	lw	ra,28(sp)
	lw	s0,24(sp)
	addi	sp,sp,32
	jr	ra
	.size	getInt, .-getInt
	.align	2
	.globl	toString
	.type	toString, @function
toString:
	addi	sp,sp,-48
	sw	ra,44(sp)
	sw	s0,40(sp)
	addi	s0,sp,48
	sw	a0,-36(s0)
	li	a0,20
	call	malloc
	mv	a5,a0
	sw	a5,-20(s0)
	lw	a2,-36(s0)
	lui	a5,%hi(.LC1)
	addi	a1,a5,%lo(.LC1)
	lw	a0,-20(s0)
	call	sprintf
	lw	a5,-20(s0)
	mv	a0,a5
	lw	ra,44(sp)
	lw	s0,40(sp)
	addi	sp,sp,48
	jr	ra
	.size	toString, .-toString
	.align	2
	.globl	__mx_concatenateString
	.type	__mx_concatenateString, @function
__mx_concatenateString:
	addi	sp,sp,-48
	sw	ra,44(sp)
	sw	s0,40(sp)
	sw	s1,36(sp)
	addi	s0,sp,48
	sw	a0,-36(s0)
	sw	a1,-40(s0)
	lw	a0,-36(s0)
	call	strlen
	mv	s1,a0
	lw	a0,-40(s0)
	call	strlen
	mv	a5,a0
	add	a5,s1,a5
	addi	a5,a5,1
	sw	a5,-20(s0)
	lw	a5,-20(s0)
	mv	a0,a5
	call	malloc
	mv	a5,a0
	sw	a5,-24(s0)
	lw	a5,-24(s0)
	sb	zero,0(a5)
	lw	a1,-36(s0)
	lw	a0,-24(s0)
	call	strcat
	lw	a1,-40(s0)
	lw	a0,-24(s0)
	call	strcat
	lw	a5,-20(s0)
	addi	a5,a5,-1
	lw	a4,-24(s0)
	add	a5,a4,a5
	sb	zero,0(a5)
	lw	a5,-24(s0)
	mv	a0,a5
	lw	ra,44(sp)
	lw	s0,40(sp)
	lw	s1,36(sp)
	addi	sp,sp,48
	jr	ra
	.size	__mx_concatenateString, .-__mx_concatenateString
	.align	2
	.globl	__mx_stringLt
	.type	__mx_stringLt, @function
__mx_stringLt:
	addi	sp,sp,-32
	sw	ra,28(sp)
	sw	s0,24(sp)
	addi	s0,sp,32
	sw	a0,-20(s0)
	sw	a1,-24(s0)
	lw	a1,-24(s0)
	lw	a0,-20(s0)
	call	strcmp
	mv	a5,a0
	srli	a5,a5,31
	andi	a5,a5,0xff
	mv	a0,a5
	lw	ra,28(sp)
	lw	s0,24(sp)
	addi	sp,sp,32
	jr	ra
	.size	__mx_stringLt, .-__mx_stringLt
	.align	2
	.globl	__mx_stringLe
	.type	__mx_stringLe, @function
__mx_stringLe:
	addi	sp,sp,-32
	sw	ra,28(sp)
	sw	s0,24(sp)
	addi	s0,sp,32
	sw	a0,-20(s0)
	sw	a1,-24(s0)
	lw	a1,-24(s0)
	lw	a0,-20(s0)
	call	strcmp
	mv	a5,a0
	slti	a5,a5,1
	andi	a5,a5,0xff
	mv	a0,a5
	lw	ra,28(sp)
	lw	s0,24(sp)
	addi	sp,sp,32
	jr	ra
	.size	__mx_stringLe, .-__mx_stringLe
	.align	2
	.globl	__mx_stringGt
	.type	__mx_stringGt, @function
__mx_stringGt:
	addi	sp,sp,-32
	sw	ra,28(sp)
	sw	s0,24(sp)
	addi	s0,sp,32
	sw	a0,-20(s0)
	sw	a1,-24(s0)
	lw	a1,-24(s0)
	lw	a0,-20(s0)
	call	strcmp
	mv	a5,a0
	sgt	a5,a5,zero
	andi	a5,a5,0xff
	mv	a0,a5
	lw	ra,28(sp)
	lw	s0,24(sp)
	addi	sp,sp,32
	jr	ra
	.size	__mx_stringGt, .-__mx_stringGt
	.align	2
	.globl	__mx_stringGe
	.type	__mx_stringGe, @function
__mx_stringGe:
	addi	sp,sp,-32
	sw	ra,28(sp)
	sw	s0,24(sp)
	addi	s0,sp,32
	sw	a0,-20(s0)
	sw	a1,-24(s0)
	lw	a1,-24(s0)
	lw	a0,-20(s0)
	call	strcmp
	mv	a5,a0
	not	a5,a5
	srli	a5,a5,31
	andi	a5,a5,0xff
	mv	a0,a5
	lw	ra,28(sp)
	lw	s0,24(sp)
	addi	sp,sp,32
	jr	ra
	.size	__mx_stringGe, .-__mx_stringGe
	.align	2
	.globl	__mx_stringEq
	.type	__mx_stringEq, @function
__mx_stringEq:
	addi	sp,sp,-32
	sw	ra,28(sp)
	sw	s0,24(sp)
	addi	s0,sp,32
	sw	a0,-20(s0)
	sw	a1,-24(s0)
	lw	a1,-24(s0)
	lw	a0,-20(s0)
	call	strcmp
	mv	a5,a0
	seqz	a5,a5
	andi	a5,a5,0xff
	mv	a0,a5
	lw	ra,28(sp)
	lw	s0,24(sp)
	addi	sp,sp,32
	jr	ra
	.size	__mx_stringEq, .-__mx_stringEq
	.align	2
	.globl	__mx_stringNe
	.type	__mx_stringNe, @function
__mx_stringNe:
	addi	sp,sp,-32
	sw	ra,28(sp)
	sw	s0,24(sp)
	addi	s0,sp,32
	sw	a0,-20(s0)
	sw	a1,-24(s0)
	lw	a1,-24(s0)
	lw	a0,-20(s0)
	call	strcmp
	mv	a5,a0
	snez	a5,a5
	andi	a5,a5,0xff
	mv	a0,a5
	lw	ra,28(sp)
	lw	s0,24(sp)
	addi	sp,sp,32
	jr	ra
	.size	__mx_stringNe, .-__mx_stringNe
	.align	2
	.globl	__mx_stringLength
	.type	__mx_stringLength, @function
__mx_stringLength:
	addi	sp,sp,-32
	sw	ra,28(sp)
	sw	s0,24(sp)
	addi	s0,sp,32
	sw	a0,-20(s0)
	lw	a0,-20(s0)
	call	strlen
	mv	a5,a0
	mv	a0,a5
	lw	ra,28(sp)
	lw	s0,24(sp)
	addi	sp,sp,32
	jr	ra
	.size	__mx_stringLength, .-__mx_stringLength
	.align	2
	.globl	__mx_stringSubstring
	.type	__mx_stringSubstring, @function
__mx_stringSubstring:
	addi	sp,sp,-48
	sw	ra,44(sp)
	sw	s0,40(sp)
	addi	s0,sp,48
	sw	a0,-36(s0)
	sw	a1,-40(s0)
	sw	a2,-44(s0)
	lw	a4,-44(s0)
	lw	a5,-40(s0)
	sub	a5,a4,a5
	addi	a5,a5,1
	sw	a5,-20(s0)
	lw	a5,-20(s0)
	mv	a0,a5
	call	malloc
	mv	a5,a0
	sw	a5,-24(s0)
	lw	a5,-40(s0)
	lw	a4,-36(s0)
	add	a5,a4,a5
	lw	a4,-20(s0)
	mv	a2,a4
	mv	a1,a5
	lw	a0,-24(s0)
	call	memcpy
	lw	a5,-20(s0)
	addi	a5,a5,-1
	lw	a4,-24(s0)
	add	a5,a4,a5
	sb	zero,0(a5)
	lw	a5,-24(s0)
	mv	a0,a5
	lw	ra,44(sp)
	lw	s0,40(sp)
	addi	sp,sp,48
	jr	ra
	.size	__mx_stringSubstring, .-__mx_stringSubstring
	.align	2
	.globl	__mx_stringParseInt
	.type	__mx_stringParseInt, @function
__mx_stringParseInt:
	addi	sp,sp,-48
	sw	ra,44(sp)
	sw	s0,40(sp)
	addi	s0,sp,48
	sw	a0,-36(s0)
	addi	a5,s0,-20
	mv	a2,a5
	lui	a5,%hi(.LC1)
	addi	a1,a5,%lo(.LC1)
	lw	a0,-36(s0)
	call	sscanf
	lw	a5,-20(s0)
	mv	a0,a5
	lw	ra,44(sp)
	lw	s0,40(sp)
	addi	sp,sp,48
	jr	ra
	.size	__mx_stringParseInt, .-__mx_stringParseInt
	.align	2
	.globl	__mx_stringOrd
	.type	__mx_stringOrd, @function
__mx_stringOrd:
	addi	sp,sp,-32
	sw	s0,28(sp)
	addi	s0,sp,32
	sw	a0,-20(s0)
	sw	a1,-24(s0)
	lw	a5,-24(s0)
	lw	a4,-20(s0)
	add	a5,a4,a5
	lbu	a5,0(a5)
	mv	a0,a5
	lw	s0,28(sp)
	addi	sp,sp,32
	jr	ra
	.size	__mx_stringOrd, .-__mx_stringOrd
	.align	2
	.globl	__mx_malloc
	.type	__mx_malloc, @function
__mx_malloc:
	addi	sp,sp,-32
	sw	ra,28(sp)
	sw	s0,24(sp)
	addi	s0,sp,32
	sw	a0,-20(s0)
	lw	a5,-20(s0)
	mv	a0,a5
	call	malloc
	mv	a5,a0
	mv	a0,a5
	lw	ra,28(sp)
	lw	s0,24(sp)
	addi	sp,sp,32
	jr	ra
	.size	__mx_malloc, .-__mx_malloc
	.ident	"GCC: (GNU) 11.1.0"
