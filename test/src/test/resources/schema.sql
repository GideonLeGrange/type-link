
CREATE TABLE IF NOT EXISTS Client(
    id BIGINT PRIMARY KEY,
    name VARCHAR(32)
);

CREATE TABLE IF NOT EXISTS Person (
    id BIGINT PRIMARY KEY,
    clientId BIGINT,
    name VARCHAR(32),
    age INT,
    sex TEXT CHECK (sex in ('MALE','FEMALE','OTHER')),
    birthDay DATE,
    email VARCHAR(128)
);

CREATE TABLE IF NOT EXISTS Invoice(
    id BIGINT PRIMARY KEY,
    description VARCHAR(32),
    clientId BIGINT,
    invoiceDate DATE,
    amount DECIMAL(10,2),
    paid boolean
);

CREATE TABLE IF NOT EXISTS Vendor(
    id BIGINT PRIMARY KEY,
    name VARCHAR(32)
);

CREATE TABLE IF NOT EXISTS Town(
    id BIGINT PRIMARY KEY,
    name VARCHAR(64),
    alt float
);

CREATE TABLE IF NOT EXISTS Meeting(
    id BIGINT PRIMARY KEY,
    subject VARCHAR(64),
    startTime timestamp null default null,
    endTime timestamp null default null
);
