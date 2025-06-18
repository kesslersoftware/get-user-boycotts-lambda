package com.boycottpro.userboycotts;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;

import com.boycottpro.models.UserBoycotts;
import com.boycottpro.userboycotts.model.CauseSummary;
import com.boycottpro.userboycotts.model.ResponsePojo;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.*;

import java.util.*;
import java.util.stream.Collectors;

public class GetBoycottsByCompanyAndUserHandler implements RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {

    private static final String TABLE_NAME = "";
    private final DynamoDbClient dynamoDb;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public GetBoycottsByCompanyAndUserHandler() {
        this.dynamoDb = DynamoDbClient.create();
    }

    public GetBoycottsByCompanyAndUserHandler(DynamoDbClient dynamoDb) {
        this.dynamoDb = dynamoDb;
    }

    @Override
    public APIGatewayProxyResponseEvent handleRequest(APIGatewayProxyRequestEvent event, Context context) {
        try {
            Map<String, String> pathParams = event.getPathParameters();
            String userId = (pathParams != null) ? pathParams.get("user_id") : null;
            String companyId = (pathParams != null) ? pathParams.get("company_id") : null;
            if (userId == null || userId.isEmpty()) {
                return new APIGatewayProxyResponseEvent()
                        .withStatusCode(400)
                        .withBody("{\"error\":\"Missing user_id in path\"}");
            }
            if (companyId == null || companyId.isEmpty()) {
                return new APIGatewayProxyResponseEvent()
                        .withStatusCode(400)
                        .withBody("{\"error\":\"Missing company_id in path\"}");
            }
            ResponsePojo results = getBoycottWithOldestTimestamp(userId, companyId);
            String responseBody = objectMapper.writeValueAsString(results);
            return new APIGatewayProxyResponseEvent()
                    .withStatusCode(200)
                    .withHeaders(Map.of("Content-Type", "application/json"))
                    .withBody(responseBody);
        } catch (Exception e) {
            e.printStackTrace();
            return new APIGatewayProxyResponseEvent()
                    .withStatusCode(500)
                    .withBody("{\"error\": \"Unexpected server error: " + e.getMessage() + "\"}");
        }
    }
    private ResponsePojo getBoycottWithOldestTimestamp(String userId, String companyId) {
        QueryRequest request = QueryRequest.builder()
                .tableName("user_boycotts")
                .keyConditionExpression("user_id = :uid")
                .expressionAttributeValues(Map.of(":uid", AttributeValue.fromS(userId)))
                .build();

        QueryResponse response = dynamoDb.query(request);
        // Filter for records where company_id matches the given companyId
        List<Map<String, AttributeValue>> matchingRecords = response.items().stream()
                .filter(item -> item.containsKey("company_id") &&
                        item.get("company_id").s().equals(companyId))
                .collect(Collectors.toList());
        if (matchingRecords.isEmpty()) {
            // No boycott found for this user+company
            ResponsePojo result = new ResponsePojo();
            result.setBoycotting(false);
            return result;
        }
        // Map each record to a CauseSummary
        List<CauseSummary> reasons = matchingRecords.stream()
                .map(item -> new CauseSummary(
                        item.getOrDefault("cause_id", AttributeValue.fromS("")).s(),
                        item.getOrDefault("cause_desc", AttributeValue.fromS("")).s()))
                .collect(Collectors.toList());
        // Find the record with the earliest timestamp
        Map<String, AttributeValue> earliest = matchingRecords.stream()
                .filter(item -> item.containsKey("timestamp") && item.get("timestamp").s() != null && !item.get("timestamp").s().isEmpty())
                .min(Comparator.comparing(item -> item.get("timestamp").s()))
                .orElse(null);
        // Populate final response
        ResponsePojo result = new ResponsePojo();
        result.setBoycotting(true);
        result.setCompany_id(companyId);
        result.setCompany_name(earliest.getOrDefault("company_name", AttributeValue.fromS("")).s());
        result.setBoycottingSince(earliest.getOrDefault("timestamp", AttributeValue.fromS("0")).s());
        result.setReasons(reasons);

        return result;
    }

}