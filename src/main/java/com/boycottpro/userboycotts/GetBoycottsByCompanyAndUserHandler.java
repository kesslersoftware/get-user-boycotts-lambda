package com.boycottpro.userboycotts;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;

import com.boycottpro.models.UserBoycotts;
import com.boycottpro.userboycotts.model.CauseSummary;
import com.boycottpro.userboycotts.model.ResponsePojo;
import com.boycottpro.utilities.JwtUtility;
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
        String sub = null;
        try {
            sub = JwtUtility.getSubFromRestEvent(event);
            if (sub == null) return response(401, Map.of("message", "Unauthorized"));
            Map<String, String> pathParams = event.getPathParameters();
            String companyId = (pathParams != null) ? pathParams.get("company_id") : null;
            if (companyId == null || companyId.isEmpty()) {
                return response(400,Map.of("error","Missing company_id in path"));
            }
            ResponsePojo results = getBoycottWithOldestTimestamp(sub, companyId);
            return response(200,results);
        } catch (Exception e) {
            System.out.println(e.getMessage() + " for user " + sub);
            return response(500,Map.of("error", "Unexpected server error: " + e.getMessage()) );
        }
    }

    private APIGatewayProxyResponseEvent response(int status, Object body) {
        String responseBody = null;
        try {
            responseBody = objectMapper.writeValueAsString(body);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
        return new APIGatewayProxyResponseEvent()
                .withStatusCode(status)
                .withHeaders(Map.of("Content-Type", "application/json"))
                .withBody(responseBody);
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
                .map(item -> {
                        AttributeValue causeDescAttr = item.get("cause_desc");
                        AttributeValue personalReasonAttr = item.get("personal_reason");
                        if(causeDescAttr != null && causeDescAttr.s() != null && !causeDescAttr.s().isEmpty()) {
                            return new CauseSummary(
                                    item.getOrDefault("cause_id", AttributeValue.fromS("")).s(),
                                    causeDescAttr.s(),false);
                        } else {
                            return new CauseSummary(
                                    item.getOrDefault("cause_id", AttributeValue.fromS("")).s(),
                                    personalReasonAttr.s(),true);
                        }
                        })
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