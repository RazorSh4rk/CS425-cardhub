CREATE TABLE IF NOT EXISTS testdata (
    id INT AUTO_INCREMENT PRIMARY KEY,
    echo VARCHAR(255)
);

INSERT INTO testdata (echo) VALUES ('Hello World');
