# CardHub: Use Case Diagrams

Levente Szabo - 618088

---

## 1. Overall System Use Case Diagram

This diagram shows all major use cases and the actors who interact with them.

```mermaid
graph TB
    Customer((Customer))
    Staff((Store Staff))
    Admin((Administrator))

    subgraph CardHub_System["CardHub System"]
        UC1[UC-1: Register Account]
        UC2[UC-2: Login]
        UC3[UC-3: Browse Cards]
        UC4[UC-4: Search Cards]
        UC5[UC-5: Add to Cart]
        UC6[UC-6: Checkout and Pay]
        UC7[UC-7: Get Price Quote]
        UC8[UC-8: Complete Sell Transaction]
        UC9[UC-9: Add Card to Inventory]
        UC10[UC-10: Update Card Price]
        UC11[UC-11: Post Trade Offer]
        UC12[UC-12: Browse Trade Offers]
        UC13[UC-13: Post Tournament]
        UC14[UC-14: View Tournaments]
    end

    Customer --> UC1
    Customer --> UC2
    Customer --> UC3
    Customer --> UC4
    Customer --> UC5
    Customer --> UC6
    Customer --> UC7
    Customer --> UC11
    Customer --> UC12
    Customer --> UC13
    Customer --> UC14

    Staff --> UC2
    Staff --> UC3
    Staff --> UC4
    Staff --> UC8
    Staff --> UC9
    Staff --> UC10

    Admin --> UC2
    Admin --> UC9
    Admin --> UC10
```

---

## 2. Customer Use Cases

This diagram focuses on the features available to customers.

```mermaid
graph LR
    Customer((Customer))

    subgraph Customer_Features["Customer Features"]
        UC1[Register Account]
        UC2[Login]
        UC3[Browse Cards]
        UC4[Search Cards]
        UC5[Add to Cart]
        UC6[Checkout and Pay]
        UC7[Get Price Quote<br/>for Card to Sell]
        UC11[Post Trade Offer]
        UC12[Browse Trade Offers]
        UC13[Post Tournament]
        UC14[View Tournaments]
    end

    Customer --> UC1
    Customer --> UC2
    Customer --> UC3
    Customer --> UC4
    Customer --> UC5
    Customer --> UC6
    Customer --> UC7
    Customer --> UC11
    Customer --> UC12
    Customer --> UC13
    Customer --> UC14
```

---

## 3. Store Staff Use Cases

This diagram shows the features available to store staff members.

```mermaid
graph LR
    Staff((Store Staff))

    subgraph Staff_Features["Store Staff Features"]
        UC2[Login]
        UC3[Browse Cards]
        UC4[Search Cards]
        UC8[Complete Sell<br/>Transaction]
        UC9[Add Card to<br/>Inventory]
        UC10[Update Card<br/>Price]
    end

    Staff --> UC2
    Staff --> UC3
    Staff --> UC4
    Staff --> UC8
    Staff --> UC9
    Staff --> UC10
```

---

## 4. Administrator Use Cases

This diagram shows the features available to administrators.

```mermaid
graph LR
    Admin((Administrator))

    subgraph Admin_Features["Administrator Features"]
        UC2[Login]
        UC9[Add Card to<br/>Inventory]
        UC10[Update Card<br/>Price]
    end

    Admin --> UC2
    Admin --> UC9
    Admin --> UC10
```

---

## Use Case Summary

| Use Case ID | Use Case Name | Primary Actor(s) | Description |
|-------------|---------------|------------------|-------------|
| UC-1 | Register Account | Customer | Create new customer account |
| UC-2 | Login | Customer, Staff, Administrator | Login to system |
| UC-3 | Browse Cards | Customer, Staff | Browse card inventory |
| UC-4 | Search Cards | Customer, Staff | Search for specific cards |
| UC-5 | Add to Cart | Customer | Add cards to shopping cart |
| UC-6 | Checkout and Pay | Customer | Complete purchase with in-store pickup |
| UC-7 | Get Price Quote | Customer | Get instant price quote to sell card |
| UC-8 | Complete Sell Transaction | Staff | Complete sell transaction in store |
| UC-9 | Add Card to Inventory | Staff, Administrator | Add card to inventory |
| UC-10 | Update Card Price | Staff, Administrator | Update price for a card |
| UC-11 | Post Trade Offer | Customer | Post offer on trading marketplace |
| UC-12 | Browse Trade Offers | Customer | Browse and contact traders |
| UC-13 | Post Tournament | Customer | Post tournament announcement and reserve room |
| UC-14 | View Tournaments | Customer | View upcoming tournaments |
