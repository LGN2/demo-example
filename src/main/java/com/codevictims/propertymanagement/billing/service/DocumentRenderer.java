package com.codevictims.propertymanagement.billing.service;

import com.codevictims.propertymanagement.account.entity.UserAccount;
import com.codevictims.propertymanagement.billing.entity.DepositEntry;
import com.codevictims.propertymanagement.billing.entity.Due;
import com.codevictims.propertymanagement.billing.entity.Payment;
import com.codevictims.propertymanagement.common.exception.ApiException;
import com.codevictims.propertymanagement.common.repository.Store;
import com.codevictims.propertymanagement.property.entity.Building;
import com.codevictims.propertymanagement.property.entity.Unit;
import com.codevictims.propertymanagement.security.service.Access;
import com.codevictims.propertymanagement.tenancy.entity.Lease;
import com.codevictims.propertymanagement.tenancy.entity.Tenant;

import java.time.*;
import java.util.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class DocumentRenderer {
  private final Store db;
  private final Access access;
  private final FinanceService finance;
  private final Clock clock;

  public DocumentRenderer(Store db, Access access, FinanceService finance, Clock clock) {
    this.db = db;
    this.access = access;
    this.finance = finance;
    this.clock = clock;
  }

  public String render(String type, Long id, String language) {
    if (!Set.of("ar", "en").contains(language)) throw ApiException.invalid("INVALID_INPUT");
    boolean ar = language.equals("ar");
    String title, body;
    if (type.equals("lease")) {
      Lease l = access.lease(id, false);
      Tenant t = db.get(Tenant.class, l.tenantId);
      Unit u = db.get(Unit.class, l.unitId);
      Building b = db.get(Building.class, l.buildingId);
      title = ar ? "مسودة عقد إيجار" : "Draft lease agreement";
      body =
          "<p class=warning>"
              + (ar
                  ? "مسودة للمراجعة. ليست عقداً موقعاً أو إثبات تسجيل بلدي. يجب مراجعة الشروط"
                        + " القانونية قبل التوقيع."
                  : "Draft for review. This is not a signed agreement or proof of municipality"
                        + " registration. Obtain appropriate legal review before signing.")
              + "</p>"
              + table(
                  List.of(
                      pair(
                          ar ? "المالك" : "Owner",
                          db.get(UserAccount.class, b.ownerId).displayName),
                      pair(ar ? "المستأجر" : "Tenant", t.name),
                      pair(ar ? "المبنى / الوحدة" : "Building / unit", b.name + " / " + u.code),
                      pair(ar ? "العنوان" : "Address", b.address),
                      pair(ar ? "الفترة" : "Term", l.startDate + " — " + l.endDate),
                      pair(
                          ar ? "الإيجار الشهري، ر.ع." : "Monthly rent, OMR",
                          l.rent.toPlainString()),
                      pair(
                          ar ? "التأمين، ر.ع." : "Security deposit, OMR",
                          l.deposit.toPlainString()),
                      pair(
                          ar ? "المعاملة الضريبية" : "Tax treatment",
                          l.taxTreatment + " / " + l.taxRate),
                      pair(
                          ar ? "البلدية" : "Municipality",
                          l.municipalityStatus + " / " + l.municipalityReference)))
              + "<p>"
              + (ar
                  ? "إيجار شهري ثابت. تُسجل دفعات الإيجار والتأمين بشكل منفصل. الشروط الإضافية"
                        + " والتوقيعات تُستكمل بعد المراجعة."
                  : "Fixed monthly rent. Rent and security-deposit payments are recorded"
                        + " separately. Additional terms and signatures must be completed after"
                        + " review.")
              + "</p><p>"
              + (ar
                  ? "توقيع المالك: __________ توقيع المستأجر: __________"
                  : "Owner signature: __________ Tenant signature: __________")
              + "</p>";
    } else if (type.equals("statement")) {
      Lease l = access.lease(id, false);
      title = ar ? "كشف حساب المستأجر" : "Tenant statement";
      var rows = new ArrayList<List<String>>();
      rows.add(pair(ar ? "العقد" : "Lease", l.id.toString()));
      for (var d : finance.dues(id, LocalDate.now(clock)))
        rows.add(
            pair(
                d.dueDate().toString(),
                "OMR "
                    + d.amount()
                    + " / "
                    + (ar ? "مدفوع " : "paid ")
                    + d.paid()
                    + " / "
                    + (ar ? "متبقي " : "outstanding ")
                    + d.outstanding()));
      rows.add(pair(ar ? "التأمين المحتفظ به" : "Deposit held", finance.held(id).toPlainString()));
      body = table(rows);
    } else if (type.equals("invoice")) {
      Due d = db.get(Due.class, id);
      Lease l = access.lease(d.leaseId, false);
      title = ar ? "فاتورة إيجار" : "Rent invoice";
      body =
          table(
              List.of(
                  pair(ar ? "الفاتورة" : "Invoice", "INV-" + id),
                  pair(ar ? "العقد" : "Lease", l.id.toString()),
                  pair(ar ? "الاستحقاق" : "Due date", d.dueDate.toString()),
                  pair(ar ? "الإيجار" : "Rent", d.rentAmount.toPlainString()),
                  pair(ar ? "الضريبة" : "Tax", d.taxAmount.toPlainString()),
                  pair(ar ? "الإجمالي، ر.ع." : "Total, OMR", d.amount.toPlainString()),
                  pair(ar ? "المعاملة الضريبية" : "Tax treatment", l.taxTreatment)));
    } else if (type.equals("receipt")) {
      Payment p = db.get(Payment.class, id);
      access.lease(p.leaseId, false);
      title = ar ? "إيصال دفع" : "Payment receipt";
      body =
          table(
              List.of(
                  pair(ar ? "الإيصال" : "Receipt", "PAY-" + id),
                  pair(ar ? "العقد" : "Lease", p.leaseId.toString()),
                  pair(ar ? "التاريخ" : "Date", p.effectiveDate.toString()),
                  pair(ar ? "المبلغ، ر.ع." : "Amount, OMR", p.amount.toPlainString()),
                  pair(ar ? "الطريقة" : "Method", p.method),
                  pair(ar ? "المرجع" : "Reference", p.reference),
                  pair(
                      ar ? "الحالة" : "Status",
                      p.reversedOn == null
                          ? (ar ? "مسدد" : "Settled")
                          : (ar ? "معكوس" : "Reversed"))));
    } else if (type.equals("deposit")) {
      DepositEntry d = db.get(DepositEntry.class, id);
      access.lease(d.leaseId, false);
      title = ar ? "سجل تأمين" : "Deposit record";
      body =
          table(
              List.of(
                  pair(ar ? "المرجع" : "Reference", "DEP-" + id),
                  pair(ar ? "النوع" : "Type", d.kind),
                  pair(ar ? "المبلغ، ر.ع." : "Amount, OMR", d.amount.toPlainString()),
                  pair(ar ? "التاريخ" : "Date", d.effectiveDate.toString()),
                  pair(ar ? "السبب" : "Reason", d.reason)));
    } else throw ApiException.missing();
    return "<!doctype html><html lang=\""
        + language
        + "\" dir=\""
        + (ar ? "rtl" : "ltr")
        + "\"><meta charset=utf-8><meta name=viewport"
        + " content=\"width=device-width,initial-scale=1\"><title>"
        + escape(title)
        + "</title><style>body{font:16px Arial;max-width:850px;margin:40px"
        + " auto;padding:24px;color:#182d2b}table{border-collapse:collapse;width:100%}td{padding:12px;border:1px"
        + " solid"
        + " #ccc}.warning{padding:16px;background:#fff4d6}h1{color:#14675b}</style><body><p>BAYT /"
        + " بيت</p><h1>"
        + escape(title)
        + "</h1>"
        + body
        + "<p>"
        + LocalDate.now(clock)
        + " · Asia/Muscat · OMR</p></body></html>";
  }

  private List<String> pair(String a, String b) {
    return List.of(a, b);
  }

  private String table(List<List<String>> rows) {
    StringBuilder b = new StringBuilder("<table>");
    for (var row : rows)
      b.append("<tr><td>")
          .append(escape(row.get(0)))
          .append("</td><td>")
          .append(escape(row.get(1)))
          .append("</td></tr>");
    return b.append("</table>").toString();
  }

  public static String escape(String s) {
    return s.replace("&", "&amp;")
        .replace("<", "&lt;")
        .replace(">", "&gt;")
        .replace("\"", "&quot;")
        .replace("'", "&#39;");
  }
}
