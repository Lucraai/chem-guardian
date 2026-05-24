from __future__ import annotations

import re
import uuid
from typing import Any

from app.schemas import (
    AnalyzeScenarioRequest,
    AskRequest,
    AskResponse,
    GenerateChecklistRequest,
    IngestChunk,
    IngestDocumentRequest,
    IngestDocumentResponse,
    PrecheckTicketRequest,
    SourceRef,
)
from app.services.document_parser import DocumentParser
from app.services.knowledge_base import KnowledgeBase, KnowledgeChunk


class AdvisorService:
    def __init__(self) -> None:
        self.knowledge_base = KnowledgeBase()
        self.document_parser = DocumentParser()

    def ask(self, request: AskRequest) -> AskResponse:
        sources = self.knowledge_base.search(request.question, request.tenant_id, limit=5)
        risk_level = self._detect_risk_level(request.question, sources)
        answer = self._compose_answer(request.question, sources, risk_level)
        return AskResponse(
            question=request.question,
            answer=answer,
            sources=[self._to_source_ref(item) for item in sources],
            risk_level=risk_level,
        )

    def ingest_document(self, request: IngestDocumentRequest) -> IngestDocumentResponse:
        chunks = request.chunks or self._split_text_to_chunks(request.raw_text or "")
        knowledge_chunks = self._to_knowledge_chunks(request, chunks)
        self.knowledge_base.upsert_chunks(knowledge_chunks)
        return IngestDocumentResponse(
            document_code=request.document_code,
            document_name=request.document_name,
            chunk_count=len(knowledge_chunks),
            status="INGESTED",
        )

    def ingest_file(
        self,
        *,
        document_code: str | None,
        document_name: str,
        doc_type: str,
        file_name: str | None,
        content_type: str | None,
        tenant_id: int,
        version: str | None,
        source_uri: str | None,
        data: bytes,
    ) -> IngestDocumentResponse:
        raw_text = self.document_parser.extract_text(file_name, content_type, data)
        if not raw_text.strip():
            raw_text = f"Uploaded file {file_name or document_name} could not be fully extracted."

        request = IngestDocumentRequest(
            document_code=document_code,
            document_name=document_name,
            doc_type=doc_type,
            version=version,
            source_uri=source_uri or file_name,
            raw_text=raw_text,
            tenant_id=tenant_id,
        )
        return self.ingest_document(request)

    def analyze_scenario(self, request: AnalyzeScenarioRequest) -> dict[str, object]:
        sources = self.knowledge_base.search(request.scenario, request.tenant_id, limit=3)
        risk_level = self._detect_risk_level(request.scenario, sources)
        return {
            "scenario": request.scenario,
            "risk_level": risk_level,
            "key_points": [self._compact_point(item) for item in sources],
            "recommended_actions": self._recommended_actions(request.scenario, risk_level),
        }

    def generate_checklist(self, request: GenerateChecklistRequest) -> dict[str, object]:
        sources = self.knowledge_base.search(request.scenario, request.tenant_id, limit=3)
        checklist = self._recommended_actions(
            request.scenario,
            self._detect_risk_level(request.scenario, sources),
        )
        return {
            "scenario": request.scenario,
            "items": checklist,
            "sources": [self._to_source_ref(item).model_dump() for item in sources],
        }

    def precheck_ticket(self, request: PrecheckTicketRequest) -> dict[str, object]:
        ticket_type = request.ticket_type.lower()
        issues: list[str] = []
        if not request.content:
            issues.append("Ticket content is empty.")
        if "hot work" in ticket_type or "动火" in ticket_type:
            if not request.content.get("monitoring"):
                issues.append("Hot work ticket is missing monitoring/guarding information.")
        if "confined space" in ticket_type or "受限空间" in ticket_type:
            if not request.content.get("gas_detection"):
                issues.append("Confined space ticket is missing gas detection information.")
        status = "PASS" if not issues else "REVIEW"
        return {
            "ticket_type": request.ticket_type,
            "status": status,
            "issues": issues,
        }

    def _compose_answer(self, question: str, sources: list[dict[str, object]], risk_level: str) -> str:
        if not sources:
            return (
                f"No sufficiently relevant clauses were retrieved for: {question}. "
                f"Please enrich the document base and verify against company procedures. "
                f"Current risk level: {risk_level}."
            )

        lines = [f"Relevant references for: {question}"]
        for item in sources[:3]:
            clause = item.get("clause_no") or "UNNUMBERED"
            title = item.get("clause_title") or item.get("document_name") or "RELATED STANDARD"
            text = item.get("text") or ""
            lines.append(f"- {title} ({clause}): {text}")
        lines.append(f"Overall risk level: {risk_level}.")
        lines.append("For high-risk operations, final confirmation must come from site management and safety review.")
        return "\n".join(lines)

    def _detect_risk_level(self, question: str, sources: list[dict[str, object]]) -> str:
        text = question.lower()
        high_keywords = [
            "hot work",
            "动火",
            "confined space",
            "受限空间",
            "high place",
            "高处",
            "blind plate",
            "盲板",
            "explosion",
            "爆炸",
            "leak",
            "泄漏",
            "inspection",
            "检修",
            "temporary power",
            "临时用电",
        ]
        medium_keywords = ["ticket", "票", "ledger", "台账", "check", "检查", "rectify", "整改", "patrol", "巡检"]
        if any(keyword in text for keyword in high_keywords):
            return "HIGH"
        if any(keyword in text for keyword in medium_keywords):
            return "MEDIUM"
        if sources:
            return "MEDIUM"
        return "UNKNOWN"

    def _recommended_actions(self, scenario: str, risk_level: str) -> list[str]:
        actions = [
            "Confirm the applicable standard version and internal procedures.",
            "Verify the work permit, approvals, and site conditions.",
            "Record the inspection result and retain evidence materials.",
        ]
        scenario_lower = scenario.lower()
        if risk_level == "HIGH":
            actions.insert(0, "Mandatory joint review by safety management and site owner.")
        if "受限空间" in scenario or "confined space" in scenario_lower:
            actions.append("Confirm ventilation, detection, guarding, and emergency rescue readiness.")
        if "动火" in scenario or "hot work" in scenario_lower:
            actions.append("Confirm fire prevention measures, guardian assignment, and work area boundaries.")
        if "临时用电" in scenario or "temporary power" in scenario_lower:
            actions.append("Confirm wiring inspection, protection devices, and permit validity.")
        return actions

    def _compact_point(self, item: dict[str, object]) -> str:
        clause = item.get("clause_no") or "UNNUMBERED"
        title = item.get("clause_title") or item.get("document_name") or "RELATED STANDARD"
        return f"{title} / {clause}"

    def _to_source_ref(self, item: dict[str, object]) -> SourceRef:
        return SourceRef(
            document_code=item.get("document_code"),
            document_name=item.get("document_name"),
            clause_no=item.get("clause_no"),
            clause_text=item.get("text"),
            source_uri=item.get("source_uri"),
            score=item.get("score"),
        )

    def _split_text_to_chunks(self, text: str) -> list[IngestChunk]:
        normalized = text.replace("\r\n", "\n").replace("\r", "\n").strip()
        if not normalized:
            return [
                IngestChunk(
                    clause_no="AUTO-1",
                    clause_title="Default Example Clause",
                    text="The system splits standards into clauses and builds an index for scenario and clause retrieval.",
                )
            ]

        paragraphs = [p.strip() for p in re.split(r"\n\s*\n", normalized) if p.strip()]
        if len(paragraphs) <= 1:
            paragraphs = [p.strip() for p in normalized.split("\n") if p.strip()]
        if len(paragraphs) <= 1:
            paragraphs = [p.strip() for p in re.split(r"(?=(?:第[一二三四五六七八九十百千\d]+条|[一二三四五六七八九十]+、|\d+[.、]))", normalized) if p.strip()]
        if not paragraphs:
            paragraphs = [normalized]

        result: list[IngestChunk] = []
        for index, paragraph in enumerate(paragraphs):
            title = self._derive_title(paragraph, index)
            result.append(
                IngestChunk(
                    clause_no=f"AUTO-{index + 1}",
                    clause_title=title,
                    text=paragraph,
                )
            )
        return result

    def _to_knowledge_chunks(
        self,
        request: IngestDocumentRequest,
        chunks: list[IngestChunk],
    ) -> list[KnowledgeChunk]:
        knowledge_chunks: list[KnowledgeChunk] = []
        for idx, chunk in enumerate(chunks):
            knowledge_chunks.append(
                KnowledgeChunk(
                    id=str(uuid.uuid4()),
                    document_code=request.document_code,
                    document_name=request.document_name,
                    doc_type=request.doc_type,
                    version=request.version,
                    clause_no=chunk.clause_no or f"CHUNK-{idx + 1}",
                    clause_title=chunk.clause_title,
                    text=chunk.text,
                    source_uri=request.source_uri,
                    tenant_id=request.tenant_id,
                )
            )
        return knowledge_chunks

    @staticmethod
    def _derive_title(text: str, index: int) -> str:
        lines = [line.strip(" 。；;:：") for line in text.split("\n") if line.strip()]
        first_line = lines[0] if lines else ""
        if len(first_line) <= 40:
            return first_line or f"Auto Split Segment {index + 1}"
        return f"Auto Split Segment {index + 1}"

