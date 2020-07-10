package de.bord.festival.controllers;

import de.bord.festival.models.Event;
import de.bord.festival.repository.EventRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@Controller
public class BuyTicketController {


    @Autowired EventRepository eventRepository;

////////////////// Event auswählen

    @GetMapping("/buy_ticket")
    public String eventOverwiew(ModelMap model){

      List<Event> events = eventRepository.findAll();
        //String[] flowers = new String[] { "Rose", "Lily", "Tulip", "Carnation", "Hyacinth" };
        model.addAttribute("events", events);


        return "/buy_ticket";
    }
    /*
    <tr th:each="Event : ${events}">
        <td th:text="${events.name}"></td>
        <td th:text="${events.address}"></td>
        <td th:text="*{events.id}"></td>

        <form action="#"  class="form" th:action="@{'/publish-post/'+${post.id}}" method="post">

    </tr>
    */
  ////////////////////////////




    @RequestMapping(value = "/buy_ticket", method= RequestMethod.POST)
    public String ticketOverview(@RequestParam(value = "eventId", required = true) Integer eventId,ModelMap model){
       Event theEvent = eventRepository.findById(eventId);
       model.addAttribute("theEvent", theEvent);

/*
<p th:text="'Campingticket: ' + ${theEvent.getTheActualPricelevel().getCampingTicketPrice()}"></p>
<p th:text="'Dayticket: ' + ${theEvent.getTheActualPricelevel().getDayTicketPrice()}"></p>
<p th:text="'Vipticket: ' +${theEvent.getTheActualPricelevel().getVipTicketPrice()}"></p>
*/


        return "buy_ticket2";
    }

    @GetMapping("/buy_ticket2")
    public String blaaa(ModelMap model){

        return "buy_ticket2";
    }

   // @PostMapping("/")
}
