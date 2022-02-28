#!python3

import os
import time
import sys

"""
    Modify following configurations to adapt to your environment.
"""
# test_cases_dir = './testcases/sema/'
# test_cases_dir = './testcases/codegen/'
# test_cases_dir = './testcases/optim/'
# test_cases_dir = './testcases/optim-new/'

test_cases_dir = './testcases/optim-new/'
if len(sys.argv) > 1:
    test_cases_dir = './testcases/' + sys.argv[1] + '/'

compile_cmd = "bash ./build.bash"
execute_cmd = "java -ea -cp lib/antlr-4.9.1-complete.jar:./myout PrismCube -emit-asm -i ./bin/test.mx -o ./bin/test.s -arch x86_64 -O2"
excluded_test_cases = ["foo.mx"]
ravel_path = "./lib/ravel --enable-cache"
builtin_path = "./builtin/builtin.s"
halt_on_3_fails = True

color_red = "\033[0;31m"
color_green = "\033[0;32m"
color_none = "\033[0m"


def collect_test_cases():
    test_cases = []
    for f in os.listdir(test_cases_dir):
        if os.path.splitext(f)[1] == '.mx':
            test_cases.append(f)
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
    os.system('cp {} ./bin/builtin.s'.format(builtin_path))
    total = 0
    passed = 0
    continue_fail = 0
    max_len = max(len(i) for i in test_cases)
    max_len += 5
    failed_cases = []
    for t in test_cases:
        if halt_on_3_fails and (continue_fail > 2):
            break
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
        if os.system('{}'.format(execute_cmd)):  # input redirect done in execute_cmd
            print(color_red + "Compilation Failed" + color_none)
            continue_fail += 1
            failed_cases.append((t, len(src_text)))
            continue
        print("(T=%.2fs)" % (time.time() - start), end=" ")
        if os.system('{} --input-file=./bin/test.in --output-file=./bin/test.out ./bin/test.s ./bin/builtin.s 1>./bin/ravel.out 2>/dev/null'.format(ravel_path)):
            print(color_red + "Runtime Error" + color_none)
            continue_fail += 1
            failed_cases.append((t, len(src_text)))
            continue
        if os.system('diff -B -b ./bin/test.out ./bin/test.ans > ./bin/diff.out'):
            print(color_red + "Wrong Answer" + color_none)
            continue_fail += 1
            failed_cases.append((t, len(src_text)))
            continue
        passed += 1
        continue_fail = 0
        print(color_green + "Accepted" + color_none)

    print("total {}, passed {}, ratio {}%".format(total, passed, passed / total * 100))
    if len(failed_cases) > 0:
        print("failed cases (name, src code size):", failed_cases)
        exit(1)


if __name__ == '__main__':
    main()
