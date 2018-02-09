package dominic.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InvoiceConsumerDTO {

    /**
     * 发票ID
     */
    private Long invoiceId;
    /**
     * 订单项
     */
    private List<InvoiceConsumerSubItemDTO> subItemList;
}
