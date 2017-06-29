package pothi_discord.rest;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Created by Pascal Pothmann on 29.06.2017.
 */
@RestController
public class HelloController {

    @RequestMapping("/")
    public String index(){
        return "Hi";
    }
}
