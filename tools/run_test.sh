clang -S -emit-llvm ../builtin/builtin.c -o ../builtin/builtin.ll -O0
scp ../builtin/builtin.ll b.ll
scp ../bin/test.ll t.ll
llvm-as b.ll -o b.bc
llvm-as t.ll -o t.bc
llvm-link t.bc b.bc -o l.bc
clang l.bc -o out
./out
rm b.ll
rm t.ll
rm b.bc
rm t.bc
rm l.bc
rm out