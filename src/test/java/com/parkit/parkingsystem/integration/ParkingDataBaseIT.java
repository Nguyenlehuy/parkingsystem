package com.parkit.parkingsystem.integration;

import static org.mockito.Mockito.when;

import java.util.Date;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import com.parkit.parkingsystem.constants.ParkingType;
import com.parkit.parkingsystem.dao.ParkingSpotDAO;
import com.parkit.parkingsystem.dao.TicketDAO;
import com.parkit.parkingsystem.model.ParkingSpot;
import com.parkit.parkingsystem.model.Ticket;
import com.parkit.parkingsystem.service.FareCalculatorService;
import com.parkit.parkingsystem.service.ParkingService;
import com.parkit.parkingsystem.util.InputReaderUtil;

@ExtendWith(MockitoExtension.class)
public class ParkingDataBaseIT {

	@Mock
	private static ParkingSpotDAO parkingSpotDAO;
	@Mock
	private static TicketDAO ticketDAO;

	@Mock
	private static InputReaderUtil inputReaderUtil;
	@Mock
	private static FareCalculatorService fareCalculatorService;

	@BeforeAll
	private static void setUp() throws Exception {
		parkingSpotDAO = new ParkingSpotDAO();
		ticketDAO = new TicketDAO();

	}

	@AfterAll
	private static void tearDown() {

	}

	@Test
	public void testParkingACar() throws Exception {
		when(inputReaderUtil.readSelection()).thenReturn(1);
		when(inputReaderUtil.readVehicleRegistrationNumber()).thenReturn("ABCDEF");
		ParkingService parkingService = new ParkingService(inputReaderUtil, parkingSpotDAO, ticketDAO);
		when(parkingSpotDAO.getNextAvailableSlot(ParkingType.CAR)).thenReturn(2);

		parkingService.processIncomingVehicle();

		// check that a ticket is actually saved in DB and Parking table is updated with
		// availability
		Mockito.verify(parkingSpotDAO).updateParking(Mockito.any(ParkingSpot.class));
		Mockito.verify(ticketDAO).saveTicket(Mockito.any(Ticket.class));
	}

	@Test
	public void testParkingLotExit() throws Exception {
		testParkingACar();
		Ticket ticket = new Ticket();
		ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.CAR, false);
		ticket.setParkingSpot(parkingSpot);
		Date inTime = new Date();
		inTime.setTime(System.currentTimeMillis() - (45 * 60 * 1000));// 45 minutes parking time should give 3/4th
																		// parking fare
		ticket.setInTime(inTime);
		when(ticketDAO.getTicket("ABCDEF")).thenReturn(ticket);
		when(ticketDAO.updateTicket(ticket)).thenReturn(true);
		ParkingService parkingService = new ParkingService(inputReaderUtil, parkingSpotDAO, ticketDAO);

		parkingService.processExitingVehicle();
		
		// check that the fare generated and out time are populated correctly in the
		// database
		Mockito.verify(ticketDAO).updateTicket(ticket);
		Mockito.verify(parkingSpotDAO).updateParking(parkingSpot);
		
	}

}
