set ff=UNIX
set -e
cat | java -ea -cp lib/antlr-4.9.1-complete.jar:./myout PrismCube -llvm-only -O2 -print-reg-name