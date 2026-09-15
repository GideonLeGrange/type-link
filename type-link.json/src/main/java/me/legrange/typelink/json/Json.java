package me.legrange.typelink.json;

import com.google.gson.*;
import me.legrange.typelink.sql.structure.*;

import java.lang.reflect.Type;

public final class Json {

    public static String toJson(SqlQuery query) {
        return  gson().toJson(query);
    }

    public static SqlQuery fromJson(String json) {
        return gson().fromJson(json, SqlQuery.class);
    }

    private static Gson gson() {
        return new GsonBuilder()
                .setPrettyPrinting()
                .registerTypeAdapter(Class.class, new ClassTypeAdapter())
                .registerTypeAdapter(SqlConstant.class, new SqlConstantTypeAdapter())
                .registerTypeAdapter(SqlColumn.class, new SqlColumnTypeAdapter())
                .registerTypeAdapter(SqlClause.class, new SqlClauseTypeAdapter())
                .registerTypeAdapter(SqlJoinClause.class, new SqlJoinClauseTypeAdapter())
                .registerTypeAdapter(SqlPart.class, new SqlPartTypeAdapter())
                .registerTypeAdapter(SqlValue.class, new SqlValueTypeAdapter())
                .registerTypeAdapter(SqlLimit.class, new SqlLimitTypeAdapter())
                .create();
    }

    private Json() {
    }

    private static class ClassTypeAdapter implements JsonSerializer<Class<?>>, JsonDeserializer<Class<?>> {
        @Override
        public JsonElement serialize(Class<?> src, Type typeOfSrc, JsonSerializationContext context) {
            return new JsonPrimitive(src.getName());
        }

