/*
 * Copyright 2016-2018 Universitat Politècnica de València
 * Copyright 2016-2018 Università della Calabria
 * Copyright 2016-2018 Prodevelop, SL
 * Copyright 2016-2018 Technische Universiteit Eindhoven
 * Copyright 2016-2018 Fundación de la Comunidad Valenciana para la
 * Investigación, Promoción y Estudios Comerciales de Valenciaport
 * Copyright 2016-2018 Rinicom Ltd
 * Copyright 2016-2018 Association pour le développement de la formation
 * professionnelle dans le transport
 * Copyright 2016-2018 Noatum Ports Valenciana, S.A.U.
 * Copyright 2016-2018 XLAB razvoj programske opreme in svetovanje d.o.o.
 * Copyright 2016-2018 Systems Research Institute Polish Academy of Sciences
 * Copyright 2016-2018 Azienda Sanitaria Locale TO5
 * Copyright 2016-2018 Alessandro Bassi Consulting SARL
 * Copyright 2016-2018 Neways Technologies B.V.
 *
 * See the NOTICE file distributed with this work for additional information
 * regarding copyright ownership.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package eu.interiot.gateway.commons.virtual.core.impl;

import java.net.InetSocketAddress;
import java.util.Collection;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

import eu.interiot.gateway.commons.api.device.DeviceDefinition;
import eu.interiot.gateway.commons.api.messages.GatewayInfoMessage;
import eu.interiot.gateway.commons.virtual.api.ApiResponse;
import eu.interiot.gateway.commons.virtual.api.remote.PhysicalRemoteGatewayService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

@Api("physical")
@Path("/physical")
public class PhysicalApiRouter{
	
	@GET
    @Path("/devices")
    @Produces(MediaType.APPLICATION_JSON)
	@ApiOperation(value = "Get a list of devices connected to this Gateway", response = DeviceDefinition[].class)
    public Response listDevices() {
		Collection<DeviceDefinition> devices;
		try {
			devices = PhysicalRemoteGatewayService.instance().getDeviceList();
		} catch (Exception e) {
			return ApiResponse.error(500, e).build();
		}
		return ApiResponse.jsonResponse(200, devices).build();
    }
	
	@GET
    @Path("/id")
    @Produces(MediaType.APPLICATION_JSON)
	@ApiOperation(value = "Get the physical gateway Id connected", response = String.class)
    public Response gatewayId() {
		try {
			return ApiResponse.jsonResponse(200, PhysicalRemoteGatewayService.instance().getRemoteInfo().getUUID()).build();
		} catch (Exception e) {
			return ApiResponse.error(500, e).build();
		}
    }
	
	@GET
    @Path("/status")
    @Produces(MediaType.APPLICATION_JSON)
	@ApiOperation(value = "Check the connection status of the physical Gateway", response = String.class)
    public Response status() {
		try {
			return ApiResponse.jsonResponse(200, new JsonPrimitive(PhysicalRemoteGatewayService.instance().isConnected())).build();
		} catch (Exception e) {
			return ApiResponse.error(500, e).build();
		}
    }
	
	@GET
    @Path("/info")
    @Produces(MediaType.APPLICATION_JSON)
	@ApiOperation(value = "Get physical gateway information", response = String.class)
    public Response info() {
		try {
			GatewayInfoMessage info = (GatewayInfoMessage) PhysicalRemoteGatewayService.instance().getRemoteInfo();
			JsonObject obj = info.toJson().getAsJsonObject("payload");
			InetSocketAddress socketInfo = PhysicalRemoteGatewayService.instance().getRemoteSocketInfo();
			obj.addProperty("host", socketInfo.getAddress().getHostAddress());
			obj.addProperty("port", socketInfo.getPort());
			return ApiResponse.jsonResponse(200, obj).build();
		} catch (Exception e) {
			return ApiResponse.error(500, e).build();
		}
    }
	
}