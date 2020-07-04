package de.bord.festival.models;

import de.bord.festival.eventManagement.IEvent;
import de.bord.festival.exception.*;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.LinkedList;
import java.util.List;

/**
 * Represents a festival with required features
 *
 * example (ArrayList<PriceLevel> priceLevels) for sensible TicketManager creation:
 * TicketManager
 * PriceLevels[0]: PercentageForPricelevel=25.00, dayTicketPrice=30.00, CampingTicketPrice=40.00, VipTicketPrice=60.00
 * PriceLevels[1]: PercentageForPricelevel=52.25, dayTicketPrice=30.00, CampingTicketPrice=49.00, VipTicketPrice=80.00
 * PriceLevels[2]: PercentageForPricelevel=89.99, dayTicketPrice=40.99, CampingTicketPrice=51.49, VipTicketPrice=89.55
 * PriceLevels[3]: PercentageForPricelevel=100.00, dayTicketPrice=45.00, CampingTicketPrice=55.00, VipTicketPrice=101.12
 *
 * it is recommended to set the last PercentageForPricelevel to 100.00
 */
@Entity
public class Event extends AbstractModel implements IEvent {
    @NotNull
    @OneToOne(cascade = CascadeType.ALL)
    private TicketManager ticketManager;
    @NotNull
    @Size(min=2, max=50)
    private String name;
    @NotNull
    private double budget;
    @Transient
    private double actualCosts = 0;
    @NotNull
    @OneToOne(cascade = CascadeType.ALL)
    private LineUp lineUp;
    @OneToMany(cascade = CascadeType.ALL)
    private List<Client> client;
    private int maxCapacity;
    @NotNull
    @OneToOne(cascade = CascadeType.ALL)
    private Address address;

    public Event() {}

    private Event(LocalTime startTime, LocalTime endTime, long breakBetweenTwoBandsInMinute, LocalDate startDate, LocalDate endDate, String name,
                  double budget, int maxCapacity, Stage stage, TicketManager ticketManager, Address address){

        lineUp = new LineUp(startTime, endTime, breakBetweenTwoBandsInMinute, startDate, endDate, stage, this);
        client = new LinkedList<>();
        this.maxCapacity = maxCapacity;
        this.budget = budget;
        this.name = name;
        this.ticketManager = ticketManager;
        this.address=address;

    }

    /**
     * Using static function to avoid exception in constructor of event
     *
     * @throws DateDisorderException if end date<start date
     */
    public static Event getNewEvent(LocalTime startTime, LocalTime endTime, long breakBetweenTwoBandsInMinute, LocalDate startDate, LocalDate endDate, String name,
                                    double budget, int maxCapacity, Stage stage, TicketManager ticketManager, Address address) throws DateDisorderException {
        if (endDate.isBefore(startDate)) {
            throw new DateDisorderException("End date can't be before start date");
        }

        return new Event(startTime, endTime, breakBetweenTwoBandsInMinute, startDate, endDate, name, budget, maxCapacity, stage, ticketManager, address);

    }
    public int getNumberOfBands() {
        return lineUp.getNumberOfBands();
    }

    public int getNumberOfStages() {
        return lineUp.getNumberOfStages();
    }

    public int getNumberOfDays() {
        return lineUp.getNumberOfDays();
    }

    /**
     * detects if the price of band is affordable for the event budget
     *
     * @param band provides the price (with the function getPrice), which should be compared in method
     * @return true if the band is affordable for the budget of an event, otherwise false
     */
    private boolean isNewBandAffordable(Band band) {
        return actualCosts + band.getPricePerEvent() <= budget;
    }

    /**
     * see lineUp.addStage(Stage stage)
     *
     * @param stage
     * @return true, if the stage already exist in event, otherwise return false
     */
    public boolean addStage(Stage stage) {
        return lineUp.addStage(stage);
    }

    /**
     * Removes stage from all programs and from the list of stages
     * Removes stage, if it has no set time slots and the number of stages > 1
     *
     * @param id id, on which the stage should be removed
     * @return true, if the stage is removed, otherwise false
     */
    public boolean removeStage(int id) {
        return lineUp.removeStage(id);
    }

    /**
     * Adds band to the event
     *
     * @param band           object, which should be added
     * @param minutesOnStage minutes the given band wants play on the stage
     * @return the information, which is relevant for band: stage, date, time, if the timeSlot is found,
     * otherwise null
     * @throws BudgetOverflowException, if the band is to expensive
     * @throws TimeSlotCantBeFoundException,   if the band plays on another stage at the same time
     */
    public EventInfo addBand(Band band, long minutesOnStage) throws BudgetOverflowException, TimeSlotCantBeFoundException {
        if (!isNewBandAffordable(band)) {
            throw new BudgetOverflowException("The budget is not enough for this band");
        }
        EventInfo eventInfo = lineUp.addBand(band, minutesOnStage);
        if (eventInfo != null) {
            band.addEventInfo(eventInfo);
            return eventInfo;

        }
        else{
            throw new TimeSlotCantBeFoundException("There is not found any time slot");
        }

    }

