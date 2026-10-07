package me.legrange.typelink.sql.parser;

import me.legrange.typelink.TableMapper;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.concurrent.ConcurrentHashMap;

/**
 * What each column of a table holds, as its mapper says. Asking a mapper means walking the type's components or
 * fields, which is too much to repeat for every column reference in every query, so the answer is kept per mapper and
 * type. A mapper is expected to describe a type the same way for as long as it lives.
 */
final class StoredTypes {

    private static final Map<TableMapper<?>, Map<Class<?>, Map<String, Class<?>>>> CACHE =
            Collections.synchronizedMap(new WeakHashMap<>());

    private StoredTypes() {
    }

    /** The type the mapper gives {@code column}, or {@code declared} when the mapper does not list that column. */
    @SuppressWarnings({"unchecked", "rawtypes"})
    static Class<?> of(TableMapper mapper, Class<?> type, String column, Class<?> declared) {
        var byType = CACHE.computeIfAbsent(mapper, _ -> new ConcurrentHashMap<>());
        var columns = byType.computeIfAbsent(type, _ -> {
            var found = new HashMap<String, Class<?>>();
            for (var name : (Iterable<String>) mapper.columnNames(type)) {
                found.put(name, mapper.columnType(type, name));
            }
            return Map.copyOf(found);
        });
        return columns.getOrDefault(column, declared);
    }
}
