package it.pagopa.pn.apikey.manager.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class InternalPaDetailDto {

    @JsonProperty("id")
    private String id;

    @JsonProperty("name")
    private String name;
}
