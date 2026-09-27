package om.bayt.api;
import om.bayt.service.*;
import om.bayt.security.Access;
import org.springframework.web.bind.annotation.*;
import java.util.*;

@RestController
@RequestMapping("/api/maintenance")
public class MaintenanceController {
    private final MaintenanceService s;private final Access access;private final AiAssistant ai;
    public MaintenanceController(MaintenanceService s,Access access,AiAssistant ai){this.s=s;this.access=access;this.ai=ai;}
    @GetMapping Object list(@RequestParam(required=false) Long buildingId,@RequestParam(defaultValue="0") int page,@RequestParam(defaultValue="20") int size,@RequestParam(defaultValue="") String q,@RequestParam(defaultValue="") String status){return PageSlice.of(s.list(buildingId).stream().filter(m->status.isBlank()||m.status.equals(status)).toList(),page,size,q,m->m.description+" "+m.category+" "+m.status);}
    @GetMapping("/{id}") Object get(@PathVariable Long id){return access.maintenance(id);}
    @PostMapping Object create(@RequestBody Map<String,Object> b){return s.create(new Input(b));}
    @PostMapping("/{id}/status") Object transition(@PathVariable Long id,@RequestBody Map<String,Object> b){return s.transition(id,new Input(b));}
    @PostMapping("/{id}/comments") void comment(@PathVariable Long id,@RequestBody Map<String,Object> b){s.comment(id,new Input(b));}
    @PostMapping("/{id}/approve-summary") Object approve(@PathVariable Long id,@RequestBody Map<String,Object> b){return s.approve(id,new Input(b));}
    @PostMapping("/{id}/ai-suggestion") Object suggest(@PathVariable Long id,@RequestBody Map<String,Object> b){
        var m=access.maintenance(id);access.manage(m.buildingId);String language=new Input(b).choice("language","ar","en");
        // The request is already committed; provider I/O never shares its creation transaction.
        return s.aiResult(id,ai.suggest(m.description,language));
    }
}
