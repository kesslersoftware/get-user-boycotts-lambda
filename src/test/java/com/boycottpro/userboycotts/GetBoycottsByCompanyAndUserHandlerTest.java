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
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import com.fasterxml.jackson.core.JsonProcessingException;

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

    @Test
    public void testDefaultConstructor() {
        // Test the default constructor coverage
        // Note: This may fail in environments without AWS credentials/region configured
        try {
            GetBoycottsByCompanyAndUserHandler handler = new GetBoycottsByCompanyAndUserHandler();
            assertNotNull(handler);

            // Verify DynamoDbClient was created (using reflection to access private field)
            try {
                Field dynamoDbField = GetBoycottsByCompanyAndUserHandler.class.getDeclaredField("dynamoDb");
                dynamoDbField.setAccessible(true);
                DynamoDbClient dynamoDb = (DynamoDbClient) dynamoDbField.get(handler);
                assertNotNull(dynamoDb);
            } catch (NoSuchFieldException | IllegalAccessException e) {
                fail("Failed to access DynamoDbClient field: " + e.getMessage());
            }
        } catch (software.amazon.awssdk.core.exception.SdkClientException e) {
            // AWS SDK can't initialize due to missing region configuration
            // This is expected in Jenkins without AWS credentials - test passes
            System.out.println("Skipping DynamoDbClient verification due to AWS SDK configuration: " + e.getMessage());
        }
    }

    @Test
    public void testUnauthorizedUser() {
        // Test the unauthorized block coverage
        handler = new GetBoycottsByCompanyAndUserHandler(dynamoDb);

        // Create event without JWT token (or invalid token that returns null sub)
        APIGatewayProxyRequestEvent event = new APIGatewayProxyRequestEvent();
        // No authorizer context, so JwtUtility.getSubFromRestEvent will return null

        APIGatewayProxyResponseEvent response = handler.handleRequest(event, null);

        assertEquals(401, response.getStatusCode());
        assertTrue(response.getBody().contains("Unauthorized"));
    }

    @Test
    public void testJsonProcessingExceptionInResponse() throws Exception {
        // Test JsonProcessingException coverage in response method by using reflection
        handler = new GetBoycottsByCompanyAndUserHandler(dynamoDb);

        // Use reflection to access the private response method
        java.lang.reflect.Method responseMethod = GetBoycottsByCompanyAndUserHandler.class.getDeclaredMethod("response", int.class, Object.class);
        responseMethod.setAccessible(true);

        // Create an object that will cause JsonProcessingException
        Object problematicObject = new Object() {
            public Object writeReplace() throws java.io.ObjectStreamException {
                throw new java.io.NotSerializableException("Not serializable");
            }
        };

        // Create a circular reference object that will cause JsonProcessingException
        Map<String, Object> circularMap = new HashMap<>();
        circularMap.put("self", circularMap);

        // This should trigger the JsonProcessingException -> RuntimeException path
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            try {
                responseMethod.invoke(handler, 500, circularMap);
            } catch (java.lang.reflect.InvocationTargetException e) {
                if (e.getCause() instanceof RuntimeException) {
                    throw (RuntimeException) e.getCause();
                }
                throw new RuntimeException(e.getCause());
            }
        });

        // Verify it's ultimately caused by JsonProcessingException
        Throwable cause = exception.getCause();
        assertTrue(cause instanceof JsonProcessingException,
                "Expected JsonProcessingException, got: " + cause.getClass().getSimpleName());
    }

    @Test
    public void testGenericExceptionHandling() {
        // Test the generic Exception catch block coverage
        handler = new GetBoycottsByCompanyAndUserHandler(dynamoDb);
        String companyId = "test-company";
        Map<String, String> pathParams = Map.of(
                "user_id", "s",
                "company_id", companyId
        );
        // Create a valid JWT event
        APIGatewayProxyRequestEvent event = new APIGatewayProxyRequestEvent();
        Map<String, String> claims = Map.of("sub", "11111111-2222-3333-4444-555555555555");
        Map<String, Object> authorizer = new HashMap<>();
        authorizer.put("claims", claims);

        APIGatewayProxyRequestEvent.ProxyRequestContext rc = new APIGatewayProxyRequestEvent.ProxyRequestContext();
        rc.setAuthorizer(authorizer);
        event.setRequestContext(rc);
        event.setPathParameters(pathParams);
        // Mock DynamoDB to throw a generic exception (e.g., RuntimeException)
        when(dynamoDb.query(any(QueryRequest.class)))
                .thenThrow(new RuntimeException("Database connection failed"));

        // Act
        APIGatewayProxyResponseEvent response = handler.handleRequest(event, null);

        // Assert
        assertEquals(500, response.getStatusCode());
        assertTrue(response.getBody().contains("Unexpected server error"));
    }

    @Test
    public void testEmptyCompanyIdReturns400() {
        // Test line 48: companyId.isEmpty() branch
        handler = new GetBoycottsByCompanyAndUserHandler(dynamoDb);
        Map<String, String> pathParams = Map.of("company_id", "");

        APIGatewayProxyRequestEvent event = new APIGatewayProxyRequestEvent();
        Map<String, String> claims = Map.of("sub", "11111111-2222-3333-4444-555555555555");
        Map<String, Object> authorizer = new HashMap<>();
        authorizer.put("claims", claims);

        APIGatewayProxyRequestEvent.ProxyRequestContext rc = new APIGatewayProxyRequestEvent.ProxyRequestContext();
        rc.setAuthorizer(authorizer);
        event.setRequestContext(rc);
        event.setPathParameters(pathParams);

        APIGatewayProxyResponseEvent response = handler.handleRequest(event, context);
        assertEquals(400, response.getStatusCode());
        assertTrue(response.getBody().contains("Missing company_id"));
    }

    @Test
    public void testItemMissingCompanyIdKey() {
        // Test lines 85-86: item without company_id key is filtered out
        handler = new GetBoycottsByCompanyAndUserHandler(dynamoDb);
        String companyId = "test-company";
        Map<String, String> pathParams = Map.of("company_id", companyId);

        // Create item WITHOUT company_id key
        Map<String, AttributeValue> itemWithoutCompanyId = Map.of(
                "user_id", AttributeValue.fromS("test-user"),
                "timestamp", AttributeValue.fromS("2025-01-01T00:00:00Z")
        );

        QueryResponse queryResponse = QueryResponse.builder()
                .items(List.of(itemWithoutCompanyId))
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
        // Should return boycotting=false since no matching company_id found
        assertTrue(response.getBody().contains("\"boycotting\":false"));
    }

    @Test
    public void testPersonalReasonWhenCauseDescIsEmpty() {
        // Test lines 99, 104-106: use personal_reason when cause_desc is null/empty
        handler = new GetBoycottsByCompanyAndUserHandler(dynamoDb);
        String companyId = "test-company";
        Map<String, String> pathParams = Map.of("company_id", companyId);

        // Create item with empty cause_desc, should use personal_reason
        Map<String, AttributeValue> item = Map.of(
                "user_id", AttributeValue.fromS("test-user"),
                "company_id", AttributeValue.fromS(companyId),
                "company_name", AttributeValue.fromS("Test Corp"),
                "timestamp", AttributeValue.fromS("2025-01-01T00:00:00Z"),
                "cause_id", AttributeValue.fromS(""),
                "cause_desc", AttributeValue.fromS(""),  // Empty string
                "personal_reason", AttributeValue.fromS("My personal reason")
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
        assertTrue(response.getBody().contains("My personal reason"));
        assertTrue(response.getBody().contains("\"personal_reason\":true"));
    }

    @Test
    public void testItemsWithMissingOrEmptyTimestamp() {
        // Test lines 112-113: items without timestamp or with null/empty timestamp are filtered
        handler = new GetBoycottsByCompanyAndUserHandler(dynamoDb);
        String companyId = "test-company";
        Map<String, String> pathParams = Map.of("company_id", companyId);

        // Create item with valid timestamp
        Map<String, AttributeValue> itemWithTimestamp = Map.of(
                "user_id", AttributeValue.fromS("test-user"),
                "company_id", AttributeValue.fromS(companyId),
                "company_name", AttributeValue.fromS("Test Corp"),
                "timestamp", AttributeValue.fromS("2025-01-01T00:00:00Z"),
                "cause_id", AttributeValue.fromS("cause1"),
                "cause_desc", AttributeValue.fromS("Cause 1")
        );

        // Create item WITHOUT timestamp key
        Map<String, AttributeValue> itemWithoutTimestamp = new HashMap<>();
        itemWithoutTimestamp.put("user_id", AttributeValue.fromS("test-user"));
        itemWithoutTimestamp.put("company_id", AttributeValue.fromS(companyId));
        itemWithoutTimestamp.put("cause_desc", AttributeValue.fromS("Cause 2"));

        QueryResponse queryResponse = QueryResponse.builder()
                .items(List.of(itemWithTimestamp, itemWithoutTimestamp))
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
        // Should use the item with timestamp as "earliest"
        assertTrue(response.getBody().contains("2025-01-01T00:00:00Z"));
    }
}
