
ALTER TABLE account
    ADD frozen_amount VARCHAR(255);
ALTER TABLE account
    ADD account_status VARCHAR(255);

ALTER TABLE transaction
    ADD timestamp TIMESTAMP;
ALTER TABLE transaction
    ADD transaction_status VARCHAR(255);

ALTER TABLE client
    ADD client_id BIGINT PRIMARY KEY;