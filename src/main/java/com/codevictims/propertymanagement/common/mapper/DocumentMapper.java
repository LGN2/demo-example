package com.codevictims.propertymanagement.common.mapper;
import com.codevictims.propertymanagement.common.entity.Document;
import com.codevictims.propertymanagement.common.dto.response.DocumentResponse;
public final class DocumentMapper {
  private DocumentMapper() {}
  public static DocumentResponse toResponse(Document entity) {
    if (entity == null) return null;
    return new DocumentResponse(entity.id, entity.version, entity.createdAt, entity.buildingId, entity.unitId, entity.tenantId, entity.maintenanceId, entity.readingId, entity.kind, entity.filename, entity.contentType, entity.sizeBytes, entity.scanStatus, entity.expiryDate);
  }
}
