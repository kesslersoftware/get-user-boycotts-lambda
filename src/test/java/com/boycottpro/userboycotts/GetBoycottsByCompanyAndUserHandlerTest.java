package com.boycottpro.userboycotts;

import com.amazonaws.services.lambda.runtime.Context;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.*;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;

@ExtendWith(MockitoExtension.class)
public class GetBoycottsByCompanyAndUserHandlerTest {

    @Mock
    private DynamoDbClient dynamoDb;

    @InjectMocks
    private GetBoycottsByCompanyAndUserHandler handler;

    @Mock
    private Context context;

    @Test
    public void testValidRequestReturnsResponse() throws Exception {
        String userId = "test-user";
        String companyId = "test-company";

        Map<String, String> pathParams = Map.of(
                "user_id", "s",
                "company_id", companyId
        );

        Map<String, AttributeValue> item = Map.of(
                "user_id", AttributeValue.fromS(userId),
                "company_id", AttributeValue.fromS(companyId),
                "company_name", AttributeValue.fromS("Test Corp"),
                "timestamp", AttributeValue.fromS("2025-01-01T00:00:00Z"),
                "cause_id", AttributeValue.fromS("cause1"),
                "cause_desc", AttributeValue.fromS("Test Cause")
        );

        QueryResponse queryResponse = QueryResponse.builder()
                .items(List.of(item))
                .build();

        when(dynamoDb.query(any(QueryRequest.class))).thenReturn(queryResponse);

        APIGatewayProxyRequestEvent event = new APIGatewayProxyRequestEvent();
        Map<String, String> claims = Map.of("sub", "11111111-2222-3333-4444-555555555555");
        Map<String, Object> authorizer = new HashMap<>();
        authorizer.put("claims", claims);

        APIGatewayProxyRequestEvent.ProxyRequestContext rc = new APIGatewayProxyRequestEvent.ProxyRequestContext();
        rc.setAuthorizer(authorizer);
        event.setRequestContext(rc);
        event.setPathParameters(pathParams);

        APIGatewayProxyResponseEvent response = handler.handleRequest(event, context);

        assertEquals(200, response.getStatusCode());
        assertTrue(response.getBody().contains("Test Corp"));
        assertTrue(response.getBody().contains("Test Cause"));
        assertTrue(response.getBody().contains("true")); // isBoycotting
    }

    @Test
    public void testMissingUserId() {
        APIGatewayProxyRequestEvent event = null;

        var response = handler.handleRequest(event, mock(Context.class));

        assertEquals(401, response.getStatusCode());
        assertTrue(response.getBody().contains("Unauthorized"));
    }

    @Test
    public void testMissingCompanyId() {
        APIGatewayProxyRequestEvent event = new APIGatewayProxyRequestEvent();
        Map<String, String> claims = Map.of("sub", "11111111-2222-3333-4444-555555555555");
        Map<String, Object> authorizer = new HashMap<>();
        authorizer.put("claims", claims);

        APIGatewayProxyRequestEvent.ProxyRequestContext rc = new APIGatewayProxyRequestEvent.ProxyRequestContext();
        rc.setAuthorizer(authorizer);
        event.setRequestContext(rc);

        // Path param "s" since client calls /users/s
        event.setPathParameters(Map.of("user_id", "s"));

        APIGatewayProxyResponseEvent response = handler.handleRequest(event, context);
        assertEquals(400, response.getStatusCode());
        assertTrue(response.getBody().contains("Missing company_id"));
    }

    @Test
    public void testNoMatchingRecords() {
        String userId = "test-user";
        String companyId = "test-company";
        Map<String, String> pathParams = Map.of(
                "user_id", "s",
                "company_id", companyId
        );
        QueryResponse queryResponse = QueryResponse.builder()
                .items(Collections.emptyList())
                .build();

        when(dynamoDb.query(any(QueryRequest.class))).thenReturn(queryResponse);

        APIGatewayProxyRequestEvent event = new APIGatewayProxyRequestEvent();
        Map<String, String> claims = Map.of("sub", "11111111-2222-3333-4444-555555555555");
        Map<String, Object> authorizer = new HashMap<>();
        authorizer.put("claims", claims);

        APIGatewayProxyRequestEvent.ProxyRequestContext rc = new APIGatewayProxyRequestEvent.ProxyRequestContext();
        rc.setAuthorizer(authorizer);
        event.setRequestContext(rc);
        event.setPathParameters(pathParams);

        APIGatewayProxyResponseEvent response = handler.handleRequest(event, context);
        assertEquals(200, response.getStatusCode());
        assertTrue(response.getBody().contains("\"boycotting\":false"));
    }
}
