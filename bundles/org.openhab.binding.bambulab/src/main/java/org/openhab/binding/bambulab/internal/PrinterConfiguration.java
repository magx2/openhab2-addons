/**
 * Copyright (c) 2010-2025 Contributors to the openHAB project
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.openhab.binding.bambulab.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;
import pl.grzeslowski.jbambuapi.PrinterClientConfig;

import java.util.UUID;

import static pl.grzeslowski.jbambuapi.PrinterClientConfig.*;

/**
 * The {@link PrinterConfiguration} class contains fields mapping thing configuration parameters.
 *
 * @author Martin Grześlowski - Initial contribution
 */
@NonNullByDefault
public class PrinterConfiguration {
    public String serial = "";
    public String scheme = SCHEME;
    public String hostname = "";
    public int port = DEFAULT_PORT;
    public String username = "";
    public String accessCode = "";
}
