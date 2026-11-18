#!/usr/bin/env python3
"""Fail closed on sensitive files added/changed by a pull request or push.

Never print secret material. GitHub push protection and repository rulesets
must also be enabled by an administrator.
"""
import os
import re
import subprocess
import sys

DENIED_PATH = re.compile(
    r"(^|/)(?:\.env(?:\.(?!example$)[^/]*)?|\.pgpass|\.npmrc|key\.properties|secrets\.properties)$"
    r"|(^|/)(?:ops/(?:private|recovery)|docs/(?:private|internal)|\.internal|\.secrets)/"
    r"|^docs/(?:updates|worklog|changelog)/[^/]*internal[^/]*\.(?:md|txt|json|log)$"
    r"|\.(?:pem|key|p12|pfx|jks|keystore|age|tar\.zst\.enc|dump|backup|sql\.gz|sql\.zst|jsonl|ndjson|log)$",
    re.IGNORECASE,
)
TOKEN_PATTERNS = [
    ("private key", re.compile(r"-----BEGIN [A-Z ]*PRIVATE KEY-----")),
    ("GitHub token", re.compile(r"gh[pousr]_[A-Za-z0-9]{36}|github_pat_[A-Za-z0-9_]{22,}")),
    ("cloud key", re.compile(r"AKIA[0-9A-Z]{16}")),
    ("Discord webhook", re.compile(r"https://discord(?:app)?\.com/api/webhooks/\d+/[A-Za-z0-9_-]{20,}")),
    ("credential URL", re.compile(r"://[A-Za-z0-9._%+-]+:[^@/\s\"']{6,}@")),
]

def path_blocked(path):
    return bool(DENIED_PATH.search(path))

def content_findings(content):
    found = set()
    for line in content.splitlines():
        if chr(36) + "{" in line or "CHANGE_ME" in line or "USER:PASSWORD@HOST" in line:
            continue
        for name, pattern in TOKEN_PATTERNS:
            if pattern.search(line):
                found.add(name)
    return sorted(found)

def changed_paths(base):
    completed = subprocess.run(
        ["git", "diff", "--name-only", "--diff-filter=ACMR", "-z", base, "HEAD"],
        check=True, stdout=subprocess.PIPE,
    )
    return [x.decode("utf-8", errors="replace") for x in completed.stdout.split(b"\0") if x]

def main():
    base = os.environ.get("SECURITY_BASE_SHA", "")
    if not re.fullmatch(r"[0-9a-f]{40}", base):
        print("SECURITY_BASE_SHA missing or invalid; fail closed", file=sys.stderr)
        return 1
    try:
        paths = changed_paths(base)
    except subprocess.CalledProcessError:
        print("Unable to determine changed files; fail closed", file=sys.stderr)
        return 1
    bad = []
    for path in paths:
        if path_blocked(path):
            bad.append(f"{path}: private file path forbidden")
            continue
        try:
            data = open(path, "rb").read()
        except (OSError, ValueError):
            bad.append(f"{path}: cannot verify file contents")
            continue
        if b"\0" in data or len(data) > 5_000_000:
            continue
        for kind in content_findings(data.decode("utf-8", errors="replace")):
            bad.append(f"{path}: {kind}")
    for item in bad:
        print(item, file=sys.stderr)
    if bad:
        return 1
    print(f"sensitive file check passed: {len(paths)} added/changed files")
    return 0

if __name__ == "__main__":
    sys.exit(main())
