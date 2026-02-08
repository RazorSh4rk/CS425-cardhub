# CardHub: Collaboration and VOPC Diagrams

Levente Szabo - 618088

## 1. Customer Login

### Collaboration Diagram

The collaboration diagram shows how objects interact during the login process. Numbers indicate message sequence.

```mermaid
flowchart LR
    subgraph Actors
        Customer((Customer))
    end

    subgraph Boundary
        LoginForm[LoginForm]
    end

    subgraph Control
        LoginController[LoginController]
    end

    subgraph Entity
        CustomerEntity[(Customer)]
        Session[(Session)]
    end

    Customer -->|1. navigateToLogin 2. enterCredentials| LoginForm
    LoginForm -->|2.1. authenticate| LoginController
    LoginController -->|2.1.1. findByEmail 2.1.8. updateLastLoginTime| CustomerEntity
    LoginController -->|2.1.6. createSession| Session
    LoginForm -.->|1.1. displayLoginForm 2.3. redirectToDashboard 2.4. displayWelcomeMessage| Customer
```

**Message Flow:**
- 1. Customer navigates to login page
  - 1.1. LoginForm displays the login form
- 2. Customer enters email and password
  - 2.1. LoginForm sends credentials to LoginController for authentication
    - 2.1.1. LoginController looks up customer by email
    - 2.1.3-2.1.5. LoginController verifies password and account status
    - 2.1.6. LoginController creates a new session
    - 2.1.8. LoginController updates last login time
    - 2.2-2.4. Success response flows back to customer

---

### VOPC Diagram

```mermaid
classDiagram
    class LoginForm {
        +navigateToLogin()
        +enterCredentials(email, password)
        +displayLoginForm()
        +redirectToDashboard(role)
        +displayWelcomeMessage(name)
    }

    class LoginController {
        +authenticate(email, password)
        -verifyPassword(password, storedHash)
        -checkEmailVerified()
        -checkAccountEnabled()
    }

    class Customer {
        -customerId : Number
        -email : String
        -passwordHash : String
        -name : String
        -role : String
        -emailVerified : Boolean
        -accountEnabled : Boolean
        -lastLoginTime : Date
        +findByEmail(email)
        +updateLastLoginTime(customerId)
    }

    class Session {
        -sessionId : Number
        -customerId : Number
        -sessionToken : String
        -createdAt : Date
        -expiresAt : Date
        +createSession(customerId)
        +getSession(token)
    }

    LoginForm --> LoginController : sends credentials
    LoginController --> Customer : validates user
    LoginController --> Session : creates session
    Customer "1" -- "0..*" Session : has
```

---

## 2. Customer Purchase Checkout

### Collaboration Diagram

```mermaid
flowchart TB
    subgraph Actors
        Customer((Customer))
    end

    subgraph Boundary
        CheckoutForm[CheckoutForm]
        PaymentForm[PaymentForm]
    end

    subgraph Control
        CheckoutController[CheckoutController]
        PaymentController[PaymentController]
    end

    subgraph Entity
        Cart[(Cart)]
        Inventory[(Inventory)]
        Order[(Order)]
    end

    subgraph External
        PaymentGateway{{PaymentGateway}}
        EmailService{{EmailService}}
    end

    Customer -->|1. clickCheckout 2. enterContactInfo| CheckoutForm
    Customer -->|3. selectPaymentMethod 4. enterPaymentDetails| PaymentForm

    CheckoutForm -->|1.1. initiateCheckout 2.1. validateContactInfo| CheckoutController
    CheckoutController -->|1.1.1. getCartItems| Cart
    CheckoutController -->|1.1.3. checkAvailability| Inventory

    PaymentForm -->|4.1. processPayment| PaymentController
    PaymentController -->|4.1.1. chargeCard| PaymentGateway
    PaymentController -->|4.1.3. createOrder| Order
    PaymentController -->|4.1.5. removeItems| Inventory
    PaymentController -->|4.1.6. clearCart| Cart
    PaymentController -->|4.1.7. sendConfirmation| EmailService

    CheckoutForm -.->|1.3. showCheckoutPage| Customer
    PaymentForm -.->|4.3. displayConfirmation| Customer
```

**Message Flow:**
1. Customer clicks checkout
1.1. CheckoutController gets cart items and checks inventory
1.2-1.3. Order summary displayed to customer
2. Customer enters contact info for pickup
3-4. Customer enters payment details
4.1. PaymentController processes payment through gateway
4.1.3-4.1.6. Order created, inventory updated, cart cleared
4.1.7. Confirmation email sent
4.3. Confirmation displayed to customer

---

### VOPC Diagram

