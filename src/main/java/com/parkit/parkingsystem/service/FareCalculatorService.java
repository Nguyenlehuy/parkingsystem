package com.parkit.parkingsystem.service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;

import com.parkit.parkingsystem.constants.Fare;
import com.parkit.parkingsystem.model.Ticket;

public class FareCalculatorService {

	private static final double RATE_DISCOUNT = 0.95;

	public void calculateFare(Ticket ticket) {
		if ((ticket.getOutTime() == null) || (ticket.getOutTime().before(ticket.getInTime()))) {
			throw new IllegalArgumentException("Out time provided is incorrect:" + ticket.getOutTime().toString());
		}

		LocalDateTime inTime = ticket.getInTime().toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
		LocalDateTime outTime = ticket.getOutTime().toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();

		Duration duration = Duration.between(inTime, outTime);
		long durationMinutes = duration.toMinutes();

		long durationHour = duration.toHours();

		double price;
		double rate = 1;

		if (durationHour == 0 && (durationMinutes <= 30)) {
			rate = 0;
		}
		if ((durationHour == 0) && (30 < durationMinutes) && (durationMinutes < 60)) {
			rate = 0.75;
		} else {
			rate = durationHour;
		}

		switch (ticket.getParkingSpot().getParkingType()) {
		case CAR: {
			price = rate * Fare.CAR_RATE_PER_HOUR;
			break;
		}
		case BIKE: {
			price = rate * Fare.BIKE_RATE_PER_HOUR;
			break;
		}
		default:
			throw new IllegalArgumentException("Unkown Parking Type");
		}

		// STORY2
		if (ticket.getReUsed()) {
			// discount 5%
			price = price * RATE_DISCOUNT;
		}

		ticket.setPrice(price);
	}
}