package om.bayt.service;
import om.bayt.domain.*;
import org.springframework.boot.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@Profile("!dev")
public class BootstrapAdmin implements ApplicationRunner {
    private final UserRepository users;private final PasswordEncoder passwords;private final String username,password;
    public BootstrapAdmin(UserRepository users,PasswordEncoder passwords,@Value("${BOOTSTRAP_ADMIN_USERNAME:}") String username,@Value("${BOOTSTRAP_ADMIN_PASSWORD:}") String password){this.users=users;this.passwords=passwords;this.username=username;this.password=password;}
    @Override @Transactional public void run(ApplicationArguments args){
        if(users.count()>0||username.isBlank())return;
        if(password.length()<12||password.length()>72)throw new IllegalStateException("Bootstrap password must have 12–72 characters");
        UserAccount u=new UserAccount();u.username=username;u.displayName="Platform administrator";u.role="PLATFORM_ADMIN";u.active=true;u.passwordHash=passwords.encode(password);users.save(u);
    }
}