        @Override
        public Class<?> deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            try {
                return Thread.currentThread().getContextClassLoader().loadClass(json.getAsString());
            } catch (ClassNotFoundException e) {
                throw new JsonParseException("Class not found: " + json.getAsString(), e);
            }
        }
    }

    private static class SqlConstantTypeAdapter implements JsonSerializer<SqlConstant>, JsonDeserializer<SqlConstant> {
        @Override
        public JsonElement serialize(SqlConstant src, Type typeOfSrc, JsonSerializationContext context) {
            var result = new JsonObject();
            var value = src.value();
            if (value != null) {
                result.addProperty("valueType", value.getClass().getName());
                result.add("value", context.serialize(value));
            } else {
                result.add("value", JsonNull.INSTANCE);
            }
            return result;
        }

        @Override
        public SqlConstant deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            var jsonObject = json.getAsJsonObject();
            var valueElement = jsonObject.get("value");
            
            if (valueElement == null || valueElement.isJsonNull()) {
                return new SqlConstant(null);
            }
            
            var valueTypeElement = jsonObject.get("valueType");
            if (valueTypeElement == null) {
                // Fallback for backward compatibility - just use the value as-is
                return new SqlConstant(context.deserialize(valueElement, Object.class));
            }
            
            var valueTypeName = valueTypeElement.getAsString();
            try {
                var valueType = Thread.currentThread().getContextClassLoader().loadClass(valueTypeName);
                var value = context.deserialize(valueElement, valueType);
                return new SqlConstant(value);
            } catch (ClassNotFoundException e) {
                throw new JsonParseException("Unknown value type: " + valueTypeName, e);
            }
        }
    }

    private static class SqlColumnTypeAdapter implements JsonSerializer<SqlColumn>, JsonDeserializer<SqlColumn> {
        @Override
        public JsonElement serialize(SqlColumn src, Type typeOfSrc, JsonSerializationContext context) {
            var result = new JsonObject();
            result.addProperty("_type", src.getClass().getSimpleName());
            result.add("data", context.serialize(src, src.getClass()));
            return result;
        }

        @Override
        public SqlColumn deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            if (!json.isJsonObject()) {
                throw new JsonParseException("Expected JsonObject for SqlColumn but got: " + json);
            }
            var jsonObject = json.getAsJsonObject();
            
            // Check if this is a SqlConstant (has "value" field instead of "_type")
            if (jsonObject.has("value") && !jsonObject.has("_type")) {
                return context.deserialize(json, SqlConstant.class);
            }
            
            var typeElement = jsonObject.get("_type");
            if (typeElement == null || !typeElement.isJsonPrimitive()) {
                throw new JsonParseException("Missing or invalid _type field in SqlColumn JSON");
            }
            var typeName = typeElement.getAsString();
            var data = jsonObject.get("data");
            if (data == null) {
                throw new JsonParseException("Missing data field in SqlColumn JSON");
            }
            
            return switch (typeName) {
                case "SqlAll" -> context.deserialize(data, SqlAll.class);
                case "SqlTable" -> context.deserialize(data, SqlTable.class);
                case "SqlTableColumn" -> context.deserialize(data, SqlTableColumn.class);
                case "SqlSubSelect" -> context.deserialize(data, SqlSubSelect.class);
                case "SqlConcat" -> context.deserialize(data, SqlConcat.class);
                // SqlFunction implementations
                case "SqlAvg" -> context.deserialize(data, SqlAvg.class);
                case "SqlCount" -> context.deserialize(data, SqlCount.class);
                case "SqlMax" -> context.deserialize(data, SqlMax.class);
                case "SqlMin" -> context.deserialize(data, SqlMin.class);
                case "SqlSum" -> context.deserialize(data, SqlSum.class);
                // SqlOperation implementations
                case "SqlAdd" -> context.deserialize(data, SqlAdd.class);
                case "SqlDivide" -> context.deserialize(data, SqlDivide.class);
                case "SqlMultiply" -> context.deserialize(data, SqlMultiply.class);
                case "SqlSubtract" -> context.deserialize(data, SqlSubtract.class);
                default -> throw new JsonParseException("Unknown SqlColumn type: " + typeName);
            };
        }
    }

    private static class SqlClauseTypeAdapter implements JsonSerializer<SqlClause>, JsonDeserializer<SqlClause> {
        @Override
        public JsonElement serialize(SqlClause src, Type typeOfSrc, JsonSerializationContext context) {
            var result = new JsonObject();
            result.addProperty("_type", src.getClass().getSimpleName());
            result.add("data", context.serialize(src, src.getClass()));
            return result;
        }

        @Override
        public SqlClause deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            var jsonObject = json.getAsJsonObject();
            var typeName = jsonObject.get("_type").getAsString();
            var data = jsonObject.get("data");
            
            return switch (typeName) {
                case "SqlSubSelect" -> context.deserialize(data, SqlSubSelect.class);
                case "SqlNot" -> context.deserialize(data, SqlNot.class);
                case "SqlNull" -> context.deserialize(data, SqlNull.class);
                // SqlLogicalOperator implementations
                case "SqlAnd" -> context.deserialize(data, SqlAnd.class);
                case "SqlOr" -> context.deserialize(data, SqlOr.class);
                // SqlRelationalOperator implementations
                case "SqlEq" -> context.deserialize(data, SqlEq.class);
                case "SqlNeq" -> context.deserialize(data, SqlNeq.class);
                case "SqlGt" -> context.deserialize(data, SqlGt.class);
                case "SqlGe" -> context.deserialize(data, SqlGe.class);
                case "SqlLt" -> context.deserialize(data, SqlLt.class);
                case "SqlLe" -> context.deserialize(data, SqlLe.class);
                case "SqlLike" -> context.deserialize(data, SqlLike.class);
                case "SqlNotLike" -> context.deserialize(data, SqlNotLike.class);
                case "SqlInSet" -> context.deserialize(data, SqlInSet.class);
                case "SqlNotInSet" -> context.deserialize(data, SqlNotInSet.class);
                case "SqlIsNull" -> context.deserialize(data, SqlIsNull.class);
                case "SqlIsNotNull" -> context.deserialize(data, SqlIsNotNull.class);
                default -> throw new JsonParseException("Unknown SqlClause type: " + typeName);
            };
        }
    }

    private static class SqlJoinClauseTypeAdapter implements JsonSerializer<SqlJoinClause>, JsonDeserializer<SqlJoinClause> {
        @Override
        public JsonElement serialize(SqlJoinClause src, Type typeOfSrc, JsonSerializationContext context) {
            var result = new JsonObject();
            result.addProperty("_type", src.getClass().getSimpleName());
            result.add("data", context.serialize(src, src.getClass()));
            return result;
        }

        @Override
        public SqlJoinClause deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            var jsonObject = json.getAsJsonObject();
            var typeName = jsonObject.get("_type").getAsString();
            var data = jsonObject.get("data");
            
            return switch (typeName) {
                case "SqlInnerJoin" -> context.deserialize(data, SqlInnerJoin.class);
                case "SqlLeftOuterJoin" -> context.deserialize(data, SqlLeftOuterJoin.class);
                case "SqlRightOuterJoin" -> context.deserialize(data, SqlRightOuterJoin.class);
                case "SqlFullOuterJoin" -> context.deserialize(data, SqlFullOuterJoin.class);
                default -> throw new JsonParseException("Unknown SqlJoinClause type: " + typeName);
            };
        }
    }

    private static class SqlPartTypeAdapter implements JsonSerializer<SqlPart>, JsonDeserializer<SqlPart> {
        @Override
        public JsonElement serialize(SqlPart src, Type typeOfSrc, JsonSerializationContext context) {
            var result = new JsonObject();
            result.addProperty("_type", src.getClass().getSimpleName());
            result.add("data", context.serialize(src, src.getClass()));
            return result;
        }

        @Override
        public SqlPart deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            var jsonObject = json.getAsJsonObject();
            
            // Check if this is a SqlConstant (has "value" and optionally "valueType" instead of "_type")
            if (jsonObject.has("value") && !jsonObject.has("_type")) {
                return context.deserialize(json, SqlConstant.class);
            }
            
            var typeElement = jsonObject.get("_type");
            if (typeElement == null) {
                throw new JsonParseException("Missing _type field in SqlPart JSON");
            }
            var typeName = typeElement.getAsString();
            
            // SqlPart permits SqlColumn, SqlValue, SqlClause - delegate to their respective adapters
            // Try each type in order based on the typename
            return switch (typeName) {
                // SqlColumn types (excluding SqlConstant which has its own adapter)
                case "SqlAll", "SqlTable", "SqlTableColumn", "SqlSubSelect", "SqlConcat",
                     "SqlAvg", "SqlCount", "SqlMax", "SqlMin", "SqlSum",
                     "SqlAdd", "SqlDivide", "SqlMultiply", "SqlSubtract" -> 
                    context.deserialize(json, SqlColumn.class);
                // SqlValue types  
                case "SqlList" -> context.deserialize(json, SqlValue.class);
                // SqlClause types
                case "SqlNot", "SqlNull", "SqlAnd", "SqlOr",
                     "SqlEq", "SqlNeq", "SqlGt", "SqlGe", "SqlLt", "SqlLe",
                     "SqlLike", "SqlNotLike", "SqlInSet", "SqlNotInSet",
                     "SqlIsNull", "SqlIsNotNull" -> 
                    context.deserialize(json, SqlClause.class);
                default -> throw new JsonParseException("Unknown SqlPart type: " + typeName);
            };
        }
    }

    private static class SqlValueTypeAdapter implements JsonSerializer<SqlValue>, JsonDeserializer<SqlValue> {
        @Override
        public JsonElement serialize(SqlValue src, Type typeOfSrc, JsonSerializationContext context) {
            var result = new JsonObject();
            result.addProperty("_type", src.getClass().getSimpleName());
            result.add("data", context.serialize(src, src.getClass()));
            return result;
        }

        @Override
        public SqlValue deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            var jsonObject = json.getAsJsonObject();
            
            // Check if this is a SqlConstant (has "value" field instead of "_type")
            if (jsonObject.has("value") && !jsonObject.has("_type")) {
                return context.deserialize(json, SqlConstant.class);
            }
            
            var typeElement = jsonObject.get("_type");
            if (typeElement == null) {
                throw new JsonParseException("Missing _type field in SqlValue JSON");
            }
            var typeName = typeElement.getAsString();
            var data = jsonObject.get("data");
            
            return switch (typeName) {
                case "SqlList" -> context.deserialize(data, SqlList.class);
                default -> throw new JsonParseException("Unknown SqlValue type: " + typeName);
            };
        }
    }

    private static class SqlLimitTypeAdapter implements JsonSerializer<SqlLimit>, JsonDeserializer<SqlLimit> {
        @Override
        public JsonElement serialize(SqlLimit src, Type typeOfSrc, JsonSerializationContext context) {
            var result = new JsonObject();
            result.addProperty("_type", src.getClass().getSimpleName());
            result.add("data", context.serialize(src, src.getClass()));
            return result;
        }

        @Override
        public SqlLimit deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            var jsonObject = json.getAsJsonObject();
            var typeElement = jsonObject.get("_type");
            if (typeElement == null) {
                throw new JsonParseException("Missing _type field in SqlLimit JSON");
            }
            var typeName = typeElement.getAsString();
            var data = jsonObject.get("data");
            
            return switch (typeName) {
                case "SqlLimited" -> context.deserialize(data, SqlLimited.class);
                case "SqlNotLimited" -> context.deserialize(data, SqlNotLimited.class);
                default -> throw new JsonParseException("Unknown SqlLimit type: " + typeName);
            };
        }
    }
}
