package com.tradeops.model.response;
import com.tradeops.model.response.ErrorDetail;
import lombok.Builder;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Builder
public record ErrorResponse(
        ErrorDetail error
) {}
