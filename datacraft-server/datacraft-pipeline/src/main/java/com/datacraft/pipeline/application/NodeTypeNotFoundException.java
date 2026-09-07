package com.datacraft.pipeline.application;

public class NodeTypeNotFoundException extends RuntimeException {
    public NodeTypeNotFoundException(String type) {
        super("节点类型不存在: " + type);
    }
}
