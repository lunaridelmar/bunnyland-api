-- Create initial admin user
INSERT INTO users (email, password_hash, display_name, city, country, created_at)
VALUES (
  'admin@bunnyland.com',
  '$2a$10$h8cAKQF2XnTQpZihUhSivOqhz7zPh9tM3J9G2hGaaJDuslmip5qqq',
  'Admin',
  'Hamburg',
  'Germany',
  NOW()
);

-- Assign ADMIN role
INSERT INTO user_roles (user_id, role)
VALUES (
  (SELECT id FROM users WHERE email = 'admin@bunnyland.com'),
  'ADMIN'
);
