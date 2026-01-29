# CardHub: Sequence Diagrams

Levente Szabo - 618088

---

This guy in the lecture slides was so funny to me that I just had to include him here.

![Boundary Class Snowman](./pictures/snow.png)

---

## Customer Login

```mermaid
sequenceDiagram
    actor Customer
    participant LoginForm as LoginForm
    participant LoginController as LoginController
    participant CustomerDB as Customer
    participant SessionDB as Session

    Customer->>LoginForm: 1. navigateToLogin()
    LoginForm-->>Customer: 1.1. displayLoginForm()

    Customer->>LoginForm: 2. enterCredentials(email, password)
    LoginForm->>LoginController: 2.1. authenticate(email, password)

    LoginController->>CustomerDB: 2.1.1. findByEmail(email)
    CustomerDB-->>LoginController: 2.1.2. return customerData

    LoginController->>LoginController: 2.1.3. verifyPassword(password, storedHash)
    LoginController->>LoginController: 2.1.4. checkEmailVerified()
    LoginController->>LoginController: 2.1.5. checkAccountEnabled()

    LoginController->>SessionDB: 2.1.6. createSession(customerId)
    SessionDB-->>LoginController: 2.1.7. return sessionToken

    LoginController->>CustomerDB: 2.1.8. updateLastLoginTime(customerId)

    LoginController-->>LoginForm: 2.2. return authSuccess(sessionToken, role)
    LoginForm-->>Customer: 2.3. redirectToDashboard(role)
    LoginForm-->>Customer: 2.4. displayWelcomeMessage(customerName)
```

1. Customer navigates to the login page
2. Customer enters email and password
3. LoginController validates credentials against Customer entity
4. Session is created and stored
5. Customer is redirected to appropriate dashboard based on role

---

## Customer Purchase Checkout

```mermaid
sequenceDiagram
    actor Customer
    participant CheckoutForm as CheckoutForm
    participant CheckoutController as CheckoutController
    participant CartDB as Cart
    participant InventoryDB as Inventory<
    participant PaymentForm as PaymentForm
    participant PaymentController as PaymentController
    participant PaymentGateway as PaymentGateway<
    participant OrderDB as Order
    participant EmailService as EmailService

    Customer->>CheckoutForm: 1. clickCheckout()
    CheckoutForm->>CheckoutController: 1.1. initiateCheckout(customerId)

    CheckoutController->>CartDB: 1.1.1. getCartItems(customerId)
    CartDB-->>CheckoutController: 1.1.2. return cartItems

    loop For each cart item
        CheckoutController->>InventoryDB: 1.1.3. checkAvailability(cardId, quantity)
        InventoryDB-->>CheckoutController: 1.1.4. return available
    end

    CheckoutController-->>CheckoutForm: 1.2. displayOrderSummary(items, total, tax)
    CheckoutForm-->>Customer: 1.3. showCheckoutPage()

    Customer->>CheckoutForm: 2. enterContactInfo(email, phone, name)
    CheckoutForm->>CheckoutController: 2.1. validateContactInfo()
    CheckoutController-->>CheckoutForm: 2.2. showPickupDetails(storeAddress, readyTime)

    Customer->>PaymentForm: 3. selectPaymentMethod("Credit Card")
    PaymentForm-->>Customer: 3.1. displaySecurePaymentForm()

    Customer->>PaymentForm: 4. enterPaymentDetails(cardNumber, expiry, cvv)
    PaymentForm->>PaymentController: 4.1. processPayment(paymentDetails, total)

    PaymentController->>PaymentGateway: 4.1.1. chargeCard(paymentDetails, total)
    PaymentGateway-->>PaymentController: 4.1.2. return paymentSuccess(transactionId)

    PaymentController->>OrderDB: 4.1.3. createOrder(customerId, items, paymentInfo)
    OrderDB-->>PaymentController: 4.1.4. return orderNumber

    PaymentController->>InventoryDB: 4.1.5. removeItems(cartItems)

    PaymentController->>CartDB: 4.1.6. clearCart(customerId)

    PaymentController->>EmailService: 4.1.7. sendConfirmation(email, orderDetails)

    PaymentController-->>PaymentForm: 4.2. return orderConfirmation(orderNumber)
    PaymentForm-->>Customer: 4.3. displayConfirmation(orderNumber, pickupTime)
```

