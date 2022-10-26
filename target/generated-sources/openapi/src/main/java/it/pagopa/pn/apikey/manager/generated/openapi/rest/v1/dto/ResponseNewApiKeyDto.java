package it.pagopa.pn.apikey.manager.generated.openapi.rest.v1.dto;

import java.net.URI;
import java.util.Objects;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonTypeName;
import java.time.OffsetDateTime;
import javax.validation.Valid;
import javax.validation.constraints.*;


import java.util.*;
import javax.annotation.Generated;

/**
 * Response per la creazione di una nuova api key
 */

@JsonTypeName("ResponseNewApiKey")
@Generated(value = "org.openapitools.codegen.languages.SpringCodegen", date = "2022-10-26T15:16:54.071822400+02:00[Europe/Berlin]")
public class ResponseNewApiKeyDto {

  @JsonProperty("id")
  private String id;

  @JsonProperty("api-key")
  private String apiKey;

  public ResponseNewApiKeyDto id(String id) {
    this.id = id;
    return this;
  }

  /**
   * id dell'api key appena generata
   * @return id
  */
  @NotNull 
  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public ResponseNewApiKeyDto apiKey(String apiKey) {
    this.apiKey = apiKey;
    return this;
  }

  /**
   * Valore dell'api key appena generata
   * @return apiKey
  */
  @NotNull 
  public String getApiKey() {
    return apiKey;
  }

  public void setApiKey(String apiKey) {
    this.apiKey = apiKey;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    ResponseNewApiKeyDto responseNewApiKey = (ResponseNewApiKeyDto) o;
    return Objects.equals(this.id, responseNewApiKey.id) &&
        Objects.equals(this.apiKey, responseNewApiKey.apiKey);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, apiKey);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class ResponseNewApiKeyDto {\n");
    sb.append("    id: ").append(toIndentedString(id)).append("\n");
    sb.append("    apiKey: ").append(toIndentedString(apiKey)).append("\n");
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

