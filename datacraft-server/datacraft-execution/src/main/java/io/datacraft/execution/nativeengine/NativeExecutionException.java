package io.datacraft.execution.nativeengine;

public class NativeExecutionException extends RuntimeException {
    public NativeExecutionException(String message) {
        super(message);
    }

    public NativeExecutionException(String message, Throwable cause) {
        super(message, cause);
    }
}
