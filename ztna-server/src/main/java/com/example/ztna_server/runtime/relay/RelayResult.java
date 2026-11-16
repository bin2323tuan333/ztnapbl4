package com.example.ztna_server.runtime.relay;

public enum RelayResult {
    SUCCESS,
    INVALID_FRAME,
    SESSION_NOT_FOUND,
    SESSION_NOT_ACTIVE,
    SOURCE_INACTIVE,
    DESTINATION_INACTIVE
}
