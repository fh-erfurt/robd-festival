package de.bord.festival;

public class CampingTicket extends Ticket {
    private String campingPlaceName;
    public CampingTicket(TicketType ticketType, int id, String description,
                         boolean available, double standardPrice, String campingPlaceName, Event event){

        super(ticketType, id, description, available, standardPrice, event);
        this.campingPlaceName=campingPlaceName;

    }
}