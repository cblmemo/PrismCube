set ff=UNIX
set -e
mkdir -p myout
find ./src -name *.java | javac -d myout -cp lib/antlr-4.9.1-complete.jar @/dev/stdin