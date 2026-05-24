from __future__ import annotations

import json
from pathlib import Path
from urllib import request


ROOT = Path(__file__).resolve().parents[1]
AI_INGEST_URL = "http://localhost:8000/ingest-document"
SEED_FILE = ROOT / "docs" / "seed_national_standards.md"


def post_json(url: str, payload: dict) -> dict:
    data = json.dumps(payload, ensure_ascii=False).encode("utf-8")
    req = request.Request(url, data=data, headers={"Content-Type": "application/json"}, method="POST")
    with request.urlopen(req, timeout=30) as resp:
        return json.loads(resp.read().decode("utf-8"))


def main() -> None:
    raw_text = SEED_FILE.read_text(encoding="utf-8")
    result = post_json(
        AI_INGEST_URL,
        {
            "document_code": "SEED-NATIONAL-STANDARDS",
            "document_name": "National Standards Seed Pack",
            "doc_type": "SEED",
            "version": "1.0",
            "source_uri": str(SEED_FILE),
            "raw_text": raw_text,
            "tenant_id": 1,
            "chunks": [
                {
                    "clause_no": "GB30871-2022",
                    "clause_title": "危险化学品企业特殊作业安全规范",
                    "text": (
                        "GB 30871-2022 focuses on special work permit control, site conditions, monitoring, isolation, "
                        "and emergency readiness for hazardous chemicals enterprises."
                    ),
                },
                {
                    "clause_no": "GB45673-2025",
                    "clause_title": "危险化学品企业安全生产标准化通用规范",
                    "text": (
                        "GB 45673-2025 defines the basic and management requirements for safety production "
                        "standardization in hazardous chemicals enterprises."
                    ),
                },
                {
                    "clause_no": "GBT40640.4-2021",
                    "clause_title": "化学品管理信息化 第4部分：化学品定位系统通用规范",
                    "text": (
                        "GB/T 40640.4-2021 supports the design and construction of personnel positioning systems "
                        "for hazardous chemicals production and storage sites."
                    ),
                },
                {
                    "clause_no": "GB2894-2025",
                    "clause_title": "安全色和安全标志",
                    "text": (
                        "GB 2894-2025 provides guidance for safety colors and signs, useful for site warning "
                        "labels, area marking, and visual management."
                    ),
                },
                {
                    "clause_no": "GB2811",
                    "clause_title": "头部防护 安全帽",
                    "text": (
                        "GB 2811 supports helmet selection and on-site personal protective equipment checks for "
                        "entry and inspection scenarios."
                    ),
                },
                {
                    "clause_no": "GB6095",
                    "clause_title": "安全带",
                    "text": (
                        "GB 6095 is commonly used for fall protection checks in high-place work, maintenance, "
                        "and edge work scenarios."
                    ),
                },
                {
                    "clause_no": "GBT11651",
                    "clause_title": "个体防护装备选用规范",
                    "text": (
                        "GB/T 11651 supports PPE selection, onboarding checks, contractor management, and site "
                        "briefing recommendations."
                    ),
                },
                {
                    "clause_no": "GB16483",
                    "clause_title": "化学品安全技术说明书 内容和项目顺序",
                    "text": (
                        "GB 16483 supports SDS/MSDS queries, chemical property checks, and emergency information "
                        "lookup for hazardous chemicals."
                    ),
                },
                {
                    "clause_no": "GB50016",
                    "clause_title": "建筑设计防火规范",
                    "text": (
                        "GB 50016 is a common reference for fire protection checks related to buildings, warehouses, "
                        "and evacuation planning."
                    ),
                },
                {
                    "clause_no": "GB50160",
                    "clause_title": "石油化工企业设计防火规范",
                    "text": (
                        "GB 50160 is commonly referenced for fire protection, spacing, and layout questions in "
                        "petrochemical sites."
                    ),
                },
                {
                    "clause_no": "GBT50493",
                    "clause_title": "石油化工可燃气体和有毒气体检测报警设计标准",
                    "text": (
                        "GB/T 50493 supports gas detector layout, alarm linkage, and site alarm inspection in "
                        "hazardous area scenarios."
                    ),
                },
                {
                    "clause_no": "GB3869",
                    "clause_title": "体力劳动强度分级",
                    "text": (
                        "GB 3869 can be used for work intensity classification, task arrangement, and workload "
                        "risk hints."
                    ),
                },
                {
                    "clause_no": "GBT4200",
                    "clause_title": "高温作业分级",
                    "text": (
                        "GB/T 4200 is useful for heat exposure warnings, summer maintenance, and hot environment "
                        "work risk prompts."
                    ),
                },
                {
                    "clause_no": "GB5082",
                    "clause_title": "起重吊运指挥信号",
                    "text": (
                        "GB 5082 supports lifting operation command signals and site signal consistency checks."
                    ),
                },
            ],
        },
    )
    print(json.dumps(result, ensure_ascii=False, indent=2))


if __name__ == "__main__":
    main()
