package om.bayt.api;
import om.bayt.service.OperationsService;
import org.springframework.web.bind.annotation.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.*;

@RestController
@RequestMapping("/api")
public class OperationsController {
    private final OperationsService s;private final ObjectMapper json;
    public OperationsController(OperationsService s,ObjectMapper json){this.s=s;this.json=json;}
    @GetMapping("/operations/{type}") Object list(@PathVariable String type,@RequestParam(required=false) Long buildingId,@RequestParam(defaultValue="0") int page,@RequestParam(defaultValue="20") int size,@RequestParam(defaultValue="") String q){return PageSlice.of(s.list(type,buildingId),page,size,q,r->{try{return json.writeValueAsString(r);}catch(Exception e){return "";}});}
    @GetMapping("/operations/{type}/{id}") Object get(@PathVariable String type,@PathVariable Long id){return s.get(type,id);}
    @PostMapping("/operations/{type}") Object create(@PathVariable String type,@RequestBody Map<String,Object> b){return s.save(type,null,new Input(b));}
    @PutMapping("/operations/{type}/{id}") Object update(@PathVariable String type,@PathVariable Long id,@RequestBody Map<String,Object> b){return s.save(type,id,new Input(b));}
    @PostMapping("/operations/visits/{id}/checkout") Object checkout(@PathVariable Long id){return s.checkout(id);}
    @PostMapping("/operations/preventive/{id}/complete") Object complete(@PathVariable Long id){return s.complete(id);}
    @GetMapping("/operations/meters/{id}/readings") Object readings(@PathVariable Long id){return s.readings(id);}
    @PostMapping("/operations/meters/{id}/readings") Object reading(@PathVariable Long id,@RequestBody Map<String,Object> b){return s.reading(id,new Input(b));}
    @GetMapping("/vendor-performance") Object performance(@RequestParam(required=false) Long buildingId){return s.vendorPerformance(buildingId);}
    @GetMapping("/assignees") Object assignees(@RequestParam Long buildingId){return s.assignees(buildingId).stream().map(u->Map.of("id",u.id,"displayName",u.displayName,"role",u.role)).toList();}
}
