import me.legrange.typelink.json.Json;
import me.legrange.typelink.sql.structure.*;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class TestConstantTypePersistence {

    @Test
    public void testIntegerConstantPreservesType() {
        var original = new SqlConstant(18);
        
        var json = Json.toJson(new SqlQuery(
                new SqlSelect(original, false),
                null, null, null, null, null, null, null
        ));
        
        var deserialized = Json.fromJson(json);
        var deserializedConstant = (SqlConstant) deserialized.select().columns();
        
        System.out.println("Original value: " + original.value() + " (type: " + original.value().getClass().getName() + ")");
        System.out.println("Deserialized value: " + deserializedConstant.value() + " (type: " + deserializedConstant.value().getClass().getName() + ")");
        
        assertThat(deserializedConstant.value()).isInstanceOf(Integer.class);
        assertThat(deserializedConstant.value()).isEqualTo(18);
    }

    @Test
    public void testDoubleConstantPreservesType() {
        var original = new SqlConstant(1000.0);
        
        var json = Json.toJson(new SqlQuery(
                new SqlSelect(original, false),
                null, null, null, null, null, null, null
        ));
        
        var deserialized = Json.fromJson(json);
        var deserializedConstant = (SqlConstant) deserialized.select().columns();
        
        System.out.println("Original value: " + original.value() + " (type: " + original.value().getClass().getName() + ")");
        System.out.println("Deserialized value: " + deserializedConstant.value() + " (type: " + deserializedConstant.value().getClass().getName() + ")");
        
        assertThat(deserializedConstant.value()).isInstanceOf(Double.class);
        assertThat(deserializedConstant.value()).isEqualTo(1000.0);
    }

    @Test
    public void testBooleanConstantPreservesType() {
        var original = new SqlConstant(true);
        
        var json = Json.toJson(new SqlQuery(
                new SqlSelect(original, false),
                null, null, null, null, null, null, null
        ));
        
        var deserialized = Json.fromJson(json);
        var deserializedConstant = (SqlConstant) deserialized.select().columns();
        
        System.out.println("Original value: " + original.value() + " (type: " + original.value().getClass().getName() + ")");
        System.out.println("Deserialized value: " + deserializedConstant.value() + " (type: " + deserializedConstant.value().getClass().getName() + ")");
        System.out.println("JSON:\n" + json);
        
        assertThat(deserializedConstant.value()).isInstanceOf(Boolean.class);
        assertThat(deserializedConstant.value()).isEqualTo(true);
    }
}
