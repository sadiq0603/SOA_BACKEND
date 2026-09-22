-- Starter catalog data for BiblioTech.
-- Run this against bibliotech_books after the book-service has created its tables.
-- Safe to run more than once.

INSERT INTO branches (name, location, active)
SELECT 'Vijayawada Central', 'Vijayawada', true
WHERE NOT EXISTS (
    SELECT 1 FROM branches WHERE name = 'Vijayawada Central'
);

INSERT INTO branches (name, location, active)
SELECT 'Hyderabad Knowledge Center', 'Hyderabad', true
WHERE NOT EXISTS (
    SELECT 1 FROM branches WHERE name = 'Hyderabad Knowledge Center'
);

INSERT INTO branches (name, location, active)
SELECT 'Visakhapatnam Academic Library', 'Visakhapatnam', true
WHERE NOT EXISTS (
    SELECT 1 FROM branches WHERE name = 'Visakhapatnam Academic Library'
);

INSERT INTO branches (name, location, active)
SELECT 'Guntur Engineering Library', 'Guntur', true
WHERE NOT EXISTS (
    SELECT 1 FROM branches WHERE name = 'Guntur Engineering Library'
);

INSERT INTO books (
    book_code, isbn, title, author, category, description,
    cover_image, total_copies, available_copies, status, branch_id
)
SELECT
    'BK-JAV-001', '9780134685991', 'Effective Java (3rd Edition)', 'Joshua Bloch',
    'Computer Science',
    'The definitive guide to Java best practices, design patterns, and idiomatic coding.',
    NULL, 4, 4, 'AVAILABLE', b.id
FROM branches b
WHERE b.name = 'Vijayawada Central'
  AND NOT EXISTS (SELECT 1 FROM books WHERE book_code = 'BK-JAV-001');

INSERT INTO books (
    book_code, isbn, title, author, category, description,
    cover_image, total_copies, available_copies, status, branch_id
)
SELECT
    'BK-DB-001', '9780078022159', 'Database System Concepts', 'Silberschatz, Korth & Sudarshan',
    'Computer Science',
    'A comprehensive introduction to database architecture, design, and implementation.',
    NULL, 2, 2, 'AVAILABLE', b.id
FROM branches b
WHERE b.name = 'Hyderabad Knowledge Center'
  AND NOT EXISTS (SELECT 1 FROM books WHERE book_code = 'BK-DB-001');

INSERT INTO books (
    book_code, isbn, title, author, category, description,
    cover_image, total_copies, available_copies, status, branch_id
)
SELECT
    'BK-DATA-001', '9781449373320', 'Designing Data-Intensive Applications', 'Martin Kleppmann',
    'Engineering',
    'The big ideas behind reliable, scalable, and maintainable data systems.',
    NULL, 3, 3, 'AVAILABLE', b.id
FROM branches b
WHERE b.name = 'Visakhapatnam Academic Library'
  AND NOT EXISTS (SELECT 1 FROM books WHERE book_code = 'BK-DATA-001');

INSERT INTO books (
    book_code, isbn, title, author, category, description,
    cover_image, total_copies, available_copies, status, branch_id
)
SELECT
    'BK-ARCH-001', '9780134494166', 'Clean Architecture', 'Robert C. Martin',
    'Software Design',
    'Principles and practices for building maintainable software systems.',
    NULL, 6, 6, 'AVAILABLE', b.id
FROM branches b
WHERE b.name = 'Guntur Engineering Library'
  AND NOT EXISTS (SELECT 1 FROM books WHERE book_code = 'BK-ARCH-001');