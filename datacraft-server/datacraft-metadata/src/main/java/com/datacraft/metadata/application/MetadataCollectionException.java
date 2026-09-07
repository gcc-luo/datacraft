package com.datacraft.metadata.application;

public class MetadataCollectionException extends RuntimeException {
    public MetadataCollectionException() {
        super("metadata collection failed");
    }

    public MetadataCollectionException(Throwable cause) {
        super("metadata collection failed", cause);
    }
}
