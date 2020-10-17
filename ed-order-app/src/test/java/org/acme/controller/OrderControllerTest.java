package org.acme.controller;

import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.keycloak.representations.AccessTokenResponse;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.emptyOrNullString;
import static org.hamcrest.Matchers.emptyString;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;

import java.util.UUID;

import org.acme.dto.OrderDTO;
import org.acme.dto.OrderRequestDTO;

import static org.hamcrest.Matchers.containsString;

import org.apache.http.HttpStatus;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import static io.restassured.RestAssured.given;


import io.quarkus.test.junit.QuarkusTest;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;

@QuarkusTest
@TestMethodOrder(OrderAnnotation.class)
class OrderControllerTest {
	
	static String adminToken;
	
	@ConfigProperty(name = "quarkus.oidc.auth-server-url")
	String serverUrl;
	
	@ConfigProperty(name = "quarkus.oidc.client-id")
	String clientId;
	
	@ConfigProperty(name = "quarkus.oidc.credentials.secret")
	String secret;
	
	@ConfigProperty(name = "user1.name")
	String adminUserName;
	
	@ConfigProperty(name = "user1.password")
	String adminPassword;
	
	@ConfigProperty(name = "user2.name")
	String standardUserName;
	
	@ConfigProperty(name = "user2.password")
	String standardUserPassword;

	static String idToGet;

	static OrderDTO responseDTO;

	static OrderRequestDTO requestDTO=new OrderRequestDTO("CONFIRMED", 54.99);
		
	static String standardUserToken;
	
	static OrderDTO editDTO;
	
	@Test
	@Order(0)
	void admin_should_retrieve_token() {
		
		adminToken=getAccessToken(adminUserName,adminPassword);

		assertThat(adminToken, is(not(emptyOrNullString())));
		
	}
	@Test
	@Order(0)
	void user_should_retrieve_token() {
		
		standardUserToken=getAccessToken(standardUserName,standardUserPassword);

		assertThat(standardUserToken, is(not(emptyOrNullString())));
		
	}
	
	@Test
	@Order(1)
	void admin_should_retrieve_404_when_order_list_is_empty() {
		
		given()
			.auth().oauth2(adminToken)
	        .contentType(ContentType.JSON)
	        .when()
	        .get("/orders")
	        .then()
	        	.statusCode(HttpStatus.SC_NOT_FOUND)
	        	;
		
	}

	@Test
	@Order(1)
	void user_should_be_blocked_when_add_orders() {
		
			given()
				.auth().oauth2(standardUserToken)
		        .contentType(ContentType.JSON)
		        .when()
		        .post("/orders")
		        .then()
		        	.statusCode(HttpStatus.SC_FORBIDDEN)
		        	
		        	;
	}
	
	@Test
	@Order(2)
	void admin_should_retrieve_400_when_request_body_is_empty() {
		
			given()
				.auth().oauth2(adminToken)
		        .contentType(ContentType.JSON)
		        .when()
		        .post("/orders")
		        .then()
		        	.statusCode(HttpStatus.SC_BAD_REQUEST)
		        	;
	}
	
