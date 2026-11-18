import importlib.util
import pathlib
import unittest

path = pathlib.Path(__file__).with_name("check_sensitive_files.py")
spec = importlib.util.spec_from_file_location("sensitive_guard", path)
guard = importlib.util.module_from_spec(spec)
spec.loader.exec_module(guard)

class GuardTests(unittest.TestCase):
    def test_private_paths(self):
        for name in [
            "app/release.jks", "key.properties", "docs/updates/v1-internal.md",
            "ops/private/archive.json", "secrets.properties", "backup.tar.zst.enc",
            "nested/.env.prod", ".pgpass", "docs/internal/raw.md"
        ]:
            self.assertTrue(guard.path_blocked(name), name)

    def test_public_paths(self):
        for name in [
            ".env.example", "docs/worklog/sanitized.md", "app/build.gradle.kts",
            "app/src/main/AndroidManifest.xml"
        ]:
            self.assertFalse(guard.path_blocked(name), name)

    def test_secret_detection(self):
        self.assertIn("GitHub token", guard.content_findings("x=ghp_" + "A" * 36))
        self.assertIn("Discord webhook", guard.content_findings(
            "url=https://discord.com/api/webhooks/123456/" + "x" * 30))
        self.assertFalse(guard.content_findings("TOKEN=" + chr(36) + "{TOKEN}"))
        self.assertFalse(guard.content_findings("TOKEN=CHANGE_ME"))

if __name__ == "__main__":
    unittest.main()
