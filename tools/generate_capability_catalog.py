import json
from pathlib import Path

src = Path("/root/worktrees/app-api-full-v442-server/docs/mobile-api-contract.json")
out = Path("/root/worktrees/app-api-full-v442-app/app/src/main/java/com/example/woldeokmoneyverse/data/remote/AppCapabilityCatalog.kt")
data = json.loads(src.read_text())

def q(value):
    return '"' + str(value).replace("\\", "\\\\").replace('"', '\\"').replace("\n", " ") + '"'

lines = [
    "package com.example.woldeokmoneyverse.data.remote", "",
    "enum class CapabilityFieldSource { PATH, QUERY, BODY }", "",
    "data class CapabilityField(",
    "    val name: String,",
    "    val source: CapabilityFieldSource,",
    "    val required: Boolean,",
    "    val kind: String",
    ")", "",
    "data class AppCapability(",
    "    val id: String,",
    "    val group: String,",
    "    val title: String,",
    "    val method: String,",
    "    internal val internalPath: String,",
    "    val fields: List<CapabilityField>,",
    "    val hasBody: Boolean,",
    "    val adminOnly: Boolean,",
    "    val destructive: Boolean",
    ")", "",
    "object AppCapabilityCatalog {",
    "    val all: List<AppCapability> = listOf("
]
for index, endpoint in enumerate(data["endpoints"]):
    path = endpoint["path"]
    method = endpoint["method"]
    parts = path.split("/")
    group = parts[3] if len(parts) > 3 else "general"
    title = endpoint.get("purpose") or endpoint.get("summary") or endpoint.get("operationId") or f"{method} {group}"
    fields = []
    for parameter in endpoint.get("parameters") or []:
        location = parameter.get("in")
        if location in ("path", "query"):
            schema = parameter.get("schema") or {}
            fields.append((
                parameter.get("name", "value"),
                "PATH" if location == "path" else "QUERY",
                bool(parameter.get("required")),
                schema.get("type", "string")
            ))
    request_body = endpoint.get("requestBody") or {}
    schema = request_body.get("resolvedSchema") or request_body.get("schema") or {}
    if schema.get("type") == "object":
        required = set(schema.get("required") or [])
        for name, prop in (schema.get("properties") or {}).items():
            fields.append((name, "BODY", name in required, prop.get("type", "string")))
    elif request_body:
        fields.append(("request", "BODY", bool(request_body.get("required")), schema.get("type", "json")))
    field_text = ", ".join(
        f"CapabilityField({q(name)}, CapabilityFieldSource.{location}, {str(required).lower()}, {q(kind)})"
        for name, location, required, kind in fields
    )
    destructive = method == "DELETE" or any(word in title.lower() for word in ("삭제", "해제", "취소", "회수", "종료"))
    lines.extend([
        "        AppCapability(",
        f"            id = {q(endpoint.get('operationId') or f'capability_{index}')},",
        f"            group = {q(group)},",
        f"            title = {q(title)},",
        f"            method = {q(method)},",
        f"            internalPath = {q(path.lstrip('/'))},",
        f"            fields = listOf({field_text}),",
        f"            hasBody = {str(bool(request_body)).lower()},",
        f"            adminOnly = {str(path.startswith('/app-api/v1/admin/')).lower()},",
        f"            destructive = {str(destructive).lower()}",
        "        ),"
    ])

lines.extend([
    "    )", "",
    "    val member: List<AppCapability> = all.filterNot { it.adminOnly }",
    "    val admin: List<AppCapability> = all.filter { it.adminOnly }",
    "}"
])
out.write_text("\n".join(lines) + "\n")
print(f"generated {len(data['endpoints'])} capabilities -> {out}")
