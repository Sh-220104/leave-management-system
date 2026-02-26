DROP TABLE IF EXISTS leave_request;
DROP TABLE IF EXISTS leave_balance;
DROP TABLE IF EXISTS employee_roles;
DROP TABLE IF EXISTS employee;
DROP TABLE IF EXISTS leave_type;

CREATE TABLE IF NOT EXISTS leave_type (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    type VARCHAR(50) NOT NULL,
    description VARCHAR(255) NOT NULL
);

CREATE TABLE IF NOT EXISTS employee (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    email VARCHAR(100) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL
);

CREATE TABLE IF NOT EXISTS employee_roles (
    employee_id BIGINT NOT NULL,
    role VARCHAR(50) NOT NULL,
    PRIMARY KEY (employee_id, role),
    FOREIGN KEY (employee_id) REFERENCES employee(id)
);

CREATE TABLE IF NOT EXISTS leave_balance (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    employee_id BIGINT NOT NULL,
    leave_type_id BIGINT NOT NULL,
    balance DOUBLE NOT NULL,
    FOREIGN KEY (employee_id) REFERENCES employee(id),
    FOREIGN KEY (leave_type_id) REFERENCES leave_type(id)
);

CREATE TABLE IF NOT EXISTS leave_request (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    employee_id BIGINT NOT NULL,
    leave_type_id BIGINT NOT NULL,
    start_date DATE NOT NULL,
    end_date DATE NOT NULL,
    status VARCHAR(30) NOT NULL,
    notes VARCHAR(512),
    manager_comment VARCHAR(512),
    created_on DATE,
    decision_on DATE,
    FOREIGN KEY (employee_id) REFERENCES employee(id),
    FOREIGN KEY (leave_type_id) REFERENCES leave_type(id)
);
