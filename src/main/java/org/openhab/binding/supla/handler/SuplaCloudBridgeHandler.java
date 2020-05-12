/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 * <p>
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.supla.handler;

import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.util.SelfSignedCertificate;
import org.eclipse.smarthome.config.core.Configuration;
import org.eclipse.smarthome.core.common.ThreadPoolManager;
import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.binding.BaseBridgeHandler;
import org.eclipse.smarthome.core.types.Command;
import org.openhab.binding.supla.internal.SuplaDeviceRegistry;
import org.openhab.binding.supla.internal.discovery.SuplaDiscoveryService;
import org.openhab.binding.supla.internal.server.SuplaChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pl.grzeslowski.jsupla.protocol.impl.calltypes.CallTypeParserImpl;
import pl.grzeslowski.jsupla.protocol.impl.decoders.DecoderFactoryImpl;
import pl.grzeslowski.jsupla.protocol.impl.encoders.EncoderFactoryImpl;
import pl.grzeslowski.jsupla.server.api.Channel;
import pl.grzeslowski.jsupla.server.api.Server;
import pl.grzeslowski.jsupla.server.api.ServerFactory;
import pl.grzeslowski.jsupla.server.api.ServerProperties;
import pl.grzeslowski.jsupla.server.netty.api.NettyServerFactory;

import javax.net.ssl.SSLException;
import java.math.BigDecimal;
import java.security.cert.CertificateException;
import java.util.Arrays;
import java.util.concurrent.ScheduledExecutorService;

import static org.eclipse.smarthome.core.thing.ThingStatus.OFFLINE;
import static org.eclipse.smarthome.core.thing.ThingStatus.ONLINE;
import static org.eclipse.smarthome.core.thing.ThingStatusDetail.CONFIGURATION_ERROR;
import static org.openhab.binding.supla.SuplaBindingConstants.CONFIG_AUTH_KEY;
import static org.openhab.binding.supla.SuplaBindingConstants.CONFIG_EMAIL;
import static org.openhab.binding.supla.SuplaBindingConstants.CONFIG_PORT;
import static org.openhab.binding.supla.SuplaBindingConstants.CONFIG_SERVER_ACCESS_ID;
import static org.openhab.binding.supla.SuplaBindingConstants.CONFIG_SERVER_ACCESS_ID_PASSWORD;
import static org.openhab.binding.supla.SuplaBindingConstants.CONNECTED_DEVICES_CHANNEL_ID;
import static pl.grzeslowski.jsupla.server.api.ServerProperties.fromList;
import static pl.grzeslowski.jsupla.server.netty.api.NettyServerFactory.PORT;
import static pl.grzeslowski.jsupla.server.netty.api.NettyServerFactory.SSL_CTX;

/**
 * @author Grzeslowski - Initial contribution
 */
public class SuplaCloudBridgeHandler extends BaseBridgeHandler {
    private final Logger logger = LoggerFactory.getLogger(SuplaCloudBridgeHandler.class);
    private final SuplaDeviceRegistry suplaDeviceRegistry;
    private ScheduledExecutorService scheduledPool;
    private Server server;
    private SuplaDiscoveryService suplaDiscoveryService;

    private int numberOfConnectedDevices = 0;

    private int port;
    private int serverAccessId;
    private char[] serverAccessIdPassword;
    private String email;
    private String authKey;

    public SuplaCloudBridgeHandler(final Bridge bridge, final SuplaDeviceRegistry suplaDeviceRegistry) {
        super(bridge);
        this.suplaDeviceRegistry = suplaDeviceRegistry;
    }

