from __future__ import annotations

import json
from pathlib import Path
from urllib import error, request


ROOT = Path(__file__).resolve().parents[1]
SAMPLE_FILE = ROOT / "docs" / "sample_standard_hot_work.md"
AI_INGEST_URL = "http://localhost:8000/ingest-document"
AI_ASK_URL = "http://localhost:8000/ask"


def post_json(url: str, payload: dict) -> dict:
    data = json.dumps(payload, ensure_ascii=False).encode("utf-8")
    req = request.Request(url, data=data, headers={"Content-Type": "application/json"}, method="POST")
    with request.urlopen(req, timeout=30) as resp:
        body = resp.read().decode("utf-8")
        return json.loads(body)


def main() -> None:
    raw_text = SAMPLE_FILE.read_text(encoding="utf-8")
    ingest_payload = {
        "document_code": "GB-30871-SEED",
        "document_name": "GB 30871-2022 Special Work Safety Example",
        "doc_type": "GB",
        "version": "2022",
        "source_uri": str(SAMPLE_FILE),
        "raw_text": raw_text,
        "tenant_id": 1,
        "chunks": [],
    }
    ingest_result = post_json(AI_INGEST_URL, ingest_payload)
    print("INGEST RESULT:")
    print(json.dumps(ingest_result, ensure_ascii=False, indent=2))

    ask_payload = {
        "question": "动火作业前需要确认哪些内容？",
        "tenant_id": 1,
    }
    ask_result = post_json(AI_ASK_URL, ask_payload)
    print("\nASK RESULT:")
    print(json.dumps(ask_result, ensure_ascii=False, indent=2))


if __name__ == "__main__":
    try:
        main()
    except FileNotFoundError:
        print(f"Sample file not found: {SAMPLE_FILE}")
    except error.URLError as exc:
        print(f"Request failed: {exc}")

