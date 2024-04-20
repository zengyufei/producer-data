package com.zyf.producer.enums;

public enum DbType {

    DEFAULT("default"),
    MYSQL("mysql");

    private final String key;

    DbType(String key) {
        this.key = key;
    }

    public String getKey() {
        return key;
    }
}
