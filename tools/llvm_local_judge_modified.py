#!python3

import os
import time

test_cases_dir = './testcases/codegen/'
compile_cmd = "bash ./build.bash"
excluded_test_cases = ["foo.mx"]
builtin_path = "./builtin/builtin.ll"
bin_path = "./bin/"
halt_on_3_fails = True
calculate_score = False
# When test_codegen && use_llvm is true, the output should be a .ll file, and we will use llc to
# compile it into asm. You can test the correctness of your IR-gen with this.
use_llvm = True
llc_cmd = 'llc'

color_red = "\033[0;31m"
color_green = "\033[0;32m"
color_none = "\033[0m"


def collect_test_cases():
    test_cases = []
    for f in os.listdir(test_cases_dir):
        if os.path.splitext(f)[1] == '.mx':
            test_cases.append(f)
    print(len(test_cases))
    for s in excluded_test_cases:
        if s in test_cases:
            test_cases.remove(s)
    test_cases.sort()
    return test_cases


def parse_test_case(test_case_path):
    with open(test_case_path, 'r') as f:
        lines = f.read().split('\n')
    src_start_idx = lines.index('*/', lines.index('/*')) + 1
    src_text = '\n'.join(lines[src_start_idx:])

    input_start_idx = lines.index('=== input ===') + 1
    input_end_idx = lines.index('=== end ===', input_start_idx)
    input_text = '\n'.join(lines[input_start_idx:input_end_idx])

    output_start_idx = lines.index('=== output ===') + 1
    output_end_idx = lines.index('=== end ===', output_start_idx)
    output_text = '\n'.join(lines[output_start_idx:output_end_idx])

    return src_text, input_text, output_text


def main():
    if os.system(compile_cmd):
        print(color_red + "Fail when building your compiler...")
        return
    test_cases = collect_test_cases()
    #     os.system('clang -S -emit-llvm builtin/builtin.c -o builtin/builtin.ll')
    os.system('cp {} ./bin/b.ll'.format(builtin_path))
    total = 0
    passed = 0
    continue_fail = 0
    max_len = max(len(i) for i in test_cases)
    max_len += 5
    for t in test_cases:
        if halt_on_3_fails and (continue_fail > 2):
            exit(1)
        total += 1
        src_text, input_text, output_text = parse_test_case(test_cases_dir + t)
        with open('./bin/test.mx', 'w') as f:
            f.write(src_text)
        with open('./bin/test.in', 'w') as f:
            f.write(input_text)
        with open('./bin/test.ans', 'w') as f:
            f.write(output_text)

        print(t + ':', end='')
        for i in range(len(t), max_len):
            print(end=' ')
        start = time.time()
        if os.system('bash ./ir.bash < ./bin/test.mx > ./bin/test.ll'):
            print(color_red + "Compilation Failed" + color_none)
            continue_fail += 1
            continue
        print("(T=%.2fs)" % (time.time() - start), end=" ")
        os.system("clang ./bin/test.ll ./bin/b.ll -o ./bin/a.out -Wno-override-module")
        # if os.system("./bin/a.out < ./bin/test.in > ./bin/test.out"):
        #     print(color_red + "Runtime Error" + color_none)
        #     continue_fail += 1
        #     continue
        os.system("./bin/a.out < ./bin/test.in > ./bin/test.out")
        if os.system('diff -B -b ./bin/test.out ./bin/test.ans > ./bin/diff.out'):
            print(color_red + "Wrong Answer" + color_none)
            continue_fail += 1
            continue
        passed += 1
        continue_fail = 0
        print(color_green + "Accepted" + color_none)

    print("total {}, passed {}, ratio {}".format(total, passed, passed / total))


if __name__ == '__main__':
    main()
