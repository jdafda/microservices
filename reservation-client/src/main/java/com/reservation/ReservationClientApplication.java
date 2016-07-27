package com.reservation;

import com.netflix.hystrix.contrib.javanica.annotation.HystrixCommand;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.circuitbreaker.EnableCircuitBreaker;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.client.loadbalancer.LoadBalancerClient;
import org.springframework.cloud.netflix.zuul.EnableZuulProxy;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.cloud.stream.messaging.Source;
import org.springframework.context.annotation.Bean;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.hateoas.Resources;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableResourceServer;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.util.Collection;
import java.util.Collections;
import java.util.stream.Collectors;

@EnableCircuitBreaker
@EnableBinding(Source.class)
@EnableZuulProxy
@EnableDiscoveryClient
@SpringBootApplication
public class ReservationClientApplication {

    public static void main(String[] args) {
        SpringApplication.run(ReservationClientApplication.class, args);
    }

    //@LoadBalanced  - remove LoadBalanced , Added Ribbon.
    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }

}


@RestController
@RequestMapping("/reservations")
class ReservationApiGatewayRestController {


    @Autowired
    private LoadBalancerClient loadBalancerClient;

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private Source source;

    @RequestMapping(method = RequestMethod.POST)
    public void addReservations(@RequestBody Reservation aReservation) {
        Message<Reservation> reservationMessage = MessageBuilder.withPayload(aReservation).build();
        this.source.output().send(reservationMessage);
    }

    public Collection<String> getReservationNamesFallback() {
        return Collections.emptyList();
    }

    @Autowired
    DiscoveryClient discoveryClient;

    @HystrixCommand(fallbackMethod = "getReservationNamesFallback")
    @RequestMapping(value = "/names", method = RequestMethod.GET)
    public Collection<String> getReservationNames() {

        discoveryClient.getServices().forEach(System.out::print);

        ParameterizedTypeReference<Resources<Reservation>> parameterizedTypeReference = new ParameterizedTypeReference<Resources<Reservation>>() {
        };

        ResponseEntity<Resources<Reservation>> entity =
                this.restTemplate.exchange(getServiceURL("reservation-service") + "/reservations", HttpMethod.GET, null, parameterizedTypeReference);

        return entity
                .getBody()
                .getContent()
                .stream()
                .map(Reservation::getReservationName)
                .collect(Collectors.toList());
    }

    private URI getServiceURL(String serviceName) {
        return  loadBalancerClient
                    .choose(serviceName)
                    .getUri();
    }
}

class Reservation {
    private String reservationName;
    private Long id;

    public String getReservationName() {
        return reservationName;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }
}


