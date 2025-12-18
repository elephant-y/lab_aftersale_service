package org.oomall.labaftersale.mapper.po;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Consignee {
    private String name;
    private String mobile;
    private Long regionId;
    private String address;
}
