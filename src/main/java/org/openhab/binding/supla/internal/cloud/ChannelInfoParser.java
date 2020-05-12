package org.openhab.binding.supla.internal.cloud;

import org.eclipse.smarthome.core.thing.ChannelUID;

import java.util.Optional;

import static java.util.Arrays.stream;

public class ChannelInfoParser {
    public static final ChannelInfoParser PARSER = new ChannelInfoParser();

    public ChannelInfo parse(ChannelUID channelUID) {
        final String fullId = channelUID.getId();
        final Optional<AdditionalChannelType> additionalChannelType =
                stream(AdditionalChannelType.values())
                        .filter(type -> fullId.endsWith(type.getSuffix()))
                        .findAny();
        final String trimmedId;
        if (additionalChannelType.isPresent()) {
            final AdditionalChannelType type = additionalChannelType.get();
            trimmedId = fullId.substring(0, fullId.length() - type.getSuffix().length());
        } else {
            trimmedId = fullId;
        }
        return new ChannelInfo(parse(trimmedId, fullId), additionalChannelType.orElse(null));
    }

    private int parse(String id, String fullId) {
        try {
            return Integer.parseInt(id);
        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException("Given id `" + id + "` is not int! Full ID = `" + fullId + "`", ex);
        }
    }
}
