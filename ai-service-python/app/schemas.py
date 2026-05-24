from pydantic import BaseModel, Field


class AskRequest(BaseModel):
    question: str = Field(min_length=1)
    tenant_id: int = 1


class SourceRef(BaseModel):
    document_code: str | None = None
    document_name: str | None = None
    clause_no: str | None = None
    clause_text: str | None = None
    source_uri: str | None = None
    score: float | None = None


class AskResponse(BaseModel):
    question: str
    answer: str
    sources: list[SourceRef]
    risk_level: str


class IngestChunk(BaseModel):
    clause_no: str | None = None
    clause_title: str | None = None
    text: str = Field(min_length=1)


class IngestDocumentRequest(BaseModel):
    document_code: str | None = None
    document_name: str
    doc_type: str
    version: str | None = None
    source_uri: str | None = None
    raw_text: str | None = None
    tenant_id: int = 1
    chunks: list[IngestChunk] = Field(default_factory=list)


class IngestDocumentResponse(BaseModel):
    document_code: str | None = None
    document_name: str
    chunk_count: int
    status: str


class AnalyzeScenarioRequest(BaseModel):
    scenario: str = Field(min_length=1)
    tenant_id: int = 1


class GenerateChecklistRequest(BaseModel):
    scenario: str = Field(min_length=1)
    tenant_id: int = 1


class PrecheckTicketRequest(BaseModel):
    ticket_type: str = Field(min_length=1)
    content: dict
    tenant_id: int = 1
