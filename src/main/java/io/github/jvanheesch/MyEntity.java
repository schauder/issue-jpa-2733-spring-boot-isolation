package io.github.jvanheesch;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;

@Entity
public class MyEntity {
    @Id
    private Long id;
    private String someValue;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getSomeValue() {
        return someValue;
    }

    public void setSomeValue(String someValue) {
        this.someValue = someValue;
    }
}
