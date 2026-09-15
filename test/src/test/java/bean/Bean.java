package bean;

import java.util.Objects;

/**
 * Base class for all bean entities with a common id field.
 */
public abstract class Bean {
    private Long id;

    protected Bean() {
    }

    protected Bean(Long id) {
        this.id = id;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Bean bean = (Bean) o;
        return Objects.equals(id, bean.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
