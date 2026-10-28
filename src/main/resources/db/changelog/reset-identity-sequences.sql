-- data.sql seeds customer/order rows with explicit ids via raw SQL, which never advances the
-- identity sequences backing GenerationType.IDENTITY. Without this, the first INSERT through the
-- application after a fresh seed collides with an existing seeded row's id.
SELECT setval('customer_id_seq', COALESCE((SELECT MAX(id) FROM customer), 0) + 1, false);
SELECT setval('order_id_seq', COALESCE((SELECT MAX(id) FROM "order"), 0) + 1, false);
