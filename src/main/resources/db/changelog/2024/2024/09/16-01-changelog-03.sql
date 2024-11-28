CREATE SEQUENCE IF NOT EXISTS account_seq START WITH 1 INCREMENT BY 50;
CREATE SEQUENCE IF NOT EXISTS transaction_seq START WITH 1 INCREMENT BY 50;

CREATE TABLE IF NOT EXISTS account
(
    id BIGINT PRIMARY KEY,
    client_id BIGINT NOT NULL REFERENCES client(id),
    account_type VARCHAR(25),
    balance TEXT
);

CREATE TABLE IF NOT EXISTS transaction
(
    id BIGINT PRIMARY KEY,
    account_id BIGINT NOT NULL REFERENCES account(id),
    amount TEXT,
    execution_time TIME
);



