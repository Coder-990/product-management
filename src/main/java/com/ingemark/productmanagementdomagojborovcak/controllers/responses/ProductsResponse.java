package com.ingemark.productmanagementdomagojborovcak.controllers.responses;

import lombok.Builder;

import java.util.List;
@Builder(setterPrefix = "of")
public record ProductsResponse(List<ProductResponse> products) {
}
