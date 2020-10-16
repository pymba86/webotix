package ru.webotix.script;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.hibernate.validator.constraints.Length;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import java.io.Serializable;

@Entity(name = ScriptParameter.TABLE_NAME)
class ScriptParameter {

    static final String TABLE_NAME = "ScriptParameter";
    static final String SCRIPT_ID_FIELD = "scriptId";
    static final String NAME_FIELD = "name";
    static final String DESCRIPTION_FIELD = "description";
    static final String DEFAULT_VALUE_FIELD = "descriptionValue";
    static final String MANDATORY_FIELD = "mandatory";

    @Embeddable
    public static class Id implements Serializable {

        @Length(min = 1, max = 45)
        @Column(name = SCRIPT_ID_FIELD, nullable = false, updatable = false, insertable = true)
        private String scriptId;

        @Length(min = 1, max = 255)
        @Column(name = NAME_FIELD, nullable = false, updatable = false, insertable = true)
        private String name;

        public Id() {}

        public Id(String scriptId, String name) {
            super();
            this.scriptId = scriptId;
            this.name = name;
        }

        @Override
        public String toString() {
            return "ScriptParameter.Id [scriptId=" + scriptId + ", name=" + name + "]";
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + ((name == null) ? 0 : name.hashCode());
            result = prime * result + ((scriptId == null) ? 0 : scriptId.hashCode());
            return result;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) return true;
            if (obj == null) return false;
            if (getClass() != obj.getClass()) return false;
            Id other = (Id) obj;
            if (name == null) {
                if (other.name != null) return false;
            } else if (!name.equals(other.name)) return false;
            if (scriptId == null) {
                return other.scriptId == null;
            } else return scriptId.equals(other.scriptId);
        }
    }

    @EmbeddedId
    private final Id id = new Id();

    @JsonProperty
    @Length(min = 1, max = 255)
    @Column(name = DESCRIPTION_FIELD, nullable = false)
    private String description;

    @JsonProperty(value = "default")
    @Length(max = 255)
    @Column(name = DEFAULT_VALUE_FIELD)
    private String defaultValue;

    @JsonProperty
    @Column(name = MANDATORY_FIELD, nullable = false)
    private boolean mandatory;

    @JsonProperty
    public String scriptId() {
        return id.scriptId;
    }

    @JsonProperty
    public String name() {
        return id.name;
    }

    public String description() {
        return description;
    }

    public String defaultValue() {
        return defaultValue;
    }

    public boolean mandatory() {
        return mandatory;
    }

    @JsonProperty
    void setScriptId(String scriptId) {
        this.id.scriptId = scriptId;
    }

    @JsonProperty
    void setName(String name) {
        this.id.name = name;
    }

    void setDescription(String description) {
        this.description = description;
    }

    void setDefaultValue(String defaultValue) {
        this.defaultValue = defaultValue;
    }

    void setMandatory(boolean mandatory) {
        this.mandatory = mandatory;
    }

    void setParent(Script script) {
        this.id.scriptId = script.id();
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((defaultValue == null) ? 0 : defaultValue.hashCode());
        result = prime * result + ((description == null) ? 0 : description.hashCode());
        result = prime * result + id.hashCode();
        result = prime * result + (mandatory ? 1231 : 1237);
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null) return false;
        if (getClass() != obj.getClass()) return false;
        ScriptParameter other = (ScriptParameter) obj;
        if (defaultValue == null) {
            if (other.defaultValue != null) return false;
        } else if (!defaultValue.equals(other.defaultValue)) return false;
        if (description == null) {
            if (other.description != null) return false;
        } else if (!description.equals(other.description)) return false;
        if (!id.equals(other.id)) return false;
        return mandatory == other.mandatory;
    }

    @Override
    public String toString() {
        return "ScriptParameter [id="
                + id
                + ", description="
                + description
                + ", defaultValue="
                + defaultValue
                + ", mandatory="
                + mandatory
                + "]";
    }

}
