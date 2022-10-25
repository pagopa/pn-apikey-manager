package it.pagopa.pn.apikey.manager.generated.openapi.rest.v1.dto;

import java.net.URI;
import java.util.Objects;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonTypeName;
import it.pagopa.pn.apikey.manager.generated.openapi.rest.v1.dto.ApiKeyStatusDto;
import it.pagopa.pn.apikey.manager.generated.openapi.rest.v1.dto.ApiKeyStatusHistoryDto;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import org.springframework.format.annotation.DateTimeFormat;
import java.time.OffsetDateTime;
import javax.validation.Valid;
import javax.validation.constraints.*;


import java.util.*;
import javax.annotation.Generated;

/**
 * ApiKeyRowDto
 */

@JsonTypeName("ApiKeyRow")
@Generated(value = "org.openapitools.codegen.languages.SpringCodegen", date = "2022-10-17T17:00:54.448171300+02:00[Europe/Berlin]")
public class ApiKeyRowDto {

  @JsonProperty("id")
  private String id;

  @JsonProperty("name")
  private String name;

  @JsonProperty("value")
  private String value;

  @JsonProperty("lastUpdate")
  @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
  private Date lastUpdate;

  @JsonProperty("groups")
  @Valid
  private List<String> groups = null;

  @JsonProperty("status")
  private ApiKeyStatusDto status;

  @JsonProperty("statusHistory")
  @Valid
  private List<ApiKeyStatusHistoryDto> statusHistory = null;

  public ApiKeyRowDto id(String id) {
    this.id = id;
    return this;
  }

  /**
   * Id dell'api key
   * @return id
  */
  
  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public ApiKeyRowDto name(String name) {
    this.name = name;
    return this;
  }

  /**
   * Nome dell'api key
   * @return name
  */
  
  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public ApiKeyRowDto value(String value) {
    this.value = value;
    return this;
  }

  /**
   * Valore dell'api key
   * @return value
  */
  
  public String getValue() {
    return value;
  }

  public void setValue(String value) {
    this.value = value;
  }

  public ApiKeyRowDto lastUpdate(Date lastUpdate) {
    this.lastUpdate = lastUpdate;
    return this;
  }

  /**
   * Data ultima modifica
   * @return lastUpdate
  */
  @Valid 
  public Date getLastUpdate() {
    return lastUpdate;
  }

  public void setLastUpdate(Date lastUpdate) {
    this.lastUpdate = lastUpdate;
  }

  public ApiKeyRowDto groups(List<String> groups) {
    this.groups = groups;
    return this;
  }

  public ApiKeyRowDto addGroupsItem(String groupsItem) {
    if (this.groups == null) {
      this.groups = new ArrayList<>();
    }
    this.groups.add(groupsItem);
    return this;
  }

  /**
   * Gruppi a cui appartiene l'api key
   * @return groups
  */
  
  public List<String> getGroups() {
    return groups;
  }

  public void setGroups(List<String> groups) {
    this.groups = groups;
  }

  public ApiKeyRowDto status(ApiKeyStatusDto status) {
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

  public ApiKeyRowDto statusHistory(List<ApiKeyStatusHistoryDto> statusHistory) {
    this.statusHistory = statusHistory;
    return this;
  }

  public ApiKeyRowDto addStatusHistoryItem(ApiKeyStatusHistoryDto statusHistoryItem) {
    if (this.statusHistory == null) {
      this.statusHistory = new ArrayList<>();
    }
    this.statusHistory.add(statusHistoryItem);
    return this;
  }

  /**
   * Storico degli stati dell'api key
   * @return statusHistory
  */
  @Valid 
  public List<ApiKeyStatusHistoryDto> getStatusHistory() {
    return statusHistory;
  }

  public void setStatusHistory(List<ApiKeyStatusHistoryDto> statusHistory) {
    this.statusHistory = statusHistory;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    ApiKeyRowDto apiKeyRow = (ApiKeyRowDto) o;
    return Objects.equals(this.id, apiKeyRow.id) &&
        Objects.equals(this.name, apiKeyRow.name) &&
        Objects.equals(this.value, apiKeyRow.value) &&
        Objects.equals(this.lastUpdate, apiKeyRow.lastUpdate) &&
        Objects.equals(this.groups, apiKeyRow.groups) &&
        Objects.equals(this.status, apiKeyRow.status) &&
        Objects.equals(this.statusHistory, apiKeyRow.statusHistory);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, name, value, lastUpdate, groups, status, statusHistory);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class ApiKeyRowDto {\n");
    sb.append("    id: ").append(toIndentedString(id)).append("\n");
    sb.append("    name: ").append(toIndentedString(name)).append("\n");
    sb.append("    value: ").append(toIndentedString(value)).append("\n");
    sb.append("    lastUpdate: ").append(toIndentedString(lastUpdate)).append("\n");
    sb.append("    groups: ").append(toIndentedString(groups)).append("\n");
    sb.append("    status: ").append(toIndentedString(status)).append("\n");
    sb.append("    statusHistory: ").append(toIndentedString(statusHistory)).append("\n");
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

