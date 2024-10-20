create table PRODUCT
(
    ID           BIGSERIAL          NOT NULL,
    CODE         varchar(10) UNIQUE NOT NULL,
    NAME         varchar(255)       NOT NULL,
    PRICE_EUR    numeric(38, 2)     NOT NULL,
    DESCRIPTION  varchar(255),
    IS_AVAILABLE boolean            NOT NULL,
    primary key (ID)
);