package om.bayt.api;
import om.bayt.service.*;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import java.time.*;

@RestController
@RequestMapping("/api")
public class ReportController {
    private final ReportService reports;private final DocumentRenderer documents;private final Clock clock;
    public ReportController(ReportService reports,DocumentRenderer documents,Clock clock){this.reports=reports;this.documents=documents;this.clock=clock;}
    @GetMapping("/dashboard") Object dashboard(@RequestParam(required=false) Long buildingId,@RequestParam(required=false) Long unitId,@RequestParam(required=false) LocalDate from,@RequestParam(required=false) LocalDate to,@RequestParam(required=false) LocalDate asOf){LocalDate today=LocalDate.now(clock);return reports.dashboard(buildingId,unitId,from==null?today.withDayOfMonth(1):from,to==null?today.withDayOfMonth(today.lengthOfMonth()):to,asOf==null?today:asOf);}
    @GetMapping("/reports/finance.csv") ResponseEntity<String> csv(@RequestParam(required=false) Long buildingId,@RequestParam(required=false) Long unitId,@RequestParam(required=false) LocalDate from,@RequestParam(required=false) LocalDate to,@RequestParam(required=false) LocalDate asOf){LocalDate today=LocalDate.now(clock);return ResponseEntity.ok().header("Content-Disposition","attachment; filename=bayt-finance.csv").contentType(MediaType.parseMediaType("text/csv;charset=UTF-8")).body(reports.csv(buildingId,unitId,from==null?today.withDayOfMonth(1):from,to==null?today:to,asOf==null?today:asOf));}
    @GetMapping("/print/{type}/{id}") ResponseEntity<String> print(@PathVariable String type,@PathVariable Long id,@RequestParam(defaultValue="ar") String language){String html=documents.render(type,id,language);return ResponseEntity.ok().header("Content-Disposition","attachment; filename=bayt-"+type+"-"+id+"-"+language+".html").header("Content-Security-Policy","default-src 'none'; style-src 'unsafe-inline'; sandbox").contentType(MediaType.parseMediaType("text/html;charset=UTF-8")).body(html);}
}
