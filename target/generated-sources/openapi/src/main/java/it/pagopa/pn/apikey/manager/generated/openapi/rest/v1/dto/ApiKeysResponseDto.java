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
@Generated(value = "org.openapitools.codegen.languages.SpringCodegen", date = "2022-10-17T17:00:54.448171300+02:00[Europe/Berlin]")
public class ApiKeysResponseDto {

  @JsonProperty("items")
  @Valid
  private List<ApiKeyRowDto> items = new ArrayList<>();

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

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    ApiKeysResponseDto apiKeysResponse = (ApiKeysResponseDto) o;
    return Objects.equals(this.items, apiKeysResponse.items);
  }

  @Override
  public int hashCode() {
    return Objects.hash(items);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class ApiKeysResponseDto {\n");
    sb.append("    items: ").append(toIndentedString(items)).append("\n");
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

