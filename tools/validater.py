import os
import subprocess

dir_path = "../example/output/"
files = os.listdir(dir_path)
new_files = []

for f in files:
    new_files.append(dir_path + f)


to_run = ["python3", "validate_fifo.py", "--proc_num", str(len(files))] + new_files

print(to_run)
subprocess.run(to_run)
