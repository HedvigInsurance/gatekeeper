-- migrate:up
CREATE TABLE "refresh_tokens" (
    "id" uuid PRIMARY KEY,
    "token" text UNIQUE NOT NULL,
    "subject" text NOT NULL,
    "client_id" uuid NOT NULL,
    "scopes" text[] NOT NULL,
    "created_at" timestamp NOT NULL,
    "used_at" timestamp NULL,
    "revoked_at" timestamp NULL
);

-- migrate:down
DROP TABLE "refresh_tokens";
