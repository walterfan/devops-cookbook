import com.github.walterfan.guestbook.dao.MessageDao;
import com.github.walterfan.guestbook.service.MessageService;
import org.mockito.Mockito;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

/**
 * Created by walter on 15/11/2016.
 */
@Configuration
@EnableWebMvc
public class MessageTestConfig {

    @Bean
    public MessageService messageService() {
        return Mockito.mock(MessageService.class, Mockito.withSettings().defaultAnswer(Mockito.RETURNS_SMART_NULLS));
    }


    @Bean
    public MessageDao messageDao() {
        return Mockito.mock(MessageDao.class, Mockito.withSettings().defaultAnswer(Mockito.RETURNS_SMART_NULLS));
    }
}
