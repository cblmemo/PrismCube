set ff=UNIX
set -e
java -cp ./lib/antlr-4.9.1-complete.jar:./out/production/PrismCube PrismCube -i ./bin/test.mx -o ./bin/test.ll -emit-llvm
clang -S -emit-llvm ./builtin/builtin.c -o ./builtin/builtin.ll -O0
scp ./builtin/builtin.ll ./bin/b.ll
scp ./bin/test.ll ./bin/t.ll
llvm-as ./bin/t.ll -o ./bin/t.bc
llvm-as ./bin/b.ll -o ./bin/b.bc
llvm-link ./bin/t.bc ./bin/b.bc -o ./bin/l.bc
clang ./bin/l.bc -o ./bin/a.out
#llvm-link ./bin/t.ll ./bin/b.ll -o ./bin/l.ll
#lli ./bin/l.ll
#rm ./bin/l.ll
rm ./bin/b.ll
rm ./bin/t.ll
rm ./bin/*.bc
echo "build finished."
#./bin/a.out
rm ./bin/a.out