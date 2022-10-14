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
 * Gets or Sets CxTypeAuthFleet
 */

@Generated(value = "org.openapitools.codegen.languages.SpringCodegen", date = "2022-10-14T12:19:13.902105400+02:00[Europe/Berlin]")
public enum CxTypeAuthFleetDto {
  
  PA("PA"),
  
  PF("PF"),
  
  PG("PG");

  private String value;

  CxTypeAuthFleetDto(String value) {
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
  public static CxTypeAuthFleetDto fromValue(String value) {
    for (CxTypeAuthFleetDto b : CxTypeAuthFleetDto.values()) {
      if (b.value.equals(value)) {
        return b;
      }
    }
    throw new IllegalArgumentException("Unexpected value '" + value + "'");
  }
}

