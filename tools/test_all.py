import os
def run():
    failed = 0
    print("codegen")
    failed += os.system("python3 tools/codegen_local_judge_modified.py codegen")
    print("optim-new")
    failed += os.system("python3 tools/codegen_local_judge_modified.py")
    print("optim")
    failed += os.system("python3 tools/codegen_local_judge_modified.py optim")
    if failed != 0:
        print("\033[0;31m" + "failed!" + "\033[0m")