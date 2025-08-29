create table users (
  id bigserial primary key,
  email varchar(255) not null unique,
  password_hash varchar(255) not null,
  display_name varchar(255),
  city varchar(255),
  country varchar(255),
  created_at timestamp not null
);

create table user_roles (
  user_id bigint not null references users(id) on delete cascade,
  role varchar(50) not null
);
