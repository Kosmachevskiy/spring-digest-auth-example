package icecream;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;

/**
 * Created by kosmachevskiy on 04.05.17.
 */

@RestController
public class Controller {

    @GetMapping("/secret")
    public String privateData(HttpServletRequest request) {
        return "Secret!";
    }

    @GetMapping("/public")
    public String publicData() {
        return "Hello!";
    }
}
