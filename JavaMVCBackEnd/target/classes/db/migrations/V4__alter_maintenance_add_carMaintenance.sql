ALTER TABLE maintenance
    ADD COLUMN car_id BIGINT NOT NULL;

ALTER TABLE maintenance
    ADD CONSTRAINT fk_car_maintenance
        FOREIGN KEY (car_id)
            REFERENCES cars(id)
            ON DELETE CASCADE;