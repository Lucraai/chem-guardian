from datetime import datetime, timezone

from fastapi import FastAPI, File, Form, UploadFile
from fastapi.middleware.cors import CORSMiddleware

from app.schemas import (
    AnalyzeScenarioRequest,
    AskRequest,
    AskResponse,
    GenerateChecklistRequest,
    IngestDocumentRequest,
    IngestDocumentResponse,
    PrecheckTicketRequest,
)
from app.services.advisor_service import AdvisorService


app = FastAPI(title="Chem AI Service", version="0.1.0")
advisor_service = AdvisorService()

app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)


@app.get("/health")
def health() -> dict[str, str]:
    return {
        "status": "UP",
        "service": "chem-ai-service",
        "timestamp": datetime.now(timezone.utc).isoformat(),
    }


@app.post("/ask", response_model=AskResponse)
def ask(request: AskRequest) -> AskResponse:
    return advisor_service.ask(request)


@app.post("/ingest-document", response_model=IngestDocumentResponse)
def ingest_document(request: IngestDocumentRequest) -> IngestDocumentResponse:
    return advisor_service.ingest_document(request)


@app.post("/ingest-file", response_model=IngestDocumentResponse)
async def ingest_file(
    file: UploadFile = File(...),
    document_name: str = Form(...),
    doc_type: str = Form(...),
    document_code: str | None = Form(None),
    version: str | None = Form(None),
    source_uri: str | None = Form(None),
    tenant_id: int = Form(1),
) -> IngestDocumentResponse:
    data = await file.read()
    return advisor_service.ingest_file(
        document_code=document_code,
        document_name=document_name,
        doc_type=doc_type,
        file_name=file.filename,
        content_type=file.content_type,
        tenant_id=tenant_id,
        version=version,
        source_uri=source_uri,
        data=data,
    )


@app.post("/analyze-scenario")
def analyze_scenario(request: AnalyzeScenarioRequest) -> dict[str, object]:
    return advisor_service.analyze_scenario(request)


@app.post("/generate-checklist")
def generate_checklist(request: GenerateChecklistRequest) -> dict[str, object]:
    return advisor_service.generate_checklist(request)


@app.post("/precheck-ticket")
def precheck_ticket(request: PrecheckTicketRequest) -> dict[str, object]:
    return advisor_service.precheck_ticket(request)
