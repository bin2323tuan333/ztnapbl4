package com.example.ztna_server;

import com.example.ztna_server.routes.RoutingTable;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class ZtnaServerApplication {
    public static void main(String[] args) {
		SpringApplication.run(ZtnaServerApplication.class, args);
	}
}
