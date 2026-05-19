package com.tfind.toilet.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.sleepycat.je.Cursor;
import com.sleepycat.je.Database;
import com.sleepycat.je.DatabaseEntry;
import com.sleepycat.je.Environment;
import com.sleepycat.je.LockMode;
import com.sleepycat.je.OperationStatus;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class BerkeleyDbService {

    private final Environment environment;
    private final ObjectMapper objectMapper;
    private final String KEY_SEPARATOR = "::";

    public BerkeleyDbService(Environment environment) {
        this.environment = environment;
        this.objectMapper = new ObjectMapper();
    }

    public String save(String collection, Object entity) {
        try {
            String json = objectMapper.writeValueAsString(entity);
            Database db = getOrCreateDatabase(collection);
            String id = UUID.randomUUID().toString();
            String key = collection + KEY_SEPARATOR + id;
            DatabaseEntry keyEntry = new DatabaseEntry(key.getBytes("UTF-8"));
            DatabaseEntry valueEntry = new DatabaseEntry(json.getBytes("UTF-8"));
            OperationStatus status = db.put(null, keyEntry, valueEntry);
            if (status != OperationStatus.SUCCESS) {
                throw new RuntimeException("Failed to save entity to collection: " + collection);
            }
            return id;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public String getById(String collection, String id) {
        try {
            Database db = getOrCreateDatabase(collection);
            String key = collection + KEY_SEPARATOR + id;
            DatabaseEntry keyEntry = new DatabaseEntry(key.getBytes("UTF-8"));
            DatabaseEntry valueEntry = new DatabaseEntry();
            OperationStatus status = db.get(null, keyEntry, valueEntry, LockMode.DEFAULT);
            if (status == OperationStatus.SUCCESS) {
                return new String(valueEntry.getData(), "UTF-8");
            }
            return null;
        } catch (Exception e) {
            return null;
        }
    }

    public List<String> query(String collection, String queryStr) {
        try {
            List<String> results = new ArrayList<>();
            Database db = getOrCreateDatabase(collection);
            Cursor cursor = db.openCursor(null, null);
            DatabaseEntry keyEntry = new DatabaseEntry();
            DatabaseEntry valueEntry = new DatabaseEntry();
            String collectionPrefix = collection + KEY_SEPARATOR;

            while (cursor.getNext(keyEntry, valueEntry, LockMode.DEFAULT) == OperationStatus.SUCCESS) {
                String key = new String(keyEntry.getData(), "UTF-8");
                if (key.startsWith(collectionPrefix)) {
                    String json = new String(valueEntry.getData(), "UTF-8");
                    if (matchesQuery(json, queryStr)) {
                        results.add(json);
                    }
                }
            }
            cursor.close();
            return results;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void update(String collection, String id, Object entity) {
        try {
            String json = objectMapper.writeValueAsString(entity);
            Database db = getOrCreateDatabase(collection);
            String key = collection + KEY_SEPARATOR + id;
            DatabaseEntry keyEntry = new DatabaseEntry(key.getBytes("UTF-8"));
            DatabaseEntry valueEntry = new DatabaseEntry(json.getBytes("UTF-8"));
            OperationStatus status = db.put(null, keyEntry, valueEntry);
            if (status != OperationStatus.SUCCESS) {
                throw new RuntimeException("Failed to update entity in collection: " + collection);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void delete(String collection, String id) {
        try {
            Database db = getOrCreateDatabase(collection);
            String key = collection + KEY_SEPARATOR + id;
            DatabaseEntry keyEntry = new DatabaseEntry(key.getBytes("UTF-8"));
            db.delete(null, keyEntry);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public long count(String collection, String queryStr) {
        try {
            long count = 0;
            Database db = getOrCreateDatabase(collection);
            Cursor cursor = db.openCursor(null, null);
            DatabaseEntry keyEntry = new DatabaseEntry();
            DatabaseEntry valueEntry = new DatabaseEntry();
            String collectionPrefix = collection + KEY_SEPARATOR;

            while (cursor.getNext(keyEntry, valueEntry, LockMode.DEFAULT) == OperationStatus.SUCCESS) {
                String key = new String(keyEntry.getData(), "UTF-8");
                if (key.startsWith(collectionPrefix)) {
                    String json = new String(valueEntry.getData(), "UTF-8");
                    if (matchesQuery(json, queryStr)) {
                        count++;
                    }
                }
            }
            cursor.close();
            return count;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public <T> T getById(String collection, String id, Class<T> clazz) {
        String json = getById(collection, id);
        if (json == null) {
            return null;
        }
        try {
            return objectMapper.readValue(json, clazz);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public <T> List<T> query(String collection, String queryStr, Class<T> clazz) {
        List<String> jsonList = query(collection, queryStr);
        List<T> results = new ArrayList<>();
        for (String json : jsonList) {
            try {
                results.add(objectMapper.readValue(json, clazz));
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        return results;
    }

    public Environment getEnvironment() {
        return environment;
    }

    private Database getOrCreateDatabase(String collection) {
        String dbName = "berkeleydb_" + collection;
        com.sleepycat.je.DatabaseConfig dbConfig = new com.sleepycat.je.DatabaseConfig();
        dbConfig.setAllowCreate(true);
        dbConfig.setTransactional(true);
        return environment.openDatabase(null, dbName, dbConfig);
    }

    private boolean matchesQuery(String json, String queryStr) {
        try {
            JsonNode node = objectMapper.readTree(json);
            String[] conditions = queryStr.split("\\s+and\\s+");
            for (String condition : conditions) {
                condition = condition.trim();
                if (!evaluateCondition(node, condition)) {
                    return false;
                }
            }
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private boolean evaluateCondition(JsonNode node, String condition) {
        condition = condition.startsWith("/") ? condition.substring(1) : condition;

        Pattern eqPattern = Pattern.compile("^(.+?)\\s*=\\s*:?(.+)$");
        Pattern eqFalsePattern = Pattern.compile("^(.+?)\\s*=\\s*false$");
        Pattern eqTruePattern = Pattern.compile("^(.+?)\\s*=\\s*true$");
        Pattern nePattern = Pattern.compile("^(.+?)\\s*!=\\s*:?(.+)$");

        Matcher eqFalseMatcher = eqFalsePattern.matcher(condition);
        if (eqFalseMatcher.matches()) {
            String field = eqFalseMatcher.group(1).trim();
            JsonNode value = getNodeValue(node, field);
            return value != null && value.asBoolean(false) == false;
        }

        Matcher eqTrueMatcher = eqTruePattern.matcher(condition);
        if (eqTrueMatcher.matches()) {
            String field = eqTrueMatcher.group(1).trim();
            JsonNode value = getNodeValue(node, field);
            return value != null && value.asBoolean(false) == true;
        }

        Matcher eqMatcher = eqPattern.matcher(condition);
        if (eqMatcher.matches()) {
            String field = eqMatcher.group(1).trim();
            String paramRef = eqMatcher.group(2).trim();

            if (paramRef.startsWith(":")) {
                int paramIndex = Integer.parseInt(paramRef.substring(1)) - 1;
                JsonNode value = getNodeValue(node, field);
                if (value == null) return false;

                if (value.isTextual()) {
                    Object paramValue = getParamValue(paramIndex);
                    return paramValue != null && value.asText().equals(paramValue.toString());
                } else if (value.isBoolean()) {
                    Boolean paramValue = getParamValue(paramIndex, Boolean.class);
                    return paramValue != null && value.asBoolean() == paramValue;
                } else if (value.isNumber()) {
                    Number paramValue = getParamValue(paramIndex, Number.class);
                    return paramValue != null && value.asDouble() == paramValue.doubleValue();
                }
                return false;
            } else {
                JsonNode value = getNodeValue(node, field);
                if (value == null) return false;

                if (value.isTextual()) {
                    return value.asText().equals(paramRef);
                } else if (value.isBoolean()) {
                    return value.asBoolean() == Boolean.parseBoolean(paramRef);
                } else if (value.isNumber()) {
                    try {
                        return value.asDouble() == Double.parseDouble(paramRef);
                    } catch (NumberFormatException e) {
                        return false;
                    }
                }
                return false;
            }
        }

        Matcher neMatcher = nePattern.matcher(condition);
        if (neMatcher.matches()) {
            String field = neMatcher.group(1).trim();
            String paramRef = neMatcher.group(2).trim();

            if (paramRef.startsWith(":")) {
                int paramIndex = Integer.parseInt(paramRef.substring(1)) - 1;
                JsonNode value = getNodeValue(node, field);
                if (value == null) return true;

                if (value.isTextual()) {
                    Object paramValue = getParamValue(paramIndex);
                    return paramValue == null || !value.asText().equals(paramValue.toString());
                } else if (value.isBoolean()) {
                    Boolean paramValue = getParamValue(paramIndex, Boolean.class);
                    return paramValue == null || value.asBoolean() != paramValue;
                } else if (value.isNumber()) {
                    Number paramValue = getParamValue(paramIndex, Number.class);
                    return paramValue == null || value.asDouble() != paramValue.doubleValue();
                }
                return true;
            } else {
                JsonNode value = getNodeValue(node, field);
                if (value == null) return true;

                if (value.isTextual()) {
                    return !value.asText().equals(paramRef);
                } else if (value.isBoolean()) {
                    return !value.asBoolean() == Boolean.parseBoolean(paramRef);
                } else if (value.isNumber()) {
                    try {
                        return value.asDouble() != Double.parseDouble(paramRef);
                    } catch (NumberFormatException e) {
                        return true;
                    }
                }
                return true;
            }
        }

        return true;
    }

    private JsonNode getNodeValue(JsonNode node, String field) {
        String[] parts = field.split("/");
        JsonNode current = node;
        for (String part : parts) {
            if (part.isEmpty()) continue;
            if (current == null || current.isMissingNode()) {
                return null;
            }
            current = current.get(part);
        }
        return current;
    }

    private static ThreadLocal<List<Object>> queryParams = new ThreadLocal<>();

    public static void setQueryParams(List<Object> params) {
        queryParams.set(params);
    }

    public static Object getParamValue(int index) {
        List<Object> params = queryParams.get();
        if (params == null || index >= params.size()) {
            return null;
        }
        return params.get(index);
    }

    @SuppressWarnings("unchecked")
    public static <T> T getParamValue(int index, Class<T> type) {
        Object value = getParamValue(index);
        if (value == null) {
            return null;
        }
        return (T) value;
    }
}
