CREATE TABLE IF NOT EXISTS client (
    id BIGINT PRIMARY KEY,  -- Assuming 'id' is a unique identifier for each client
    mail VARCHAR(255) NOT NULL,  -- Email field with a length limit (you can adjust the size)
    password VARCHAR(45) NOT NULL,  -- Password field with size constraints (adjust as needed)
    role VARCHAR(30) DEFAULT 'client',  -- Role field with default value 'client'
    address VARCHAR(30),  -- Address field with size constraint (you can adjust the size)
    balance NUMERIC(15, 2) DEFAULT 0.00,  -- Balance field with default value 0.00 (BigDecimal equivalent)
    CONSTRAINT unique_mail UNIQUE(mail)  -- Ensure the 'mail' field is unique
);
