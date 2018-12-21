-- migrate:up
CREATE TABLE "clients" (
    "client_id" uuid PRIMARY KEY,
    "client_secret" text NOT NULL,
    "redirect_uris" text[] NOT NULL,
    "authorized_grant_types" text[] NOT NULL,
    "authorized_scopes" text[] NOT NULL,
    "created_at" timestamp NOT NULL,
    "deactivated_at" timestamp NULL,
    "created_by" text NOT NULL
);

-- migrate:down
DROP TABLE "clients";
