create table announcement_applications (
  id             bigserial primary key,
  announcement_id bigint not null references announcements(id) on delete cascade,
  message        text not null,
  contact        varchar(200) not null,
  created_at     timestamp not null default now()
);

create index idx_app_announcement on announcement_applications(announcement_id);
