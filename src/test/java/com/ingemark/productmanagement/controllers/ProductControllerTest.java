package com.ingemark.productmanagement.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.ingemark.productmanagement.TestBase;
import com.ingemark.productmanagement.controllers.responses.ProductResponse;
import com.ingemark.productmanagement.controllers.responses.ProductsResponse;
import com.ingemark.productmanagement.enums.Action;
import com.ingemark.productmanagement.fixtures.ProductFixtures;
import com.ingemark.productmanagement.helpers.KafkaTestListener;
import com.ingemark.productmanagement.mappers.ProductMapper;
import com.ingemark.productmanagement.messaging.outbound.CreatedProductEvent;
import com.ingemark.productmanagement.messaging.outbound.DeletedProductEvent;
import com.ingemark.productmanagement.messaging.outbound.UpdatedProductEvent;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.Objects;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@AutoConfigureMockMvc
class ProductControllerTest extends TestBase {

    @Autowired
    private ProductMapper productMapper;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private KafkaTestListener kafkaTestListener;

    @BeforeEach
    void clean() throws Exception {
        super.setUp();
        kafkaTestListener.setLatestEvent(null);
    }


    @Test
    @DisplayName("""
            Given multiple products exists in list,
            when fetching all products,
            then is expected to return products of list size
            """)
    void shouldReturnAllProducts() throws Exception {
        // given
        stubHnbCurrencyExchangeApi();
        productRepository.saveAll(ProductFixtures.getProductList());
        // when & then
        mockMvc.perform(get("/products"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.products", hasSize(5)))
                .andReturn();
    }

    @Test
    @DisplayName("""
            Given empty list of products exists in list,
            when fetching all products,
            then empty array should be returned
            """)
    void shouldReturnEmptyProductList() throws Exception {
        mockMvc.perform(get("/products"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.products", hasSize(0)))
                .andReturn();
    }

    @Test
    @DisplayName("""
            Given multiple products exists in list,
            when fetching paginated and filtered products by name,
            then expect correct product is found
            """)
    void shouldReturnFilteredAndPaginatedProducts() throws Exception {
        // given
        var products = productRepository.saveAll(ProductFixtures.getProductList());
        stubHnbCurrencyExchangeApi();
        // when
        var result = mockMvc.perform(get("/products?page=0&size=2"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andReturn()
                .getResponse()
                .getContentAsString();
        // then
        var productsResponse = objectMapper.readValue(result, ProductsResponse.class);
        var firstProduct = products.get(0);
        var firstProductsResponse = productsResponse.products().get(0);
        var secondProduct = products.get(1);
        var secondProductResponse = productsResponse.products().get(1);

        assertThat(productsResponse.products()).hasSize(2);
        assertThat(firstProductsResponse.id()).isEqualTo(firstProduct.getId());
        assertThat(secondProductResponse.id()).isGreaterThan(firstProductsResponse.id());
        assertThat(firstProductsResponse.code()).isEqualTo(firstProduct.getCode());
        assertThat(firstProductsResponse.name()).isEqualTo(firstProduct.getName());
        assertThat(firstProductsResponse.priceEur()).isEqualTo(firstProduct.getPriceEur());
        assertThat(firstProductsResponse.priceUsd()).isEqualTo(productMapper.convertToUsdPrice(firstProduct.getPriceEur()));
        assertThat(firstProductsResponse.description()).isEqualTo(firstProduct.getDescription());
        assertThat(firstProductsResponse.isAvailable()).isEqualTo(firstProduct.getIsAvailable());
        assertThat(firstProductsResponse.isAvailable()).isTrue();

        assertThat(secondProductResponse.id()).isEqualTo(secondProduct.getId());
        assertThat(secondProductResponse.code()).isEqualTo(secondProduct.getCode());
        assertThat(secondProductResponse.name()).isEqualTo(secondProduct.getName());
        assertThat(secondProductResponse.priceEur()).isEqualTo(secondProduct.getPriceEur());
        assertThat(secondProductResponse.priceUsd()).isEqualTo(productMapper.convertToUsdPrice(secondProduct.getPriceEur()));
        assertThat(secondProductResponse.description()).isEqualTo(secondProduct.getDescription());
        assertThat(secondProductResponse.isAvailable()).isTrue();
    }

    @Test
    @DisplayName("""
            Given multiple products exists in list,
            when fetching paginated and filtered products by name,
            then expect correct product is found
            """)
    void shouldReturnFilteredAndSortedProductsByName() throws Exception {
        // given
        var products = productRepository.saveAll(ProductFixtures.getProductList());
        stubHnbCurrencyExchangeApi();
        // when
        var result = mockMvc.perform(get("/products?name=Desktop PC"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andReturn()
                .getResponse()
                .getContentAsString();
        // then
        var secondProduct = products.get(1);
        var thirdProductsResponse = objectMapper.readValue(result, ProductsResponse.class);
        var thirdProduct = products.get(2);
        var thirdProductResponse = thirdProductsResponse.products().getFirst();

        assertThat(thirdProductsResponse.products()).hasSize(1);
        assertThat(thirdProductResponse.id()).isEqualTo(thirdProduct.getId());
        assertThat(thirdProductResponse.id()).isGreaterThan(secondProduct.getId());
        assertThat(thirdProductResponse.code()).isEqualTo(thirdProduct.getCode());
        assertThat(thirdProductResponse.name()).isEqualTo(thirdProduct.getName());
        assertThat(thirdProductResponse.priceEur()).isEqualTo(thirdProduct.getPriceEur());
        assertThat(thirdProductResponse.priceUsd()).isEqualTo(productMapper.convertToUsdPrice(thirdProduct.getPriceEur()));
        assertThat(thirdProductResponse.description()).isEqualTo(thirdProduct.getDescription());
        assertThat(thirdProductResponse.isAvailable()).isTrue();
    }

    @Test
    @DisplayName("""
            Given multiple products exists in list,
            when fetching paginated products by over max page and size,
            then validation exception and bad request are returned with correct message
            """)
    void shouldReturnValidationExceptionIfValidationFailsOverMaxPageAndSize() throws Exception {
        // given
        productRepository.saveAll(ProductFixtures.getProductList());
        // when
        var result = mockMvc.perform(get("/products?page=101&size=101"))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON_VALUE))
                .andReturn()
                .getResponse()
                .getContentAsString();
        // then
        var response = objectMapper.readValue(result, ProblemDetail.class);
        assertThat(response.getType()).hasToString("about:blank");
        assertThat(response.getTitle()).isEqualTo("Bad Request");
        assertThat(response.getStatus()).isEqualTo(400);
        assertThat(response.getDetail()).isEqualTo("attribute page must be below than 100, and attribute size must be below than 100");
        assertThat(response.getInstance()).hasToString("/products");
    }

    @Test
    @DisplayName("""
            Given multiple products exists in list,
            when fetching paginated products by below min page and size,
            then validation exception and bad request are returned with correct message
            """)
    void shouldReturnValidationExceptionIfValidationFailsBelowMinPageAndSize() throws Exception {
        // given
        productRepository.saveAll(ProductFixtures.getProductList());
        // when
        var result = mockMvc.perform(get("/products?page=-1&size=-1"))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON_VALUE))
                .andReturn()
                .getResponse()
                .getContentAsString();
        // then
        var response = objectMapper.readValue(result, ProblemDetail.class);
        assertThat(response.getType()).hasToString("about:blank");
        assertThat(response.getTitle()).isEqualTo("Bad Request");
        assertThat(response.getStatus()).isEqualTo(400);
        assertThat(response.getDetail()).isEqualTo("attribute page must be positive number, and attribute size must be greater than 1");
        assertThat(response.getInstance()).hasToString("/products");
    }

    @Test
    @DisplayName("""
            Given multiple products exists in list,
            when fetching all products,
            then expected product should be returned by requested id
            """)
    void shouldReturnedProductByRequestedId() throws Exception {
        // given
        var product = productRepository.save(ProductFixtures.productFive());
        var id = product.getId();
        stubHnbCurrencyExchangeApi();
        // when
        var result = mockMvc.perform(get("/products/" + id))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andReturn()
                .getResponse()
                .getContentAsString();
        //then
        var productResponse = objectMapper.readValue(result, ProductResponse.class);
        assertThat(productResponse.id()).isEqualTo(id);
        assertThat(productResponse.code()).isEqualTo(product.getCode());
        assertThat(productResponse.name()).isEqualTo(product.getName());
        assertThat(productResponse.priceEur()).isEqualTo(product.getPriceEur());
        assertThat(productResponse.priceUsd()).isEqualTo(productMapper.convertToUsdPrice(product.getPriceEur()));
        assertThat(productResponse.description()).isEqualTo(product.getDescription());
        assertThat(productResponse.isAvailable()).isEqualTo(product.getIsAvailable());
        assertThat(productResponse.isAvailable()).isFalse();
    }

    @Test
    @DisplayName("""
            Given single product in list, and HNB API serivce is down,
            when fetching product by requested id,
            then platform exception must be thrown and bad internal server error are returned
            """)
    void shouldReturnedHnbApiError() throws Exception {
        // given
        var product = productRepository.save(ProductFixtures.productFive());
        var id = product.getId();
        stubHnbCurrencyExchangeApiError();
        // when
        var result = mockMvc.perform(get("/products/" + id))
                .andExpect(status().is5xxServerError())
                .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
                .andReturn()
                .getResponse()
                .getContentAsString();
        //then
        var response = objectMapper.readValue(result, ProblemDetail.class);
        assertThat(response.getType()).hasToString("about:blank");
        assertThat(response.getTitle()).isEqualTo("Internal Server Error");
        assertThat(response.getStatus()).isEqualTo(500);
        assertThat(response.getDetail()).isEqualTo("Hnb api error");
        assertThat(response.getInstance()).hasToString("/products/" + id);
    }

    @Test
    @DisplayName("""
            Given multiple products exists in list,
            when fetching all products,
            then not found status should be returned by non existing id
            """)
    void shouldReturnedNotFoundByNonExistingId() throws Exception {
        // given
        productRepository.saveAll(ProductFixtures.getProductList());
        // when
        var result = mockMvc.perform(get("/products/-5"))
                .andExpect(status().isNotFound())
                .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON_VALUE))
                .andReturn()
                .getResponse()
                .getContentAsString();
        // then
        var response = objectMapper.readValue(result, ProblemDetail.class);
        assertThat(response.getType()).hasToString("about:blank");
        assertThat(response.getTitle()).isEqualTo("Not Found");
        assertThat(response.getStatus()).isEqualTo(404);
        assertThat(response.getDetail()).isEqualTo("Could not find product by this id -5");
        assertThat(response.getInstance()).hasToString("/products/-5");
    }

    @Test
    @DisplayName("""
            Given product does not exist in list,
            when new product is created and data is send to kafka
            then new product is returned with message in kafka
            """)
    void shouldCreateNewProductAndReturnStatusCreated() throws Exception {
        // given
        productRepository.saveAll(ProductFixtures.getProductList());
        var products = productRepository.findAll();
        var lastProduct = products.getLast();
        var productRequest = ProductFixtures.newProductRequest();
        stubHnbCurrencyExchangeApi();
        // when
        var result = mockMvc.perform(post("/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(productRequest)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();
        //then
        var productResponse = objectMapper.readValue(result, ProductResponse.class);
        assertThat(productResponse.id()).isGreaterThan(lastProduct.getId());
        assertThat(productResponse.code()).isEqualTo(productRequest.code());
        assertThat(productResponse.name()).isEqualTo(productRequest.name());
        assertThat(productResponse.priceEur()).isEqualTo(productRequest.priceEur());
        assertThat(productResponse.priceUsd()).isEqualTo(productMapper.convertToUsdPrice(productRequest.priceEur()));
        assertThat(productResponse.description()).isEqualTo(productRequest.description());
        assertThat(productResponse.isAvailable()).isTrue();

        Awaitility.await().until(() -> Objects.nonNull(kafkaTestListener.getLatestEvent()));
        var latestEvent = kafkaTestListener.getLatestEvent();
        var createdProductEvent = objectMapper.readValue(latestEvent, CreatedProductEvent.class);
        assertThat(createdProductEvent.eventId()).isNotNull();
        assertThat(createdProductEvent.timestamp()).isBefore(ZonedDateTime.now());
        assertThat(createdProductEvent.action()).isEqualTo(Action.CREATE);
        assertThat(createdProductEvent.product().id()).isEqualTo(productResponse.id());
        assertThat(createdProductEvent.product().code()).isEqualTo(productResponse.code());
        assertThat(createdProductEvent.product().name()).isEqualTo(productResponse.name());
        assertThat(createdProductEvent.product().priceEur()).isEqualTo(productResponse.priceEur());
        assertThat(createdProductEvent.product().description()).isEqualTo(productResponse.description());
        assertThat(createdProductEvent.product().isAvailable()).isTrue();
    }

    @Test
    @DisplayName("""
            Given new product code exist in list,
            when existing is found,
            then validation exception is returned for status bad request
            """)
    void shouldReturnBadRequestForNewProductOfExistingCode() throws Exception {
        productRepository.saveAll(ProductFixtures.getProductList());
        var existingProduct = ProductFixtures.productFive();
        // when
        var result = mockMvc.perform(post("/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(existingProduct)))
                .andExpect(status().isBadRequest())
                .andReturn()
                .getResponse()
                .getContentAsString();
        // then
        var response = objectMapper.readValue(result, ProblemDetail.class);
        assertThat(response.getType()).hasToString("about:blank");
        assertThat(response.getTitle()).isEqualTo("Bad Request");
        assertThat(response.getStatus()).isEqualTo(400);
        assertThat(response.getDetail()).isEqualTo("Data integrity violation exception");
        assertThat(response.getInstance()).hasToString("/products");
    }

    @Test
    @DisplayName("""
            Given product not exist in list,
            when new is unable to create,
            then validation exception is throw for code is not exactly 10 chars long
            """)
    void shouldReturnValidationExceptionForNewProductIfValidationFailsForCodeISNot10CharsLong() throws Exception {
        // given
        productRepository.saveAll(ProductFixtures.getProductList());
        var newProduct = ProductFixtures.newProduct();
        newProduct.setCode("1541fgdfGTFGrfg");
        // when
        var result = mockMvc.perform(post("/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newProduct)))
                .andExpect(status().isBadRequest())
                .andReturn()
                .getResponse()
                .getContentAsString();
        // then
        var response = objectMapper.readValue(result, ProblemDetail.class);
        assertThat(response.getType()).hasToString("about:blank");
        assertThat(response.getTitle()).isEqualTo("Bad Request");
        assertThat(response.getStatus()).isEqualTo(400);
        assertThat(response.getDetail()).isEqualTo("code: Property must be exactly 10 characters");
        assertThat(response.getInstance()).hasToString("/products");
    }

    @Test
    @DisplayName("""
            Given product not exist in list,
            when new product is unable to create,
            then validation exception is throw for blank name
            """)
    void shouldReturnValidationExceptionForNewProductIfValidationFailsForBlankName() throws Exception {
        // given
        productRepository.saveAll(ProductFixtures.getProductList());
        var newProduct = ProductFixtures.newProduct();
        newProduct.setName("");
        // when
        var result = mockMvc.perform(post("/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newProduct)))
                .andExpect(status().isBadRequest())
                .andReturn()
                .getResponse()
                .getContentAsString();
        // then
        var response = objectMapper.readValue(result, ProblemDetail.class);
        assertThat(response.getType()).hasToString("about:blank");
        assertThat(response.getTitle()).isEqualTo("Bad Request");
        assertThat(response.getStatus()).isEqualTo(400);
        assertThat(response.getDetail()).isEqualTo("name: must not be blank");
        assertThat(response.getInstance()).hasToString("/products");
    }

    @Test
    @DisplayName("""
            Given product not exist in list,
            when new is unable to create,
            then validation exception is throw for price must be grater than or equal zero
            """)
    void shouldReturnValidationExceptionForNewProductIfValidationFailsForPriceMustBePositiveNumber() throws Exception {
        // given
        productRepository.saveAll(ProductFixtures.getProductList());
        var newProduct = ProductFixtures.newProduct();
        newProduct.setPriceEur(BigDecimal.valueOf(-54.45));
        // when
        var result = mockMvc.perform(post("/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newProduct)))
                .andExpect(status().isBadRequest())
                .andReturn()
                .getResponse()
                .getContentAsString();
        // then
        var response = objectMapper.readValue(result, ProblemDetail.class);
        assertThat(response.getType()).hasToString("about:blank");
        assertThat(response.getTitle()).isEqualTo("Bad Request");
        assertThat(response.getStatus()).isEqualTo(400);
        assertThat(response.getDetail()).isEqualTo("priceEur: must be greater than or equal to 0");
        assertThat(response.getInstance()).hasToString("/products");
    }

    @Test
    @DisplayName("""
            Given product does exist in list,
            when current product is updated and new data is send to kafka
            then current product is returned with new data and message in kafka
            """)
    void shouldUpdateProductAndReturnStatusCodeOk() throws Exception {
        // given
        var product = productRepository.save(ProductFixtures.productTwo());
        var id = product.getId();
        var productRequest = ProductFixtures.productRequestOne();
        stubHnbCurrencyExchangeApi();
        // when
        var result = mockMvc.perform(put("/products/" + id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(productRequest)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();
        //then
        var productResponse = objectMapper.readValue(result, ProductResponse.class);
        assertThat(productResponse.id()).isEqualTo(id);
        assertThat(productResponse.code()).isEqualTo(productRequest.code());
        assertThat(productResponse.name()).isEqualTo(productRequest.name());
        assertThat(productResponse.priceEur()).isEqualTo(productRequest.priceEur());
        assertThat(productResponse.priceUsd()).isEqualTo(productMapper.convertToUsdPrice(productRequest.priceEur()));
        assertThat(productResponse.description()).isEqualTo(productRequest.description());
        assertThat(productResponse.isAvailable()).isTrue();

        Awaitility.await().until(() -> Objects.nonNull(kafkaTestListener.getLatestEvent()));
        var latestEvent = kafkaTestListener.getLatestEvent();
        var updatedProductEvent = objectMapper.readValue(latestEvent, UpdatedProductEvent.class);
        assertThat(updatedProductEvent.eventId()).isNotNull();
        assertThat(updatedProductEvent.timestamp()).isBefore(ZonedDateTime.now());
        assertThat(updatedProductEvent.action()).isEqualTo(Action.UPDATE);
        assertThat(updatedProductEvent.product().id()).isEqualTo(productResponse.id());
        assertThat(updatedProductEvent.product().code()).isEqualTo(productResponse.code());
        assertThat(updatedProductEvent.product().name()).isEqualTo(productResponse.name());
        assertThat(updatedProductEvent.product().priceEur()).isEqualTo(productResponse.priceEur());
        assertThat(updatedProductEvent.product().description()).isEqualTo(productResponse.description());
        assertThat(updatedProductEvent.product().isAvailable()).isTrue();
    }

    @Test
    @DisplayName("""
            Given product does not exist in list,
            when current product is unable to updated,
            then status code not found is returned
            """)
    void shouldReturnStatusCodeNotFoundForUpdateOfNonExistingId() throws Exception {
        // given
        productRepository.saveAll(ProductFixtures.getProductList());
        var product = ProductFixtures.productFour();
        product.setId(-1L);
        var id = product.getId();
        // when
        var result = mockMvc.perform(put("/products/" + id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(product)))
                .andExpect(status().isNotFound())
                .andReturn()
                .getResponse()
                .getContentAsString();
        // then
        var response = objectMapper.readValue(result, ProblemDetail.class);
        assertThat(response.getType()).hasToString("about:blank");
        assertThat(response.getTitle()).isEqualTo("Not Found");
        assertThat(response.getStatus()).isEqualTo(404);
        assertThat(response.getDetail()).isEqualTo("Could not find product by this id " + id);
        assertThat(response.getInstance()).hasToString("/products/" + id);
    }

    @Test
    @DisplayName("""
            Given product exist in list,
            when existing is found,
            then validation exception is thrown for code exists
            """)
    void shouldReturnBadRequestOfNonUpdatedProductForExistingCode() throws Exception {
        // given
        var products = productRepository.saveAll(ProductFixtures.getProductList());
        var productRequest = ProductFixtures.productRequestTwo();
        var id = products.get(2).getId();
        // when
        var result = mockMvc.perform(put("/products/" + id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(productRequest)))
                .andExpect(status().isBadRequest())
                .andReturn()
                .getResponse()
                .getContentAsString();
        // then
        var response = objectMapper.readValue(result, ProblemDetail.class);
        assertThat(response.getType()).hasToString("about:blank");
        assertThat(response.getTitle()).isEqualTo("Bad Request");
        assertThat(response.getStatus()).isEqualTo(400);
        assertThat(response.getDetail()).isEqualTo("Data integrity violation exception");
        assertThat(response.getInstance()).hasToString("/products/" + id);
    }

    @Test
    @DisplayName("""
            Given product exist in list,
            when present product is unable to update,
            then validation exception is throw for blank name
            """)
    void shouldReturnValidationExceptionOfNonUpdatedProductIfValidationFailsForBlankName() throws Exception {
        // given
        productRepository.saveAll(ProductFixtures.getProductList());
        var existingProduct = ProductFixtures.productThree();
        existingProduct.setName("");
        // when
        var result = mockMvc.perform(put("/products/" + existingProduct.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(existingProduct)))
                .andExpect(status().isBadRequest())
                .andReturn()
                .getResponse()
                .getContentAsString();
        // then
        var response = objectMapper.readValue(result, ProblemDetail.class);
        assertThat(response.getType()).hasToString("about:blank");
        assertThat(response.getTitle()).isEqualTo("Bad Request");
        assertThat(response.getStatus()).isEqualTo(400);
        assertThat(response.getDetail()).isEqualTo("name: must not be blank");
        assertThat(response.getInstance()).hasToString("/products/" + existingProduct.getId());
    }

    @Test
    @DisplayName("""
            Given product exist in list,
            when current product is unable to update,
            then validation exception is throw for price must be grater than or equal zero
            """)
    void shouldReturnValidationExceptionOFNonUpdatedProductIfValidationFailsForPriceMustBePositiveNumber() throws Exception {
        // given
        productRepository.saveAll(ProductFixtures.getProductList());
        var existingProduct = ProductFixtures.productTwo();
        existingProduct.setPriceEur(BigDecimal.valueOf(-145, 45));
        // when
        var result = mockMvc.perform(put("/products/" + existingProduct.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(existingProduct)))
                .andExpect(status().isBadRequest())
                .andReturn()
                .getResponse()
                .getContentAsString();
        // then
        var response = objectMapper.readValue(result, ProblemDetail.class);
        assertThat(response.getType()).hasToString("about:blank");
        assertThat(response.getTitle()).isEqualTo("Bad Request");
        assertThat(response.getStatus()).isEqualTo(400);
        assertThat(response.getDetail()).isEqualTo("priceEur: must be greater than or equal to 0");
        assertThat(response.getInstance()).hasToString("/products/" + existingProduct.getId());
    }

    @Test
    @DisplayName("""
            Given multiple products exists in list,
            when fetching product by requested existing id and new data is send to kafka
            then current product is returned with new data and message in kafka
            then expected product should be removed
            """)
    void shouldReturnStatusNoContentOfRemovedProduct() throws Exception {
        // given
        var products = productRepository.saveAll(ProductFixtures.getProductList());
        var id = products.get(4).getId();
        // when & then
        mockMvc.perform(delete("/products/" + id))
                .andExpect(status().isNoContent());

        Awaitility.await().until(() -> Objects.nonNull(kafkaTestListener.getLatestEvent()));
        var latestEvent = kafkaTestListener.getLatestEvent();
        var deletedProductEvent = objectMapper.readValue(latestEvent, DeletedProductEvent.class);
        assertThat(deletedProductEvent.eventId()).isNotNull();
        assertThat(deletedProductEvent.timestamp()).isBefore(ZonedDateTime.now());
        assertThat(deletedProductEvent.action()).isEqualTo(Action.DELETE);
        assertThat(deletedProductEvent.product().id()).isEqualTo(id);
    }

    @Test
    @DisplayName("""
            Given multiple products exists in list,
            when fetching product by non existing id,
            then not found status should be returned
            """)
    void shouldReturnStatusNotFoundOfNotRemoveProductByNonExistingId() throws Exception {
        // given
        var products = productRepository.saveAll(ProductFixtures.getProductList());
        var id = products.get(4).getId() + 1;
        // when & then
        var result = mockMvc.perform(delete("/products/" + id))
                .andExpect(status().isNotFound())
                .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON_VALUE))
                .andReturn()
                .getResponse()
                .getContentAsString();
        // then
        var response = objectMapper.readValue(result, ProblemDetail.class);
        assertThat(response.getType()).hasToString("about:blank");
        assertThat(response.getTitle()).isEqualTo("Not Found");
        assertThat(response.getStatus()).isEqualTo(404);
        assertThat(response.getDetail()).isEqualTo("Could not find product by this id " + id);
        assertThat(response.getInstance()).hasToString("/products/" + id);
    }

    void stubHnbCurrencyExchangeApi() {
        wireMockServer.stubFor(WireMock.get(WireMock.urlEqualTo("/mock/products"))
                .willReturn(WireMock.aResponse()
                        .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                        .withBody("""
                                [
                                   {
                                      "broj_tecajnice":"206",
                                      "datum_primjene":"2023-10-19",
                                      "drzava":"SAD",
                                      "drzava_iso":"USA",
                                      "sifra_valute":"840",
                                      "valuta":"USD",
                                      "kupovni_tecaj":"1,1000",
                                      "srednji_tecaj":"1,0565",
                                      "prodajni_tecaj":"1,0549"
                                   }
                                ]
                                """)
                )
        );
    }

    void stubHnbCurrencyExchangeApiError() {
        wireMockServer.stubFor(WireMock.get(WireMock.urlEqualTo("/mock/products"))
                .willReturn(WireMock.serverError()
                        .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_PROBLEM_JSON_VALUE)
                        .withBody("""
                                {
                                  "error": "service HNB API currently out of reach!"
                                }
                                """)
                )
        );
    }
}