/**
 * Copyright (c) 2014 Digi International Inc.,
 * All rights not expressly granted are reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 *
 * Digi International Inc. 11001 Bren Road East, Minnetonka, MN 55343
 * =======================================================================
 */
package com.digi.xbee.api;

import static org.junit.Assert.*;

import java.io.IOException;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import com.digi.xbee.api.connection.serial.SerialPortRxTx;
import com.digi.xbee.api.exceptions.InterfaceNotOpenException;
import com.digi.xbee.api.exceptions.InvalidOperatingModeException;
import com.digi.xbee.api.exceptions.OperationNotSupportedException;
import com.digi.xbee.api.exceptions.XBeeException;
import com.digi.xbee.api.models.OperatingMode;
import com.digi.xbee.api.models.XBee16BitAddress;
import com.digi.xbee.api.models.XBee64BitAddress;
import com.digi.xbee.api.packet.XBeeAPIPacket;
import com.digi.xbee.api.packet.common.TransmitPacket;

@RunWith(PowerMockRunner.class)
@PrepareForTest({ZigBeeDevice.class})
public class SendSerialDataAsyncZigBeeTest {
	
	// Constants.
	private static final XBee16BitAddress XBEE_16BIT_ADDRESS = new XBee16BitAddress("0123");
	private static final XBee64BitAddress XBEE_64BIT_ADDRESS = new XBee64BitAddress("0123456789ABCDEF");
	
	private static final String SEND_DATA = "data";
	private static final byte[] SEND_DATA_BYTES = SEND_DATA.getBytes();
	
	private static final String SEND_XBEE_PACKET_ASYNC_METHOD = "sendXBeePacketAsync";
	
	// Variables.
	private SerialPortRxTx mockedPort;
	private ZigBeeDevice zigbeeDevice;
	private RemoteZigBeeDevice mockedRemoteDevice;
	
	private TransmitPacket transmitPacket;
	
	@Before
	public void setup() throws Exception {
		// Mock an RxTx IConnectionInterface.
		mockedPort = Mockito.mock(SerialPortRxTx.class);
		// When checking if the connection is open, return true.
		Mockito.when(mockedPort.isOpen()).thenReturn(true);
		
		// Instantiate a ZigbeeDevice object with the mocked interface.
		zigbeeDevice = PowerMockito.spy(new ZigBeeDevice(mockedPort));
		
		// Mock Transmit Request packet.
		transmitPacket = Mockito.mock(TransmitPacket.class);
		
		// Mock a RemoteZigBeeDevice to be used as parameter in the send serial data async. command.
		mockedRemoteDevice = Mockito.mock(RemoteZigBeeDevice.class);
		Mockito.when(mockedRemoteDevice.get64BitAddress()).thenReturn(XBEE_64BIT_ADDRESS);
		
		// Whenever a TransmitPacket class is instantiated, the mocked transmitPacket packet should be returned.
		PowerMockito.whenNew(TransmitPacket.class).withAnyArguments().thenReturn(transmitPacket);
	}
	
	/**
	 * Verify that we receive a null pointer exception when either the address or the 
	 * data to be sent is null.
	 */
	@Test
	public void testSendSerialDataAsyncZigBeeInvalidParams() {
		// Try to send serial data with a null 64-bit address.
		try {
			zigbeeDevice.sendSerialDataAsync((XBee64BitAddress)null, XBEE_16BIT_ADDRESS, SEND_DATA_BYTES);
			fail("Serial data shouldn't have been sent successfully.");
		} catch (Exception e) {
			assertEquals(NullPointerException.class, e.getClass());
		}
		// Try to send serial data with a null 16-bit address.
		try {
			zigbeeDevice.sendSerialDataAsync(XBEE_64BIT_ADDRESS, (XBee16BitAddress)null, SEND_DATA_BYTES);
			fail("Serial data shouldn't have been sent successfully.");
		} catch (Exception e) {
			assertEquals(NullPointerException.class, e.getClass());
		}
		// Try to send serial data with a null 64-bit and 16-bit addresses.
		try {
			zigbeeDevice.sendSerialDataAsync((XBee64BitAddress)null, (XBee16BitAddress)null, SEND_DATA_BYTES);
			fail("Serial data shouldn't have been sent successfully.");
		} catch (Exception e) {
			assertEquals(NullPointerException.class, e.getClass());
		}
		// Try to send serial data with a null RemoteZigBeeDevice.
		try {
			zigbeeDevice.sendSerialDataAsync((RemoteZigBeeDevice)null, SEND_DATA_BYTES);
			fail("Serial data shouldn't have been sent successfully.");
		} catch (Exception e) {
			assertEquals(NullPointerException.class, e.getClass());
		} 
		// Try to send serial data with null data. 64-bit/16-bit addresses.
		try {
			zigbeeDevice.sendSerialDataAsync(XBEE_64BIT_ADDRESS, XBEE_16BIT_ADDRESS, null);
			fail("Serial data shouldn't have been sent successfully.");
		} catch (Exception e) {
			assertEquals(NullPointerException.class, e.getClass());
		}
		// Try to send serial data with null data. RemoteZigBeeDevice device.
		try {
			zigbeeDevice.sendSerialDataAsync(mockedRemoteDevice, null);
			fail("Serial data shouldn't have been sent successfully.");
		} catch (Exception e) {
			assertEquals(NullPointerException.class, e.getClass());
		}
	}
	
