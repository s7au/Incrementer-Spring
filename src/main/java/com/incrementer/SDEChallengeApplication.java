package com.incrementer;

import java.net.InetAddress;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.core.env.Environment;

import com.incrementer.data.InterfaceImplementation;

@SpringBootApplication
public class SDEChallengeApplication {

    private static final Logger log = LoggerFactory.getLogger(SDEChallengeApplication.class);

	public static void main(String[] args) {
	    // this part is pretty much copy-pasted from JHipster
	    ApplicationContext ctx = SpringApplication.run(SDEChallengeApplication.class, args);
		Environment env = ctx.getEnvironment();
		String protocol = "http";
        if (env.getProperty("server.ssl.key-store") != null) {
            protocol = "https";
        }
        String hostAddress = "localhost";
        try {
            hostAddress = InetAddress.getLocalHost().getHostAddress();
        } catch (Exception e) {
            log.warn("The host name could not be determined, using `localhost` as fallback");
        }
        log.info("\n----------------------------------------------------------\n\t" +
                "Application is running! Access URLs:\n\t" +
                "Local: \t\t{}://localhost:{}\n\t" +
                "External: \t{}://{}:{}\n" +
                "----------------------------------------------------------",
            protocol,
            env.getProperty("server.port"),
            protocol,
            hostAddress,
            env.getProperty("server.port"));
        InterfaceImplementation bean = ctx.getBean(InterfaceImplementation.class);
        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                bean.outputData();
            }
        });
	}

}

