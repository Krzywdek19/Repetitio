package pl.exceptionhandled.repetitio;

import org.springframework.boot.SpringApplication;

public class TestRepetitioApplication {

	public static void main(String[] args) {
		SpringApplication.from(RepetitioApplication::main).with(TestcontainersConfiguration.class).run(args);
	}

}
