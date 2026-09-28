package com.codevictims.propertymanagement.account.mapper;
import com.codevictims.propertymanagement.account.entity.UserAccount;
import com.codevictims.propertymanagement.account.dto.response.UserAccountResponse;
public final class UserAccountMapper {
  private UserAccountMapper() {}
  public static UserAccountResponse toResponse(UserAccount entity) {
    if (entity == null) return null;
    return new UserAccountResponse(entity.id, entity.version, entity.createdAt, entity.username, entity.displayName, entity.role, entity.ownerId, entity.active, entity.taxRegistered);
  }
}
