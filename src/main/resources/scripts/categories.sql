CREATE TABLE IF NOT EXISTS categories (
                                          id          BIGSERIAL PRIMARY KEY,
                                          title       VARCHAR NOT NULL,
                                          description VARCHAR,
                                          parent_id   BIGINT REFERENCES categories(id) ON DELETE SET NULL,
                                          created_at  TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                                          updated_at  TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);