alter table standard_library_entry
    add column if not exists current_version_no integer not null default 1,
    add column if not exists archived boolean not null default false,
    add column if not exists deleted_at timestamp null;

create table if not exists standard_library_version_history (
    id bigserial primary key,
    entry_id bigint not null references standard_library_entry(id) on delete cascade,
    version_no integer not null,
    change_type varchar(32) not null,
    change_note text,
    snapshot_json text not null,
    tenant_id bigint not null default 1,
    created_at timestamp not null default now()
);

create index if not exists idx_standard_library_version_entry on standard_library_version_history(entry_id);
create index if not exists idx_standard_library_version_tenant on standard_library_version_history(tenant_id);
