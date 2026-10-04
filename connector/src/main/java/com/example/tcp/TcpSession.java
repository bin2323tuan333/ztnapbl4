package com.example.tcp;

import io.netty.channel.Channel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TcpSession {
    private int streamId;
    private String service;
    Channel tcpChannel;
    Channel wsChannel;
}
