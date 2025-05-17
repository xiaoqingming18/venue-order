package com.xcl.venueserver.common.exception;

/**
 * JWT令牌异常
 */
public class JwtException extends RuntimeException {
    
    public JwtException(String message) {
        super(message);
    }
    
    public JwtException(String message, Throwable cause) {
        super(message, cause);
    }
} 