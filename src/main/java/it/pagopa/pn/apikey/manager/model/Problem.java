package it.pagopa.pn.apikey.manager.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.format.annotation.DateTimeFormat;

import javax.validation.Valid;
import javax.validation.constraints.*;
import java.io.Serializable;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Problem implements Serializable {
    private static final long serialVersionUID = 1L;
    @JsonProperty("status")
    private Integer status;
    @JsonProperty("title")
    private String title;
    @JsonProperty("detail")
    private String detail;
    @JsonProperty("traceId")
    private String traceId;
    @JsonProperty("timestamp")
    @DateTimeFormat(
            iso = DateTimeFormat.ISO.DATE_TIME
    )
    private OffsetDateTime timestamp;
    @JsonProperty("errors")
    @Valid
    private List<ProblemError> errors = new ArrayList();

    public Problem status(Integer status) {
        this.status = status;
        return this;
    }

    @NotNull
    @Min(100L)
    @Max(600L)
    public Integer getStatus() {
        return this.status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public Problem title(String title) {
        this.title = title;
        return this;
    }

    @NotNull
    @Pattern(
            regexp = "^[ -~]{0,64}$"
    )
    @Size(
            max = 64
    )
    public String getTitle() {
        return this.title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Problem detail(String detail) {
        this.detail = detail;
        return this;
    }

    @NotNull
    @Pattern(
            regexp = "^.{0,4096}$"
    )
    @Size(
            max = 4096
    )
    public String getDetail() {
        return this.detail;
    }

    public void setDetail(String detail) {
        this.detail = detail;
    }

    public Problem traceId(String traceId) {
        this.traceId = traceId;
        return this;
    }

    public String getTraceId() {
        return this.traceId;
    }

    public void setTraceId(String traceId) {
        this.traceId = traceId;
    }

    public Problem timestamp(OffsetDateTime timestamp) {
        this.timestamp = timestamp;
        return this;
    }

    @NotNull
    @Valid
    public OffsetDateTime getTimestamp() {
        return this.timestamp;
    }

    public void setTimestamp(OffsetDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public Problem errors(List<ProblemError> errors) {
        this.errors = errors;
        return this;
    }

    public Problem addErrorsItem(ProblemError errorsItem) {
        if (this.errors == null) {
            this.errors = new ArrayList();
        }

        this.errors.add(errorsItem);
        return this;
    }

    @NotNull
    @Valid
    @Size(
            min = 1
    )
    public List<ProblemError> getErrors() {
        return this.errors;
    }

    public void setErrors(List<ProblemError> errors) {
        this.errors = errors;
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        } else if (o != null && this.getClass() == o.getClass()) {
            Problem problem = (Problem) o;
            return Objects.equals(this.status, problem.status) && Objects.equals(this.title, problem.title) && Objects.equals(this.detail, problem.detail) && Objects.equals(this.traceId, problem.traceId) && Objects.equals(this.timestamp, problem.timestamp) && Objects.equals(this.errors, problem.errors);
        } else {
            return false;
        }
    }

    public int hashCode() {
        return Objects.hash(new Object[]{this.status, this.title, this.detail, this.traceId, this.timestamp, this.errors});
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class Problem {\n");
        sb.append("    status: ").append(this.toIndentedString(this.status)).append("\n");
        sb.append("    title: ").append(this.toIndentedString(this.title)).append("\n");
        sb.append("    detail: ").append(this.toIndentedString(this.detail)).append("\n");
        sb.append("    traceId: ").append(this.toIndentedString(this.traceId)).append("\n");
        sb.append("    timestamp: ").append(this.toIndentedString(this.timestamp)).append("\n");
        sb.append("    errors: ").append(this.toIndentedString(this.errors)).append("\n");
        sb.append("}");
        return sb.toString();
    }

    private String toIndentedString(Object o) {
        return o == null ? "null" : o.toString().replace("\n", "\n    ");
    }

    public static Problem.ProblemBuilder builder() {
        return new Problem.ProblemBuilder();
    }

    public Problem.ProblemBuilder toBuilder() {
        return (new Problem.ProblemBuilder()).status(this.status).title(this.title).detail(this.detail).traceId(this.traceId).timestamp(this.timestamp).errors(this.errors);
    }

    public Problem() {
    }

    public Problem(Integer status, String title, String detail, String traceId, OffsetDateTime timestamp, List<ProblemError> errors) {
        this.status = status;
        this.title = title;
        this.detail = detail;
        this.traceId = traceId;
        this.timestamp = timestamp;
        this.errors = errors;
    }

    public static class ProblemBuilder {
        private Integer status;
        private String title;
        private String detail;
        private String traceId;
        private OffsetDateTime timestamp;
        private List<ProblemError> errors;

        ProblemBuilder() {
        }

        @JsonProperty("status")
        public Problem.ProblemBuilder status(Integer status) {
            this.status = status;
            return this;
        }

        @JsonProperty("title")
        public Problem.ProblemBuilder title(String title) {
            this.title = title;
            return this;
        }

        @JsonProperty("detail")
        public Problem.ProblemBuilder detail(String detail) {
            this.detail = detail;
            return this;
        }

        @JsonProperty("traceId")
        public Problem.ProblemBuilder traceId(String traceId) {
            this.traceId = traceId;
            return this;
        }

        @JsonProperty("timestamp")
        public Problem.ProblemBuilder timestamp(OffsetDateTime timestamp) {
            this.timestamp = timestamp;
            return this;
        }

        @JsonProperty("errors")
        public Problem.ProblemBuilder errors(List<ProblemError> errors) {
            this.errors = errors;
            return this;
        }

        public Problem build() {
            return new Problem(this.status, this.title, this.detail, this.traceId, this.timestamp, this.errors);
        }

        public String toString() {
            return "Problem.ProblemBuilder(status=" + this.status + ", title=" + this.title + ", detail=" + this.detail + ", traceId=" + this.traceId + ", timestamp=" + this.timestamp + ", errors=" + this.errors + ")";
        }
    }
}
