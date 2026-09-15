package me.legrange.typelink.sql.unpack;

import me.legrange.typelink.*;
import me.legrange.typelink.lambda.structure.*;

import java.lang.reflect.InvocationTargetException;
import java.util.*;

import static java.lang.String.format;

@SuppressWarnings({"rawtypes", "unchecked"})
public final class Unpack {

    public static List<Object> unpack(List<List<?>> data, TableMapper<?> mapper, SelectionLink selection) {
        var selections = expand(selection);
        var collators = new ArrayList<ResultCollator>();
        for (var i = 0; i < selections.size(); ++i) {
            collators.add(getCollator(mapper, selections.get(i), i));
        }
       return data.stream()
                .map(cols -> {
                    var row = new ArrayList<>();
                    for (var collator : collators) {
                        var collated = collator.collate(cols);
                        row.addAll(Arrays.asList(collated));
                    }
                    return makeRow(row);
                }).toList();
    }

    private static Object makeRow(List<?> data) {
        return data.size() == 1 ? data.getFirst() : Rows.makeRow(data);
    }

    private static  List<SelectionLink> expand(SelectionLink  selection) {
        Link link = selection;
        var res = new ArrayList<SelectionLink>();
        while (link instanceof Selection) {
            res.addFirst((SelectionLink) link);
            link = link.left();
        }
        return res;
    }

    private static ResultCollator getCollator(TableMapper mapper, SelectionLink selection, int pos) {
        if (selection instanceof FunctionSelectionLink fsl && fsl.getFunction() instanceof Selector<?> function) {
            return switch (function.type()) {
                case COUNT_COLUMN, SUM_COLUMN, MIN_COLUMN, MAX_COLUMN, AVG_COLUMN, COUNT_ROWS ->
                        r -> new Object[]{r.get(pos)};
                case LIST_ROW -> List::toArray;
            };
        }
        if (selection.lambda() != null) {
            return value(mapper, selection.lambda().value(), pos);
        }
        throw new UnpackException("Cannot find a way to unpack the selection. BUG!");
    }

    private static ResultCollator value(TableMapper mapper, Value value, int pos) {
        return switch (value) {
            case null -> List::toArray;
            case Argument argument -> argument(mapper, argument);
            case Call call -> call(mapper, call, pos);
            case Operator _ -> passthrough(pos);
            case FieldReference fieldReference -> fieldReference(mapper, fieldReference, pos);
            default ->
                    throw new UnpackException(format("Don't how to determine how to unpack data for a %s. BUG!", value.getClass().getSimpleName()));
        };
    }

    private static ResultCollator argument(TableMapper mapper, Argument argument) {
        if (mapper.isTable(argument.type())) {
            return List::toArray;
        }
        throw new UnpackException(format("Don't how to determine how to unpack data for an %s if it is not referencing a table. BUG!", argument.getClass().getSimpleName()));
    }

    private static ResultCollator fieldReference(TableMapper mapper, FieldReference fieldReference, int pos) {
        return switch (fieldReference) {
            case InstanceFieldReference instanceFieldReference -> {
                if (mapper.isColumn(instanceFieldReference.field())) {
                    yield v -> new Object[]{v.get(pos)};
                }
                throw new UnpackException(format("Don't how to determine how to unpack data for an %s if it is not referencing a table. BUG!", fieldReference.getClass().getSimpleName()));
            }
            case StaticFieldReference _ ->
                    throw new UnpackException(format("Don't how to determine how to unpack data for a %s. BUG!", fieldReference.getClass().getSimpleName()));
        };
    }

    private static ResultCollator call(TableMapper mapper, Call call, int pos) {
        return switch (call) {
            case ConstructorCall constructorCall -> row -> {
                try {
                    var cons = constructorCall.constructor();
                    cons.setAccessible(true);
                    return new Object[]{constructorCall.constructor().newInstance(row.toArray())};
                } catch (InstantiationException | InvocationTargetException | IllegalAccessException e) {
                    throw new UnpackException(e.getMessage(), e);
                }
            };
            case MethodCall methodCall -> methodCall(mapper, methodCall, pos);
            case StaticMethodCall _ ->
                    throw new UnpackException(format("Don't how to determine how to unpack data for a %s. BUG!", call.getClass().getSimpleName()));
        };
    }

    private static ResultCollator methodCall(TableMapper mapper, MethodCall call, int pos) {
        if (mapper.isColumn(call.method())) {
            return v -> new Object[]{v.get(pos)};
        }
        return v -> new Object[]{v.get(pos)};
    }

    private static ResultCollator passthrough(int pos) {
        return v -> new Object[]{v.get(pos)};
    }


    private Unpack() {
    }

}
