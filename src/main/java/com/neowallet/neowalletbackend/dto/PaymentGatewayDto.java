package com.neowallet.neowalletbackend.dto;

import com.neowallet.neowalletbackend.model.PaymentStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@AllArgsConstructor
@NoArgsConstructor
public class PaymentGatewayDto {

    private Long paymentId;

    private String status;

    private String gatewayId;
}
