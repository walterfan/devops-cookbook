package com.github.walterfan.hello;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;

/**
 * Created by yafan on 8/10/2017.
 */
@Controller
public class HelloController {

    // inject via application.properties
    @Value("${hello.message:test}")
    private String message = "Hello World";

    @RequestMapping("/")
    public String index() {
        return "index";
    }

    @RequestMapping("/hello")
    public String welcome(Map<String, Object> model) {
        model.put("message", this.message);
        return "hello";
    }

    @RequestMapping("/sessions")
    protected String updateSession(Map<String, Object> model, HttpServletRequest req, HttpServletResponse resp) {
        String attributeName = req.getParameter("attributeName");
        String attributeValue = req.getParameter("attributeValue");
        req.getSession().setAttribute(attributeName, attributeValue);
        model.put("message", attributeName + "=" + attributeValue);
        return "hello";
    }


}
