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
package eu.interiot.gateway.dmanager.api.impl;

import java.io.File;
import java.io.FileReader;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.StringJoiner;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.osgi.framework.BundleContext;

import eu.interiot.gateway.commons.api.command.CommandLine.Command;
import eu.interiot.gateway.commons.api.command.CommandLine.Option;
import eu.interiot.gateway.commons.api.command.CommandLine.Parameters;
import eu.interiot.gateway.commons.api.command.CommandLine.ParentCommand;
import eu.interiot.gateway.commons.api.command.ExecutableCommand;
import eu.interiot.gateway.commons.api.device.Action;
import eu.interiot.gateway.commons.api.device.Attribute;
import eu.interiot.gateway.commons.api.device.DeviceDefinition;
import eu.interiot.gateway.commons.api.device.Measurement;
import eu.interiot.gateway.commons.api.device.Measurement.MeasurementData;
import eu.interiot.gateway.commons.physical.api.dmanager.Device;
import eu.interiot.gateway.commons.physical.api.dmanager.DeviceManager;
import eu.interiot.gateway.commons.physical.api.registry.RegistryService;
import eu.interiot.gateway.dmanager.api.impl.DeviceCommands.DeviceAction;
import eu.interiot.gateway.dmanager.api.impl.DeviceCommands.DeviceAdd;
import eu.interiot.gateway.dmanager.api.impl.DeviceCommands.DeviceConnect;
import eu.interiot.gateway.dmanager.api.impl.DeviceCommands.DeviceDisconnect;
import eu.interiot.gateway.dmanager.api.impl.DeviceCommands.DeviceList;
import eu.interiot.gateway.dmanager.api.impl.DeviceCommands.DeviceMeasurement;

@Command(name="device", description="Controls devices registered in the gateway", subcommands= {
		DeviceList.class,
		DeviceConnect.class,
		DeviceDisconnect.class,
		DeviceAction.class,
		DeviceMeasurement.class,
		DeviceAdd.class
})
public class DeviceCommands extends ExecutableCommand{

	private static Logger log = LogManager.getLogger(DeviceCommands.class);
	
	@Option(names = {"-h", "--help"}, usageHelp = true, description = "Show this help message and exit.")
    private boolean helpRequested;

	private final DeviceManager devManager;
	private final BundleContext bundleContext;
	
	private RegistryService registry;
	
	public DeviceCommands(DeviceManager devManager, BundleContext bundleContext) {
		this.devManager = devManager;
		this.bundleContext = bundleContext;
	}
	
	@Override
	public void execute(PrintWriter out) throws Exception {
		
	}
	
	private RegistryService getDeviceRegistry() {
		if(registry == null) registry = this.bundleContext.getService(this.bundleContext.getServiceReference(RegistryService.class));
		return registry;
	}
	
	@Command(name="list", description="list registered devices")
	public static class DeviceList extends ExecutableCommand{

		@Option(names = {"-h", "--help"}, usageHelp = true, description = "Show this help message and exit.")
	    private boolean helpRequested;
		
		private static int colsize = 25;
		private static String rowFormat = "%1$-"+colsize+"."+colsize+"s    "+"%2$-"+colsize+"."+colsize+"s    "+"%3$-"+colsize+"."+colsize+"s    "+"%4$-"+colsize+"."+colsize+"s";
		private static char rowSeparatorChar = '-';
		private static String rowSeparator;
		
		@ParentCommand
		private DeviceCommands parent;
		
		static{
			char [] chars = new char[colsize];
			Arrays.fill(chars, rowSeparatorChar);
			rowSeparator = new String(chars);
		}
		
		@Override
		public void execute(PrintWriter out) throws Exception {
			StringJoiner sj = new StringJoiner("\n");
			sj.add(String.format(rowFormat, "ID", "TYPE", "CONTROLLER", "STATE"));
			sj.add(String.format(rowFormat, rowSeparator, rowSeparator, rowSeparator, rowSeparator));
			parent.devManager.getDevices().forEach(device -> {
				DeviceDefinition deviceDef = device.getDeviceDefinition();
				String uuid = deviceDef.getId();
				uuid = uuid.length() > colsize ? "..." + uuid.substring(uuid.length() - colsize + 3, uuid.length()) : uuid;
				String type = deviceDef.getType();
				type = type.length() > colsize ? type.substring(0, colsize - 3) + "..." : type;
				String controller = deviceDef.getController();
				if(controller == null) controller = "N/A";
				String state = device.getState().toString();
				state = state.length() > colsize ? state.substring(0, colsize - 3) + "..." : state;
				sj.add(String.format(rowFormat, uuid, type, controller, state));
				
			});
			out.println(sj.toString());
			out.flush();
		}
		
	}
	
