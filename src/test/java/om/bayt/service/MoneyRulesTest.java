package om.bayt.service;
import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
class MoneyRulesTest {
    @Test void partialPaymentLeavesTwoHundred(){var parts=MoneyRules.allocate(new BigDecimal("100.000"),List.of(new MoneyRules.Balance(1,new BigDecimal("300.000"))));assertEquals(new BigDecimal("200.000"),new BigDecimal("300.000").subtract(parts.get(0).amount()));}
    @Test void allocatesOldestFirst(){var p=MoneyRules.allocate(new BigDecimal("350.000"),List.of(new MoneyRules.Balance(1,new BigDecimal("300.000")),new MoneyRules.Balance(2,new BigDecimal("300.000"))));assertEquals(new BigDecimal("300.000"),p.get(0).amount());assertEquals(2,p.get(1).dueId());assertEquals(new BigDecimal("50.000"),p.get(1).amount());}
    @Test void overpaymentIsRejected(){assertThrows(IllegalArgumentException.class,()->MoneyRules.allocate(new BigDecimal("300.001"),List.of(new MoneyRules.Balance(1,new BigDecimal("300.000")))));}
    @Test void nonpositiveAndSubBaisaAmountsAreRejected(){assertThrows(IllegalArgumentException.class,()->MoneyRules.allocate(BigDecimal.ZERO,List.of()));assertThrows(ArithmeticException.class,()->MoneyRules.allocate(new BigDecimal("1.0001"),List.of()));}
    @Test void reversalPreservesHistoricalSettlement(){var paid=LocalDate.of(2026,1,1);var reversed=paid.plusDays(3);assertTrue(MoneyRules.settledOn(paid,reversed,paid.plusDays(2)));assertFalse(MoneyRules.settledOn(paid,reversed,reversed));assertFalse(MoneyRules.settledOn(paid,null,paid.minusDays(1)));}
}
