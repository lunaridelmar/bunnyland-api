package fur.bunnyland.bunnylandapi;

import org.springframework.boot.SpringApplication;

public class TestBunnylandApiApplication {

	public static void main(String[] args) {
		SpringApplication.from(BunnylandApiApplication::main).with(TestcontainersConfiguration.class).run(args);
	}

}
