import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.context.WebApplicationContext;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import javax.inject.Inject;

/**
 * Created by walter on 07/11/2016.
 */

@WebAppConfiguration
@ContextConfiguration(classes = { MessageTestConfig.class })
public class MessageControllerTest  extends AbstractTestNGSpringContextTests {
    @Inject
    protected WebApplicationContext wac;


    protected MockMvc mockMvc;


    @BeforeMethod
    public void setup() {



    }

    @Test
    public void testCreateMessage() throws Exception {

    }
}