    /**
     * Removes the band from entire event: from all programs and timeslots
     *
     * @param band the band, that should be removed
     * @return true, if the band is removed, otherwise false
     */
    public boolean removeBand(Band band) {
        if (lineUp.removeBand(band)) {
            actualCosts -= band.getPricePerEvent();
            band.removeEventInfo();
            return true;
        }
        return false;
    }

    /**
     * Removes the band only from given date, time and stage
     *
     * @param band        the band, that should be removed
     * @param dateAndTime date and time, on which the band should be removed
     * @return
     */
    public boolean removeBand(Band band, LocalDateTime dateAndTime) {

        if (this.lineUp.removeBand(band, dateAndTime)) {
            band.removeEventInfo(dateAndTime);
            //if band does not play on event anymore
            if (playsBandOnEvent(band)) {
                actualCosts -= band.getPricePerEvent();
            }
            return true;
        }
        return false;
    }
    public boolean playsBandOnEvent(Band band){
        return (band.getNumberOfEventInfo() == 0);
    }

    /**
     * Adds to the actual costs variable
     * called by Lineup when a new band is added to the event
     *
     * @param amount
     */
    public void addToTheActualCosts(double amount) {
        actualCosts += amount;
    }

    public int getNumberOfDayTickets() { return ticketManager.getNumberOfDayTickets(); }
    public int getNumberOfCampingTickets() { return ticketManager.getNumberOfCampingTickets(); }
    public int getNumberOfVipTickets() { return ticketManager.getNumberOfVipTickets(); }
    public int totalNumberOfTickets(){return ticketManager.totalNumberOfTickets();}

    public int getNumberOfSoldDayTickets(){ return ticketManager.getNumberOfSoldDayTickets();}
    public int getNumberOfSoldCampingTickets(){ return ticketManager.getNumberOfSoldCampingTickets();}
    public int getNumberOfSoldVipTickets(){ return ticketManager.getNumberOfSoldVipTickets();}
    public int totalNumberOfSoldTickets(){ return ticketManager.totalNumberOfSoldTickets();}
    public double totalNumberOfSoldTicketsInPercent(){return ticketManager.totalNumberOfSoldTicketsInPercent();}

    public int getNumberOfDayTicketsLeft() { return ticketManager.getNumberOfDayTicketsLeft(); }
    public int getNumberOfCampingTicketsLeft() { return ticketManager.getNumberOfCampingTicketsLeft(); }
    public int getNumberOfVipTicketsLeft() { return ticketManager.getNumberOfVipTicketsLeft(); }
    public int totalNumberOfTicketsLeft(){return ticketManager.totalNumberOfTicketsLeft();}

    public void setTicketStdPrice(double stdPrice, Ticket.TicketType type){
        this.ticketManager.setTicketStdPrice(stdPrice, type);
    }

    public void setTicketDescription(String description, Ticket.TicketType type){
        this.ticketManager.setTicketDescription(description, type);
    }

    public void sellTickets(Client client) throws TicketNotAvailableException {
        ticketManager.sellTickets(client);
    }

    public double getIncomeTicketSales(){
        return ticketManager.getIncomeTicketSales();
    }

    public int getActualPriceLevelIndex(){return ticketManager.getActualPriceLevelIndex();}

    /**
     *
     * @param index
     * @return the percentage of the selected priceLevel that must be exceeded to activate the next price level
     */
    public double getPercentageForPriceLevel(int index) throws PriceLevelNotAvailableException {
        return ticketManager.getPercentageForPriceLevel(index);
    }

    /**
     *
     * @param isPriceLevelChangeAutomatic true for automatic, false for manually price level change
     */
    public void setAutomaticPriceLevelChange(boolean isPriceLevelChangeAutomatic) {
        ticketManager.setAutomaticPriceLevelChange(isPriceLevelChangeAutomatic);
    }

    /**
     * shows whether the price level changes automatically
     * @return
     */
    public boolean getAutomaticPriceLevelChange(){
        return ticketManager.getAutomaticPriceLevelChange();
    }

    public boolean setPriceLevel(int index) throws PriceLevelNotAvailableException {return ticketManager.setPriceLevel(index);}

    public String getName() {
        return name;
    }
    public int getMaxCapacity() {
        return maxCapacity;
    }

    public double getBudget() {
        return budget;
    }
    public LocalTime getStartTime() {
        return this.lineUp.getStartTime();
    }

    public LocalTime getEndTime() {
        return lineUp.getEndTime();
    }

    public Address getAddress() {
        return address;
    }
    public List<Band> getBands(){
        return lineUp.getBands();
    }

}