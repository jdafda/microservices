package com.reservation;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import org.springframework.data.rest.core.annotation.RestResource;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import java.util.Collection;
import java.util.stream.Stream;


@SpringBootApplication
public class ReservationServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(ReservationServiceApplication.class, args);
    }

    @Bean
    public CommandLineRunner runner(ReservationRepository reservationRepository) {
        return strings -> {
            Stream.of("Max","John","Eric","Thomas")
                    .forEach(name -> reservationRepository.save(new Reservation(name)));
            reservationRepository.findAll().forEach(System.out::println);

        };
    }
}

@RefreshScope
@RestController
class ReservationService {

    @Value("${message}")
    private String message;

    @Autowired
    private ReservationRepository reservationRepository;

    @RequestMapping(name = "/message", method = RequestMethod.GET)
    public String message() {
        return message;
    }

}

@RepositoryRestResource
interface ReservationRepository extends JpaRepository<Reservation, Long> {
    @RestResource(path = "by-name")
    Collection<Reservation> findByReservationName(@Param("rn") String rn);
}

@Entity
class Reservation {

    @javax.persistence.Id
    @GeneratedValue
    private Long id;
    private String reservationName;

    public Reservation() {
    }

    public Reservation(String reservationName) {
        this.reservationName = reservationName;
    }

    public Long getId() {
        return id;
    }

    public String getReservationName() {
        return reservationName;
    }

    @Override
    public String toString() {
        return "Reservation{" +
                "id=" + id +
                ", reservationName='" + reservationName + '\'' +
                '}';
    }
}
