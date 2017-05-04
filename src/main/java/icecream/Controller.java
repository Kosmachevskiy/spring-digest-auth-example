package icecream;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Created by kosmachevskiy on 04.05.17.
 */

@RestController
public class Controller {

    @GetMapping("/")
    public String privateData(){
        return "Secret!";
    }

    @GetMapping("/public")
    public String publicData(){
        return "Hello!";
    }
}
