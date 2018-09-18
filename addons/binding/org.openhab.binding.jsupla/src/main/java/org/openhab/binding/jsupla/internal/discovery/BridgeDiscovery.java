package org.openhab.binding.jsupla.internal.discovery;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import org.eclipse.smarthome.config.discovery.AbstractDiscoveryService;
import org.eclipse.smarthome.config.discovery.DiscoveryResult;
import org.eclipse.smarthome.config.discovery.DiscoveryResultBuilder;
import org.eclipse.smarthome.config.discovery.DiscoveryService;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.Random;

import static java.lang.String.valueOf;
import static org.openhab.binding.jsupla.jSuplaBindingConstants.CONFIG_PORT;
import static org.openhab.binding.jsupla.jSuplaBindingConstants.DEFAULT_PORT;
import static org.openhab.binding.jsupla.jSuplaBindingConstants.JSUPLA_SERVER_TYPE;

@Component(service = DiscoveryService.class, immediate = true, configurationPid = "binding.jsupla")
public class BridgeDiscovery extends AbstractDiscoveryService {
    private final Logger logger = LoggerFactory.getLogger(BridgeDiscovery.class);
    private final Random random = new Random();

    public BridgeDiscovery() {
        super(ImmutableSet.of(JSUPLA_SERVER_TYPE), 10, true);
    }

    @Override
    protected void startScan() {
        discover();
        stopScan();
    }

    @Override
    protected void startBackgroundDiscovery() {
        discover();
    }

    private void discover() {
        ThingUID thingUID = new ThingUID(JSUPLA_SERVER_TYPE, valueOf(DEFAULT_PORT));
        final String label = "jSupla Server";
        final DiscoveryResult discoveryResult = DiscoveryResultBuilder.create(thingUID)
                                                        .withProperties(buildThingProperties())
                                                        .withLabel(label)
                                                        .build();
        logger.debug("Adding server to discovery; {}", label);
        thingDiscovered(discoveryResult);
    }

    private Map<String, Object> buildThingProperties() {
        return ImmutableMap.<String, Object>builder()
                       .put(CONFIG_PORT, DEFAULT_PORT)
                       .build();
    }
}
