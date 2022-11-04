package it.pagopa.pn.apikey.manager.generated.openapi.rest.v1.dto;

import java.net.URI;
import java.util.Objects;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonTypeName;
import it.pagopa.pn.apikey.manager.generated.openapi.rest.v1.dto.ApiKeyRowDto;
import java.util.ArrayList;
import java.util.List;
import java.time.OffsetDateTime;
import javax.validation.Valid;
import javax.validation.constraints.*;


import java.util.*;
import javax.annotation.Generated;

/**
 * Dto contenente la lista delle api keys associate ad un utente.
 */

@JsonTypeName("ApiKeysResponse")
@Generated(value = "org.openapitools.codegen.languages.SpringCodegen", date = "2022-11-03T15:12:57.419945100+01:00[Europe/Berlin]")
public class ApiKeysResponseDto {

  @JsonProperty("items")
  @Valid
  private List<ApiKeyRowDto> items = new ArrayList<>();

  @JsonProperty("lastKey")
  private String lastKey;

  @JsonProperty("lastUpdate")
  private String lastUpdate;

  public ApiKeysResponseDto items(List<ApiKeyRowDto> items) {
    this.items = items;
    return this;
  }

  public ApiKeysResponseDto addItemsItem(ApiKeyRowDto itemsItem) {
    if (this.items == null) {
      this.items = new ArrayList<>();
    }
    this.items.add(itemsItem);
    return this;
  }

  /**
   * Get items
   * @return items
  */
  @NotNull @Valid 
  public List<ApiKeyRowDto> getItems() {
    return items;
  }

  public void setItems(List<ApiKeyRowDto> items) {
    this.items = items;
  }

  public ApiKeysResponseDto lastKey(String lastKey) {
    this.lastKey = lastKey;
    return this;
  }

  /**
   * Get lastKey
   * @return lastKey
  */
  
  public String getLastKey() {
    return lastKey;
  }

  public void setLastKey(String lastKey) {
    this.lastKey = lastKey;
  }

  public ApiKeysResponseDto lastUpdate(String lastUpdate) {
    this.lastUpdate = lastUpdate;
    return this;
  }

  /**
   * Get lastUpdate
   * @return lastUpdate
  */
  
  public String getLastUpdate() {
    return lastUpdate;
  }

  public void setLastUpdate(String lastUpdate) {
    this.lastUpdate = lastUpdate;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    ApiKeysResponseDto apiKeysResponse = (ApiKeysResponseDto) o;
    return Objects.equals(this.items, apiKeysResponse.items) &&
        Objects.equals(this.lastKey, apiKeysResponse.lastKey) &&
        Objects.equals(this.lastUpdate, apiKeysResponse.lastUpdate);
  }

  @Override
  public int hashCode() {
    return Objects.hash(items, lastKey, lastUpdate);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class ApiKeysResponseDto {\n");
    sb.append("    items: ").append(toIndentedString(items)).append("\n");
    sb.append("    lastKey: ").append(toIndentedString(lastKey)).append("\n");
    sb.append("    lastUpdate: ").append(toIndentedString(lastUpdate)).append("\n");
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

