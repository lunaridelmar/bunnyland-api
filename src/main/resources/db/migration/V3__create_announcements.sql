create table announcements (
  id           bigserial primary key,
  owner_id     bigint not null references users(id) on delete cascade,
  title        varchar(200) not null,
  description  text not null,
  city         varchar(120),
  country      varchar(120),
  start_date   date,
  end_date     date,
  status       varchar(20) not null default 'OPEN',
  created_at   timestamp not null default now()
);

create index idx_ann_owner on announcements(owner_id);
create index idx_ann_status on announcements(status);
create index idx_ann_city on announcements(city);
create index idx_ann_country on announcements(country);
