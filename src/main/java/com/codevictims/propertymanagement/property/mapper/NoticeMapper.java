package com.codevictims.propertymanagement.property.mapper;
import com.codevictims.propertymanagement.property.entity.Notice;
import com.codevictims.propertymanagement.property.dto.response.NoticeResponse;
public final class NoticeMapper {
  private NoticeMapper() {}
  public static NoticeResponse toResponse(Notice entity) {
    if (entity == null) return null;
    return new NoticeResponse(entity.id, entity.version, entity.createdAt, entity.buildingId, entity.titleAr, entity.titleEn, entity.bodyAr, entity.bodyEn);
  }
}
