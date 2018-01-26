package org.icgc.dcc.song.indexer;

import org.icgc.dcc.song.indexer.service.SongStreamingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.actuate.autoconfigure.ManagementWebSecurityAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.SecurityAutoConfiguration;
import org.springframework.context.ApplicationContext;

@SpringBootApplication(exclude = { SecurityAutoConfiguration.class, ManagementWebSecurityAutoConfiguration.class })
public class IndexerMain {

    public static void main(String... args) {

        ApplicationContext context = SpringApplication.run(IndexerMain.class, args);
        SongStreamingService service = context.getBean(SongStreamingService.class);
        service.run();
    }

}
