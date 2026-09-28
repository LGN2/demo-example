package com.codevictims.propertymanagement.common.controller;

import com.codevictims.propertymanagement.common.dto.PageSlice;
import com.codevictims.propertymanagement.common.dto.request.DocumentUploadRequest;
import com.codevictims.propertymanagement.common.dto.response.DocumentResponse;
import com.codevictims.propertymanagement.common.mapper.DocumentMapper;
import com.codevictims.propertymanagement.common.mapper.RequestMapper;
import com.codevictims.propertymanagement.common.service.FileVault;
import java.nio.charset.StandardCharsets;
import java.util.*;
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
  PageSlice<DocumentResponse> list(
      @RequestParam(required = false) Long buildingId,
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "20") int size,
      @RequestParam(defaultValue = "") String q) {
    return PageSlice.of(vault.list(buildingId), page, size, q, d -> d.filename + " " + d.kind)
        .map(DocumentMapper::toResponse);
  }

  @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  DocumentResponse upload(
      @jakarta.validation.Valid @ModelAttribute DocumentUploadRequest fields,
      @RequestParam("file") MultipartFile file)
      throws java.io.IOException {
    return DocumentMapper.toResponse(vault.upload(RequestMapper.toInput(fields), file));
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
