-- Drop tables if they exist (to allow re-running the script)
DROP TABLE IF EXISTS LINE_ITEM;
DROP TABLE IF EXISTS INVOICE;
DROP TABLE IF EXISTS PRODUCT;
DROP TABLE IF EXISTS CUSTOMER;
DROP TABLE IF EXISTS COMPANY;

-- 1. COMPANY Table
CREATE TABLE COMPANY (
    companyID SERIAL PRIMARY KEY,
    companyName VARCHAR(255) NOT NULL,
    address TEXT,
    website VARCHAR(255),
    phone VARCHAR(50),
    termsInstructions TEXT
);

-- 2. CUSTOMER Table
CREATE TABLE CUSTOMER (
    customerID SERIAL PRIMARY KEY,
    customerName VARCHAR(255) NOT NULL,
    address TEXT,
    email VARCHAR(255),
    phone VARCHAR(50)
);

-- 3. PRODUCT Table
CREATE TABLE PRODUCT (
    productID SERIAL PRIMARY KEY,
    productName VARCHAR(255) NOT NULL,
    defaultPrice DECIMAL(10, 2)
);

-- 4. INVOICE Table
CREATE TABLE INVOICE (
    invoiceNo VARCHAR(50) PRIMARY KEY,
    companyID INT REFERENCES COMPANY(companyID),
    customerID INT REFERENCES CUSTOMER(customerID),
    invoiceDate DATE NOT NULL,
    dueDate DATE,
    discount DECIMAL(10, 2) DEFAULT 0.00,
    taxRate DECIMAL(5, 2) DEFAULT 0.00
);

-- 5. LINE_ITEM Table
CREATE TABLE LINE_ITEM (
    lineItemID SERIAL PRIMARY KEY,
    invoiceNo VARCHAR(50) REFERENCES INVOICE(invoiceNo) ON DELETE CASCADE,
    productID INT REFERENCES PRODUCT(productID),
    description TEXT,
    qtyHr DECIMAL(10, 2) NOT NULL,
    unitPrice DECIMAL(10, 2) NOT NULL
);

-- Sample Data
INSERT INTO COMPANY (companyName, address, website, phone, termsInstructions)
VALUES ('Stanford Plumbing & Heating', '123 Tech Lane, Palo Alto, CA', 'www.stanfordph.com', '555-0123', 'Payment due within 30 days.');

INSERT INTO PRODUCT (productName, defaultPrice)
VALUES ('Standard Service Call', 85.00),
       ('Emergency Pipe Repair', 150.00),
       ('Faucet Installation', 120.00),
       ('Water Heater Tune-up', 200.00);

INSERT INTO CUSTOMER (customerName, address, email, phone)
VALUES ('John Doe', '456 Oak St, Stanford, CA', 'john.doe@email.com', '555-9876');
