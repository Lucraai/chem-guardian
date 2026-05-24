create table if not exists standard_library_entry (
    id bigserial primary key,
    code varchar(128) not null unique,
    name varchar(256) not null,
    status varchar(32) not null default 'ACTIVE',
    publish_date varchar(32),
    implement_date varchar(32),
    scope text,
    summary text,
    tags_json text,
    scenarios_json text,
    source_url text,
    tenant_id bigint not null default 1,
    created_at timestamp not null default now(),
    updated_at timestamp not null default now()
);

create index if not exists idx_standard_library_tenant on standard_library_entry(tenant_id);
create index if not exists idx_standard_library_code on standard_library_entry(code);
