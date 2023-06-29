package com.sumant.learning.boot3democlient;

import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.graphql.data.method.annotation.BatchMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.graphql.data.method.annotation.SchemaMapping;
import org.springframework.stereotype.Controller;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.support.WebClientAdapter;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.invoker.HttpServiceProxyFactory;
import reactor.core.publisher.Flux;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.prefs.PreferencesFactory;

@SpringBootApplication
public class Boot3DemoClientApplication {

    public static void main(String[] args) {
        SpringApplication.run(Boot3DemoClientApplication.class, args);
    }

    @Bean
    ApplicationRunner applicationRunner(CustomerHttpClient customerHttpClient) {
        return args -> customerHttpClient.getAllCustomers().subscribe(System.out::println);
    }

    @Bean
    CustomerHttpClient customerHttpClient(WebClient.Builder builder){
        var webClient = builder.baseUrl("http://localhost:9091").build();
        var webClientAdaptor = WebClientAdapter.forClient(webClient);
        var httpServiceProxy = HttpServiceProxyFactory
                .builder()
                .clientAdapter(webClientAdaptor)
                .build()
                .createClient(CustomerHttpClient.class);

        return httpServiceProxy;

    }

}

record Customer(Integer id, String name){}
record Profile (Integer id){}

//Declarative HTTP Interface
interface CustomerHttpClient {

    @GetExchange("/customers")
    Flux<Customer> getAllCustomers();

    @GetExchange("/customers/{name}")
    Flux<Customer> getCustomerByName(String name);
}

@Controller
class CustomerGraphQLController{

    private final CustomerHttpClient customerHttpClient;

    CustomerGraphQLController(CustomerHttpClient customerHttpClient) {
        this.customerHttpClient = customerHttpClient;
    }

    @BatchMapping
    Map<Customer, Profile> profile(List<Customer> customerList){
        var customerProfileMap = new HashMap<Customer, Profile>();
        for (var customer : customerList){
            customerProfileMap.put(customer, new Profile(customer.id()));
        }
        return customerProfileMap;
    }

//    @SchemaMapping(typeName = "Customer")
//    Profile profile(Customer customer){
//        System.out.println("Customer:" + customer.name());
//        return new Profile(customer.id());
//    }

    //@SchemaMapping(typeName = "Query", field = "customers")
    @QueryMapping(value = "customers")
    Flux<Customer> getAllCustomers(){
        return customerHttpClient.getAllCustomers();
    }
}