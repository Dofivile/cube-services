# Financial Connections use cases

View options for integrating Financial Connections and common use cases.

Financial Connections allows your users to link their financial accounts to collect *ACH* (Automated Clearing House (ACH) is a US financial network used for electronic payments and money transfers that doesn’t rely on paper checks, credit card networks, wire transfers, or cash) payments, facilitate Connect *payouts* (A payout is the transfer of funds to an external account, usually a bank account, in the form of a deposit), and build financial data products. It also enables your users to connect their accounts in fewer steps with [Link](https://docs.stripe.com/payments/link.md), allowing them to save and quickly reuse their bank account details across Stripe merchants. View the following integration paths based on your requirements, and some common use cases for Financial Connections data below.

How you integrate Financial Connections depends on your desired use cases.

| Use case                                                                                                          | Example                                                                                    | Recommended integration                                                                                                                                                                                                                                                                                                      |
| ----------------------------------------------------------------------------------------------------------------- | ------------------------------------------------------------------------------------------ | ---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| [ACH Direct Debit payments](https://docs.stripe.com/financial-connections/use-cases.md#ach-direct-debit)          | - Bank-based payments
  - Wallet transfers and top-ups
  - Bill payments                   | An ACH payments integration:
  - [Setup Intents](https://docs.stripe.com/api/setup_intents.md)
  - [Payment Intents](https://docs.stripe.com/api/payment_intents.md)
  - [Checkout](https://docs.stripe.com/payments/checkout.md)
  - [Invoices](https://docs.stripe.com/invoicing.md)                                       |
| [Connect payouts](https://docs.stripe.com/financial-connections/use-cases.md#custom-connect-payouts)              | - Pay out funds to Connected Accounts                                                      | A Connect onboarding integration:
  - [Hosted onboarding](https://docs.stripe.com/connect/payouts-bank-accounts.md?bank-account-collection-integration=prebuilt-web-form)
  - [Setup Intents](https://docs.stripe.com/connect/payouts-bank-accounts.md?bank-account-collection-integration=direct-api#create-a-setup-intent) |
| [Building financial products](https://docs.stripe.com/financial-connections/use-cases.md#building-other-products) | - Financial management application
  - Wealth management application
  - Loan underwriting | [Financial Connections Sessions](https://docs.stripe.com/api/financial_connections/sessions/object.md)                                                                                                                                                                                                                       |

To process ACH payments or facilitate Connect payouts, use a recommended payments integration in the table above, such as Setup Intents. This makes sure that the collected accounts are compatible with [ACH transfers](https://docs.stripe.com/payments/ach-direct-debit.md).

The Financial Connections Sessions API provides more flexibility for use cases that won’t use the collected accounts for ACH payments or Connect payouts. It has configuration options that let your users link additional [account types](https://docs.stripe.com/api/financial_connections/accounts/object.md#financial_connections_account_object-category) that aren’t compatible with ACH, and you can collect multiple accounts in a single session.

Regardless of which API you choose to integrate with, you can request access to additional data on your users’ financial accounts. You can then use this data to optimize your integration or create more complex products. The following sections provide more context on the above use cases, including recommendations on which data to access.

## ACH Direct Debit payments 

- [Balances](https://docs.stripe.com/financial-connections/balances.md)

In accordance with [Nacha regulations](https://support.stripe.com/questions/nacha-bank-account-validation-rule), you must verify a user’s account to accept an ACH Direct Debit payment or transfer. With Financial Connections, your customer authenticates their bank account and provides permission to share the details you need to charge their account, such as a tokenized account and routing number.

Collecting bank account details from a customer with Financial Connections can help you:

- Improve payment reliability by verifying that a user’s bank account is open and able to accept ACH direct debits.
- Increase checkout conversion by eliminating the need for your customers to leave your website or application to locate their account and routing numbers.
- Save development time by eliminating the need for you to build bank account form validation when your customer enters account details.

To verify a bank account to accept an ACH Direct Debit payment with Financial Connections, use Stripe’s Payment Intent or Setup Intent APIs. Alternatively, you can use a hosted Stripe payments integration such as [Checkout](https://docs.stripe.com/payments/checkout.md) or the [Payment Element](https://docs.stripe.com/payments/payment-element.md).

Learn how to [collect a bank account to accept an ACH Direct Debit payment](https://docs.stripe.com/financial-connections/ach-direct-debit-payments.md).

Optionally, you can request permission from your customers to retrieve additional data on their [Financial Connections account](https://docs.stripe.com/api/financial_connections/accounts/object.md). We recommend that you access [balances](https://docs.stripe.com/financial-connections/balances.md) data to perform balance checks prior to processing the payment.

Manual account entry and microdeposit verification are available as a fallback method for this use case. However, accounts that you add through microdeposits won’t have access to additional account data.

## Custom Connect payouts 

- [Ownership](https://docs.stripe.com/financial-connections/ownership.md)

Use Financial Connections with Connect to verify bank accounts for Custom connected accounts, thereby facilitating payouts. This allows your connected account to authenticate its own bank account and provide permission to share details you need for payouts, like tokenized account and routing numbers.

Collecting account details from your connected account with Financial Connections can help you:

- Increase onboarding conversion by eliminating the need for your connected account to leave your website or application to locate their account and routing numbers.
- Reduce first payout failure rates by eliminating errors that result from manual entry of account and routing numbers.
- Make sure you don’t need to store sensitive data such as account and routing numbers on your server.
- Save development time by eliminating the need for you to build bank account form validation when your connected account enters their bank account details.

Optionally, you can request permission from your connected account to retrieve additional data on their Financial Connections account. Consider accessing [ownership](https://docs.stripe.com/financial-connections/ownership.md) details to optimize your onboarding process. Retrieving ownership data on an account can help you mitigate fraud by verifying an account’s ownership details, such as the name and address of the account holder.

Learn how to [collect a bank account to initiate payouts to a US Custom Connect account](https://docs.stripe.com/financial-connections/connect-payouts.md).

Manual account entry and microdeposit verification are available as a fallback method for this use case (for example, if your connected account can’t find their institution or otherwise authenticate). However, accounts that you add through microdeposits won’t have access to additional account data.

## Build financial products 

- [Balances](https://docs.stripe.com/financial-connections/balances.md)
- [Ownership](https://docs.stripe.com/financial-connections/ownership.md)
- [Transactions](https://docs.stripe.com/financial-connections/transactions.md)

Use Financial Connections to access external bank account data that you can use to build financial products.

After your user has consented to share data from their financial accounts, you can retrieve data for various use cases, such as:

- Help your user track expenses, handle bills, manage their finances and take control of their financial well-being with [transactions](https://docs.stripe.com/financial-connections/transactions.md) data.
- Speed up underwriting and improve access to credit and other financial services with transactions and balances data.

Learn how to [collect an account to access data](https://docs.stripe.com/financial-connections/other-data-powered-products.md) using Financial Connections’ Sessions API.

Manual account entry and microdeposit verification aren’t available in the authentication flow for this use case because the primary goal of collecting an account is data accessibility.

You can [convert most previously linked Financial Connections accounts to Payment Methods](https://docs.stripe.com/financial-connections/other-data-powered-products.md?platform=web#accept-ach-direct-debit). However, if your integration uses the Sessions API, the linked account might not be compatible with ACH.


# Collect a bank account to use ACH Direct Debit payments with account data

Use account data such as balances with your payments integration.

Not sure about which Financial Connections integration to use? See our [overview of integration options](https://docs.stripe.com/financial-connections/use-cases.md).

Stripe offers a number of ways to accept ACH Direct Debit payments from your users. All of these methods require that you [verify](https://docs.stripe.com/payments/ach-direct-debit.md#verification) the user’s account before you can debit their account. You can use Financial Connections to perform instant bank account verification along with features such as balance or ownership checks. When using Financial Connections for your ACH flows, you can:

- Reduce your payment failure rate from closed or inactive accounts
- Improve payments conversion by keeping users on session, instead of forcing them to leave your payments flow to locate their accounts and routing numbers
- Save development time by eliminating the need to create a custom bank account collection form
- Enable the collection of additional bank account data, such as balances and ownership information

## Before you begin

Financial Connections is the default verification method for all hosted ACH payment flows, such as Checkout or the Payment Element. If you use a hosted flow, skip directly to [accessing additional account data](https://docs.stripe.com/financial-connections/ach-direct-debit-payments.md#access). Set up your integration to [collect ACH payments](https://docs.stripe.com/payments/ach-direct-debit/accept-a-payment.md?platform=web&ui=stripe-hosted) if you haven’t already done so.

## Enable Financial Connections

The `verification_method` parameter on various API resources controls whether Financial Connections is enabled for bank account verification. Financial Connections with microdeposit fallback is the default.

> Bank accounts that your customers link through manual entry and microdeposits won’t have access to additional bank account data like balances, ownership, and transactions.

| Verification method   | Description                                                                                            |
| --------------------- | ------------------------------------------------------------------------------------------------------ |
| `automatic` (default) | Financial Connections with the option to manually enter bank account information and use microdeposits |
| `instant`             | Financial Connections only, with no manual entry and microdeposit fallback                             |
| `microdeposits`       | Manual entry and microdeposits only                                                                    |

This option is available on the following APIs:

Additional steps, such as NACHA mandate collection, are required for businesses that don’t use a Stripe-hosted integration such as Payment Element, Checkout, or Hosted Invoicing. See [this section of the ACH guide](https://docs.stripe.com/payments/ach-direct-debit/accept-a-payment.md?platform=web&ui=direct-api#web-collect-details).

- [PaymentIntent](https://docs.stripe.com/api/payment_intents/create.md#create_payment_intent-payment_method_options-us_bank_account-verification_method)
- [SetupIntent](https://docs.stripe.com/api/setup_intents/create.md#create_setup_intent-payment_method_options-us_bank_account-verification_method)
- [CheckoutSession](https://docs.stripe.com/api/checkout/sessions/create.md#create_checkout_session-payment_method_options-us_bank_account-verification_method)
- [Invoice](https://docs.stripe.com/api/invoices/create.md#create_invoice-payment_settings-payment_method_options-us_bank_account-verification_method)
- [Subscription](https://docs.stripe.com/api/subscriptions/create.md#create_subscription-payment_settings-payment_method_options-us_bank_account-verification_method)
- [Payment Element](https://docs.stripe.com/js/elements_object/create_without_intent#stripe_elements_no_intent-options-paymentMethodOptions-us_bank_account-verification_method)

## Create a customer [Recommended]

We recommend that you create a *Customer* (Customer objects represent customers of your business. They let you reuse payment methods and give you the ability to track multiple payments) with an email address to represent your user, that you then attach to your payment. Attaching a `Customer` object allows you to [list previously linked accounts ](https://docs.stripe.com/api/financial_connections/accounts/list.md) later. By providing an email address on the `Customer` object, Financial Connections can improve the authentication flow by simplifying sign-in or sign-up for your user, depending on whether they’re a returning [Link](https://support.stripe.com/questions/link-for-financial-connections-support-for-businesses) user.

```curl
curl https://api.stripe.com/v1/customers \
  -u "<<YOUR_SECRET_KEY>>:" \
  -d email={{CUSTOMER_EMAIL}}
```

```cli
stripe customers create  \
  --email={{CUSTOMER_EMAIL}}
```

```ruby
# Set your secret key. Remember to switch to your live secret key in production.
# See your keys here: https://dashboard.stripe.com/apikeys
client = Stripe::StripeClient.new("<<YOUR_SECRET_KEY>>")

customer = client.v1.customers.create({email: '{{CUSTOMER_EMAIL}}'})
```

```python
# Set your secret key. Remember to switch to your live secret key in production.
# See your keys here: https://dashboard.stripe.com/apikeys
client = StripeClient("<<YOUR_SECRET_KEY>>")

# For SDK versions 12.4.0 or lower, remove '.v1' from the following line.
customer = client.v1.customers.create({"email": "{{CUSTOMER_EMAIL}}"})
```

```php
// Set your secret key. Remember to switch to your live secret key in production.
// See your keys here: https://dashboard.stripe.com/apikeys
$stripe = new \Stripe\StripeClient('<<YOUR_SECRET_KEY>>');

$customer = $stripe->customers->create(['email' => '{{CUSTOMER_EMAIL}}']);
```

```java
// Set your secret key. Remember to switch to your live secret key in production.
// See your keys here: https://dashboard.stripe.com/apikeys
StripeClient client = new StripeClient("<<YOUR_SECRET_KEY>>");

CustomerCreateParams params =
  CustomerCreateParams.builder().setEmail("{{CUSTOMER_EMAIL}}").build();

// For SDK versions 29.4.0 or lower, remove '.v1()' from the following line.
Customer customer = client.v1().customers().create(params);
```

```node
// Set your secret key. Remember to switch to your live secret key in production.
// See your keys here: https://dashboard.stripe.com/apikeys
const stripe = require('stripe')('<<YOUR_SECRET_KEY>>');

const customer = await stripe.customers.create({
  email: '{{CUSTOMER_EMAIL}}',
});
```

```go
// Set your secret key. Remember to switch to your live secret key in production.
// See your keys here: https://dashboard.stripe.com/apikeys
sc := stripe.NewClient("<<YOUR_SECRET_KEY>>")
params := &stripe.CustomerCreateParams{Email: stripe.String("{{CUSTOMER_EMAIL}}")}
result, err := sc.V1Customers.Create(context.TODO(), params)
```

```dotnet
// Set your secret key. Remember to switch to your live secret key in production.
// See your keys here: https://dashboard.stripe.com/apikeys
var options = new CustomerCreateOptions { Email = "{{CUSTOMER_EMAIL}}" };
var client = new StripeClient("<<YOUR_SECRET_KEY>>");
var service = client.V1.Customers;
Customer customer = service.Create(options);
```

## Request access to additional account data

To access additional account data on Financial Connections Accounts, first make sure you’ve submitted your Financial Connections application by checking [Financial Connections settings in the Dashboard](https://dashboard.stripe.com/settings/financial-connections). To view this page, activate your account. How you configure which types of account data you have access to depends on your integration.

#### Dynamic payment methods

If you use Stripe’s [dynamic payment method feature](https://docs.stripe.com/payments/payment-methods/dynamic-payment-methods.md) to collect ACH payments for non-Connect use cases, you can configure requested Financial Connections data directly from the [ACH Dashboard settings page](https://dashboard.stripe.com/test/settings/payment_methods). Account and routing number is always required for ACH debits—other data types are optional.

> We recommend configuring permissions in the Dashboard because it allows you to change which data you collect without any code changes.

To override the Dashboard configuration, specify Financial Connections permissions directly in the API. To do this for PaymentIntents:

```curl
curl https://api.stripe.com/v1/payment_intents \
  -u "<<YOUR_SECRET_KEY>>:" \
  -d amount=2000 \
  -d currency=usd \
  -d customer="{{CUSTOMER_ID}}" \
  -d "automatic_payment_methods[enabled]"=true \
  -d "payment_method_options[us_bank_account][financial_connections][permissions][]"=payment_method \
  -d "payment_method_options[us_bank_account][financial_connections][permissions][]"=balances \
  -d "payment_method_options[us_bank_account][financial_connections][permissions][]"=ownership \
  -d "payment_method_options[us_bank_account][financial_connections][permissions][]"=transactions
```

```cli
stripe payment_intents create  \
  --amount=2000 \
  --currency=usd \
  --customer="{{CUSTOMER_ID}}" \
  -d "automatic_payment_methods[enabled]"=true \
  -d "payment_method_options[us_bank_account][financial_connections][permissions][0]"=payment_method \
  -d "payment_method_options[us_bank_account][financial_connections][permissions][1]"=balances \
  -d "payment_method_options[us_bank_account][financial_connections][permissions][2]"=ownership \
  -d "payment_method_options[us_bank_account][financial_connections][permissions][3]"=transactions
```

```ruby
# Set your secret key. Remember to switch to your live secret key in production.
# See your keys here: https://dashboard.stripe.com/apikeys
client = Stripe::StripeClient.new("<<YOUR_SECRET_KEY>>")

payment_intent = client.v1.payment_intents.create({
  amount: 2000,
  currency: 'usd',
  customer: '{{CUSTOMER_ID}}',
  automatic_payment_methods: {enabled: true},
  payment_method_options: {
    us_bank_account: {
      financial_connections: {
        permissions: ['payment_method', 'balances', 'ownership', 'transactions'],
      },
    },
  },
})
```

```python
# Set your secret key. Remember to switch to your live secret key in production.
# See your keys here: https://dashboard.stripe.com/apikeys
client = StripeClient("<<YOUR_SECRET_KEY>>")

# For SDK versions 12.4.0 or lower, remove '.v1' from the following line.
payment_intent = client.v1.payment_intents.create({
  "amount": 2000,
  "currency": "usd",
  "customer": "{{CUSTOMER_ID}}",
  "automatic_payment_methods": {"enabled": True},
  "payment_method_options": {
    "us_bank_account": {
      "financial_connections": {
        "permissions": ["payment_method", "balances", "ownership", "transactions"],
      },
    },
  },
})
```

```php
// Set your secret key. Remember to switch to your live secret key in production.
// See your keys here: https://dashboard.stripe.com/apikeys
$stripe = new \Stripe\StripeClient('<<YOUR_SECRET_KEY>>');

$paymentIntent = $stripe->paymentIntents->create([
  'amount' => 2000,
  'currency' => 'usd',
  'customer' => '{{CUSTOMER_ID}}',
  'automatic_payment_methods' => ['enabled' => true],
  'payment_method_options' => [
    'us_bank_account' => [
      'financial_connections' => [
        'permissions' => ['payment_method', 'balances', 'ownership', 'transactions'],
      ],
    ],
  ],
]);
```

```java
// Set your secret key. Remember to switch to your live secret key in production.
// See your keys here: https://dashboard.stripe.com/apikeys
StripeClient client = new StripeClient("<<YOUR_SECRET_KEY>>");

PaymentIntentCreateParams params =
  PaymentIntentCreateParams.builder()
    .setAmount(2000L)
    .setCurrency("usd")
    .setCustomer("{{CUSTOMER_ID}}")
    .setAutomaticPaymentMethods(
      PaymentIntentCreateParams.AutomaticPaymentMethods.builder().setEnabled(true).build()
    )
    .setPaymentMethodOptions(
      PaymentIntentCreateParams.PaymentMethodOptions.builder()
        .setUsBankAccount(
          PaymentIntentCreateParams.PaymentMethodOptions.UsBankAccount.builder()
            .setFinancialConnections(
              PaymentIntentCreateParams.PaymentMethodOptions.UsBankAccount.FinancialConnections.builder()
                .addPermission(
                  PaymentIntentCreateParams.PaymentMethodOptions.UsBankAccount.FinancialConnections.Permission.PAYMENT_METHOD
                )
                .addPermission(
                  PaymentIntentCreateParams.PaymentMethodOptions.UsBankAccount.FinancialConnections.Permission.BALANCES
                )
                .addPermission(
                  PaymentIntentCreateParams.PaymentMethodOptions.UsBankAccount.FinancialConnections.Permission.OWNERSHIP
                )
                .addPermission(
                  PaymentIntentCreateParams.PaymentMethodOptions.UsBankAccount.FinancialConnections.Permission.TRANSACTIONS
                )
                .build()
            )
            .build()
        )
        .build()
    )
    .build();

// For SDK versions 29.4.0 or lower, remove '.v1()' from the following line.
PaymentIntent paymentIntent = client.v1().paymentIntents().create(params);
```

```node
// Set your secret key. Remember to switch to your live secret key in production.
// See your keys here: https://dashboard.stripe.com/apikeys
const stripe = require('stripe')('<<YOUR_SECRET_KEY>>');

const paymentIntent = await stripe.paymentIntents.create({
  amount: 2000,
  currency: 'usd',
  customer: '{{CUSTOMER_ID}}',
  automatic_payment_methods: {
    enabled: true,
  },
  payment_method_options: {
    us_bank_account: {
      financial_connections: {
        permissions: ['payment_method', 'balances', 'ownership', 'transactions'],
      },
    },
  },
});
```

```go
// Set your secret key. Remember to switch to your live secret key in production.
// See your keys here: https://dashboard.stripe.com/apikeys
sc := stripe.NewClient("<<YOUR_SECRET_KEY>>")
params := &stripe.PaymentIntentCreateParams{
  Amount: stripe.Int64(2000),
  Currency: stripe.String(stripe.CurrencyUSD),
  Customer: stripe.String("{{CUSTOMER_ID}}"),
  AutomaticPaymentMethods: &stripe.PaymentIntentCreateAutomaticPaymentMethodsParams{
    Enabled: stripe.Bool(true),
  },
  PaymentMethodOptions: &stripe.PaymentIntentCreatePaymentMethodOptionsParams{
    USBankAccount: &stripe.PaymentIntentCreatePaymentMethodOptionsUSBankAccountParams{
      FinancialConnections: &stripe.PaymentIntentCreatePaymentMethodOptionsUSBankAccountFinancialConnectionsParams{
        Permissions: []*string{
          stripe.String(stripe.PaymentIntentPaymentMethodOptionsUSBankAccountFinancialConnectionsPermissionPaymentMethod),
          stripe.String(stripe.PaymentIntentPaymentMethodOptionsUSBankAccountFinancialConnectionsPermissionBalances),
          stripe.String(stripe.PaymentIntentPaymentMethodOptionsUSBankAccountFinancialConnectionsPermissionOwnership),
          stripe.String(stripe.PaymentIntentPaymentMethodOptionsUSBankAccountFinancialConnectionsPermissionTransactions),
        },
      },
    },
  },
}
result, err := sc.V1PaymentIntents.Create(context.TODO(), params)
```

```dotnet
// Set your secret key. Remember to switch to your live secret key in production.
// See your keys here: https://dashboard.stripe.com/apikeys
var options = new PaymentIntentCreateOptions
{
    Amount = 2000,
    Currency = "usd",
    Customer = "{{CUSTOMER_ID}}",
    AutomaticPaymentMethods = new PaymentIntentAutomaticPaymentMethodsOptions
    {
        Enabled = true,
    },
    PaymentMethodOptions = new PaymentIntentPaymentMethodOptionsOptions
    {
        UsBankAccount = new PaymentIntentPaymentMethodOptionsUsBankAccountOptions
        {
            FinancialConnections = new PaymentIntentPaymentMethodOptionsUsBankAccountFinancialConnectionsOptions
            {
                Permissions = new List<string>
                {
                    "payment_method",
                    "balances",
                    "ownership",
                    "transactions",
                },
            },
        },
    },
};
var client = new StripeClient("<<YOUR_SECRET_KEY>>");
var service = client.V1.PaymentIntents;
PaymentIntent paymentIntent = service.Create(options);
```

#### Payment method types

If you pass `payment_method_types` in the API directly, you must explicitly specify which Financial Connections data permissions you want in every API call. To do this for [CheckoutSession](https://docs.stripe.com/api/checkout/sessions/create.md#create_checkout_session-payment_method_options-us_bank_account-financial_connections-permissions):

```curl
curl https://api.stripe.com/v1/checkout/sessions \
  -u "<<YOUR_SECRET_KEY>>:" \
  -d customer="{{CUSTOMER_ID}}" \
  --data-urlencode success_url="https://example.com/success" \
  -d "line_items[0][price]"={PRICE_ID} \
  -d "line_items[0][quantity]"=1 \
  -d "payment_method_options[us_bank_account][financial_connections][permissions][0]"=payment_method \
  -d "payment_method_options[us_bank_account][financial_connections][permissions][1]"=balances \
  -d "payment_method_options[us_bank_account][financial_connections][permissions][2]"=ownership \
  -d "payment_method_options[us_bank_account][financial_connections][permissions][3]"=transactions
```

```cli
stripe checkout sessions create  \
  --customer="{{CUSTOMER_ID}}" \
  --success-url="https://example.com/success" \
  -d "line_items[0][price]"={PRICE_ID} \
  -d "line_items[0][quantity]"=1 \
  -d "payment_method_options[us_bank_account][financial_connections][permissions][0]"=payment_method \
  -d "payment_method_options[us_bank_account][financial_connections][permissions][1]"=balances \
  -d "payment_method_options[us_bank_account][financial_connections][permissions][2]"=ownership \
  -d "payment_method_options[us_bank_account][financial_connections][permissions][3]"=transactions
```

```ruby
# Set your secret key. Remember to switch to your live secret key in production.
# See your keys here: https://dashboard.stripe.com/apikeys
client = Stripe::StripeClient.new("<<YOUR_SECRET_KEY>>")

session = client.v1.checkout.sessions.create({
  customer: '{{CUSTOMER_ID}}',
  success_url: 'https://example.com/success',
  line_items: [
    {
      price: '{PRICE_ID}',
      quantity: 1,
    },
  ],
  payment_method_options: {
    us_bank_account: {
      financial_connections: {
        permissions: ['payment_method', 'balances', 'ownership', 'transactions'],
      },
    },
  },
})
```

```python
# Set your secret key. Remember to switch to your live secret key in production.
# See your keys here: https://dashboard.stripe.com/apikeys
client = StripeClient("<<YOUR_SECRET_KEY>>")

# For SDK versions 12.4.0 or lower, remove '.v1' from the following line.
session = client.v1.checkout.sessions.create({
  "customer": "{{CUSTOMER_ID}}",
  "success_url": "https://example.com/success",
  "line_items": [{"price": "{PRICE_ID}", "quantity": 1}],
  "payment_method_options": {
    "us_bank_account": {
      "financial_connections": {
        "permissions": ["payment_method", "balances", "ownership", "transactions"],
      },
    },
  },
})
```

```php
// Set your secret key. Remember to switch to your live secret key in production.
// See your keys here: https://dashboard.stripe.com/apikeys
$stripe = new \Stripe\StripeClient('<<YOUR_SECRET_KEY>>');

$session = $stripe->checkout->sessions->create([
  'customer' => '{{CUSTOMER_ID}}',
  'success_url' => 'https://example.com/success',
  'line_items' => [
    [
      'price' => '{PRICE_ID}',
      'quantity' => 1,
    ],
  ],
  'payment_method_options' => [
    'us_bank_account' => [
      'financial_connections' => [
        'permissions' => ['payment_method', 'balances', 'ownership', 'transactions'],
      ],
    ],
  ],
]);
```

```java
// Set your secret key. Remember to switch to your live secret key in production.
// See your keys here: https://dashboard.stripe.com/apikeys
StripeClient client = new StripeClient("<<YOUR_SECRET_KEY>>");

SessionCreateParams params =
  SessionCreateParams.builder()
    .setCustomer("{{CUSTOMER_ID}}")
    .setSuccessUrl("https://example.com/success")
    .addLineItem(
      SessionCreateParams.LineItem.builder()
        .setPrice("{PRICE_ID}")
        .setQuantity(1L)
        .build()
    )
    .setPaymentMethodOptions(
      SessionCreateParams.PaymentMethodOptions.builder()
        .setUsBankAccount(
          SessionCreateParams.PaymentMethodOptions.UsBankAccount.builder()
            .setFinancialConnections(
              SessionCreateParams.PaymentMethodOptions.UsBankAccount.FinancialConnections.builder()
                .addPermission(
                  SessionCreateParams.PaymentMethodOptions.UsBankAccount.FinancialConnections.Permission.PAYMENT_METHOD
                )
                .addPermission(
                  SessionCreateParams.PaymentMethodOptions.UsBankAccount.FinancialConnections.Permission.BALANCES
                )
                .addPermission(
                  SessionCreateParams.PaymentMethodOptions.UsBankAccount.FinancialConnections.Permission.OWNERSHIP
                )
                .addPermission(
                  SessionCreateParams.PaymentMethodOptions.UsBankAccount.FinancialConnections.Permission.TRANSACTIONS
                )
                .build()
            )
            .build()
        )
        .build()
    )
    .build();

// For SDK versions 29.4.0 or lower, remove '.v1()' from the following line.
Session session = client.v1().checkout().sessions().create(params);
```

```node
// Set your secret key. Remember to switch to your live secret key in production.
// See your keys here: https://dashboard.stripe.com/apikeys
const stripe = require('stripe')('<<YOUR_SECRET_KEY>>');

const session = await stripe.checkout.sessions.create({
  customer: '{{CUSTOMER_ID}}',
  success_url: 'https://example.com/success',
  line_items: [
    {
      price: '{PRICE_ID}',
      quantity: 1,
    },
  ],
  payment_method_options: {
    us_bank_account: {
      financial_connections: {
        permissions: ['payment_method', 'balances', 'ownership', 'transactions'],
      },
    },
  },
});
```

```go
// Set your secret key. Remember to switch to your live secret key in production.
// See your keys here: https://dashboard.stripe.com/apikeys
sc := stripe.NewClient("<<YOUR_SECRET_KEY>>")
params := &stripe.CheckoutSessionCreateParams{
  Customer: stripe.String("{{CUSTOMER_ID}}"),
  SuccessURL: stripe.String("https://example.com/success"),
  LineItems: []*stripe.CheckoutSessionCreateLineItemParams{
    &stripe.CheckoutSessionCreateLineItemParams{
      Price: stripe.String("{PRICE_ID}"),
      Quantity: stripe.Int64(1),
    },
  },
  PaymentMethodOptions: &stripe.CheckoutSessionCreatePaymentMethodOptionsParams{
    USBankAccount: &stripe.CheckoutSessionCreatePaymentMethodOptionsUSBankAccountParams{
      FinancialConnections: &stripe.CheckoutSessionCreatePaymentMethodOptionsUSBankAccountFinancialConnectionsParams{
        Permissions: []*string{
          stripe.String(stripe.CheckoutSessionPaymentMethodOptionsUSBankAccountFinancialConnectionsPermissionPaymentMethod),
          stripe.String(stripe.CheckoutSessionPaymentMethodOptionsUSBankAccountFinancialConnectionsPermissionBalances),
          stripe.String(stripe.CheckoutSessionPaymentMethodOptionsUSBankAccountFinancialConnectionsPermissionOwnership),
          stripe.String(stripe.CheckoutSessionPaymentMethodOptionsUSBankAccountFinancialConnectionsPermissionTransactions),
        },
      },
    },
  },
}
result, err := sc.V1CheckoutSessions.Create(context.TODO(), params)
```

```dotnet
// Set your secret key. Remember to switch to your live secret key in production.
// See your keys here: https://dashboard.stripe.com/apikeys
var options = new Stripe.Checkout.SessionCreateOptions
{
    Customer = "{{CUSTOMER_ID}}",
    SuccessUrl = "https://example.com/success",
    LineItems = new List<Stripe.Checkout.SessionLineItemOptions>
    {
        new Stripe.Checkout.SessionLineItemOptions { Price = "{PRICE_ID}", Quantity = 1 },
    },
    PaymentMethodOptions = new Stripe.Checkout.SessionPaymentMethodOptionsOptions
    {
        UsBankAccount = new Stripe.Checkout.SessionPaymentMethodOptionsUsBankAccountOptions
        {
            FinancialConnections = new Stripe.Checkout.SessionPaymentMethodOptionsUsBankAccountFinancialConnectionsOptions
            {
                Permissions = new List<string>
                {
                    "payment_method",
                    "balances",
                    "ownership",
                    "transactions",
                },
            },
        },
    },
};
var client = new StripeClient("<<YOUR_SECRET_KEY>>");
var service = client.V1.Checkout.Sessions;
Stripe.Checkout.Session session = service.Create(options);
```

## Use data with your ACH integration

After you’ve been approved for additional bank account data access such balances or ownership, you can use this data to improve ACH payments performance. For example, you can use balance data to reduce the risk of insufficient funds failures. See related data guides for examples:

- [Balances](https://docs.stripe.com/financial-connections/balances.md): check account balance prior to payment initiation to reduce *NSFs* (A shorthand way of referring to the Non-sufficient Funds ACH return code R01).
- [Ownership](https://docs.stripe.com/financial-connections/ownership.md): pull account owners and compare against your internal data models to catch potential fraud.
- [Transactions](https://docs.stripe.com/financial-connections/transactions.md): pull an account’s transaction history to check when the customer’s paycheck might land.

> The Risk Intelligence API is a preview feature that provides additional aggregate data to help manage risk, such as average account balance over the past 30/60/90 days, total number of credit transactions over the past 30/60/90 days, and more. If you’re interested in using this preview feature, [email us](mailto:financial-connections-beta+risk-intelligence@stripe.com) for access.

### Finding the Financial Connections Account ID

To initiate data refreshes and retrieve data on a Financial Connections account, you first need to get the account’s ID from the linked payment method by expanding the PaymentIntent’s `payment_method` property:

```curl
curl -G https://api.stripe.com/v1/payment_intents/{{PAYMENT_INTENT}} \
  -u "<<YOUR_SECRET_KEY>>:" \
  -d "expand[]"=payment_method
```

```cli
stripe payment_intents retrieve {{PAYMENT_INTENT}} \
  -d "expand[0]"=payment_method
```

```ruby
# Set your secret key. Remember to switch to your live secret key in production.
# See your keys here: https://dashboard.stripe.com/apikeys
client = Stripe::StripeClient.new("<<YOUR_SECRET_KEY>>")

payment_intent = client.v1.payment_intents.retrieve(
  '{{PAYMENT_INTENT}}',
  {expand: ['payment_method']},
)
```

```python
# Set your secret key. Remember to switch to your live secret key in production.
# See your keys here: https://dashboard.stripe.com/apikeys
client = StripeClient("<<YOUR_SECRET_KEY>>")

# For SDK versions 12.4.0 or lower, remove '.v1' from the following line.
payment_intent = client.v1.payment_intents.retrieve(
  "{{PAYMENT_INTENT}}",
  {"expand": ["payment_method"]},
)
```

```php
// Set your secret key. Remember to switch to your live secret key in production.
// See your keys here: https://dashboard.stripe.com/apikeys
$stripe = new \Stripe\StripeClient('<<YOUR_SECRET_KEY>>');

$paymentIntent = $stripe->paymentIntents->retrieve(
  '{{PAYMENT_INTENT}}',
  ['expand' => ['payment_method']]
);
```

```java
// Set your secret key. Remember to switch to your live secret key in production.
// See your keys here: https://dashboard.stripe.com/apikeys
StripeClient client = new StripeClient("<<YOUR_SECRET_KEY>>");

PaymentIntentRetrieveParams params =
  PaymentIntentRetrieveParams.builder().addExpand("payment_method").build();

// For SDK versions 29.4.0 or lower, remove '.v1()' from the following line.
PaymentIntent paymentIntent =
  client.v1().paymentIntents().retrieve("{{PAYMENT_INTENT}}", params);
```

```node
// Set your secret key. Remember to switch to your live secret key in production.
// See your keys here: https://dashboard.stripe.com/apikeys
const stripe = require('stripe')('<<YOUR_SECRET_KEY>>');

const paymentIntent = await stripe.paymentIntents.retrieve(
  '{{PAYMENT_INTENT}}',
  {
    expand: ['payment_method'],
  }
);
```

```go
// Set your secret key. Remember to switch to your live secret key in production.
// See your keys here: https://dashboard.stripe.com/apikeys
sc := stripe.NewClient("<<YOUR_SECRET_KEY>>")
params := &stripe.PaymentIntentRetrieveParams{Intent: stripe.String("{{PAYMENT_INTENT}}")}
params.AddExpand("payment_method")
result, err := sc.V1PaymentIntents.Retrieve(context.TODO(), params)
```

```dotnet
// Set your secret key. Remember to switch to your live secret key in production.
// See your keys here: https://dashboard.stripe.com/apikeys
var options = new PaymentIntentGetOptions
{
    Expand = new List<string> { "payment_method" },
};
var client = new StripeClient("<<YOUR_SECRET_KEY>>");
var service = client.V1.PaymentIntents;
PaymentIntent paymentIntent = service.Get("{{PAYMENT_INTENT}}", options);
```

The Financial Connections account ID is on the expanded payment method’s [`us_bank_account` hash](https://docs.stripe.com/api/payment_methods/object.md#payment_method_object-us_bank_account). If you allow [manual entry fallback](https://docs.stripe.com/financial-connections/ach-direct-debit-payments.md#enable) and the user manually entered their account information, this field is `null`.

```json
{
  "id": "pi_3OK3g4FitzZY8Nvm11121Lhb",
  "object": "payment_intent",
  "payment_method": {
    "us_bank_account": {"financial_connections_account": "fca_1OK123bitUAA8SvmruWkck76"
    }
    // ... other fields on the Payment Method
  }
  // ... other fields on the Payment Intent
}
```


# Collect a bank account to enhance Connect payouts

Collect your connected account's bank account and use account data to enhance payouts.

Not sure about which Financial Connections integration to use? See our [overview of integration options](https://docs.stripe.com/financial-connections/use-cases.md).

Financial Connections lets you instantly collect tokenized account and routing numbers for payouts to connected accounts where your platform is liable for negative balances, including Custom and Express accounts.

Financial Connections helps you:

- Increase onboarding conversion by eliminating the need for your connected accounts to leave your website or application to locate their account and routing numbers.
- Reduce payout failure rates by eliminating errors that result from manual entry of account and routing numbers.
- Stay secure by not storing sensitive data such as account and routing numbers on your server.
- Save development time by eliminating your need to build bank account manual entry forms.
- Enable your users to connect their accounts in fewer steps with Link, allowing them to save and quickly reuse their bank account details across Stripe merchants.

Optionally, Stripe platforms in the US can request permission from your accounts to retrieve additional data on their [Financial Connections account](https://docs.stripe.com/api/financial_connections/accounts/object.md). Consider optimizing your onboarding process by accessing [balances](https://docs.stripe.com/financial-connections/balances.md), [transactions](https://docs.stripe.com/financial-connections/transactions.md), and [ownership](https://docs.stripe.com/financial-connections/ownership.md) information.

Retrieving additional account data can help you:

- Mitigate fraud when onboarding accounts by verifying the ownership details of their bank account, such as the name and address of the account holder.
- Underwrite accounts for financial services that you might offer on your platform with balances and transactions data.

## Get started 

For accounts where your platform is liable for negative balances, such as Custom and Express accounts, enable Stripe Financial Connections either within the [Connect Onboarding](https://docs.stripe.com/connect/payouts-bank-accounts.md?bank-account-collection-method=financial-connections) web form or within your [own onboarding flow](https://docs.stripe.com/connect/payouts-bank-accounts.md?bank-account-collection-method=financial-connections&bank-account-collection-integration=direct-api).

For accounts where your platform isn’t liable for negative balances, including Standard connected accounts, account onboarding always uses Financial Connections. The platform can’t access additional bank account data on those accounts.