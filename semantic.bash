set ff=UNIX
set -e
cat | java -cp lib/antlr-4.9.1-complete.jar:./myout PrismCube -fsyntax-only