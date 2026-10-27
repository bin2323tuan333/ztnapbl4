package com.example.ztna_server.enums;

public enum Action {
    // Auth
    LOGIN,
    LOGIN_FAILED,
    LOGOUT,
    REFRESH_TOKEN,

    // User management
    CREATE_USER,
    UPDATE_USER,
    BLOCK_USER,
    UNBLOCK_USER,
    RESET_PASSWORD,
    DELETE_USER,

    // Resource / Permission 
    CREATE_RESOURCE,
    UPDATE_RESOURCE,
    DELETE_RESOURCE,
    ASSIGN_PERMISSION,
    REVOKE_PERMISSION
}