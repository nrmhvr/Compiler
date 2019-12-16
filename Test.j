.class public Test
.super java/lang/Object

.method public <init>()V
aload_0
invokenonvirtual java/lang/Object/<init>()V
return
.end method

.method public static add(II)I
.limit stack 32
.limit locals 32
iload_0 
iload_1 
iadd
istore_2 
iload_2 
ireturn
.end method

.method public static sum(I)I
.limit stack 32
.limit locals 32
ldc 0
istore_1
ldc 1
istore_2
label6:
iload_2 
iload_0 
if_icmple label0
ldc 0
goto label1
label0:
ldc 1
label1: 
ifeq label7
iload_1 
iload_2 
iadd
istore_1 
iload_2 
ldc 1 
iadd
istore_2 
goto label6
label7:
iload_1 
ireturn
.end method

.method public static simpleif(II)I
.limit stack 32
.limit locals 32
iload_0 
iload_1 
if_icmpge label0
ldc 0
goto label1
label0:
ldc 1
label1: 
ifeq label9
iload_0 
ldc 1
isub
istore_0
iload_0 
ireturn
goto label8
label9:
iload_1 
ldc 1
isub
istore_1
label8:
iload_1 
ireturn
.end method

.method public static ifstmt(II)I
.limit stack 32
.limit locals 32
iload_0 
iload_1 
isub 
ifeq label0
ldc 0
goto label1
label0:
ldc 1
label1: 
ifeq label44
iload_1 
ldc 1
isub
istore_1
goto label43
label44:
iload_0 
iload_1 
iadd
ldc 10 
if_icmplt label7
ldc 0
goto label8
label7:
ldc 1
label8: 
ifeq label42
iload_1 
iload_0 
iadd
ldc 1 
iadd
istore_0 
iload_0 
ireturn
goto label41
label42:
iload_0 
iload_1 
imul
iload_1 
if_icmplt label15
ldc 0
goto label16
label15:
ldc 1
label16: 
ifeq label18
label17:
ldc 0
goto label19
label18:
ldc 1
label19:
iload_1 
ldc 0 
if_icmple label20
ldc 0
goto label21
label20:
ldc 1
label21: 
ifne label23
pop
ldc 0
label23:
ifeq label40
iload_1 
iload_0 
irem
istore_1 
goto label39
label40:
iload_0 
iload_1 
isub 
ifeq label26
ldc 0
goto label27
label26:
ldc 1
label27: 
iload_0 
ineg
iload_1 
if_icmpgt label31
ldc 0
goto label32
label31:
ldc 1
label32: 
ifeq label34
pop
ldc 1
label34:
ifeq label38
iload_1 
iload_0 
idiv
ireturn
goto label37
label38:
label37:
label39:
label41:
label43:
iload_1 
ireturn
.end method

.method public static main([Ljava/lang/String;)V
.limit stack 32
.limit locals 32
ldc 33
istore_1
ldc 100
istore_2
getstatic java/lang/System/out Ljava/io/PrintStream;
ldc 1 
iload_1 
invokestatic Test/add(II)I
invokevirtual java/io/PrintStream/println(I)V
getstatic java/lang/System/out Ljava/io/PrintStream;
iload_2 
invokestatic Test/sum(I)I
invokevirtual java/io/PrintStream/println(I)V
getstatic java/lang/System/out Ljava/io/PrintStream;
iload_1 
iload_2 
invokestatic Test/simpleif(II)I
invokevirtual java/io/PrintStream/println(I)V
getstatic java/lang/System/out Ljava/io/PrintStream;
iload_1 
iload_2 
invokestatic Test/ifstmt(II)I
invokevirtual java/io/PrintStream/println(I)V
return
.end method

