import json, re
from pathlib import Path
from collections import Counter

server = Path("/root/worktrees/app-api-full-v442-server")
app = Path("/root/worktrees/app-api-full-v442-app")
contract = json.loads((server / "docs/mobile-api-contract.json").read_text())
endpoints = contract.get("endpoints", [])
if isinstance(endpoints, dict):
    endpoints = list(endpoints.values())

def norm(path):
    path = path.split("?")[0].strip()
    path = re.sub(r"\$\{[^}]+\}|\$[A-Za-z_][A-Za-z0-9_]*", "{}", path)
    path = re.sub(r"\{[^}]+\}", "{}", path)
    return re.sub(r"/+", "/", path)

def canon(path):
    path = norm(path)
    for pref in ("/app-api/v1", "app-api/v1", "/api/v1", "api/v1"):
        if path.startswith(pref):
            path = path[len(pref):]
    return path if path.startswith("/") else "/" + path

contract_set = set()
for e in endpoints:
    method = str(e.get("method", "")).upper()
    path = e.get("appPath") or e.get("path") or e.get("route") or ""
    if method and path:
        contract_set.add((method, canon(path)))
used = set()
files = []
helpers = {
    "contractGet": "GET", "contractPost": "POST", "contractPut": "PUT",
    "contractPatch": "PATCH", "contractDelete": "DELETE",
    "contractDeleteWithBody": "DELETE", "rawPost": "POST"
}
for path in app.rglob("*.kt"):
    text = path.read_text(errors="ignore")
    files.append((path, text))
    for match in re.finditer(r'@(GET|POST|PUT|PATCH|DELETE)\(\s*["\']([^"\']+)["\']', text):
        used.add((match.group(1), canon(match.group(2))))
    for helper, verb in helpers.items():
        pattern = r'\b' + re.escape(helper) + r'\(\s*["\']([^"\']+)["\']'
        for match in re.finditer(pattern, text):
            used.add((verb, canon(match.group(1))))

missing = sorted(contract_set - used)
print("CONTRACT_VERSION", contract.get("contractVersion") or contract.get("apiVersion"))
print("CONTRACT", len(contract_set), "USED", len(contract_set & used), "MISSING", len(missing), "APP_DISCOVERED", len(used))
print("MISSING_GROUPS", dict(Counter((p.strip("/").split("/")[0] if p.strip("/") else "root") for _, p in missing)))
print("---MISSING---")
for method, path in missing:
    print(method, path)
print("---VISIBLE_ROUTE_STRINGS---")
for path, text in files:
    for idx, line in enumerate(text.splitlines(), 1):
        if ("panel.path" in line or "app-api/v1" in line) and any(k in line for k in ("Text(", "text =", "contentDescription", "Log.", "println")):
            print(f"{path.relative_to(app)}:{idx}:{line.strip()}")