    @SuppressWarnings("deprecation")
    @Override
    public void initialize() {
        updateConnectedDevices();
        scheduledPool = ThreadPoolManager.getScheduledPool(this.getClass() + "." + port);
        final ServerFactory factory = buildServerFactory();
        try {
            final Configuration config = this.getConfig();
            serverAccessId = ((BigDecimal) config.get(CONFIG_SERVER_ACCESS_ID)).intValue();
            serverAccessIdPassword = ((String) config.get(CONFIG_SERVER_ACCESS_ID_PASSWORD)).toCharArray();
            email = (String) config.get(CONFIG_EMAIL);
            authKey = (String) config.get(CONFIG_AUTH_KEY);

            port = ((BigDecimal) config.get(CONFIG_PORT)).intValue();
            server = factory.createNewServer(buildServerProperties(port));
            server.getNewChannelsPipe().subscribe(
                    this::channelConsumer,
                    this::errorOccurredInChannel);

            logger.debug("jSuplaServer running on port {}", port);
            updateStatus(ONLINE);
        } catch (CertificateException | SSLException ex) {
            logger.error("Cannot start server!", ex);
            updateStatus(OFFLINE, CONFIGURATION_ERROR,
                    "Cannot start server! " + ex.getLocalizedMessage());
        }
    }

    private void channelConsumer(Channel channel) {
        logger.debug("Device connected to {}", toString());
        changeNumberOfConnectedDevices(1);
        newChannel(channel, serverAccessId, serverAccessIdPassword);
    }

    private void errorOccurredInChannel(Throwable ex) {
        logger.error("Error occurred in server pipe", ex);
        updateStatus(OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                "Error occurred in server pipe. Message: " + ex.getLocalizedMessage());
    }

    public void completedChannel() {
        logger.debug("Device disconnected from {}", toString());
        changeNumberOfConnectedDevices(-1);
    }

    private void changeNumberOfConnectedDevices(int delta) {
        numberOfConnectedDevices += delta;
        updateConnectedDevices();
    }

    private void updateConnectedDevices() {
        updateState(CONNECTED_DEVICES_CHANNEL_ID, new DecimalType(numberOfConnectedDevices));
    }

    private ServerFactory buildServerFactory() {
        return new NettyServerFactory(
                new CallTypeParserImpl(),
                DecoderFactoryImpl.INSTANCE,
                EncoderFactoryImpl.INSTANCE);
    }

    private ServerProperties buildServerProperties(int port)
            throws CertificateException, SSLException {
        return fromList(Arrays.asList(PORT, port, SSL_CTX, buildSslContext()));
    }

    private SslContext buildSslContext() throws CertificateException, SSLException {
        SelfSignedCertificate ssc = new SelfSignedCertificate();
        return SslContextBuilder.forServer(ssc.certificate(), ssc.privateKey()).build();
    }

    private void newChannel(final Channel channel, int serverAccessId, char[] serverAccessIdPassword) {
        logger.debug("New channel {}", channel);
        final SuplaChannel jSuplaChannel = new SuplaChannel(
                this,
                serverAccessId,
                serverAccessIdPassword,
                suplaDiscoveryService,
                channel,
                scheduledPool,
                suplaDeviceRegistry,
                email,
                authKey);

        channel.getMessagePipe().subscribe(
                jSuplaChannel::onNext,
                jSuplaChannel::onError,
                jSuplaChannel::onComplete);
    }

    @Override
    public void dispose() {
        super.dispose();
        try {
            server.close();
        } catch (Exception ex) {
            logger.error("Could not close server!", ex);
            updateStatus(OFFLINE, ThingStatusDetail.NONE,
                    "Could not close server! It's possible that restart of your RPi is required. " + ex.getLocalizedMessage());
        }
    }

    @Override
    public void handleCommand(final ChannelUID channelUID, final Command command) {
        // no commands in this bridge
    }

    public void setSuplaDiscoveryService(final SuplaDiscoveryService suplaDiscoveryService) {
        logger.trace("setSuplaDiscoveryService#{}", suplaDiscoveryService.hashCode());
        this.suplaDiscoveryService = suplaDiscoveryService;
    }

    @Override
    public String toString() {
        return "SuplaCloudBridgeHandler{" +
                       "port=" + port +
                       ", serverAccessId=" + serverAccessId +
                       "} " + super.toString();
    }
}
