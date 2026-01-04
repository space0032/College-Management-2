CREATE TABLE IF NOT EXISTS employees (
    id INT AUTO_INCREMENT PRIMARY KEY,
    employee_id VARCHAR(50) NOT NULL UNIQUE, -- e.g. EMP001
    user_id INT UNIQUE, -- Link to login user if applicable
    first_name VARCHAR(100) NOT NULL,
    last_name VARCHAR(100) NOT NULL,
    email VARCHAR(100) NOT NULL UNIQUE,
    phone VARCHAR(20),
    department_id INT,
    designation VARCHAR(100),
    join_date DATE NOT NULL,
    salary DECIMAL(10, 2),
    status ENUM('ACTIVE', 'RESIGNED', 'TERMINATED', 'ON_LEAVE') DEFAULT 'ACTIVE',
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE SET NULL,
    FOREIGN KEY (department_id) REFERENCES departments(id) ON DELETE SET NULL
);

CREATE TABLE IF NOT EXISTS payroll_entries (
    id INT AUTO_INCREMENT PRIMARY KEY,
    employee_id INT NOT NULL,
    month INT NOT NULL, -- 1-12
    year INT NOT NULL,
    basic_salary DECIMAL(10, 2) NOT NULL,
    bonuses DECIMAL(10, 2) DEFAULT 0,
    deductions DECIMAL(10, 2) DEFAULT 0,
    net_salary DECIMAL(10, 2) NOT NULL,
    payment_date DATE,
    status ENUM('PENDING', 'PAID') DEFAULT 'PENDING',
    FOREIGN KEY (employee_id) REFERENCES employees(id)
);