	/**
	 * Verify that serial data send fails when the connection is closed.
	 */
	@Test
	public void testSendSerialDataAsyncZigBeeConnectionClosed() {
		// When checking if the connection is open, return false.
		Mockito.when(mockedPort.isOpen()).thenReturn(false);
		
		// Send serial data using the 64-bit and 16-bit addresses.
		try {
			zigbeeDevice.sendSerialDataAsync(XBEE_64BIT_ADDRESS, XBEE_16BIT_ADDRESS, SEND_DATA_BYTES);
			fail("Serial data shouldn't have been sent successfully.");
		} catch (Exception e) {
			assertEquals(InterfaceNotOpenException.class, e.getClass());
		}
		// Send serial data using a RemoteZigBeeDevice as parameter.
		try {
			zigbeeDevice.sendSerialDataAsync(mockedRemoteDevice, SEND_DATA_BYTES);
			fail("Serial data shouldn't have been sent successfully.");
		} catch (Exception e) {
			assertEquals(InterfaceNotOpenException.class, e.getClass());
		}
	}
	
	/**
	 * Verify that serial data is considered successfully sent when the {@code sendXBeePacket} method 
	 * does not throw any exception.
	 * 
	 * @throws Exception 
	 */
	@Test
	public void testSendSerialDataAsyncZigBeeSuccess() throws Exception {
		// Stub the sendXBeePacketAsync method to do nothing when called.
		PowerMockito.doNothing().when(zigbeeDevice, SEND_XBEE_PACKET_ASYNC_METHOD, Mockito.eq(transmitPacket));
		
		// Verify that the packet is sent successfully when using the 64-bit and 16-bit addresses.
		zigbeeDevice.sendSerialDataAsync(XBEE_64BIT_ADDRESS, XBEE_16BIT_ADDRESS, SEND_DATA_BYTES);
		// Verify that the packet is sent successfully when using a RemoteZigBeeDevice as parameter.
		zigbeeDevice.sendSerialDataAsync(mockedRemoteDevice, SEND_DATA_BYTES);
		
		// Verify the sendXBeePacketAsync method was called 2 times (one for each data send).
		PowerMockito.verifyPrivate(zigbeeDevice, Mockito.times(2)).invoke(SEND_XBEE_PACKET_ASYNC_METHOD, (XBeeAPIPacket)Mockito.any());
	}
	
	/**
	 * Verify that serial data send fails when the operating mode is AT.
	 */
	@Test
	public void testSendSerialDataAsyncZigBeeInvalidOperatingMode() {
		// Return that the operating mode of the device is AT when asked.
		Mockito.when(zigbeeDevice.getOperatingMode()).thenReturn(OperatingMode.AT);
		
		// Send serial data using the 64-bit and 16-bit addresses.
		try {
			zigbeeDevice.sendSerialDataAsync(XBEE_64BIT_ADDRESS, XBEE_16BIT_ADDRESS, SEND_DATA_BYTES);
			fail("TransmitRequest frame shouldn't have been sent successfully.");
		} catch (Exception e) {
			assertEquals(InvalidOperatingModeException.class, e.getClass());
		}
		// Send serial data using a RemoteZigBeeDevice as parameter.
		try {
			zigbeeDevice.sendSerialDataAsync(mockedRemoteDevice, SEND_DATA_BYTES);
			fail("TransmitRequest frame shouldn't have been sent successfully.");
		} catch (Exception e) {
			assertEquals(InvalidOperatingModeException.class, e.getClass());
		}
	}
	
	/**
	 * Verify that serial data send fails (XBee exception thrown) when the {@code sendXBeePacketAsync} 
	 * method throws an IO exception.
	 * 
	 * @throws Exception 
	 */
	@Test
	public void testSendSerialDataAsyncZigBeeIOError() throws Exception {
		// Throw an IO exception when trying to send an XBee packet asynchronously.
		PowerMockito.doThrow(new IOException()).when(zigbeeDevice, SEND_XBEE_PACKET_ASYNC_METHOD, Mockito.eq(transmitPacket));
		
		// Send serial data using the 64-bit and 16-bit addresses.
		try {
			zigbeeDevice.sendSerialDataAsync(XBEE_64BIT_ADDRESS, XBEE_16BIT_ADDRESS, SEND_DATA_BYTES);
			fail("TransmitRequest frame shouldn't have been sent successfully.");
		} catch (Exception e) {
			assertEquals(XBeeException.class, e.getClass());
			assertEquals(IOException.class, e.getCause().getClass());
		}
		// Send serial data using a RemoteZigBeeDevice as parameter.
		try {
			zigbeeDevice.sendSerialDataAsync(mockedRemoteDevice, SEND_DATA_BYTES);
			fail("TransmitRequest frame shouldn't have been sent successfully.");
		} catch (Exception e) {
			assertEquals(XBeeException.class, e.getClass());
			assertEquals(IOException.class, e.getCause().getClass());
		}
	}
	
	/**
	 * Verify that when trying to send serial data from a remote ZigBee XBee device to 
	 * other device, an OperationNotSupportedException is thrown.
	 */
	@Test
	public void testSendSerialDataAsyncZigBeeFromRemoteDevices() {
		// Return that the XBee device is remote when asked.
		Mockito.when(zigbeeDevice.isRemote()).thenReturn(true);
		
		// Send serial data using the 64-bit and 16-bit addresses.
		try {
			zigbeeDevice.sendSerialData(XBEE_64BIT_ADDRESS, XBEE_16BIT_ADDRESS, SEND_DATA_BYTES);
			fail("TransmitRequest frame shouldn't have been sent successfully.");
		} catch (Exception e) {
			assertEquals(OperationNotSupportedException.class, e.getClass());
		}
		// Send serial data using a RemoteZigBeeDevice as parameter.
		try {
			zigbeeDevice.sendSerialData(mockedRemoteDevice, SEND_DATA_BYTES);
			fail("TransmitRequest frame shouldn't have been sent successfully.");
		} catch (Exception e) {
			assertEquals(OperationNotSupportedException.class, e.getClass());
		}
	}
}