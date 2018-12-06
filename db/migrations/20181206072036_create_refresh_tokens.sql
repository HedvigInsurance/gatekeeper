-- migrate:up
CREATE TABLE "refresh_tokens" (
    "id" uuid NOT NULL PRIMARY KEY,
    "subject" text NOT NULL,
    "token" text NOT NULL,
    "created_at" timestamp NOT NULL,
    "used_at" timestamp NULL
);

-- migrate:down
DROP TABLE "refresh_tokens";