	@Command(name = "connect", description = "connect a registered device by id")
	public static class DeviceConnect extends ExecutableCommand {
		
		@Option(names = {"-h", "--help"}, usageHelp = true, description = "Show this help message and exit.")
	    private boolean helpRequested;
		
		@Parameters(arity = "1...*", description = "list of device id's")
		private List<String> deviceIds;

		@ParentCommand
		private DeviceCommands parent;
		
		@Override
		public void execute(PrintWriter out) throws Exception {
			deviceIds.forEach(id -> {
				try {
					this.parent.devManager.getDevice(id).connect();
				}catch(Exception ex) {
					log.error(ex);
				}
			});
		}
		
	}
	
	@Command(name = "disconnect", description = "disconnect a registered device by id")
	public static class DeviceDisconnect extends ExecutableCommand {
		
		@Option(names = {"-h", "--help"}, usageHelp = true, description = "Show this help message and exit.")
	    private boolean helpRequested;
		
		@Parameters(arity = "1...*", description = "list of device id's")
		private List<String> deviceIds;

		@ParentCommand
		private DeviceCommands parent;
		
		@Override
		public void execute(PrintWriter out) throws Exception {
			deviceIds.forEach(id -> {
				try {
					this.parent.devManager.getDevice(id).disconnect();
				}catch(Exception ex) {
					log.error(ex);
				}
			});
		}
		
	}
	
	@Command(name = "action", description = "send an action to a device")
	public static class DeviceAction extends ExecutableCommand {
		
		@Parameters(arity="1", description = "Device UUID to send action")
		private String deviceId;
		
		@Option(names = {"-h", "--help"}, usageHelp = true, description = "Show this help message and exit.")
	    private boolean helpRequested;
		
		@Option(names = {"-i"}, description = "Send an integer value")
		private Map<String, Integer> integers;
		
		@Option(names = {"-b"}, description = "Send a boolean value")
		private Map<String, Boolean> booleans;
		
		@Option(names = {"-f"}, description = "Send a float value")
		private Map<String, Float> floats;
		
		@Option(names = {"-s"}, description = "Send a string value")
		private Map<String, String> strings;
		
		@ParentCommand
		private DeviceCommands parent;
		
		@Override
		public void execute(PrintWriter out) throws Exception {
			Device device = this.parent.devManager.getDevice(deviceId);
			
			if(Objects.isNull(device)) throw new Exception("Device not found");
			
			Action action = new Action();
			
			if(Objects.nonNull(integers))
				integers.forEach((attr, val) -> action.setValue(new Attribute(attr, Attribute.Type.INTEGER), val)); 
			
			if(Objects.nonNull(booleans))
				booleans.forEach((attr, val) -> action.setValue(new Attribute(attr, Attribute.Type.BOOLEAN), val)); 
			
			if(Objects.nonNull(floats))
				floats.forEach((attr, val) -> action.setValue(new Attribute(attr, Attribute.Type.FLOAT), val)); 
			
			if(Objects.nonNull(strings))
				strings.forEach((attr, val) -> action.setValue(new Attribute(attr, Attribute.Type.STRING), val)); 
			
			device.pushAction(action);
			
		}
		
	}
	
	@Command(name = "measure", description = "print last device measurement")
	public static class DeviceMeasurement extends ExecutableCommand {

		@Parameters(arity = "1", description = "device id")
		private String deviceId;
		
		@ParentCommand
		private DeviceCommands parent;
		
		@Override
		public void execute(PrintWriter out) throws Exception {
			Device device = this.parent.devManager.getDevice(deviceId);
			Measurement measurement = device.getLastMeasurement();
			for(MeasurementData data : measurement.getData()) out.println(data.getAttribute().getName()+": " + data.getValue());
		}
		
	}
	
	@Command(name = "add", description = "add a new device from a json file")
	public static class DeviceAdd extends ExecutableCommand {
		
		@Parameters(arity = "1", description = "json file path")
		private File jsonFile;
		
		@ParentCommand
		private DeviceCommands parent;
		
		@Override
		public void execute(PrintWriter out) throws Exception {
			this.parent.getDeviceRegistry().loadDeviceFromReader(new FileReader(this.jsonFile));
		}
	}
	
}
