package it.pagopa.pn.apikey.manager.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.io.Serializable;
import java.util.Objects;

public class ProblemError implements Serializable {
    private static final long serialVersionUID = 1L;
    @JsonProperty("code")
    private String code;
    @JsonProperty("element")
    private String element;
    @JsonProperty("detail")
    private String detail;

    public ProblemError code(String code) {
        this.code = code;
        return this;
    }

    @NotNull
    public String getCode() {
        return this.code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public ProblemError element(String element) {
        this.element = element;
        return this;
    }

    public String getElement() {
        return this.element;
    }

    public void setElement(String element) {
        this.element = element;
    }

    public ProblemError detail(String detail) {
        this.detail = detail;
        return this;
    }

    @NotNull
    @Size(
            max = 1024
    )
    public String getDetail() {
        return this.detail;
    }

    public void setDetail(String detail) {
        this.detail = detail;
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        } else if (o != null && this.getClass() == o.getClass()) {
            ProblemError problemError = (ProblemError)o;
            return Objects.equals(this.code, problemError.code) && Objects.equals(this.element, problemError.element) && Objects.equals(this.detail, problemError.detail);
        } else {
            return false;
        }
    }

    public int hashCode() {
        return Objects.hash(new Object[]{this.code, this.element, this.detail});
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class ProblemError {\n");
        sb.append("    code: ").append(this.toIndentedString(this.code)).append("\n");
        sb.append("    element: ").append(this.toIndentedString(this.element)).append("\n");
        sb.append("    detail: ").append(this.toIndentedString(this.detail)).append("\n");
        sb.append("}");
        return sb.toString();
    }

    private String toIndentedString(Object o) {
        return o == null ? "null" : o.toString().replace("\n", "\n    ");
    }

    public static ProblemError.ProblemErrorBuilder builder() {
        return new ProblemError.ProblemErrorBuilder();
    }

    public ProblemError.ProblemErrorBuilder toBuilder() {
        return (new ProblemError.ProblemErrorBuilder()).code(this.code).element(this.element).detail(this.detail);
    }

    public ProblemError() {
    }

    public ProblemError(String code, String element, String detail) {
        this.code = code;
        this.element = element;
        this.detail = detail;
    }

    public static class ProblemErrorBuilder {
        private String code;
        private String element;
        private String detail;

        ProblemErrorBuilder() {
        }

        @JsonProperty("code")
        public ProblemError.ProblemErrorBuilder code(String code) {
            this.code = code;
            return this;
        }

        @JsonProperty("element")
        public ProblemError.ProblemErrorBuilder element(String element) {
            this.element = element;
            return this;
        }

        @JsonProperty("detail")
        public ProblemError.ProblemErrorBuilder detail(String detail) {
            this.detail = detail;
            return this;
        }

        public ProblemError build() {
            return new ProblemError(this.code, this.element, this.detail);
        }

        public String toString() {
            return "ProblemError.ProblemErrorBuilder(code=" + this.code + ", element=" + this.element + ", detail=" + this.detail + ")";
        }
    }
}
