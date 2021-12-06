set ff=UNIX
set -e
java -cp ../lib/antlr-4.9.1-complete.jar:../out/production/PrismCube PrismCube -i ../bin/test.mx -o ../bin/test.ll -emit-llvm
clang -S -emit-llvm ../builtin/builtin.c -o ../builtin/builtin.ll -O0
scp ../builtin/builtin.ll b.ll
scp ../bin/test.ll t.ll
llvm-as t.ll -o t.bc
llvm-as b.ll -o b.bc
llvm-link t.bc b.bc -o l.bc
clang l.bc -o a.out
echo "build finished."
echo "--- input ---"
./a.out
rm *.ll
rm *.bc
rm a.out