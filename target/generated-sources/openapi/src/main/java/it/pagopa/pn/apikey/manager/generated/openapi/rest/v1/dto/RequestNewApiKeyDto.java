package it.pagopa.pn.apikey.manager.generated.openapi.rest.v1.dto;

import java.net.URI;
import java.util.Objects;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonTypeName;
import java.util.ArrayList;
import java.util.List;
import java.time.OffsetDateTime;
import javax.validation.Valid;
import javax.validation.constraints.*;


import java.util.*;
import javax.annotation.Generated;

/**
 * Request per la creazione di una nuova api key
 */

@JsonTypeName("RequestNewApiKey")
@Generated(value = "org.openapitools.codegen.languages.SpringCodegen", date = "2022-10-26T17:37:34.920943600+02:00[Europe/Berlin]")
public class RequestNewApiKeyDto {

  @JsonProperty("name")
  private String name;

  @JsonProperty("groups")
  @Valid
  private List<String> groups = new ArrayList<>();

  public RequestNewApiKeyDto name(String name) {
    this.name = name;
    return this;
  }

  /**
   * nome dell'api key
   * @return name
  */
  @NotNull 
  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public RequestNewApiKeyDto groups(List<String> groups) {
    this.groups = groups;
    return this;
  }

  public RequestNewApiKeyDto addGroupsItem(String groupsItem) {
    if (this.groups == null) {
      this.groups = new ArrayList<>();
    }
    this.groups.add(groupsItem);
    return this;
  }

  /**
   * Gruppi a cui appartiene l'api key (indicare l'id dei gruppi)
   * @return groups
  */
  @NotNull 
  public List<String> getGroups() {
    return groups;
  }

  public void setGroups(List<String> groups) {
    this.groups = groups;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    RequestNewApiKeyDto requestNewApiKey = (RequestNewApiKeyDto) o;
    return Objects.equals(this.name, requestNewApiKey.name) &&
        Objects.equals(this.groups, requestNewApiKey.groups);
  }

  @Override
  public int hashCode() {
    return Objects.hash(name, groups);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class RequestNewApiKeyDto {\n");
    sb.append("    name: ").append(toIndentedString(name)).append("\n");
    sb.append("    groups: ").append(toIndentedString(groups)).append("\n");
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

