package com.example.cube.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;

public class InitiateOnboardingRequest {
    @JsonProperty("return_url")
    private String returnUrl;
    
    @JsonProperty("refresh_url")
    private String refreshUrl;
    
    public String getReturnUrl() {
        return returnUrl;
    }
    
    public void setReturnUrl(String returnUrl) {
        this.returnUrl = returnUrl;
    }
    
    public String getRefreshUrl() {
        return refreshUrl;
    }
    
    public void setRefreshUrl(String refreshUrl) {
        this.refreshUrl = refreshUrl;
    }
}

