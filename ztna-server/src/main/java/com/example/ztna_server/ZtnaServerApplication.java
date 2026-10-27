package com.example.ztna_server;

import com.example.ztna_server.routes.RoutingTable;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@ConfigurationPropertiesScan
@Profile("!test")
@EnableScheduling
public class ZtnaServerApplication {
    public static void main(String[] args) {
		SpringApplication.run(ZtnaServerApplication.class, args);
	}
}
