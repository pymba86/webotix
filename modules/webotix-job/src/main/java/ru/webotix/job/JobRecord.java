package ru.webotix.job;

import com.fasterxml.jackson.annotation.JsonProperty;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.validation.constraints.NotNull;

@Entity(name = JobRecord.TABLE_NAME)
final class JobRecord {

    static final String TABLE_NAME = "Job";
    static final String ID_FILED = "id";
    static final String CONTENT_FILED = "content";
    static final String PROCESSED_FILED = "processed";

    @Id
    @Column(name = ID_FILED, nullable = false)
    @NotNull
    @JsonProperty
    private String id;

    @Column(name = CONTENT_FILED, nullable = false)
    @NotNull
    @JsonProperty
    private String content;

    @Column(name = PROCESSED_FILED, nullable = false)
    @NotNull
    @JsonProperty
    private boolean processed;

    public JobRecord() {
    }

    public JobRecord(String id, String content, boolean processed) {
        this.id = id;
        this.content = content;
        this.processed = processed;
    }

    String getContent() {
        return content;
    }

    void setContent(String content) {
        this.content = content;
    }

    boolean isProcessed() {
        return processed;
    }

    void setProcessed(boolean processed) {
        this.processed = processed;
    }
}
