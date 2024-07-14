CREATE TABLE IF NOT EXISTS users
(
    user_id BIGINT GENERATED BY DEFAULT AS IDENTITY NOT NULL,
    name    VARCHAR(250)                            NOT NULL,
    email   VARCHAR(254)                            NOT NULL,
    CONSTRAINT pk_user PRIMARY KEY (user_id),
    CONSTRAINT UQ_USER_EMAIL UNIQUE (email)
);

CREATE TABLE IF NOT EXISTS categories
(
    category_id BIGINT GENERATED BY DEFAULT AS IDENTITY NOT NULL,
    name        VARCHAR(50)                             NOT NULL,
    CONSTRAINT pk_category PRIMARY KEY (category_id),
    CONSTRAINT UQ_CATEGORY_NAME UNIQUE (name)
);

CREATE TABLE IF NOT EXISTS locations
(
    location_id BIGINT GENERATED BY DEFAULT AS IDENTITY NOT NULL,
    lat         FLOAT,
    lon         FLOAT,
    CONSTRAINT pk_location PRIMARY KEY (location_id)
);

CREATE TABLE IF NOT EXISTS events
(
    event_id           BIGINT GENERATED BY DEFAULT AS IDENTITY NOT NULL,
    annotation         VARCHAR(2000)                           NOT NULL,
    category           BIGINT                                  NOT NULL,
    confirmed_requests INT,
    created_on         TIMESTAMP WITHOUT TIME ZONE,
    description        VARCHAR(7000)                           NOT NULL,
    event_date         TIMESTAMP WITHOUT TIME ZONE             NOT NULL,
    initiator          BIGINT                                  NOT NULL,
    location           BIGINT                                  NOT NULL,
    paid               BOOLEAN,
    participant_limit  INT,
    published_on       TIMESTAMP WITHOUT TIME ZONE,
    request_moderation BOOLEAN,
    state              VARCHAR(20) ,
    title              VARCHAR(120)                            NOT NULL,
    views              INT         ,
    CONSTRAINT pk_event PRIMARY KEY (event_id),
    CONSTRAINT fk_event_user
        FOREIGN KEY (initiator) REFERENCES users (user_id),
    CONSTRAINT fk_event_location
        FOREIGN KEY (location) REFERENCES locations (location_id),
    CONSTRAINT fk_event_category
        FOREIGN KEY (category) REFERENCES categories (category_id)
);

CREATE TABLE IF NOT EXISTS requests
(
    request_id BIGINT GENERATED BY DEFAULT AS IDENTITY NOT NULL,
    created    TIMESTAMP WITHOUT TIME ZONE             NOT NULL,
    event      BIGINT                                  NOT NULL,
    requester  BIGINT                                  NOT NULL,
    status     VARCHAR(20)                             NOT NULL,
    CONSTRAINT pk_request PRIMARY KEY (request_id),
    CONSTRAINT fk_request_user
        FOREIGN KEY (requester) REFERENCES users (user_id),
    CONSTRAINT fk_request_event
        FOREIGN KEY (event) REFERENCES events (event_id)
);

CREATE TABLE IF NOT EXISTS compilations
(
    compilation_id BIGINT GENERATED BY DEFAULT AS IDENTITY NOT NULL,
    pinned BOOLEAN,
    title VARCHAR(50),
    CONSTRAINT pk_compilation PRIMARY KEY (compilation_id)
);

CREATE TABLE IF NOT EXISTS compilation_event
(
    compilation_id BIGINT,
    event_id BIGINT,
    CONSTRAINT fk_compilation_event_event
        FOREIGN KEY (event_id) REFERENCES events (event_id),
    CONSTRAINT fk_compilation_event_compilations
        FOREIGN KEY (compilation_id) REFERENCES compilations (compilation_id) ON DELETE CASCADE
);