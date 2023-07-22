DROP TABLE IF EXISTS USERS, ITEMS, REQUESTS, COMMENTS, BOOKINGS CASCADE;

CREATE TABLE IF NOT EXISTS USERS (
    user_id  bigint GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
    name     varchar(50) NOT NULL,
    email    varchar(50) NOT NULL,
    CONSTRAINT UQ_USER_EMAIL UNIQUE (email)
    );

CREATE TABLE IF NOT EXISTS REQUESTS (
    request_id   BIGINT GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
    created_date timestamp without time zone NOT NULL,
    description  VARCHAR(512) NOT NULL,
    requestor_id BIGINT REFERENCES USERS(user_id) ON DELETE CASCADE
    );

CREATE TABLE IF NOT EXISTS ITEMS (
    item_id bigint GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
    name varchar(50) NOT NULL,
    description varchar(255) NOT NULL,
    is_available boolean NOT NULL,
    owner_id bigint REFERENCES USERS(user_id) ON DELETE CASCADE,
    requests_id bigint REFERENCES REQUESTS(request_id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS BOOKINGS (
    booking_id bigint GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
    start_date timestamp without time zone NOT NULL,
    end_date timestamp without time zone NOT NULL,
    item_id bigint REFERENCES ITEMS(item_id) ON DELETE CASCADE,
    booker_id bigint REFERENCES USERS(user_id) ON DELETE CASCADE,
    status varchar NOT NULL
    );

CREATE TABLE IF NOT EXISTS COMMENTS (
    comment_id bigint GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
    text varchar(255) NOT NULL,
    item_id bigint REFERENCES ITEMS(item_id) ON DELETE CASCADE,
    author_id bigint REFERENCES USERS(user_id) ON DELETE CASCADE,
    created TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT NOW()
);

