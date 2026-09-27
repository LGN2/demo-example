package om.bayt.service;
import om.bayt.api.*;
import om.bayt.domain.*;
import om.bayt.security.Access;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.*;

@Service
@Transactional
public class AccountService {
    private final Store db; private final Access access; private final PasswordEncoder passwords; private final Audit audit;
    public AccountService(Store db, Access access, PasswordEncoder passwords, Audit audit) {this.db=db;this.access=access;this.passwords=passwords;this.audit=audit;}
    public List<UserAccount> list() {
        access.role("OWNER","PLATFORM_ADMIN");
        if (access.user().role.equals("PLATFORM_ADMIN")) return db.list(UserAccount.class,"select u from UserAccount u where u.role in ('OWNER','PLATFORM_ADMIN') order by u.id");
        return db.list(UserAccount.class,"select u from UserAccount u where u.ownerId=:id order by u.id","id",access.user().id);
    }
    public UserAccount create(Input in) {
        access.role("OWNER","PLATFORM_ADMIN");
        String role=access.user().role.equals("OWNER")?in.choice("role","MANAGER","TENANT","VENDOR","GUARD"):in.choice("role","OWNER","PLATFORM_ADMIN");
        String password=in.text("password",72);
        if(password.length()<12) throw ApiException.invalid("WEAK_PASSWORD");
        UserAccount u=new UserAccount(); u.username=in.text("username",100).toLowerCase(Locale.ROOT);
        if(!u.username.matches("[a-z0-9@._+-]{3,100}"))throw ApiException.invalid("INVALID_INPUT");
        u.displayName=in.text("displayName",255);u.role=role;u.passwordHash=passwords.encode(password);u.active=true;
        if(access.user().role.equals("OWNER"))u.ownerId=access.user().id;
        db.save(u); if(role.equals("OWNER"))u.ownerId=u.id;
        audit.add(null,"ACCOUNT",u.id,"CREATED",role);return u;
    }
    public BuildingAccess assign(Input in) {
        Long building=in.id("buildingId");access.owner(building);
        UserAccount u=access.portfolioUser(in.id("userId"),building,"MANAGER","GUARD");
        var list=db.list(BuildingAccess.class,"select a from BuildingAccess a where a.buildingId=:b and a.userId=:u","b",building,"u",u.id);
        BuildingAccess a=list.isEmpty()?new BuildingAccess():list.get(0); a.buildingId=building;a.userId=u.id;a.canWrite=u.role.equals("MANAGER")&&in.bool("canWrite");
        db.save(a);audit.add(building,"ACCESS",a.id,"ASSIGNED",u.role);return a;
    }
    public List<BuildingAccess> grants(Long building) {access.owner(building);return db.list(BuildingAccess.class,"select a from BuildingAccess a where a.buildingId=:b","b",building);}
    public void revoke(Long id) {var a=db.get(BuildingAccess.class,id);access.owner(a.buildingId);audit.add(a.buildingId,"ACCESS",id,"REVOKED","");db.remove(a);}
    public void password(Input in) {
        var u=access.user();if(!passwords.matches(in.text("currentPassword",72),u.passwordHash))throw ApiException.forbidden();
        String p=in.text("newPassword",72);if(p.length()<12)throw ApiException.invalid("WEAK_PASSWORD");u.passwordHash=passwords.encode(p);
        audit.add(null,"ACCOUNT",u.id,"PASSWORD_CHANGED","");
    }
    public UserAccount tax(Input in) {access.role("OWNER");var u=access.user();u.taxRegistered=in.bool("taxRegistered");audit.add(null,"ACCOUNT",u.id,"TAX_STATUS_CHANGED",Boolean.toString(u.taxRegistered));return u;}
}
