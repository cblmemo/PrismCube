import os
import sys


def exe(cmd):
    os.system(cmd)


def clear(case):
    if case == 1:
        exe("rm ./bin/b.ll")
        exe("rm ./bin/t.ll")
        exe("rm ./bin/a.out")
    elif case == 2:
        # todo add asm clear
        exe("")


def build():
    if os.path.exists("myout"):
        exe("rm -rf myout")
    exe("mkdir -p myout")
    exe("find ./src -name *.java | javac -d myout -cp lib/antlr-4.9.1-complete.jar @/dev/stdin")
    print("build finished.")


def ir_gen_executable():
    exe("java -cp ./lib/antlr-4.9.1-complete.jar:./myout PrismCube -i ./bin/test.mx -o ./bin/test.ll -emit-llvm")
    exe("scp ./builtin/builtin.ll ./bin/b.ll")
    exe("scp ./bin/test.ll ./bin/t.ll")
    exe("clang ./bin/b.ll ./bin/t.ll -o ./bin/a.out")
    print("ir generate and link finished.")


def run_asm():
    exe("java -cp ./lib/antlr-4.9.1-complete.jar:./myout PrismCube -i ./bin/test.mx -o ./bin/test.s -emit-asm")
    exe("rm ./bin/b.s")
    exe("rm ./bin/t.s")
    exe("scp ./builtin/builtin.s ./bin/b.s")
    exe("scp ./bin/test.s ./bin/t.s")
    print("asm generate finished.")
    exe("/Users/memory/Desktop/temp/compiler/bin/ravel ./bin/t.s ./bin/b.s")


def run_executable():
    exe("./bin/a.out")


def gen_riscv_asm():
    exe("rm bin/a.ll")
    exe("rm bin/a.s")
    # exe("clang -emit-llvm -S bin/a.c -o bin/a.ll")
    exe("clang -emit-llvm -S bin/a.c -o bin/a.ll --target=riscv32 -O0")
    exe("llc bin/a.ll -o bin/a.s --mattr=+m -march=riscv32")


def compile_test():
    exe("java -cp ./lib/antlr-4.9.1-complete.jar:./myout PrismCube -i ./bin/test.mx -o ./bin/test.ll -emit-llvm")
    exe("java -cp ./lib/antlr-4.9.1-complete.jar:./myout PrismCube -i ./bin/test.mx -o ./bin/test.s -emit-asm")


def run():
    i = 1
    case = 0
    conflict = False
    while i < len(sys.argv):
        arg = sys.argv[i]
        if arg == "--help" or arg == "-h":
            print("welcome to rainy memory's compiler runner!")
            print("now support:")
            print("---------------------------------------------------------")
            print("-h / --help       show help message")
            print("--reload          re-build compiler")
            print("-emit-asm         generate standard rv32i asm for bin/a.c")
            print("-compile-test     generate test.ll and test.s for test.mx")
            print("ir                test ir  for bin/test.mx")
            print("asm               test asm for bin/test.mx")
            print("---------------------------------------------------------")
            exit(0)
        elif arg == "--reload":
            build()
        elif arg == "ir":
            if conflict:
                print("error: mode conflict")
                exit(1)
            else:
                conflict = True
            case = 1
        elif arg == "asm":
            if conflict:
                print("error: mode conflict")
                exit(1)
            else:
                conflict = True
            case = 2
        elif arg == "-emit-asm":
            if conflict:
                print("error: mode conflict")
                exit(1)
            else:
                conflict = True
            case = 3
        elif arg == "-compile-test":
            if conflict:
                print("error: mode conflict")
                exit(1)
            else:
                conflict = True
            case = 4
        else:
            print("error: unknown argument type")
            exit(1)
        i = i + 1
    if case == 1:
        ir_gen_executable()
        run_executable()
        clear(case)
    elif case == 2:
        run_asm()
        clear(case)
    elif case == 3:
        gen_riscv_asm()
    elif case == 4:
        compile_test()


run()
