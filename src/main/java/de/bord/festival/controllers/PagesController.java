package de.bord.festival.controllers;

import de.bord.festival.exception.*;
import de.bord.festival.helper.HelpClasses;
import de.bord.festival.models.Event;
import de.bord.festival.repository.ClientRepository;
import de.bord.festival.repository.EventRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Container for all pages that have no certain mapping for its functionalities
 * Are used to add title or something else
 */
@Controller
public class PagesController {
    @Autowired
    EventRepository eventRepository;
    @Autowired
    private ClientRepository clientRepository;
    @GetMapping("/")
    public String index(Model model) throws BudgetOverflowException, TimeSlotCantBeFoundException, PriceLevelException, TimeDisorderException, DateDisorderException {
        model.addAttribute("title", "Home");

        HelpClasses helpClasses = new HelpClasses();
        Event event1 = helpClasses.getValidNDaysEvent2(5);
        event1.addStage(helpClasses.getStage());
        Event newEvent= eventRepository.save(event1);

        return "index";
    }
    @GetMapping("/error404")
    public String error(Model model) {
        model.addAttribute("title", "Error Page");
        return "error";
    }
    @GetMapping("/admin_menu")
    public String adminMenu(Model model) {
        model.addAttribute("title", "Menu");
        return "admin_menu";
    }
}
