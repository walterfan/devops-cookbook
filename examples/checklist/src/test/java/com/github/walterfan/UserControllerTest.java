package com.github.walterfan;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.walterfan.checklist.controller.UserController;
import com.github.walterfan.checklist.dto.Registration;
import com.github.walterfan.checklist.service.UserService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import javax.annotation.Resource;

import static com.github.walterfan.ChecklistTestUtil.createRegistration;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Created by walterfan on 27/2/2017.
 */

@RunWith(SpringRunner.class)
@WebMvcTest(UserController.class)
@ContextConfiguration(classes = {ChecklistTestConfig.class})
public class UserControllerTest  {

    @Autowired
    private MockMvc mockMvc;

    @InjectMocks
    @Resource(name ="userService")
    private UserService userService;


    @Autowired
    private ObjectMapper objectMapper;

    @Test
    public void testRegister404() throws Exception {

        MockHttpServletRequestBuilder request = post("/users");
        request.accept(MediaType.APPLICATION_JSON_UTF8);
        request.contentType(MediaType.APPLICATION_JSON_UTF8);


        mockMvc.perform(request)
                .andDo(print())
                .andExpect(status().is(404));
    }

    @Test
    public void testRegister200() throws Exception {
        Registration registration = createRegistration();

        byte[] jsonBytes = objectMapper.writeValueAsBytes(registration);
        MvcResult mvcResult = mockMvc.perform(
                post("/checklist/api/v1/users/register")
                        .contentType(MediaType.APPLICATION_JSON_UTF8_VALUE)
                        .content(jsonBytes)
                        .accept(MediaType.APPLICATION_JSON_UTF8))
                .andDo(print())
                .andExpect(status().is(200)).andReturn();

        String content = mvcResult.getResponse().getContentAsString();
    }


}
