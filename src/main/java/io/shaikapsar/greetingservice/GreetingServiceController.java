package io.shaikapsar.greetingservice;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class GreetingServiceController {


	@GetMapping("/")
	public String getting() {
		return "Hello world";
	}
}
