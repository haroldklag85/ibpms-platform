import subprocess
import sys
import os

def run():
    cmd = [
        r"..\maven\apache-maven-3.9.6\bin\mvn.cmd",
    ] + sys.argv[1:]
    
    print(f"Running command: {' '.join(cmd)}")
    
    # The script is in .agents/worker_m2_replace, so backend directory is two levels up plus "backend":
    backend_dir = os.path.abspath(os.path.join(os.path.dirname(__file__), "..", "..", "backend"))
    print(f"Working directory: {backend_dir}")
    
    process = subprocess.Popen(
        cmd,
        cwd=backend_dir,
        stdout=subprocess.PIPE,
        stderr=subprocess.STDOUT,
        text=True,
        encoding="utf-8",
        errors="replace"
    )
    
    for line in process.stdout:
        print(line, end="")
        sys.stdout.flush()
        
    process.wait()
    print(f"\nExit code: {process.returncode}")
    sys.exit(process.returncode)

if __name__ == "__main__":
    run()
