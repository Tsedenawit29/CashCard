package com.cashcard.controller;

import com.cashcard.entity.CashCard;
import com.cashcard.repository.CashCardRepository;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/cashcards")
public class CashCardWebController {

    private final CashCardRepository cashCardRepository;
    private final List<String> cardColors = List.of("#fce4ec", "#e1f5fe", "#e8f5e9", "#fff9c4", "#f0f4c3", "#f5f5f5"); // Light pastel colors

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
        html.append("<button id='showAddMembersForm' class='new-member-button'>+ New Member</button>");
        html.append("</header>");
        html.append("<div class='card-grid'>");
        for (int i = 0; i < cashCards.size(); i++) {
            CashCard card = cashCards.get(i);
            String cardColor = cardColors.get(i % cardColors.size());
            html.append("<div class='cash-card' style='background-color: ").append(cardColor).append("; color: #333;'>");
            html.append("<div class='card-top'>");
            html.append("<div class='member-name' style='color: #2c3e50;'>").append(card.getKidName()).append("</div>");
            html.append("<div class='balance' style='color: #27ae60;'>$").append(String.format("%.2f", card.getBalance())).append("</div>");
            html.append("</div>");
            html.append("<div class='card-bottom' style='border-top: 1px solid #e0e0e0;'>");
            html.append("<div class='card-type' style='color: #777;'>Family Cash Card</div>");
            html.append("<a href='/cashcards/edit-form/" + card.getId() + "' class='edit-member'>Edit Member</a>");
            html.append("</div>");
            html.append("</div>");
        }
        html.append("</div>"); // close card-grid

        // Initially hidden "Add Members & Cash" form
        html.append("<div id='addMembersForm' class='modal'>");
        html.append("<h1>Add New Members & Cash</h1>");
        html.append("<form action='/cashcards/process-member' method='post' class='add-multiple-members-form'>");
        html.append("<div class='members-list'>");
        html.append("<div class='form-header'>");
        html.append("<div>Family Member</div>");
        html.append("<div>Card Amount</div>");
        html.append("</div>");
        html.append("<div class='member-input'>");
        html.append("<input type='text' name='memberName' placeholder='Member Name' required/>");
        html.append("<div class='amount-input'>");
        html.append("$ <input type='number' name='cardAmount' value='100.00' step='0.01' required/>");
        html.append("</div>");
        html.append("</div>");
        html.append("</div>"); // close members-list
        html.append("<div class='form-navigation'>");
        html.append("<button type='button' class='cancel-button' onclick=\"document.getElementById('addMembersForm').style.display='none'\">Cancel</button>");
        html.append("<button type='submit' class='continue-button'>Add Member</button>");
        html.append("</div>");
        html.append("</form>");
        html.append("</div>"); // close addMembersForm

