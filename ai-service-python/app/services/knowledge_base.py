from __future__ import annotations

import os
from dataclasses import dataclass
from typing import Any

from qdrant_client import QdrantClient
from qdrant_client.http import models

from app.services.embeddings import HashingEmbedder


@dataclass
class KnowledgeChunk:
    id: str
    document_code: str | None
    document_name: str
    doc_type: str
    version: str | None
    clause_no: str | None
    clause_title: str | None
    text: str
    source_uri: str | None
    tenant_id: int


class KnowledgeBase:
    def __init__(self) -> None:
        self.embedder = HashingEmbedder()
        self.collection_name = os.getenv("QDRANT_COLLECTION", "chem_guardian_chunks")
        self.client = self._create_client()
        self._memory_chunks: list[KnowledgeChunk] = self._build_seed_chunks()
        self._ensure_collection()
        if self.client is not None:
            self._upsert_client(self._memory_chunks)

    def _create_client(self) -> QdrantClient | None:
        url = os.getenv("QDRANT_URL", "").strip()
        if not url:
            return None
        api_key = os.getenv("QDRANT_API_KEY", "").strip() or None
        try:
            return QdrantClient(url=url, api_key=api_key)
        except Exception:
            return None

    def _ensure_collection(self) -> None:
        if self.client is None:
            return
        try:
            collections = self.client.get_collections().collections
            if any(collection.name == self.collection_name for collection in collections):
                return
            self.client.create_collection(
                collection_name=self.collection_name,
                vectors_config=models.VectorParams(size=384, distance=models.Distance.COSINE),
            )
        except Exception:
            self.client = None

    def upsert_chunks(self, chunks: list[KnowledgeChunk]) -> None:
        if not chunks:
            return
        self._memory_chunks.extend(chunks)
        if self.client is None:
            return
        self._upsert_client(chunks)

    def search(self, query: str, tenant_id: int, limit: int = 5) -> list[dict[str, Any]]:
        query_vector = self.embedder.embed_text(query)
        if self.client is None:
            return self._search_memory(query_vector, tenant_id, limit)
        try:
            result = self.client.search(
                collection_name=self.collection_name,
                query_vector=query_vector,
                limit=limit,
                query_filter=models.Filter(
                    must=[
                        models.FieldCondition(
                            key="tenant_id",
                            match=models.MatchValue(value=tenant_id),
                        )
                    ]
                ),
                with_payload=True,
            )
        except Exception:
            return self._search_memory(query_vector, tenant_id, limit)

        hits: list[dict[str, Any]] = []
        for item in result:
            payload = item.payload or {}
            hits.append(
                {
                    "score": float(item.score or 0.0),
                    "document_code": payload.get("document_code"),
                    "document_name": payload.get("document_name"),
                    "doc_type": payload.get("doc_type"),
                    "version": payload.get("version"),
                    "clause_no": payload.get("clause_no"),
                    "clause_title": payload.get("clause_title"),
                    "text": payload.get("text"),
                    "source_uri": payload.get("source_uri"),
                }
            )
        return hits

    def _search_memory(self, query_vector: list[float], tenant_id: int, limit: int) -> list[dict[str, Any]]:
        hits: list[dict[str, Any]] = []
        for chunk in self._memory_chunks:
            if chunk.tenant_id != tenant_id:
                continue
            score = self._cosine(query_vector, self.embedder.embed_text(chunk.text))
            hits.append(
                {
                    "score": score,
                    "document_code": chunk.document_code,
                    "document_name": chunk.document_name,
                    "doc_type": chunk.doc_type,
                    "version": chunk.version,
                    "clause_no": chunk.clause_no,
                    "clause_title": chunk.clause_title,
                    "text": chunk.text,
                    "source_uri": chunk.source_uri,
                }
            )
        hits.sort(key=lambda item: item["score"], reverse=True)
        return hits[:limit]

    def _build_seed_chunks(self) -> list[KnowledgeChunk]:
        return [
            KnowledgeChunk(
                id="seed-1",
                document_code="SEED-001",
                document_name="Special Work Safety Example Library",
                doc_type="SEED",
                version="1.0",
                clause_no="HOT-WORK-1",
                clause_title="Hot Work",
                text=(
                    "Hot work usually requires confirmation of the work scope, approval status, "
                    "on-site monitoring, fire protection measures, and gas detection before execution."
                ),
                source_uri=None,
                tenant_id=1,
            ),
            KnowledgeChunk(
                id="seed-2",
                document_code="SEED-002",
                document_name="Special Work Safety Example Library",
                doc_type="SEED",
                version="1.0",
                clause_no="CONFINED-1",
                clause_title="Confined Space Work",
                text=(
                    "Confined space work usually requires ventilation, detection, guardian assignment, "
                    "emergency rescue readiness, and work approval, with continuous risk monitoring."
                ),
                source_uri=None,
                tenant_id=1,
            ),
            KnowledgeChunk(
                id="seed-3",
                document_code="SEED-003",
                document_name="Special Work Safety Example Library",
                doc_type="SEED",
                version="1.0",
                clause_no="TEMP-POWER-1",
                clause_title="Temporary Power Use",
                text=(
                    "Temporary power use usually requires inspection of wiring, protection devices, "
                    "approval scope, validity period, and site patrol requirements to avoid extra risks."
                ),
                source_uri=None,
                tenant_id=1,
            ),
        ]

    def _upsert_client(self, chunks: list[KnowledgeChunk]) -> None:
        if self.client is None:
            return
        try:
            points = []
            for chunk in chunks:
                vector = self.embedder.embed_text(chunk.text)
                payload = {
                    "document_code": chunk.document_code,
                    "document_name": chunk.document_name,
                    "doc_type": chunk.doc_type,
                    "version": chunk.version,
                    "clause_no": chunk.clause_no,
                    "clause_title": chunk.clause_title,
                    "text": chunk.text,
                    "source_uri": chunk.source_uri,
                    "tenant_id": chunk.tenant_id,
                }
                points.append(
                    models.PointStruct(
                        id=chunk.id,
                        vector=vector,
                        payload=payload,
                    )
                )
            self.client.upsert(collection_name=self.collection_name, points=points)
        except Exception:
            self.client = None

    @staticmethod
    def _cosine(left: list[float], right: list[float]) -> float:
        dot = sum(a * b for a, b in zip(left, right))
        left_norm = sum(a * a for a in left) ** 0.5
        right_norm = sum(b * b for b in right) ** 0.5
        if left_norm == 0 or right_norm == 0:
            return 0.0
        return dot / (left_norm * right_norm)

