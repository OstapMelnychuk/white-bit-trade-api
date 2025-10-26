package org.whitebit.tradeapi.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
@ToString
public class CandleUpdateResponse {

    @JsonProperty("method")
    private String method;

    @JsonProperty("params")
    private List<List<Object>> params;

    @JsonProperty("id")
    private Object id;


    public Candle toCandle() {
        if (params == null || params.isEmpty() || params.getFirst().size() < 8) {
            return null;
        }

        List<Object> data = params.getFirst();
        return new Candle(
                ((Number) data.get(0)).longValue(),
                Double.parseDouble(data.get(1).toString()),
                Double.parseDouble(data.get(2).toString()),
                Double.parseDouble(data.get(3).toString()),
                Double.parseDouble(data.get(4).toString()),
                Double.parseDouble(data.get(5).toString()),
                Double.parseDouble(data.get(6).toString()),
                data.get(7).toString()
        );
    }
}