package me.legrange.typelink.sql.structure;

import java.util.List;

public record SqlOrder(List<SqlColumnOrder> order) {
}
