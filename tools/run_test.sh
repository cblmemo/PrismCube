llvm-link -S ../bin/test.ll ../builtin/builtin.ll -o ../bin/test_link.ll
lli ../bin/test_link.ll
rm ../bin/test_link.ll