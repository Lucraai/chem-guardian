from __future__ import annotations

from io import BytesIO
from pathlib import Path

from pypdf import PdfReader
from PIL import Image

try:
    from docx import Document
except Exception:  # pragma: no cover - optional dependency fallback
    Document = None  # type: ignore[assignment]

try:
    import pytesseract
except Exception:  # pragma: no cover - optional dependency fallback
    pytesseract = None  # type: ignore[assignment]


class DocumentParser:
    def extract_text(self, filename: str | None, content_type: str | None, data: bytes) -> str:
        suffix = (Path(filename).suffix.lower() if filename else "").strip()
        mime = (content_type or "").lower()

        if suffix == ".pdf" or mime == "application/pdf":
            return self._extract_pdf(data)
        if suffix in {".docx", ".doc"} or "word" in mime:
            return self._extract_docx(data)
        if suffix in {".png", ".jpg", ".jpeg", ".bmp", ".tiff", ".webp"} or mime.startswith("image/"):
            return self._extract_image(data)
        return self._extract_text(data)

    def _extract_pdf(self, data: bytes) -> str:
        try:
            reader = PdfReader(BytesIO(data))
            pages: list[str] = []
            for page in reader.pages:
                text = page.extract_text() or ""
                if text.strip():
                    pages.append(text.strip())
            return "\n\n".join(pages).strip()
        except Exception:
            return self._extract_text(data)

    def _extract_docx(self, data: bytes) -> str:
        if Document is None:
            return self._extract_text(data)
        try:
            document = Document(BytesIO(data))
            paragraphs = [paragraph.text.strip() for paragraph in document.paragraphs if paragraph.text and paragraph.text.strip()]
            return "\n\n".join(paragraphs).strip()
        except Exception:
            return self._extract_text(data)

    def _extract_text(self, data: bytes) -> str:
        for encoding in ("utf-8", "utf-8-sig", "gb18030", "gbk", "latin-1"):
            try:
                return data.decode(encoding).strip()
            except Exception:
                continue
        return ""

    def _extract_image(self, data: bytes) -> str:
        if pytesseract is None:
            return ""
        try:
            image = Image.open(BytesIO(data))
            return (pytesseract.image_to_string(image, lang="chi_sim+eng") or "").strip()
        except Exception:
            try:
                image = Image.open(BytesIO(data))
                return (pytesseract.image_to_string(image) or "").strip()
            except Exception:
                return ""
