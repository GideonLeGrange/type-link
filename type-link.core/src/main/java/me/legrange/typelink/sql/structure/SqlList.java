package me.legrange.typelink.sql.structure;

import java.util.List;

public record SqlList(List<SqlConstant> values) implements SqlValue {
}
