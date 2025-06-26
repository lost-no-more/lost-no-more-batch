package lost_no_more.lost_no_more_batch;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
public class LostNoMoreBatchApplication {

	public static void main(String[] args) {
		System.exit(SpringApplication.exit(
			SpringApplication.run(LostNoMoreBatchApplication.class, args)
		));
	}

}
