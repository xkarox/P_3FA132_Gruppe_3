import os
from pathlib import Path


def run_scan():
    source_dir = Path("../src")
    requirements_file = Path("../src/main/resources/requirements.txt")

    requirements = load_requirements(requirements_file)
    found_requirements = set()

    for root, _, files in os.walk(source_dir):
        for file in files:
            if file.endswith(".java"):
                path = Path(root) / file
                try:
                    process_file(path, requirements, found_requirements)
                except IOError:
                    print(f"Error on reading from file: {path}")

    print_results(requirements, found_requirements)


def load_requirements(file):
    with open(file, encoding="utf-8") as f:
        return [line.strip() for line in f if line.strip() and not line.startswith("#")]


def process_file(file_path, requirements, found_requirements):
    req_comment_prefix = "// Req. Nr.: "  # --ignore

    with open(file_path, encoding="utf-8") as f:
        lines = f.readlines()

    for line_number, line in enumerate(lines, start=1):
        if req_comment_prefix in line:
            if "--ignore" in line:
                continue
            req_number = line.split(req_comment_prefix, 1)[1].strip()
            req_entry = get_requirement_text(req_number, requirements)
            combined_entry = f"{req_entry} | File: {file_path} | Line: {line_number}"
            found_requirements.add(combined_entry)


def get_requirement_text(req_number, requirements):
    for req in requirements:
        if req.startswith(f"{req_number}."):
            return req
    return "Unknown Requirement"


def print_results(requirements, found_requirements):
    print("Found Requirements:")
    for req in found_requirements:
        print(req)

    print("\nNot found Requirements:")
    for req in requirements:
        if not any(req in found for found in found_requirements):
            print(req)


if __name__ == "__main__":
    run_scan()
