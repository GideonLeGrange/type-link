package bean;

import java.time.LocalDate;
import java.util.Objects;

public class Person extends Bean {
    private Long clientId;
    private String name;
    private int age;
    private String email;
    private Sex sex;
    private LocalDate birthDay;

    public enum Sex {
        MALE, FEMALE
    }

    public Person() {
        super();
    }

    public Person(Long id, Long clientId, String name, int age, String email, Sex sex, LocalDate birthDay) {
        super(id);
        this.clientId = clientId;
        this.name = name;
        this.age = age;
        this.email = email;
        this.sex = sex;
        this.birthDay = birthDay;
    }

    public Long getClientId() {
        return clientId;
    }

    public void setClientId(Long clientId) {
        this.clientId = clientId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Sex getSex() {
        return sex;
    }

    public void setSex(Sex sex) {
        this.sex = sex;
    }

    public LocalDate getBirthDay() {
        return birthDay;
    }

    public void setBirthDay(LocalDate birthDay) {
        this.birthDay = birthDay;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        Person person = (Person) o;
        return age == person.age &&
               Objects.equals(clientId, person.clientId) &&
               Objects.equals(name, person.name) &&
               Objects.equals(email, person.email) &&
               sex == person.sex &&
               Objects.equals(birthDay, person.birthDay);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), clientId, name, age, email, sex, birthDay);
    }
}
