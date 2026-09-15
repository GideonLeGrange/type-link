package bean;

import java.util.Objects;

public class Town extends Bean {
    private String name;
    private float alt;

    public Town() {
        super();
    }

    public Town(Long id, String name, float alt) {
        super(id);
        this.name = name;
        this.alt = alt;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public float getAlt() {
        return alt;
    }

    public void setAlt(float alt) {
        this.alt = alt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        Town town = (Town) o;
        return Float.compare(town.alt, alt) == 0 &&
               Objects.equals(name, town.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), name, alt);
    }
}
