package com.csi.common.domain;

/**
 * Exception metier portee par les cas d utilisation lorsqu une regle du domaine est violee.
 */
public class BusinessException extends RuntimeException {
    private final String code;

    public BusinessException(String code, String message) {
        super(message);
        this.code = code;
    }

    public String code() {
        return code;
    }
}
