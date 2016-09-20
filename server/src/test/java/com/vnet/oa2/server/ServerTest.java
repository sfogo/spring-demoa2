package com.vnet.oa2.server;

import org.codehaus.jackson.map.ObjectMapper;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Base64;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment= SpringBootTest.WebEnvironment.RANDOM_PORT)
@RunWith(SpringRunner.class)
@ContextConfiguration(classes = Application.class)
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class ServerTest {
    @Autowired
    private TestRestTemplate restTemplate;

    private String getBasicAuthorizationHeader(String id, String secret) {
        return "Basic " + Base64.getEncoder().encodeToString((id+":"+secret).getBytes());
    }

    static private String tokenScopeA;

    @Test
    public void test01_OwnerPasswordCredentialsGrant() throws Exception {
        final String url = "/oauth/token";
        final String data = "grant_type=password" +
                "&username=user4" +
                "&password=password4"  +
                "&scope=A";

        final HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        headers.set("authorization", getBasicAuthorizationHeader("client1","P@55w0rd1"));

        final HttpEntity<String> entity = new HttpEntity<>(data, headers);
        final ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, entity, String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        final Map map = new ObjectMapper().readValue(response.getBody(), Map.class);
        assertThat(map.get("token_type")).isEqualTo("bearer");
        assertThat(map.get("access_token")).isNotNull();
        assertThat(map.get("refresh_token")).isNotNull();
        assertThat(map.get("scope")).isEqualTo("A");
        tokenScopeA = map.get("access_token").toString();
    }

    @Test
    public void test02_getResourceA() throws Exception {
        final HttpHeaders headers = new HttpHeaders();
        headers.set("authorization", "Bearer " + tokenScopeA);
        final HttpEntity<String> entity = new HttpEntity<>(null, headers);
        final ResponseEntity<String> response = restTemplate.exchange("/things/A/123", HttpMethod.GET,entity,String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        final Map map = new ObjectMapper().readValue(response.getBody(), Map.class);
        assertThat(map.get("requestedBy")).isEqualTo("user4");
    }

    @Test
    public void test03_getResourceB() throws Exception {
        final HttpHeaders headers = new HttpHeaders();
        headers.set("authorization", "Bearer " + tokenScopeA);
        final HttpEntity<String> entity = new HttpEntity<>(null, headers);
        final ResponseEntity<String> response = restTemplate.exchange("/things/B/456", HttpMethod.GET,entity,String.class);
        System.out.println(response.getBody());
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        final Map map = new ObjectMapper().readValue(response.getBody(), Map.class);
        assertThat(map.get("error")).isEqualTo("insufficient_scope");
    }
}
