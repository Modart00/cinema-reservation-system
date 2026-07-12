package com.modart00.cinema_reservation_system;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
@EnableCaching
public class CinemaReservationSystemApplication {

	public static void main(String[] args) {
		SpringApplication.run(CinemaReservationSystemApplication.class, args);
	}

}
