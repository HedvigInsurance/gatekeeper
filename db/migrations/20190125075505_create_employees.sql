-- migrate:up
CREATE TABLE "employees" (
    "id" uuid NOT NULL PRIMARY KEY,
    "email" text NOT NULL,
    "role" text NOT NULL,
    "first_granted_at" timestamp NOT NULL,
    "deleted_at" timestamp NULL
);

-- migrate:down
DROP TABLE "employees";
