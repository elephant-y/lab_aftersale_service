package org.oomall.labservice.mapper.Po;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Consignee {
    private String name;
    private String mobile;
    private Integer regionId;
    private String address;
}
