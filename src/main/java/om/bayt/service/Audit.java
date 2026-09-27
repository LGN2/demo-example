package om.bayt.service;
import om.bayt.domain.AuditEvent;
import om.bayt.security.Access;
import org.springframework.stereotype.Service;
@Service
public class Audit {
    private final Store db; private final Access access;
    public Audit(Store db, Access access) { this.db=db; this.access=access; }
    public void add(Long building, String type, Long id, String action, String note) {
        AuditEvent e=new AuditEvent(); e.buildingId=building; e.resourceType=type; e.resourceId=id;
        e.actorId=access.user().id; e.action=action; e.note=note; db.save(e);
    }
}
