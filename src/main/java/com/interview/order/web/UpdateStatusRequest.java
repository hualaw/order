package com.interview.order.web;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public class UpdateStatusRequest {

    @NotNull
    @Min(1)
    @Max(3)
    private Integer status;

    public UpdateStatusRequest() {
    }

    public UpdateStatusRequest(Integer status) {
        this.status = status;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }
}

