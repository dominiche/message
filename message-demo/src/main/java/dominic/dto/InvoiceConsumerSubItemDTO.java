package dominic.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InvoiceConsumerSubItemDTO {
    /**
     * 详情流水号
     */
    private String subNumber;
    /**
     * 实付金额
     */
    private java.math.BigDecimal itemCash;
}
