package com.example.ztna_server.protocol;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DataFrame {
    private int streamId;
    private int dataLen;
    private byte[] data;
}
