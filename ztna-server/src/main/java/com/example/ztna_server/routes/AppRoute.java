package com.example.ztna_server.routes;

import com.example.ztna_server.dto.App;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;



@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AppRoute {
    String connectorId;
    App app;
}
