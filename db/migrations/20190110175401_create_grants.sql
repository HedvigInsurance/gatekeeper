-- migrate:up
CREATE TABLE "grants" (
    "id" uuid NOT NULL PRIMARY KEY,
    "subject" text NOT NULL,
    "client_id" uuid NOT NULL,
    "grant_method" text NOT NULL,
    "scopes" text[],
    "granted_at" timestamp NOT NULL,
    "revoked_at" timestamp NULL
);

-- migrate:down
DROP TABLE "grants";
