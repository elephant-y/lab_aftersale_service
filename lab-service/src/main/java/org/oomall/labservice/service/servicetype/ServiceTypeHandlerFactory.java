package org.oomall.labservice.service.servicetype;

import org.oomall.labservice.model.ServiceOrderType;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class ServiceTypeHandlerFactory {

    private final Map<ServiceOrderType, ServiceTypeHandler> map;

    public ServiceTypeHandlerFactory(
            SiteServiceHandler site,
            ShipServiceHandler ship) {

        map = Map.of(
                ServiceOrderType.SITEMENT, site,
                ServiceOrderType.SHIPMENT, ship
        );
    }

    public ServiceTypeHandler get(Integer type) {
        ServiceOrderType t = ServiceOrderType.from(type);
        return map.get(t);
    }
}
