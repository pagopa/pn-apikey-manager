package it.pagopa.pn.apikey.manager.generated.openapi.rest.v1.dto;

import java.net.URI;
import java.util.Objects;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonTypeName;
import it.pagopa.pn.apikey.manager.generated.openapi.rest.v1.dto.ApiKeyStatusDto;
import java.util.Date;
import org.springframework.format.annotation.DateTimeFormat;
import java.time.OffsetDateTime;
import javax.validation.Valid;
import javax.validation.constraints.*;


import java.util.*;
import javax.annotation.Generated;

/**
 * ApiKeyStatusHistoryDto
 */

@JsonTypeName("ApiKeyStatusHistory")
@Generated(value = "org.openapitools.codegen.languages.SpringCodegen", date = "2022-10-14T12:19:13.902105400+02:00[Europe/Berlin]")
public class ApiKeyStatusHistoryDto {

  @JsonProperty("status")
  private ApiKeyStatusDto status;

  @JsonProperty("date")
  @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
  private Date date;

  @JsonProperty("changedByDenomination")
  private String changedByDenomination;

  public ApiKeyStatusHistoryDto status(ApiKeyStatusDto status) {
    this.status = status;
    return this;
  }

  /**
   * Get status
   * @return status
  */
  @Valid 
  public ApiKeyStatusDto getStatus() {
    return status;
  }

  public void setStatus(ApiKeyStatusDto status) {
    this.status = status;
  }

  public ApiKeyStatusHistoryDto date(Date date) {
    this.date = date;
    return this;
  }

  /**
   * data a cui corrisponde il cambio di stato
   * @return date
  */
  @Valid 
  public Date getDate() {
    return date;
  }

  public void setDate(Date date) {
    this.date = date;
  }

  public ApiKeyStatusHistoryDto changedByDenomination(String changedByDenomination) {
    this.changedByDenomination = changedByDenomination;
    return this;
  }

  /**
   * nome dell'utente che ha effettuato il cambio di stato
   * @return changedByDenomination
  */
  
  public String getChangedByDenomination() {
    return changedByDenomination;
  }

  public void setChangedByDenomination(String changedByDenomination) {
    this.changedByDenomination = changedByDenomination;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    ApiKeyStatusHistoryDto apiKeyStatusHistory = (ApiKeyStatusHistoryDto) o;
    return Objects.equals(this.status, apiKeyStatusHistory.status) &&
        Objects.equals(this.date, apiKeyStatusHistory.date) &&
        Objects.equals(this.changedByDenomination, apiKeyStatusHistory.changedByDenomination);
  }

  @Override
  public int hashCode() {
    return Objects.hash(status, date, changedByDenomination);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class ApiKeyStatusHistoryDto {\n");
    sb.append("    status: ").append(toIndentedString(status)).append("\n");
    sb.append("    date: ").append(toIndentedString(date)).append("\n");
    sb.append("    changedByDenomination: ").append(toIndentedString(changedByDenomination)).append("\n");
    sb.append("}");
    return sb.toString();
  }

  /**
   * Convert the given object to string with each line indented by 4 spaces
   * (except the first line).
   */
  private String toIndentedString(Object o) {
    if (o == null) {
      return "null";
    }
    return o.toString().replace("\n", "\n    ");
  }
}

