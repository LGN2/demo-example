package om.bayt.api;

import java.nio.charset.StandardCharsets;
import java.util.*;
import om.bayt.service.FileVault;
import org.springframework.core.io.Resource;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/documents")
public class FileController {
  private final FileVault vault;

  public FileController(FileVault vault) {
    this.vault = vault;
  }

  @GetMapping
  Object list(
      @RequestParam(required = false) Long buildingId,
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "20") int size,
      @RequestParam(defaultValue = "") String q) {
    return PageSlice.of(vault.list(buildingId), page, size, q, d -> d.filename + " " + d.kind);
  }

  @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  Object upload(@RequestParam Map<String, String> fields, @RequestParam("file") MultipartFile file)
      throws java.io.IOException {
    Map<String, Object> data = new HashMap<>(fields);
    return vault.upload(new Input(data), file);
  }

  @GetMapping("/{id}/download")
  ResponseEntity<Resource> download(@PathVariable Long id) {
    var d = vault.get(id);
    return ResponseEntity.ok()
        .contentType(MediaType.parseMediaType(d.contentType))
        .header(
            "Content-Disposition",
            ContentDisposition.attachment()
                .filename(d.filename, StandardCharsets.UTF_8)
                .build()
                .toString())
        .header("Cache-Control", "no-store")
        .header("Content-Security-Policy", "default-src 'none'; sandbox")
        .body(vault.download(id));
  }
}
