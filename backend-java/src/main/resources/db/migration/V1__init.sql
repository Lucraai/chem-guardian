create table if not exists standard_document (
    id bigserial primary key,
    doc_code varchar(128),
    doc_name varchar(256) not null,
    doc_type varchar(64) not null,
    version varchar(64),
    status varchar(32) not null default 'ACTIVE',
    source_uri text,
    tenant_id bigint not null default 1,
    created_at timestamp not null default now(),
    updated_at timestamp not null default now()
);

create table if not exists standard_clause (
    id bigserial primary key,
    document_id bigint not null references standard_document(id) on delete cascade,
    clause_no varchar(128),
    clause_title varchar(256),
    clause_text text not null,
    clause_tags text,
    clause_level varchar(32),
    tenant_id bigint not null default 1,
    created_at timestamp not null default now(),
    updated_at timestamp not null default now()
);

create table if not exists safety_scenario (
    id bigserial primary key,
    scenario_code varchar(128) not null,
    scenario_name varchar(256) not null,
    description text,
    enabled boolean not null default true,
    tenant_id bigint not null default 1,
    created_at timestamp not null default now(),
    updated_at timestamp not null default now()
);

create table if not exists scenario_standard_mapping (
    id bigserial primary key,
    scenario_id bigint not null references safety_scenario(id) on delete cascade,
    document_id bigint not null references standard_document(id) on delete cascade,
    clause_id bigint references standard_clause(id) on delete set null,
    match_reason text,
    tenant_id bigint not null default 1,
    created_at timestamp not null default now(),
    updated_at timestamp not null default now()
);

create table if not exists advisor_question (
    id bigserial primary key,
    question_text text not null,
    answer_text text,
    risk_level varchar(32) not null default 'UNKNOWN',
    scenario_id bigint,
    tenant_id bigint not null default 1,
    created_by bigint,
    created_at timestamp not null default now(),
    updated_at timestamp not null default now()
);

create table if not exists advisor_source_ref (
    id bigserial primary key,
    question_id bigint not null references advisor_question(id) on delete cascade,
    document_id bigint references standard_document(id) on delete set null,
    clause_id bigint references standard_clause(id) on delete set null,
    source_title varchar(512),
    source_excerpt text,
    source_uri text,
    score numeric(8,6),
    tenant_id bigint not null default 1,
    created_at timestamp not null default now(),
    updated_at timestamp not null default now()
);

create table if not exists ticket_record (
    id bigserial primary key,
    ticket_no varchar(128) not null,
    ticket_type varchar(64) not null,
    status varchar(32) not null default 'DRAFT',
    content jsonb,
    precheck_result jsonb,
    tenant_id bigint not null default 1,
    created_by bigint,
    created_at timestamp not null default now(),
    updated_at timestamp not null default now()
);

create table if not exists rectification_record (
    id bigserial primary key,
    source_type varchar(64) not null,
    source_id bigint not null,
    title varchar(256) not null,
    description text,
    status varchar(32) not null default 'OPEN',
    owner_id bigint,
    due_date date,
    closed_at timestamp,
    tenant_id bigint not null default 1,
    created_by bigint,
    created_at timestamp not null default now(),
    updated_at timestamp not null default now()
);

create table if not exists audit_log (
    id bigserial primary key,
    actor_id bigint,
    actor_name varchar(128),
    action_type varchar(128) not null,
    resource_type varchar(128) not null,
    resource_id varchar(128),
    detail jsonb,
    tenant_id bigint not null default 1,
    created_at timestamp not null default now()
);

create index if not exists idx_standard_document_tenant on standard_document(tenant_id);
create index if not exists idx_standard_clause_document on standard_clause(document_id);
create index if not exists idx_scenario_tenant on safety_scenario(tenant_id);
create index if not exists idx_mapping_scenario on scenario_standard_mapping(scenario_id);
create index if not exists idx_question_tenant on advisor_question(tenant_id);
create index if not exists idx_ticket_tenant on ticket_record(tenant_id);
create index if not exists idx_rectification_tenant on rectification_record(tenant_id);
create index if not exists idx_audit_tenant on audit_log(tenant_id);

