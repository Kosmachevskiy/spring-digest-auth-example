package icecream;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.util.DigestUtils.md5DigestAsHex;

/**
 * Created by kosmachevskiy on 05.05.17.
 */
@SpringBootTest(classes = {DigestTestApplication.class})
@RunWith(SpringRunner.class)
@AutoConfigureMockMvc
public class SecurityTest {

    @Autowired
    MockMvc mockMvc;

    @Before
    public void setUp() throws Exception {
        Assert.assertNotNull(mockMvc);
    }

    @Test
    public void publicContentShouldBeAvailable() throws Exception {

        mockMvc.perform(get("/public"))
                .andExpect(status().isOk())
                .andExpect(content().string("Hello!"))
                .andDo(print());
    }

    @Test
    public void privateContentAccessDenied() throws Exception {

        mockMvc.perform(get("/secret"))
                .andExpect(status().isUnauthorized())
                .andExpect(status().reason("Full authentication is required to access this resource"))
                .andDo(print())
                .andReturn();
    }

    @Test
    public void accessToPrivateContent() throws Exception {
        final String USER_NAME = "user";
        final String PASSWORD = "pwd";
        final String REALM = "REALM";
        final String HTTP_METHOD = "GET";
        final String URL = "/secret";
        final String nonce;

        // Empty request to get nonce //
        MvcResult result = mockMvc.perform(get(URL))
                .andExpect(status().isUnauthorized())
                .andReturn();

        nonce = parseNonce(result);

        // Calculate response value //
        String ha1 = md5DigestAsHex((USER_NAME + ":" + REALM + ":" + PASSWORD).getBytes());
        String ha2 = md5DigestAsHex((HTTP_METHOD + ":" + URL).getBytes());

        final String response = md5DigestAsHex((ha1 + ":" + nonce + ":" + ha2).getBytes());

        // Build header //
        String headerTemplate =
                "Digest username=\"%s\", realm=\"%s\", nonce=\"%s\", uri=\"%s\", response=\"%s\" ";
        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", String.format(headerTemplate, USER_NAME, REALM, nonce, URL, response));

        mockMvc.perform(get(URL).headers(headers))
                .andExpect(status().isOk())
                .andExpect(content().string("Secret!"));
    }

    private String parseNonce(MvcResult result) {
        String header = result.getResponse().getHeader("WWW-Authenticate");

        String key = "nonce=\"";
        int nonceStartIndex = header.indexOf(key) + key.length();
        int nonceEndIndex = header.indexOf("\"", nonceStartIndex);

        return header.substring(nonceStartIndex, nonceEndIndex);
    }
}