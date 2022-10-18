package it.pagopa.pn.apikey.manager.generated.openapi.rest.v1.dto;

import java.net.URI;
import java.util.Objects;
import com.fasterxml.jackson.annotation.JsonValue;
import java.time.OffsetDateTime;
import javax.validation.Valid;
import javax.validation.constraints.*;


import java.util.*;
import javax.annotation.Generated;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * Stato dell'api key
 */

@Generated(value = "org.openapitools.codegen.languages.SpringCodegen", date = "2022-10-17T17:00:54.448171300+02:00[Europe/Berlin]")
public enum ApiKeyStatusDto {
  
  CREATED("CREATED"),
  
  ENABLED("ENABLED"),
  
  BLOCKED("BLOCKED"),
  
  ROTATED("ROTATED");

  private String value;

  ApiKeyStatusDto(String value) {
    this.value = value;
  }

  @JsonValue
  public String getValue() {
    return value;
  }

  @Override
  public String toString() {
    return String.valueOf(value);
  }

  @JsonCreator
  public static ApiKeyStatusDto fromValue(String value) {
    for (ApiKeyStatusDto b : ApiKeyStatusDto.values()) {
      if (b.value.equals(value)) {
        return b;
      }
    }
    throw new IllegalArgumentException("Unexpected value '" + value + "'");
  }
}

