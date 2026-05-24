from __future__ import annotations

import hashlib
import re


class HashingEmbedder:
    def __init__(self, dimension: int = 384) -> None:
        self.dimension = dimension

    def embed_text(self, text: str) -> list[float]:
        vector = [0.0] * self.dimension
        tokens = re.findall(r"[\w\u4e00-\u9fff]+", text.lower())
        for token in tokens:
            digest = hashlib.sha256(token.encode("utf-8")).digest()
            index = int.from_bytes(digest[:4], "big") % self.dimension
            weight = 1.0 + (digest[4] / 255.0)
            vector[index] += weight
        self._normalize(vector)
        return vector

    @staticmethod
    def _normalize(vector: list[float]) -> None:
        norm = sum(value * value for value in vector) ** 0.5
        if norm == 0:
            return
        for idx, value in enumerate(vector):
            vector[idx] = value / norm

