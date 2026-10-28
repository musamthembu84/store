-- Enable trigram support so the planner can use an index for substring (ILIKE '%...%') search on customer name
CREATE EXTENSION IF NOT EXISTS pg_trgm;

CREATE INDEX idx_customer_name_trgm ON customer USING gin (name gin_trgm_ops);
