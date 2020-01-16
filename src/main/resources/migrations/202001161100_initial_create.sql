-- liquibase formatted.sql

--changeset fredrikareschoug:20200116-1100-initial-create.sql

CREATE TABLE clients
(
    client_id              uuid PRIMARY KEY,
    client_secret          text      NOT NULL,
    redirect_uris          text[]    NOT NULL,
    authorized_grant_types text[]    NOT NULL,
    authorized_scopes      text[]    NOT NULL,
    created_at             timestamp NOT NULL,
    deactivated_at         timestamp NULL,
    created_by             text      NOT NULL
);

CREATE TABLE refresh_tokens
(
    id         uuid PRIMARY KEY,
    token      text UNIQUE NOT NULL,
    subject    text        NOT NULL,
    client_id  uuid        NOT NULL,
    scopes     text[]      NOT NULL,
    created_at timestamp   NOT NULL,
    used_at    timestamp   NULL,
    revoked_at timestamp   NULL
);

CREATE TABLE grants
(
    id           uuid      NOT NULL PRIMARY KEY,
    subject      text      NOT NULL,
    client_id    uuid      NOT NULL,
    grant_method text      NOT NULL,
    scopes       text[],
    granted_at   timestamp NOT NULL,
    revoked_at   timestamp NULL
);

CREATE TABLE employees
(
    id               uuid      NOT NULL PRIMARY KEY,
    email            text      NOT NULL,
    role             text      NOT NULL,
    first_granted_at timestamp NOT NULL,
    deleted_at       timestamp NULL
);

--rollback DROP TABLE clients; DROP TABLE refresh_tokens; DROP TABLE grants; DROP TABLE employees;
