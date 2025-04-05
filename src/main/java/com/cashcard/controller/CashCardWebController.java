package com.cashcard.controller;

import com.cashcard.entity.CashCard;
import com.cashcard.repository.CashCardRepository;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/cashcards") // Keep the UI path here
public class CashCardWebController {

    private final CashCardRepository cashCardRepository;

    public CashCardWebController(CashCardRepository cashCardRepository) {
        this.cashCardRepository = cashCardRepository;
    }

    @GetMapping(produces = MediaType.TEXT_HTML_VALUE)
    @ResponseBody
    public String viewAllCashCardsHtml() {
        List<CashCard> cashCards = cashCardRepository.findAll();
        StringBuilder html = new StringBuilder("<!DOCTYPE html>");
        html.append("<html lang='en'><head><meta charset='UTF-8'>");
        html.append("<meta name='viewport' content='width=device-width, initial-scale=1.0'>");
        html.append("<title>Your Family</title>");
        html.append("<link rel='stylesheet' href='/css/style.css'></head><body>");
        html.append("<div class='container'>");
        html.append("<header class='family-header'>");
        html.append("<h1>Your Family</h1>");
        html.append("<button class='new-member-button'>+ New Member</button>");
        html.append("</header>");
        html.append("<div class='card-grid'>");
        for (CashCard card : cashCards) {
            html.append("<div class='cash-card'>");
            html.append("<div class='card-top'>");
            html.append("<div class='member-name'>").append(card.getKidName()).append("</div>");
            html.append("<div class='balance'>$").append(String.format("%.2f", card.getBalance())).append("</div>");
            html.append("</div>");
            html.append("<div class='card-bottom'>");
            html.append("<div class='card-type'>Family Cash Card</div>");
            html.append("<button class='edit-member'>Edit Member</button>");
            html.append("</div>");
            // Image handling would go here
            html.append("</div>");
        }
        html.append("<div class='add-new-placeholder'>");
        html.append("</div>");
        html.append("</div>"); // close card-grid
        html.append("</div>"); // close container

        // Basic modal for adding a new member (initially hidden)
        html.append("<div id='addMemberModal' class='modal' style='display:none;'>");
        html.append("<form action='/cashcards/add' method='post' class='add-member-form'>");
        html.append("<h2>Add New Family Member</h2>");
        html.append("<div class='form-group'><label for='kidName'>Name:</label><input type='text' id='kidName' name='kidName' required/></div>");
        html.append("<div class='form-group'><label for='balance'>Initial Balance:</label><input type='number' id='balance' name='balance' step='0.01' value='0.00' required/></div>");
        html.append("<div class='form-group'><label for='parentName'>Parent Name:</label><input type='text' id='parentName' name='parentName'/></div>");
        html.append("<button type='submit' class='submit-button'>Add</button>");
        html.append("<button type='button' class='cancel-button' onclick=\"document.getElementById('addMemberModal').style.display='none'\">Cancel</button>");
        html.append("</form>");
        html.append("</div>");

        html.append("<script>");
        html.append("document.querySelector('.new-member-button').addEventListener('click', function() {");
        html.append("document.getElementById('addMemberModal').style.display = 'block';");
        html.append("});");
        html.append("</script>");

        html.append("</body></html>");
        return html.toString();
    }

    @GetMapping(value = "/{id}", produces = MediaType.TEXT_HTML_VALUE)
    @ResponseBody
    public String findCashCardHtml(@PathVariable Long id) {
        Optional<CashCard> cashCardOptional = cashCardRepository.findById(id);
        if (cashCardOptional.isPresent()) {
            CashCard card = cashCardOptional.get();
            return "<!DOCTYPE html><html><head><title>" + card.getKidName() + "'s Card</title><link rel='stylesheet' href='/css/style.css'></head><body><div class='cash-card'><h2>" + card.getKidName() + "</h2><p>Balance: $" + card.getBalance() + "</p></div></body></html>";
        } else {
            return "<!DOCTYPE html><html><head><title>Cash Card Not Found</title></head><body><h1>Cash Card Not Found</h1></body></html>";
        }
    }

    @PostMapping(value = "/add", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE, produces = MediaType.TEXT_HTML_VALUE)
    @ResponseBody
    public String addFamilyMember(CashCard newCashCard) {
        newCashCard.setId(null); // Ensure new ID is generated
        cashCardRepository.save(newCashCard);
        return "<!DOCTYPE html><html><head><title>Member Added</title><meta http-equiv='refresh' content='2;url=/cashcards'></head><body><h1>Member Added Successfully!</h1><p>Redirecting...</p></body></html>";
    }

    @GetMapping("/add-form")
    @ResponseBody
    public String addMemberForm() {
        return "<!DOCTYPE html><html><head><title>Add New Member</title><link rel='stylesheet' href='/css/style.css'></head><body>" +
                "<h1>Add New Family Member</h1>" +
                "<form action='/cashcards/add' method='post'>" +
                "<div><label for='kidName'>Name:</label><input type='text' id='kidName' name='kidName' required/></div>" +
                "<div><label for='balance'>Initial Balance:</label><input type='number' id='balance' name='balance' step='0.01' value='0.00' required/></div>" +
                "<div><label for='parentName'>Parent Name:</label><input type='text' id='parentName' name='parentName'/></div>" +
                "<button type='submit'>Add Member</button>" +
                "</form><a href='/cashcards'>Back to Family Cards</a>" +
                "</body></html>";
    }
}