	@Test
	@Order(2)
	void admin_should_be_able_to_add_order() {
			given()
				.auth().oauth2(adminToken)
		        .contentType(ContentType.JSON)
		        .when().body(requestDTO)
		        .post("/orders")
		        .then()
		        	.statusCode(HttpStatus.SC_CREATED)
		        	.assertThat().body("id", is(not(emptyOrNullString())))
		        	.assertThat().body("status", is(not(emptyOrNullString())))
		        	.assertThat().body("totalAmount", is(notNullValue()))
		        	;
	}
	@Test
	@Order(3)
	void user_should_be_able_to_list_orders() {
				
			responseDTO=
			given()
				.auth().oauth2(standardUserToken)
		        .contentType(ContentType.JSON)
		        .when()
		        .get("/orders")
		        .then()
		        	.statusCode(HttpStatus.SC_OK)
		        	.assertThat().body("id[0]", is(not(emptyOrNullString())))
		        	.assertThat().body("status[0]", is(not(emptyOrNullString())))
		        	.assertThat().body("status[0]", is(requestDTO.getStatus()))
		        	.assertThat().body("totalAmount[0]", is(notNullValue()))
		        	.extract().as(OrderDTO[].class)[0]
		        	;

	}
	@Test
	@Order(4)
	void user_should_be_blocked_when_edit_order() {
		
		editDTO=new OrderDTO(responseDTO.getId(), "SHIPPING", responseDTO.getTotalAmount());
		
		given()
			.auth().oauth2(standardUserToken)
	        .contentType(ContentType.JSON)
	        .when().body(editDTO)
	        .patch("/orders")
	        .then()
	        	.statusCode(HttpStatus.SC_FORBIDDEN)
	        	
	        	;

	}
	@Test
	@Order(4)
	void admin_should_receive_400_when_order_is_not_valid() {
		
		given()
			.auth().oauth2(adminToken)
	        .contentType(ContentType.JSON)
	        .when().body(new OrderDTO())
	        .patch("/orders")
	        .then()
	        	.statusCode(HttpStatus.SC_BAD_REQUEST)
	        	
	        	;

	}
	@Test
	@Order(4)
	void admin_should_receive_304_when_request_has_same_value_of_original_order() {
		
		given()
			.auth().oauth2(adminToken)
	        .contentType(ContentType.JSON)
	        .when().body(responseDTO)
	        .patch("/orders")
	        .then()
	        	.statusCode(HttpStatus.SC_NOT_MODIFIED)
	        	
	        	;

	}
	@Test
	@Order(4)
	void admin_should_retrieve_404_when_edit_unexistant_order() {
		
		given()
			.auth().oauth2(adminToken)
	        .contentType(ContentType.JSON)
	        .when().body(new OrderDTO(UUID.randomUUID().toString(), responseDTO.getStatus(), responseDTO.getTotalAmount()))
	        .patch("/orders")
	        .then()
	        	.statusCode(HttpStatus.SC_NOT_FOUND)
	        	
	        	;
	}
	@Test
	@Order(5)
	void admin_should_edit_order() {
		
		given()
			.auth().oauth2(adminToken)
	        .contentType(ContentType.JSON)
	        .when().body(editDTO)
	        .patch("/orders")
	        .then()
	        	.statusCode(HttpStatus.SC_OK)
	        	.assertThat().body("id", is(not(emptyOrNullString())))
	        	.assertThat().body("status", is(not(emptyOrNullString())))
	        	.assertThat().body("status", is(editDTO.getStatus()))
	        	.assertThat().body("totalAmount", is(notNullValue()))
	        	
	        	;
	}
	@Test
	@Order(6)
	void user_should_be_blocked_when_delete_order() {
		
		given()
			.auth().oauth2(standardUserToken)
	        .contentType(ContentType.TEXT)
	        .when().pathParam("id", responseDTO.getId())
	        .delete("/orders/{id}")
	        .then()
	        	.statusCode(HttpStatus.SC_FORBIDDEN)
	        	
	        	;
	}
	@Test
	@Order(6)
	void admin_should_retrieve_404_when_delete_unexistant_order() {
		
		given()
			.auth().oauth2(adminToken)
	        .contentType(ContentType.TEXT)
	        .when().pathParam("id", UUID.randomUUID().toString())
	        .delete("/orders/{id}")
	        .then()
	        	.statusCode(HttpStatus.SC_NOT_FOUND)
	        	
	        	;
	}
	@Test
	@Order(6)
	void admin_should_retrieve_400_when_delete_without_id() {
		
		given()
			.auth().oauth2(adminToken)
	        .contentType(ContentType.TEXT)
	        .when().pathParam("id", " ")
	        .delete("/orders/{id}")
	        .then()
	        	.statusCode(HttpStatus.SC_BAD_REQUEST)
	        	
	        	;
	}
	@Test
	@Order(7)
	void admin_should_be_able_to_delete_order() {

		given()
			.auth().oauth2(adminToken)
	        .contentType(ContentType.TEXT)
	        .when()
	        .pathParam("id",responseDTO.getId() )
	        .delete("/orders/{id}")
	        .then()
	        	.statusCode(HttpStatus.SC_OK)
	        	.assertThat().body("id", is(not(emptyOrNullString())))
	        	.assertThat().body("status", is(not(emptyOrNullString())))
	        	.assertThat().body("status", is("DELETED"))
	        	.assertThat().body("totalAmount", is(notNullValue()))
	       
	        	;

	}
	@Test
	@Order(8)
	void user_should_retrieve_404_when_order_list_is_empty() {

		given()
			.auth().oauth2(standardUserToken)
	        .contentType(ContentType.JSON)
	        .when()
	        .get("/orders")
	        .then()
	        	.statusCode(HttpStatus.SC_NOT_FOUND)
	       
	        	;
	}
	@Test
	@Order(8)
	void user_should_retrieve_404_when_get_unexistant_order() {

		given()
			.auth().oauth2(standardUserToken)
	        .contentType(ContentType.TEXT)
	        .when().pathParam("id", responseDTO.getId())
	        .get("/orders/{id}")
	        .then()
	        	.statusCode(HttpStatus.SC_NOT_FOUND)
	       
	        	;
	}
	@Test
	@Order(9)
	void admin_should_save_order_and_retrieve_it() {

		idToGet=given()
		.auth().oauth2(adminToken)
        .contentType(ContentType.JSON)
        .when().body(new OrderRequestDTO(responseDTO.getStatus(), responseDTO.getTotalAmount()))
        .post("/orders")
        .then()
        	.statusCode(HttpStatus.SC_CREATED)
        	.assertThat().body("id", is(not(emptyOrNullString())))
        	.assertThat().body("status", is(not(emptyOrNullString())))
        	.assertThat().body("totalAmount", is(notNullValue()))
        .extract().path("id")
        	;
		
		given()
			.auth().oauth2(adminToken)
	        .contentType(ContentType.TEXT)
	        .when().pathParam("id", idToGet)
	        .get("/orders/{id}")
	        .then()
		        .statusCode(HttpStatus.SC_OK)
	        	.assertThat().body("id", is(not(emptyOrNullString())))
	        	.assertThat().body("id", is(idToGet))
	        	.assertThat().body("status", is(not(emptyOrNullString())))
	        	.assertThat().body("totalAmount", is(notNullValue()))
	       
	        	;
	}
	@Test
	@Order(10)
	void user_should_retrieve_existing_order() {
		
		given()
			.auth().oauth2(standardUserToken)
	        .contentType(ContentType.TEXT)
	        .when().pathParam("id", idToGet)
	        .get("/orders/{id}")
	        .then()
		        .statusCode(HttpStatus.SC_OK)
	        	.assertThat().body("id", is(not(emptyOrNullString())))
	        	.assertThat().body("status", is(not(emptyOrNullString())))
	        	.assertThat().body("totalAmount", is(notNullValue()))
	       
	        	;
	}
	@Test
	@Order(10)
	void user_should_retrieve_400_when_order_id_is_malformed() {
		
		given()
			.auth().oauth2(standardUserToken)
	        .contentType(ContentType.TEXT)
	        .when().pathParam("id", " ")
	        .get("/orders/{id}")
	        .then()
		        .statusCode(HttpStatus.SC_BAD_REQUEST)
	        		       
	        	;
	}
	@Test
	@Order(10)
	void unauthorized_should_retrieve_401_when_retrieve_order() {
		
		given()
		    .contentType(ContentType.TEXT)
		    .when().pathParam("id", responseDTO.getId())
		    .get("/orders/{id}")
		    .then()
		        .statusCode(HttpStatus.SC_UNAUTHORIZED)
        		       
        	;
	}
	
	String getAccessToken(String userName,String password) {
		return RestAssured
            .given()
	            .param("grant_type", "password")
	            .param("username", userName)
	            .param("password", password)
	            .param("client_id", "backend-service")
	            .param("client_secret", secret)
            .when()
            .post(serverUrl+"protocol/openid-connect/token")
            .as(AccessTokenResponse.class).getToken();
    }
}
