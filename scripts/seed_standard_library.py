from __future__ import annotations

import json
from urllib import request


API_URL = "http://localhost:8080/api/standards/library/seed"


def post(url: str) -> dict:
    req = request.Request(url, method="POST")
    with request.urlopen(req, timeout=30) as resp:
        return json.loads(resp.read().decode("utf-8"))


def main() -> None:
    result = post(API_URL)
    print(json.dumps(result, ensure_ascii=False, indent=2))


if __name__ == "__main__":
    main()
