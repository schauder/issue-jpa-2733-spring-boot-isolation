package io.github.jvanheesch;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import javax.annotation.PostConstruct;

@SpringBootApplication
public class Application {
    @Autowired
    private MyEntityRepository myEntityRepository;

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

    @PostConstruct
    void setup() {
        var entity = new MyEntity();
        entity.setId(1L);
        entity.setSomeValue("INITIAL");
        myEntityRepository.save(entity);
    }
}
