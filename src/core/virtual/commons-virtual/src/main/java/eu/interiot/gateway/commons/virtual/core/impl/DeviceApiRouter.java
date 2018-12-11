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

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import eu.interiot.gateway.commons.api.device.Action;
import eu.interiot.gateway.commons.api.device.DeviceDefinition;
import eu.interiot.gateway.commons.api.device.Measurement;
import eu.interiot.gateway.commons.virtual.api.ApiResponse;
import eu.interiot.gateway.commons.virtual.api.remote.PhysicalRemoteGatewayService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

@Api("device")
@Path("/device")
public class DeviceApiRouter {
	@GET
    @Path("/{deviceUid}")
    @Produces(MediaType.APPLICATION_JSON)
	@ApiOperation(value = "Get Device Description", response = DeviceDefinition.class)
    public Response getDevice(@PathParam("deviceUid") String deviceUid) {
		try {
			return ApiResponse.jsonResponse(200, PhysicalRemoteGatewayService.instance().getDevice(deviceUid)).build();
		} catch (Exception e) {
			return ApiResponse.error(500, e).build();
		}
    }
	@GET
    @Path("/{deviceUid}/read")
    @Produces(MediaType.APPLICATION_JSON)
	@ApiOperation(value = "Read last device state", response = Measurement.class)
    public Response readDevice(@PathParam("deviceUid") String deviceUid) {
		try {
			return ApiResponse.jsonResponse(200, PhysicalRemoteGatewayService.instance().readDevice(deviceUid)).build();
		} catch (Exception e) {
			return ApiResponse.error(500, e).build();
		}
    }	
	@POST
    @Path("/{deviceUid}/write")
    @Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	@ApiOperation(value = "Write state to device", response = Measurement.class)
    public Response writeDevice(@PathParam("deviceUid") String deviceUid, Action action) {
		try {
			//return ApiResponse.jsonResponse(200, data).build();
			return ApiResponse.jsonResponse(200, PhysicalRemoteGatewayService.instance().writeDevice(deviceUid, action)).build();
		} catch (Exception e) {
			return ApiResponse.error(500, e).build();
		}
    }
	@GET
    @Path("/{deviceUid}/status")
    @Produces(MediaType.APPLICATION_JSON)
	@ApiOperation(value = "Get device status", response = String.class)
    public Response deviceStatus(@PathParam("deviceUid") String deviceUid) {
		try {
			return ApiResponse.jsonResponse(200, PhysicalRemoteGatewayService.instance().getDeviceState(deviceUid)).build();
		} catch (Exception e) {
			return ApiResponse.error(500, e).build();
		}
    }
	@GET
    @Path("/{deviceUid}/start")
    @Produces(MediaType.APPLICATION_JSON)
	@ApiOperation(value = "Connect device"/*, response = Device.class*/)
    public Response startDevice(@PathParam("deviceUid") String deviceUid) {
		try {
			return ApiResponse.jsonResponse(200, PhysicalRemoteGatewayService.instance().startDevice(deviceUid)).build();
		} catch (Exception e) {
			return ApiResponse.error(500, e).build();
		}
    }
	@GET
    @Path("/{deviceUid}/stop")
    @Produces(MediaType.APPLICATION_JSON)
	@ApiOperation(value = "Disconnect device"/*, response = Device.class*/)
    public Response stopDevice(@PathParam("deviceUid") String deviceUid) {
		try {
			return ApiResponse.jsonResponse(200, PhysicalRemoteGatewayService.instance().stopDevice(deviceUid)).build();
		} catch (Exception e) {
			return ApiResponse.error(500, e).build();
		}
    }
	
}