        html.append("<script>");
        html.append("document.addEventListener('DOMContentLoaded', function() {");
        html.append("const showAddMembersFormButton = document.getElementById('showAddMembersForm');");
        html.append("const addMembersForm = document.getElementById('addMembersForm');");
        html.append("if (showAddMembersFormButton && addMembersForm) {");
        html.append("showAddMembersFormButton.addEventListener('click', function() {");
        html.append("addMembersForm.style.display = 'block';");
        html.append("});");
        html.append("}");
        html.append("const modal = document.getElementById('addMembersForm');");
        html.append("if (modal) {");
        html.append("window.addEventListener('click', function(event) {");
        html.append("if (event.target == modal) {");
        html.append("modal.style.display = 'none';");
        html.append("}");
        html.append("});");
        html.append("}");
        // Remove the JavaScript for dynamically adding multiple members
        html.append("});");
        html.append("</script>");
        html.append("</div></body></html>");
        return html.toString();
    }

    @PostMapping("/process-member")
    @ResponseBody
    public String processMember(@RequestParam("memberName") String memberName,
                                @RequestParam("cardAmount") String cardAmount,
                                RedirectAttributes redirectAttributes) {
        System.out.println("Received memberName: " + memberName);
        System.out.println("Received cardAmount: " + cardAmount);

        CashCard newCashCard = new CashCard();
        newCashCard.setKidName(memberName);
        try {
            BigDecimal balance = new BigDecimal(cardAmount);
            newCashCard.setBalance(balance);
            cashCardRepository.save(newCashCard);
            return "<!DOCTYPE html><html><head><title>Member Added</title><meta http-equiv='refresh' content='2;url=/cashcards'></head><body><h1>Member and Cash Added!</h1><p>Redirecting...</p></body></html>";
        } catch (NumberFormatException e) {
            return "Error: Invalid amount '" + cardAmount + "' for member '" + memberName + "'. Please enter a valid number.";
        }
    }

    @GetMapping(value = "/{id}", produces = MediaType.TEXT_HTML_VALUE)
    @ResponseBody
    public String findCashCardHtml(@PathVariable Long id) {
        Optional<CashCard> cashCardOptional = cashCardRepository.findById(id);
        if (cashCardOptional.isPresent()) {
            CashCard card = cashCardOptional.get();
            return "<!DOCTYPE html><html><head><title>" + card.getKidName() + "'s Card</title><link rel='stylesheet' href='/css/style.css'></head><body><div class='container'><div class='cash-card' style='background-color: " + cardColors.get((int) (card.getId() % cardColors.size())) + "; color: #333;'><div class='member-name'>" + card.getKidName() + "</div><div class='balance'>$" + String.format("%.2f", card.getBalance()) + "</div><div class='card-bottom'><div class='card-type'>Family Cash Card</div></div><p><a href='/cashcards'>Back to Family Cards</a></p></div></body></html>";
        } else {
            return "<!DOCTYPE html><html><head><title>Cash Card Not Found</title></head><body><div class='container'><h1>Cash Card Not Found</h1><p><a href='/cashcards'>Back to Family Cards</a></p></body></html>";
        }
    }

    @GetMapping("/add-form")
    @ResponseBody
    public String addMemberForm() {
        StringBuilder html = new StringBuilder("<!DOCTYPE html>");
        html.append("<html lang='en'><head><meta charset='UTF-8'>");
        html.append("<meta name='viewport' content='width=device-width, initial-scale=1.0'>");
        html.append("<title>Add New Member</title>");
        html.append("<link rel='stylesheet' href='/css/style.css'></head><body>");
        html.append("<div class='container'>");
        html.append("<h1>Add New Family Member</h1>");
        html.append("<form action='/cashcards/add' method='post' class='add-edit-form'>");
        html.append("<div class='form-group'><label for='kidName'>Name:</label><input type='text' id='kidName' name='kidName' required/></div>");
        html.append("<div class='form-group'><label for='balance'>Initial Balance:</label><input type='number' id='balance' name='balance' step='0.01' value='0.00' required/></div>");
        html.append("<div class='form-group'><label for='parentName'>Parent Name:</label><input type='text' id='parentName' name='parentName'/></div>");
        html.append("<button type='submit' class='submit-button'>Add Member</button>");
        html.append("<a href='/cashcards' class='cancel-button'>Cancel</a>");
        html.append("</form>");
        html.append("</div></body></html>");
        return html.toString();
    }

    @PostMapping(value = "/add", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE, produces = MediaType.TEXT_HTML_VALUE)
    @ResponseBody
    public String addFamilyMember(CashCard newCashCard) {
        System.out.println("Received newCashCard: " + newCashCard); // Add this line
        cashCardRepository.save(newCashCard);
        return "<!DOCTYPE html><html><head><title>Member Added</title><meta http-equiv='refresh' content='2;url=/cashcards'></head><body><h1>Member Added Successfully!</h1><p>Redirecting...</p></body></html>";
    }

    @GetMapping(value = "/edit-form/{id}", produces = MediaType.TEXT_HTML_VALUE)
    @ResponseBody
    public String showEditForm(@PathVariable Long id) {
        Optional<CashCard> cashCardOptional = cashCardRepository.findById(id);
        if (cashCardOptional.isPresent()) {
            CashCard card = cashCardOptional.get();
            StringBuilder html = new StringBuilder("<!DOCTYPE html>");
            html.append("<html lang='en'><head><meta charset='UTF-8'>");
            html.append("<meta name='viewport' content='width=device-width, initial-scale=1.0'>");
            html.append("<title>Edit Member: ").append(card.getKidName()).append("</title>");
            html.append("<link rel='stylesheet' href='/css/style.css'></head><body>");
            html.append("<div class='container edit-container'>");
            html.append("<div class='edit-header'>");
            html.append("<a href='/cashcards' class='back-link'>&larr;</a>");
            html.append("<h1>Editing ").append(card.getKidName()).append("</h1>");
            html.append("</div>");
            html.append("<div class='edit-panel'>");
            html.append("<div class='edit-form-section'>");
            html.append("<form action='/cashcards/update/" + card.getId() + "' method='post' class='add-edit-form'>");
            html.append("<div class='form-group'><label for='kidName'>Name</label><input type='text' id='kidName' name='kidName' value='").append(card.getKidName()).append("' required/></div>");
            html.append("<div class='form-group'><label for='balance'>Balance</label><input type='number' id='balance' name='balance' step='0.01' value='").append(String.format("%.2f", card.getBalance())).append("' required/></div>");
            html.append("<div class='form-group'><label for='parentName'>Parent Name</label><input type='text' id='parentName' name='parentName' value='").append(card.getParentName() == null ? "" : card.getParentName()).append("'/></div>");
            html.append("<div class='button-group'>");
            html.append("<button type='submit' class='save-button'>Save Changes</button>");
            html.append("<a href='/cashcards' class='cancel-button'>Cancel</a>");
            html.append("</div>");
            html.append("</form>");
            html.append("</div>");
            html.append("<div class='card-preview-section'>");
            html.append("<div class='cash-card preview' style='background-color: ").append(cardColors.get((int) (card.getId() % cardColors.size()))).append("; color: #333;'>");
            html.append("<div class='card-top'>");
            html.append("<div class='member-name'>").append(card.getKidName()).append("</div>");
            html.append("<div class='card-type'>Family Cash Card</div>");
            html.append("</div>");
            html.append("</div>");
            html.append("<button class='delete-button' onclick=\"window.location.href='/cashcards/delete/" + card.getId() + "'\">Delete Member</button>");
            html.append("</div>");
            html.append("</div>");
            html.append("</div></body></html>");
            return html.toString();
        } else {
            return "<!DOCTYPE html><html><head><title>Cash Card Not Found</title></head><body><div class='container'><h1>Cash Card Not Found</h1><p><a href='/cashcards'>Back to Family Cards</a></p></body></html>";
        }
    }

    @PostMapping(value = "/update/{id}", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE, produces = MediaType.TEXT_HTML_VALUE)
    @ResponseBody
    public String updateCashCard(@PathVariable Long id, CashCard updatedCashCard) {
        Optional<CashCard> existingCashCardOptional = cashCardRepository.findById(id);
        if (existingCashCardOptional.isPresent()) {
            CashCard existingCard = existingCashCardOptional.get();
            existingCard.setKidName(updatedCashCard.getKidName());
            existingCard.setBalance(updatedCashCard.getBalance());
            existingCard.setParentName(updatedCashCard.getParentName()); // Update other relevant fields
            cashCardRepository.save(existingCard);
            return "<!DOCTYPE html><html><head><title>Member Updated</title><meta http-equiv='refresh' content='2;url=/cashcards'></head><body><h1>Member Updated Successfully!</h1><p>Redirecting...</p></body></html>";
        } else {
            return "<!DOCTYPE html><html><head><title>Cash Card Not Found</title></head><body><div class='container'><h1>Cash Card Not Found</h1><p><a href='/cashcards'>Back to Family Cards</a></p></body></html>";
        }
    }
}