```mermaid
classDiagram
    class CheckoutForm {
        +clickCheckout()
        +enterContactInfo(email, phone, name)
        +showCheckoutPage()
        +displayOrderSummary(items, total, tax)
    }

    class PaymentForm {
        +selectPaymentMethod(method)
        +enterPaymentDetails(cardNumber, expiry, cvv)
        +displaySecurePaymentForm()
        +displayConfirmation(orderNumber, pickupTime)
    }

    class CheckoutController {
        +initiateCheckout(customerId)
        +validateContactInfo()
        -calculateTotal(items)
        -calculateTax(subtotal)
    }

    class PaymentController {
        +processPayment(paymentDetails, total)
        -validatePaymentDetails()
    }

    class Cart {
        -cartId : Number
        -customerId : Number
        -items : List
        -createdAt : Date
        +getCartItems(customerId)
        +clearCart(customerId)
        +addItem(cardId, quantity)
    }

    class Inventory {
        -inventoryId : Number
        -cardId : Number
        -quantity : Number
        -condition : String
        -location : String
        +checkAvailability(cardId, quantity)
        +removeItems(items)
        +addToInventory(cardDetails)
    }

    class Order {
        -orderId : Number
        -orderNumber : String
        -customerId : Number
        -items : List
        -total : Amount
        -tax : Amount
        -status : String
        -pickupTime : Date
        -createdAt : Date
        +createOrder(customerId, items, paymentInfo)
        +getOrder(orderNumber)
        +updateStatus(status)
    }

    class PaymentGateway {
        +chargeCard(paymentDetails, total)
        +refund(transactionId, amount)
    }

    class EmailService {
        +sendConfirmation(email, orderDetails)
        +sendNotification(email, message)
    }

    CheckoutForm --> CheckoutController : initiates checkout
    PaymentForm --> PaymentController : sends payment
    CheckoutController --> Cart : gets items
    CheckoutController --> Inventory : checks stock
    PaymentController --> PaymentGateway : processes payment
    PaymentController --> Order : creates order
    PaymentController --> Inventory : updates stock
    PaymentController --> Cart : clears cart
    PaymentController --> EmailService : sends confirmation

    Cart "1" -- "0..*" Inventory : contains cards from
    Order "1" -- "0..*" Inventory : reserves items from
```

---

## 3. Sell Card to Store

### Collaboration Diagram

```mermaid
flowchart TB
    subgraph Actors
        Customer((Customer))
        Staff((Staff))
    end

    subgraph Boundary
        SellCardForm[SellCardForm]
        StaffForm[StaffVerificationForm]
    end

    subgraph Control
        SellController[SellController]
    end

    subgraph Entity
        Card[(Card)]
        SellTransaction[(SellTransaction)]
        Inventory[(Inventory)]
    end

    subgraph External
        CardPriceService{{CardPriceService}}
    end

    Customer -->|1. navigateToSellPage 2. searchCard 3. selectCard 4. acceptOffer| SellCardForm
    SellCardForm -->|2.1. searchCards 3.1. getPriceQuote 4.1. createSellTransaction| SellController

    SellController -->|2.1.1. findCards| Card
    SellController -->|3.1.1. getMarketPrice| CardPriceService
    SellController -->|4.1.1. saveTransaction| SellTransaction

    Staff -->|5. lookupTransaction 6. verifyAndComplete| StaffForm
    StaffForm -->|5.1. getTransaction 6.1. completeTransaction| SellController

    SellController -->|5.1.1. findTransaction 6.1.1. updateStatus| SellTransaction
    SellController -->|6.1.2. addToInventory| Inventory

    SellCardForm -.->|1.1. displayCardSearchForm 2.3. displaySuggestions 3.3. displayOffer 4.3. displayInstructions| Customer
    StaffForm -.->|5.3. displayTransactionDetails 6.3. displayPaymentInstruction| Staff
```

**Message Flow:**
1. Customer navigates to sell page
2. Customer searches for card, system shows matches
3. Customer selects card and condition, system calculates buy price
3.1.1. SellController fetches market price from external service
3.1.3. Buy price calculated (60% of market, adjusted for condition)
4. Customer accepts offer, transaction created
5. Staff looks up transaction when customer arrives
6. Staff verifies card and completes transaction
6.1.2. Card added to inventory

---

### VOPC Diagram

```mermaid
classDiagram
    class SellCardForm {
        +navigateToSellPage()
        +searchCard(cardName)
        +selectCard(cardId, condition, quantity)
        +acceptOffer()
        +displayCardSearchForm()
        +displaySuggestions(cards)
        +displayOffer(buyPrice, cardDetails)
        +displayInstructions(transactionNumber, storeAddress)
    }

    class StaffVerificationForm {
        +lookupTransaction(transactionNumber)
        +verifyAndComplete()
        +displayTransactionDetails()
        +displayPaymentInstruction(amount)
    }

    class SellController {
        +searchCards(cardName)
        +getPriceQuote(cardId, condition, quantity)
        +createSellTransaction(customerId, cardDetails, price)
        +getTransaction(transactionNumber)
        +completeTransaction(transactionNumber, staffId)
        -calculateBuyPrice(marketPrice, condition)
    }

    class Card {
        -cardId : Number
        -name : String
        -set : String
        -rarity : String
        -imageUrl : String
        +findCards(cardName)
        +getCard(cardId)
    }

    class SellTransaction {
        -transactionId : Number
        -transactionNumber : String
        -customerId : Number
        -cardId : Number
        -condition : String
        -quantity : Number
        -buyPrice : Amount
        -status : String
        -staffId : Number
        -createdAt : Date
        -completedAt : Date
        +saveTransaction(transactionData)
        +findTransaction(transactionNumber)
        +updateStatus(status, staffId)
    }

    class Inventory {
        -inventoryId : Number
        -cardId : Number
        -quantity : Number
        -condition : String
        -location : String
        +addToInventory(cardDetails)
        +checkAvailability(cardId, quantity)
    }

    class CardPriceService {
        +getMarketPrice(cardId)
        +getPriceHistory(cardId)
    }

    SellCardForm --> SellController : sends requests
    StaffVerificationForm --> SellController : verifies transactions
    SellController --> Card : searches cards
    SellController --> CardPriceService : gets market prices
    SellController --> SellTransaction : manages transactions
    SellController --> Inventory : adds cards

    Card "1" -- "0..*" SellTransaction : referenced in
    Card "1" -- "0..*" Inventory : stored as
    SellTransaction "1" -- "0..1" Inventory : results in
```
