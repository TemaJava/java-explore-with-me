CREATE TABLE IF NOT EXISTS USERS
(
    ID    BIGINT generated by default as identity primary key,
    EMAIL VARCHAR(254) not null unique,
    NAME  VARCHAR(250) not null,
    CONSTRAINT un_user_email UNIQUE (EMAIL)
);

CREATE TABLE IF NOT EXISTS COMPILATIONS
(
    ID     BIGINT generated by default as identity primary key,
    PINNED BOOLEAN default false,
    TITLE  VARCHAR(50) not null UNIQUE
);

CREATE TABLE IF NOT EXISTS CATEGORIES
(
    ID   BIGINT generated by default as identity primary key,
    NAME VARCHAR(50) not null,
    CONSTRAINT un_category_name UNIQUE (NAME)
);

CREATE TABLE IF NOT EXISTS LOCATIONS
(
    ID  BIGINT generated by default as identity primary key,
    LAT FLOAT not null,
    LON FLOAT not null
);

CREATE TABLE IF NOT EXISTS EVENTS
(
    ID                 BIGINT generated by default as identity primary key,
    ANNOTATION         VARCHAR(2000)               not null,
    CATEGORY_ID        BIGINT                      not null,
    CREATED_ON         TIMESTAMP WITHOUT TIME ZONE not null,
    DESCRIPTION        VARCHAR(7000),
    EVENT_DATE         TIMESTAMP WITHOUT TIME ZONE not null,
    INITIATOR_ID       BIGINT                      not null,
    LOCATION_ID        BIGINT                      not null,
    PAID               BOOLEAN     DEFAULT FALSE,
    PARTICIPANT_LIMIT  INT         DEFAULT 0,
    PUBLISHED_ON       TIMESTAMP WITHOUT TIME ZONE not null,
    REQUEST_MODERATION BOOLEAN     DEFAULT TRUE,
    EVENT_STATE        VARCHAR(20) DEFAULT 'PENDING',
    TITLE              VARCHAR(120)                not null,
    CONSTRAINT fk_events_categories FOREIGN KEY (category_id) REFERENCES CATEGORIES (id),
    CONSTRAINT fk_events_users FOREIGN KEY (initiator_id) REFERENCES USERS (id),
    CONSTRAINT fk_events_location FOREIGN KEY (location_id) REFERENCES LOCATIONS (id)
);

CREATE TABLE IF NOT EXISTS COMPILATION_EVENT
(
    COMPILATION_ID BIGINT not null,
    EVENT_ID       BIGINT not null,
    CONSTRAINT PK_COMPILATION_EVENTS
        PRIMARY KEY (COMPILATION_ID, EVENT_ID),
    CONSTRAINT FK_COMPILATION_EVENT FOREIGN KEY (compilation_id) REFERENCES COMPILATIONS (id) ON DELETE CASCADE,
    CONSTRAINT FK_EVENT_COMP FOREIGN KEY (event_id) REFERENCES EVENTS (id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS REQUESTS
(
    ID           BIGINT generated by default as identity primary key,
    CREATED      TIMESTAMP WITHOUT TIME ZONE not null,
    EVENT_ID     BIGINT                      not null,
    REQUESTER_ID BIGINT                      not null,
    STATUS       VARCHAR(20)                 NOT NULL,
    CONSTRAINT FK_USER_REQ FOREIGN KEY (requester_id) REFERENCES USERS (id) ON DELETE CASCADE,
    CONSTRAINT FK_EVENT_REQ FOREIGN KEY (event_id) REFERENCES EVENTS (id) ON DELETE CASCADE
)

