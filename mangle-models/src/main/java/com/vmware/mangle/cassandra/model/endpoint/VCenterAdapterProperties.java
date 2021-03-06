/*
 * Copyright (c) 2016-2019 VMware, Inc. All Rights Reserved.
 *
 * This product is licensed to you under the Apache License, Version 2.0 (the "License").
 * You may not use this product except in compliance with the License.
 *
 * This product may include a number of subcomponents with separate copyright notices
 * and license terms. Your use of these subcomponents is subject to the terms and
 * conditions of the subcomponent's license, as noted in the LICENSE file.
 */

package com.vmware.mangle.cassandra.model.endpoint;

import java.io.Serializable;

import javax.validation.constraints.NotEmpty;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.cassandra.core.mapping.UserDefinedType;

/**
 * POJO for VC adapter spec
 *
 * @author chetanc
 */
@UserDefinedType("vCenterAdapterProperties")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class VCenterAdapterProperties implements Serializable {

    private static final long serialVersionUID = 1L;

    @NotEmpty
    private String vcAdapterUrl;
    @NotEmpty
    private String username;
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    @NotEmpty
    private String password;

    public void setVcAdapterUrl(String vcAdapterUrl) {
        this.vcAdapterUrl = vcAdapterUrl.endsWith("/") ? vcAdapterUrl : vcAdapterUrl + "/";
    }

}