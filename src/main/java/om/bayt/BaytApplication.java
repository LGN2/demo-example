package om.bayt;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableScheduling;
import java.time.Clock;
import java.time.ZoneId;

@SpringBootApplication
@EnableScheduling
public class BaytApplication {
    public static void main(String[] args) { SpringApplication.run(BaytApplication.class, args); }
    @Bean Clock clock() { return Clock.system(ZoneId.of("Asia/Muscat")); }
}
