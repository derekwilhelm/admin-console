/**
 * Copyright (c) Codice Foundation
 * <p>
 * This is free software: you can redistribute it and/or modify it under the terms of the GNU Lesser
 * General Public License as published by the Free Software Foundation, either version 3 of the
 * License, or any later version.
 * <p>
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details. A copy of the GNU Lesser General Public License
 * is distributed along with this program and can be found at
 * <http://www.gnu.org/licenses/lgpl.html>.
 */
package org.codice.ddf.admin.security.ldap.probe;

import static org.codice.ddf.admin.api.config.security.ldap.LdapConfiguration.BASE_GROUP_DN;
import static org.codice.ddf.admin.api.config.security.ldap.LdapConfiguration.BASE_USER_DN;
import static org.codice.ddf.admin.api.config.security.ldap.LdapConfiguration.BIND_KDC;
import static org.codice.ddf.admin.api.config.security.ldap.LdapConfiguration.BIND_METHOD;
import static org.codice.ddf.admin.api.config.security.ldap.LdapConfiguration.BIND_REALM;
import static org.codice.ddf.admin.api.config.security.ldap.LdapConfiguration.BIND_USER_DN;
import static org.codice.ddf.admin.api.config.security.ldap.LdapConfiguration.BIND_USER_PASSWORD;
import static org.codice.ddf.admin.api.config.security.ldap.LdapConfiguration.ENCRYPTION_METHOD;
import static org.codice.ddf.admin.api.config.security.ldap.LdapConfiguration.GROUP_OBJECT_CLASS;
import static org.codice.ddf.admin.api.config.security.ldap.LdapConfiguration.HOST_NAME;
import static org.codice.ddf.admin.api.config.security.ldap.LdapConfiguration.LDAP_TYPE;
import static org.codice.ddf.admin.api.config.security.ldap.LdapConfiguration.MEMBERSHIP_ATTRIBUTE;
import static org.codice.ddf.admin.api.config.security.ldap.LdapConfiguration.PORT;
import static org.codice.ddf.admin.api.config.security.ldap.LdapConfiguration.QUERY;
import static org.codice.ddf.admin.api.config.security.ldap.LdapConfiguration.QUERY_BASE;
import static org.codice.ddf.admin.api.config.security.ldap.LdapConfiguration.USER_NAME_ATTRIBUTE;
import static org.codice.ddf.admin.security.ldap.LdapConnectionResult.CANNOT_BIND;
import static org.codice.ddf.admin.security.ldap.LdapConnectionResult.CANNOT_CONFIGURE;
import static org.codice.ddf.admin.security.ldap.LdapConnectionResult.CANNOT_CONNECT;
import static org.codice.ddf.admin.security.ldap.LdapConnectionResult.toDescriptionMap;
import static org.codice.ddf.admin.security.ldap.test.LdapTestingCommons.bindUserToLdapConnection;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.CollectionUtils;
import org.codice.ddf.admin.api.config.security.ldap.LdapConfiguration;
import org.codice.ddf.admin.api.handler.ConfigurationMessage;
import org.codice.ddf.admin.api.handler.method.ProbeMethod;
import org.codice.ddf.admin.api.handler.report.ProbeReport;
import org.codice.ddf.admin.security.ldap.ServerGuesser;

public class DefaultDirectoryStructureProbe extends ProbeMethod<LdapConfiguration> {
    private static final String DESCRIPTION =
            "Queries the bound LDAP server, attempting to find the user and group base DNs, the "
                    + "username attribute, the group membership attribute, and the objectClass "
                    + "representing groups.";

    private static final String ID = "directoryStructure";

    private static final Map<String, String> REQUIRED_FIELDS = LdapConfiguration.buildFieldMap(
            LDAP_TYPE,
            HOST_NAME,
            PORT,
            ENCRYPTION_METHOD,
            BIND_USER_DN,
            BIND_USER_PASSWORD,
            BIND_METHOD);

    private static final Map<String, String> OPTIONAL_FIELDS = LdapConfiguration.buildFieldMap(
            BIND_REALM,
            BIND_KDC);

    private static final Map<String, String> FAILURE_TYPES = toDescriptionMap(Arrays.asList(
            CANNOT_CONFIGURE,
            CANNOT_CONNECT,
            CANNOT_BIND));

    public DefaultDirectoryStructureProbe() {
        super(ID, DESCRIPTION, REQUIRED_FIELDS, OPTIONAL_FIELDS, null, FAILURE_TYPES, null);
    }

    @Override
    public ProbeReport probe(LdapConfiguration configuration) {
        ProbeReport probeReport = new ProbeReport(new ArrayList<>());
        List<ConfigurationMessage> checkMessages =
                configuration.checkRequiredFields(REQUIRED_FIELDS.keySet());

        if (CollectionUtils.isNotEmpty(checkMessages)) {
            return new ProbeReport(checkMessages);
        }

        checkMessages = configuration.testConditionalBindFields();
        if (CollectionUtils.isNotEmpty(checkMessages)) {
            return new ProbeReport(checkMessages);
        }

        String ldapType = configuration.ldapType();
        ServerGuesser guesser = ServerGuesser.buildGuesser(ldapType,
                bindUserToLdapConnection(configuration).connection());

        if (guesser != null) {
            probeReport.addProbeResult(BASE_USER_DN, guesser.getUserBaseChoices());
            probeReport.addProbeResult(BASE_GROUP_DN, guesser.getGroupBaseChoices());
            probeReport.addProbeResult(USER_NAME_ATTRIBUTE, guesser.getUserNameAttribute());
            probeReport.addProbeResult(GROUP_OBJECT_CLASS, guesser.getGroupObjectClass());
            probeReport.addProbeResult(MEMBERSHIP_ATTRIBUTE, guesser.getMembershipAttribute());

            // TODO RAP 13 Dec 16: Better query, perhaps driven by guessers?
            probeReport.addProbeResult(QUERY, Collections.singletonList("objectClass=*"));
            probeReport.addProbeResult(QUERY_BASE, guesser.getBaseContexts());
        }

        return probeReport;
    }
}