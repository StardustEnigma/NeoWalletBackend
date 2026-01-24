package com.neowallet.neowalletbackend.dto;



import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class P2pDto {

     private Long fromUserId;
     private Long toUserId;

     private BigDecimal amount;
     private String message;


}
