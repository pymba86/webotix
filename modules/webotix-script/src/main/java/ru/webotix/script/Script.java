package ru.webotix.script;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.hibernate.validator.constraints.Length;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Transient;
import java.util.ArrayList;
import java.util.List;

@Entity(name = Script.TABLE_NAME)
 class Script {

    static final String TABLE_NAME = "Script";
    static final String ID_FIELD = "id";
    static final String NAME_FIELD = "name";
    static final String SCRIPT_FIELD = "script";
    static final String SCRIPT_HASH_FIELD = "scriptHash";

    @Id
    @JsonProperty
    @Length(min = 1, max = 45)
    @Column(name = ID_FIELD, nullable = false, updatable = false)
    private String id;

    @JsonProperty
    @Length(min = 1, max = 255)
    @Column(name = NAME_FIELD, nullable = false)
    private String name;

    @JsonProperty
    @Length
    @Column(name = SCRIPT_FIELD, nullable = false)
    private String script;

    @JsonProperty
    @Length(min = 1, max = 255)
    @Column(name = SCRIPT_HASH_FIELD, nullable = false)
    private String scriptHash;

    @Transient
    private List<ScriptParameter> params = new ArrayList<>();

    public String id() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String name() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String script() {
        return script;
    }

    public void setScript(String script) {
        this.script = script;
    }

    public String scriptHash() {
        return scriptHash;
    }

    public void setScriptHash(String scriptHash) {
        this.scriptHash = scriptHash;
    }

    @JsonProperty
    public List<ScriptParameter> parameters() {
        return params;
    }

    @JsonProperty
    public void setParams(List<ScriptParameter> params) {
        this.params = params;
    }

    @Override
    public String toString() {
        return "Script{"
                + "id="
                + id
                + ", "
                + "name="
                + name
                + ", "
                + "script="
                + script
                + ", "
                + "scriptHash="
                + scriptHash
                + ", "
                + "parameters="
                + params
                + "}";
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((id == null) ? 0 : id.hashCode());
        result = prime * result + ((name == null) ? 0 : name.hashCode());
        result = prime * result + ((params == null) ? 0 : params.hashCode());
        result = prime * result + ((script == null) ? 0 : script.hashCode());
        result = prime * result + ((scriptHash == null) ? 0 : scriptHash.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null) return false;
        if (getClass() != obj.getClass()) return false;
        Script other = (Script) obj;
        if (id == null) {
            if (other.id != null) return false;
        } else if (!id.equals(other.id)) return false;
        if (name == null) {
            if (other.name != null) return false;
        } else if (!name.equals(other.name)) return false;
        if (params == null) {
            if (other.params != null) return false;
        } else if (!params.equals(other.params)) return false;
        if (script == null) {
            if (other.script != null) return false;
        } else if (!script.equals(other.script)) return false;
        if (scriptHash == null) {
            return other.scriptHash == null;
        } else return scriptHash.equals(other.scriptHash);
    }
}
