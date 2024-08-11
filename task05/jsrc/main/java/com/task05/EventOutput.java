package com.task05;

import java.util.Map;
import java.util.Objects;
import java.util.UUID;

public class EventOutput {
    private String id;
    private int principalId;
    private String createdAt;
    private Map<String, Object> body;

    public EventOutput(String id, int principalId, String createdAt, Map<String, Object> body) {
        this.id = id;
        this.principalId = principalId;
        this.createdAt = createdAt;
        this.body = body;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public int getPrincipalId() {
        return principalId;
    }

    public void setPrincipalId(int principalId) {
        this.principalId = principalId;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public Map<String, Object> getBody() {
        return body;
    }

    public void setBody(Map<String, Object> body) {
        this.body = body;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        EventOutput that = (EventOutput) o;
        return principalId == that.principalId && Objects.equals(id, that.id) && Objects.equals(createdAt, that.createdAt) && Objects.equals(body, that.body);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, principalId, createdAt, body);
    }

    @Override
    public String toString() {
        return "EventOutput{" +
                "id=" + id +
                ", principalId=" + principalId +
                ", createdAt='" + createdAt + '\'' +
                ", body='" + body + '\'' +
                '}';
    }
}
