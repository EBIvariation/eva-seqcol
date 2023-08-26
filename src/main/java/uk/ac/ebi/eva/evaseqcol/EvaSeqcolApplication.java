package uk.ac.ebi.eva.evaseqcol;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.retry.annotation.EnableRetry;

@SpringBootApplication
@EnableRetry
public class EvaSeqcolApplication {

	public static void main(String[] args) {
		SpringApplication.run(EvaSeqcolApplication.class, args);
	}
}
