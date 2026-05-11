package io.github.rspereiratech.plugin.postman.model;

/**
 * Represents a collection-level variable in a Postman Collection.
 *
 * @param key   the variable name
 * @param value the variable value
 * @param type  the variable type (e.g. "string")
 */
public record PostmanVariable(String key, String value, String type) {}