1. Customer initiates checkout, system verifies cart items are in stock
2. Customer enters contact information for pickup
3. Customer enters payment details in secure form
4. Payment is processed through external gateway
5. Order is created, inventory updated, cart cleared
6. Confirmation email sent and displayed to customer

---

## Sell Card to Store

```mermaid
sequenceDiagram
    actor Customer
    participant SellCardForm as SellCardForm
    participant SellController as SellController
    participant CardPriceService as CardPriceService
    participant CardDB as Card
    participant TransactionDB as SellTransaction
    actor Staff
    participant StaffForm as StaffVerificationForm
    participant InventoryDB as Inventory

    Customer->>SellCardForm: 1. navigateToSellPage()
    SellCardForm-->>Customer: 1.1. displayCardSearchForm()

    Customer->>SellCardForm: 2. searchCard(cardName)
    SellCardForm->>SellController: 2.1. searchCards(cardName)
    SellController->>CardDB: 2.1.1. findCards(cardName)
    CardDB-->>SellController: 2.1.2. return matchingCards
    SellController-->>SellCardForm: 2.2. return cardSuggestions
    SellCardForm-->>Customer: 2.3. displaySuggestions(cards)

    Customer->>SellCardForm: 3. selectCard(cardId, condition, quantity)
    SellCardForm->>SellController: 3.1. getPriceQuote(cardId, condition, quantity)

    SellController->>CardPriceService: 3.1.1. getMarketPrice(cardId)
    CardPriceService-->>SellController: 3.1.2. return marketPrice

    SellController->>SellController: 3.1.3. calculateBuyPrice(marketPrice, condition)
    Note right of SellController: Buy = Market × 60%<br/>adjusted for condition

    SellController-->>SellCardForm: 3.2. return priceQuote(buyPrice)
    SellCardForm-->>Customer: 3.3. displayOffer(buyPrice, cardDetails)

    Customer->>SellCardForm: 4. acceptOffer()
    SellCardForm->>SellController: 4.1. createSellTransaction(customerId, cardDetails, price)

    SellController->>TransactionDB: 4.1.1. saveTransaction(transactionData)
    TransactionDB-->>SellController: 4.1.2. return transactionNumber

    SellController-->>SellCardForm: 4.2. return transactionCreated(transactionNumber)
    SellCardForm-->>Customer: 4.3. displayInstructions(transactionNumber, storeAddress)

    Note over Customer,Staff: Customer brings card to store

    Staff->>StaffForm: 5. lookupTransaction(transactionNumber)
    StaffForm->>SellController: 5.1. getTransaction(transactionNumber)
    SellController->>TransactionDB: 5.1.1. findTransaction(transactionNumber)
    TransactionDB-->>SellController: 5.1.2. return transactionDetails
    SellController-->>StaffForm: 5.2. return transactionDetails
    StaffForm-->>Staff: 5.3. displayTransactionDetails()

    Staff->>StaffForm: 6. verifyAndComplete()
    StaffForm->>SellController: 6.1. completeTransaction(transactionNumber, staffId)

    SellController->>TransactionDB: 6.1.1. updateStatus("Completed", staffId)
    SellController->>InventoryDB: 6.1.2. addToInventory(cardDetails)

    SellController-->>StaffForm: 6.2. return completionSuccess(paymentAmount)
    StaffForm-->>Staff: 6.3. displayPaymentInstruction(paymentAmount)
```

1. Customer searches for the card they want to sell
2. System looks up market price from external service
3. Buy price calculated (60% of market, adjusted for condition)
4. Customer accepts offer, transaction created with pending status
5. Customer brings card to store with transaction number
6. Staff verifies card condition and completes transaction
7. Card added to inventory, customer paid cash

