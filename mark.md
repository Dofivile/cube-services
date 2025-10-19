# API onboarding

Build your own onboarding flow using Stripe's APIs.

With API onboarding, you use the Accounts API to build an onboarding flow, reporting functionality, and communication channels for your users. Stripe can be completely invisible to the account holder. However, your platform is responsible for all interactions with your accounts and for collecting all the information needed to verify them.

> #### Additional responsibilities
> 
> With API onboarding, your custom flow must meet all legal and regulatory requirements in the regions where you do business. You must also commit resources to track changes to those requirements and collect updated information on an ongoing basis, at least once every six months. If you want to implement a customized onboarding flow, Stripe strongly recommends that you use [embedded onboarding](https://docs.stripe.com/connect/embedded-onboarding.md).
 (See full diagram at https://docs.stripe.com/connect/api-onboarding)
## Establish requirements

The following factors affect the [onboarding requirements](https://docs.stripe.com/connect/required-verification-information.md) for your connected accounts:

- The origin country of the connected accounts
- The [service agreement type](https://docs.stripe.com/connect/service-agreement-types.md) applicable to the connected accounts
- The [capabilities](https://docs.stripe.com/connect/account-capabilities.md) requested for the connected accounts
- The [business_type](https://docs.stripe.com/api/accounts/object.md#account_object-business_type) (for example, individual or company) and [company.structure](https://docs.stripe.com/api/accounts/object.md#account_object-company-structure) (for example, `public_corporation` or `private_partnership`)

Use the [Required Verification Information](https://docs.stripe.com/connect/required-verification-information.md) tool to see how changing these factors affects the onboarding requirements for your connected accounts.

## Create forms to collect information [Client-side]

As a best practice, organize the required parameters into logical groupings or forms in your onboarding flow. You might wish to encode a mapping between the Stripe parameters and the logical groupings. Suggested logical groupings for parameters are shown in the first column of the example requirements table.

After you encode the required parameters into your application, generate UIs for the parameters corresponding to these requirements. For each parameter, design a UI form that includes:

- Parameter label, localized to each supported country and language
- Parameter description, localized to each supported country and language
- Parameter input fields with data validation logic and document uploading where required

It’s important to architect your application logic to account for the possibility of additional parameters in the future. For example, Stripe might introduce new parameters, new verifications, or new thresholds that you must incorporate into your onboarding flows over time.

Changing any of the factors that determine your connected accounts’ requirements means you must also adjust your collection forms to handle the changed requirements. [Country](https://docs.stripe.com/api/accounts/object.md#account_object-country) and [service agreement type](https://docs.stripe.com/api/accounts/object.md#account_object-tos_acceptance-service_agreement) are immutable, while [capabilities](https://docs.stripe.com/api/accounts/object.md#account_object-capabilities) and [business type](https://docs.stripe.com/api/accounts/object.md#account_object-business_type) are mutable.

- To change an immutable field, create a new connected account with the new values to replace the existing account.
- To change a mutable field, update the connected account.

### Include the Stripe Terms of Service Agreement

Your connected accounts must accept Stripe’s terms of service before they can activate. You can [wrap Stripe’s terms of service in your own terms of service](https://docs.stripe.com/connect/updating-service-agreements.md#adding-stripes-service-agreement-to-your-terms-of-service).

## Create a connected account [Server-side]

Create an [Account](https://docs.stripe.com/api/accounts/create.md) where your platform is liable for negative balances, Stripe collects fees from your platform account, and your connected accounts don’t have access to a Stripe-hosted Dashboard. Request any capabilities that your connected accounts need. Prefill the business type and any other available information matching your [requirements](https://docs.stripe.com/connect/api-onboarding.md#establish-requirements).

Alternatively, you can create a connected account with `type` set to `custom` and desired capabilities.

If you don’t specify the country and service type agreement, they’re assigned the following default values:

- The `country` defaults to the same country as your platform.
- The service type agreement (`tos_acceptance.service_agreement`) defaults to `full`.

> To comply with French PSD2 regulations, platforms in France [must use account tokens](https://stripe.com/guides/frequently-asked-questions-about-stripe-connect-and-psd2#regulatory-status-of-connect). An additional benefit of tokens is that the platform doesn’t have to store PII data, which is transferred from the connected account directly to Stripe. For platforms in other countries, we recommend using account tokens, but they aren’t required.

#### With controller properties

```curl
curl https://api.stripe.com/v1/accounts \
  -u "<<YOUR_SECRET_KEY>>:" \
  -d "controller[losses][payments]"=application \
  -d "controller[fees][payer]"=application \
  -d "controller[stripe_dashboard][type]"=none \
  -d "controller[requirement_collection]"=application \
  -d "capabilities[card_payments][requested]"=true \
  -d "capabilities[transfers][requested]"=true \
  -d business_type=individual \
  -d country=US
```

```cli
stripe accounts create  \
  -d "controller[losses][payments]"=application \
  -d "controller[fees][payer]"=application \
  -d "controller[stripe_dashboard][type]"=none \
  -d "controller[requirement_collection]"=application \
  -d "capabilities[card_payments][requested]"=true \
  -d "capabilities[transfers][requested]"=true \
  --business-type=individual \
  --country=US
```

```ruby
# Set your secret key. Remember to switch to your live secret key in production.
# See your keys here: https://dashboard.stripe.com/apikeys
client = Stripe::StripeClient.new("<<YOUR_SECRET_KEY>>")

account = client.v1.accounts.create({
  controller: {
    losses: {payments: 'application'},
    fees: {payer: 'application'},
    stripe_dashboard: {type: 'none'},
    requirement_collection: 'application',
  },
  capabilities: {
    card_payments: {requested: true},
    transfers: {requested: true},
  },
  business_type: 'individual',
  country: 'US',
})
```

```python
# Set your secret key. Remember to switch to your live secret key in production.
# See your keys here: https://dashboard.stripe.com/apikeys
client = StripeClient("<<YOUR_SECRET_KEY>>")

# For SDK versions 12.4.0 or lower, remove '.v1' from the following line.
account = client.v1.accounts.create({
  "controller": {
    "losses": {"payments": "application"},
    "fees": {"payer": "application"},
    "stripe_dashboard": {"type": "none"},
    "requirement_collection": "application",
  },
  "capabilities": {
    "card_payments": {"requested": True},
    "transfers": {"requested": True},
  },
  "business_type": "individual",
  "country": "US",
})
```

```php
// Set your secret key. Remember to switch to your live secret key in production.
// See your keys here: https://dashboard.stripe.com/apikeys
$stripe = new \Stripe\StripeClient('<<YOUR_SECRET_KEY>>');

$account = $stripe->accounts->create([
  'controller' => [
    'losses' => ['payments' => 'application'],
    'fees' => ['payer' => 'application'],
    'stripe_dashboard' => ['type' => 'none'],
    'requirement_collection' => 'application',
  ],
  'capabilities' => [
    'card_payments' => ['requested' => true],
    'transfers' => ['requested' => true],
  ],
  'business_type' => 'individual',
  'country' => 'US',
]);
```

```java
// Set your secret key. Remember to switch to your live secret key in production.
// See your keys here: https://dashboard.stripe.com/apikeys
StripeClient client = new StripeClient("<<YOUR_SECRET_KEY>>");

AccountCreateParams params =
  AccountCreateParams.builder()
    .setController(
      AccountCreateParams.Controller.builder()
        .setLosses(
          AccountCreateParams.Controller.Losses.builder()
            .setPayments(AccountCreateParams.Controller.Losses.Payments.APPLICATION)
            .build()
        )
        .setFees(
          AccountCreateParams.Controller.Fees.builder()
            .setPayer(AccountCreateParams.Controller.Fees.Payer.APPLICATION)
            .build()
        )
        .setStripeDashboard(
          AccountCreateParams.Controller.StripeDashboard.builder()
            .setType(AccountCreateParams.Controller.StripeDashboard.Type.NONE)
            .build()
        )
        .setRequirementCollection(
          AccountCreateParams.Controller.RequirementCollection.APPLICATION
        )
        .build()
    )
    .setCapabilities(
      AccountCreateParams.Capabilities.builder()
        .setCardPayments(
          AccountCreateParams.Capabilities.CardPayments.builder()
            .setRequested(true)
            .build()
        )
        .setTransfers(
          AccountCreateParams.Capabilities.Transfers.builder().setRequested(true).build()
        )
        .build()
    )
    .setBusinessType(AccountCreateParams.BusinessType.INDIVIDUAL)
    .setCountry("US")
    .build();

// For SDK versions 29.4.0 or lower, remove '.v1()' from the following line.
Account account = client.v1().accounts().create(params);
```

```node
// Set your secret key. Remember to switch to your live secret key in production.
// See your keys here: https://dashboard.stripe.com/apikeys
const stripe = require('stripe')('<<YOUR_SECRET_KEY>>');

const account = await stripe.accounts.create({
  controller: {
    losses: {
      payments: 'application',
    },
    fees: {
      payer: 'application',
    },
    stripe_dashboard: {
      type: 'none',
    },
    requirement_collection: 'application',
  },
  capabilities: {
    card_payments: {
      requested: true,
    },
    transfers: {
      requested: true,
    },
  },
  business_type: 'individual',
  country: 'US',
});
```

```go
// Set your secret key. Remember to switch to your live secret key in production.
// See your keys here: https://dashboard.stripe.com/apikeys
sc := stripe.NewClient("<<YOUR_SECRET_KEY>>")
params := &stripe.AccountCreateParams{
  Controller: &stripe.AccountCreateControllerParams{
    Losses: &stripe.AccountCreateControllerLossesParams{
      Payments: stripe.String(stripe.AccountControllerLossesPaymentsApplication),
    },
    Fees: &stripe.AccountCreateControllerFeesParams{
      Payer: stripe.String(stripe.AccountControllerFeesPayerApplication),
    },
    StripeDashboard: &stripe.AccountCreateControllerStripeDashboardParams{
      Type: stripe.String(stripe.AccountControllerStripeDashboardTypeNone),
    },
    RequirementCollection: stripe.String(stripe.AccountControllerRequirementCollectionApplication),
  },
  Capabilities: &stripe.AccountCreateCapabilitiesParams{
    CardPayments: &stripe.AccountCreateCapabilitiesCardPaymentsParams{
      Requested: stripe.Bool(true),
    },
    Transfers: &stripe.AccountCreateCapabilitiesTransfersParams{
      Requested: stripe.Bool(true),
    },
  },
  BusinessType: stripe.String(stripe.AccountBusinessTypeIndividual),
  Country: stripe.String("US"),
}
result, err := sc.V1Accounts.Create(context.TODO(), params)
```

```dotnet
// Set your secret key. Remember to switch to your live secret key in production.
// See your keys here: https://dashboard.stripe.com/apikeys
var options = new AccountCreateOptions
{
    Controller = new AccountControllerOptions
    {
        Losses = new AccountControllerLossesOptions { Payments = "application" },
        Fees = new AccountControllerFeesOptions { Payer = "application" },
        StripeDashboard = new AccountControllerStripeDashboardOptions { Type = "none" },
        RequirementCollection = "application",
    },
    Capabilities = new AccountCapabilitiesOptions
    {
        CardPayments = new AccountCapabilitiesCardPaymentsOptions { Requested = true },
        Transfers = new AccountCapabilitiesTransfersOptions { Requested = true },
    },
    BusinessType = "individual",
    Country = "US",
};
var client = new StripeClient("<<YOUR_SECRET_KEY>>");
var service = client.V1.Accounts;
Account account = service.Create(options);
```

#### With an account type

```curl
curl https://api.stripe.com/v1/accounts \
  -u "<<YOUR_SECRET_KEY>>:" \
  -d type=custom \
  -d "capabilities[card_payments][requested]"=true \
  -d "capabilities[transfers][requested]"=true \
  -d business_type=individual \
  -d country=US
```

```cli
stripe accounts create  \
  --type=custom \
  -d "capabilities[card_payments][requested]"=true \
  -d "capabilities[transfers][requested]"=true \
  --business-type=individual \
  --country=US
```

```ruby
# Set your secret key. Remember to switch to your live secret key in production.
# See your keys here: https://dashboard.stripe.com/apikeys
client = Stripe::StripeClient.new("<<YOUR_SECRET_KEY>>")

account = client.v1.accounts.create({
  type: 'custom',
  capabilities: {
    card_payments: {requested: true},
    transfers: {requested: true},
  },
  business_type: 'individual',
  country: 'US',
})
```

```python
# Set your secret key. Remember to switch to your live secret key in production.
# See your keys here: https://dashboard.stripe.com/apikeys
client = StripeClient("<<YOUR_SECRET_KEY>>")

# For SDK versions 12.4.0 or lower, remove '.v1' from the following line.
account = client.v1.accounts.create({
  "type": "custom",
  "capabilities": {
    "card_payments": {"requested": True},
    "transfers": {"requested": True},
  },
  "business_type": "individual",
  "country": "US",
})
```

```php
// Set your secret key. Remember to switch to your live secret key in production.
// See your keys here: https://dashboard.stripe.com/apikeys
$stripe = new \Stripe\StripeClient('<<YOUR_SECRET_KEY>>');

$account = $stripe->accounts->create([
  'type' => 'custom',
  'capabilities' => [
    'card_payments' => ['requested' => true],
    'transfers' => ['requested' => true],
  ],
  'business_type' => 'individual',
  'country' => 'US',
]);
```

```java
// Set your secret key. Remember to switch to your live secret key in production.
// See your keys here: https://dashboard.stripe.com/apikeys
StripeClient client = new StripeClient("<<YOUR_SECRET_KEY>>");

AccountCreateParams params =
  AccountCreateParams.builder()
    .setType(AccountCreateParams.Type.CUSTOM)
    .setCapabilities(
      AccountCreateParams.Capabilities.builder()
        .setCardPayments(
          AccountCreateParams.Capabilities.CardPayments.builder()
            .setRequested(true)
            .build()
        )
        .setTransfers(
          AccountCreateParams.Capabilities.Transfers.builder().setRequested(true).build()
        )
        .build()
    )
    .setBusinessType(AccountCreateParams.BusinessType.INDIVIDUAL)
    .setCountry("US")
    .build();

// For SDK versions 29.4.0 or lower, remove '.v1()' from the following line.
Account account = client.v1().accounts().create(params);
```

```node
// Set your secret key. Remember to switch to your live secret key in production.
// See your keys here: https://dashboard.stripe.com/apikeys
const stripe = require('stripe')('<<YOUR_SECRET_KEY>>');

const account = await stripe.accounts.create({
  type: 'custom',
  capabilities: {
    card_payments: {
      requested: true,
    },
    transfers: {
      requested: true,
    },
  },
  business_type: 'individual',
  country: 'US',
});
```

```go
// Set your secret key. Remember to switch to your live secret key in production.
// See your keys here: https://dashboard.stripe.com/apikeys
sc := stripe.NewClient("<<YOUR_SECRET_KEY>>")
params := &stripe.AccountCreateParams{
  Type: stripe.String(stripe.AccountTypeCustom),
  Capabilities: &stripe.AccountCreateCapabilitiesParams{
    CardPayments: &stripe.AccountCreateCapabilitiesCardPaymentsParams{
      Requested: stripe.Bool(true),
    },
    Transfers: &stripe.AccountCreateCapabilitiesTransfersParams{
      Requested: stripe.Bool(true),
    },
  },
  BusinessType: stripe.String(stripe.AccountBusinessTypeIndividual),
  Country: stripe.String("US"),
}
result, err := sc.V1Accounts.Create(context.TODO(), params)
```

```dotnet
// Set your secret key. Remember to switch to your live secret key in production.
// See your keys here: https://dashboard.stripe.com/apikeys
var options = new AccountCreateOptions
{
    Type = "custom",
    Capabilities = new AccountCapabilitiesOptions
    {
        CardPayments = new AccountCapabilitiesCardPaymentsOptions { Requested = true },
        Transfers = new AccountCapabilitiesTransfersOptions { Requested = true },
    },
    BusinessType = "individual",
    Country = "US",
};
var client = new StripeClient("<<YOUR_SECRET_KEY>>");
var service = client.V1.Accounts;
Account account = service.Create(options);
```

## Determine the information to collect [Server-side]

As the platform, you must decide if you want to collect the required information from your connected accounts *up front* (Upfront onboarding is a type of onboarding where you collect all required verification information from your users at sign-up) or *incrementally* (Incremental onboarding is a type of onboarding where you gradually collect required verification information from your users. You collect a minimum amount of information at sign-up, and you collect more information as the connected account earns more revenue). Up-front onboarding collects the `eventually_due` requirements for the account, while incremental onboarding only collects the `currently_due` requirements.

| Onboarding type | Advantages                                                                                                                                                                                                               |
| --------------- | ------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------ |
| **Up-front**    | - Normally requires only one request for all information
  - Avoids the possibility of payout and processing issues due to missed deadlines
  - Exposes potential risk early when accounts refuse to provide information |
| **Incremental** | - Accounts can onboard quickly because they don’t have to provide as much information                                                                                                                                    |

To determine whether to use up-front or incremental onboarding, review the [requirements](https://docs.stripe.com/connect/required-verification-information.md) for your connected accounts’ locations and capabilities. While Stripe tries to minimize any impact to connected accounts, requirements might change over time.

For connected accounts where you’re responsible for requirement collection, you can customize the behavior of [future requirements](https://docs.stripe.com/connect/handle-verification-updates.md) using the `collection_options` parameter. To collect the account’s future requirements, set [`collection_options.future_requirements`](https://docs.stripe.com/api/account_links/create.md#create_account_link-collection_options-future_requirements) to `include`.

To implement your onboarding strategy, inspect the requirements hash of the connected account you created. The requirements hash provides a complete list of the information you must collect to activate the connected account.

- For incremental onboarding, inspect the `currently_due` hash in the requirements hash and build an onboarding flow that only collects those requirements.
- For up-front onboarding, inspect the`currently_due` and `eventually_due` hashes in the requirements hash, and build an onboarding flow that collects those requirements.

```json
{
  ...
  "requirements": {
    "alternatives": [],
    "current_deadline": null,
    "currently_due": [
      "business_profile.product_description",
      "business_profile.support_phone",
      "business_profile.url",
      "external_account",
      "tos_acceptance.date",
      "tos_acceptance.ip"
    ],
    "disabled_reason": "requirements.past_due",
    "errors": [],"eventually_due": [
      "business_profile.product_description",
      "business_profile.support_phone",
      "business_profile.url",
      "external_account",
      "tos_acceptance.date",
      "tos_acceptance.ip"
    ],
    "past_due": [],
    "pending_verification": []
  },
  ...
}
```

## Handle liveness requirements

An account can have one or more [Person](https://docs.stripe.com/api/persons.md) objects with a `proof_of_liveness` requirement. A `proof_of_liveness` requirement might require collection of an electronic ID credential such as [MyInfo](https://www.singpass.gov.sg/main/individuals/) in Singapore, or by using Stripe Identity to collect a document or selfie. We recommend using Stripe-hosted or embedded onboarding to satisfy all variations of the `proof_of_liveness` requirement.

#### Hosted

[Stripe-hosted onboarding](https://docs.stripe.com/connect/hosted-onboarding.md) can complete all variations of `proof_of_liveness` requirements.

[Create an Account Link](https://docs.stripe.com/connect/hosted-onboarding.md#create-account-link) using the connected account ID, and send the account to the `url` returned.

```curl
curl https://api.stripe.com/v1/account_links \
  -u "<<YOUR_SECRET_KEY>>:" \
  -d account="{{CONNECTEDACCOUNT_ID}}" \
  --data-urlencode refresh_url="https://example.com/refresh" \
  --data-urlencode return_url="https://example.com/return" \
  -d type=account_onboarding \
  -d "collection_options[fields]"=currently_due
```

```cli
stripe account_links create  \
  --account="{{CONNECTEDACCOUNT_ID}}" \
  --refresh-url="https://example.com/refresh" \
  --return-url="https://example.com/return" \
  --type=account_onboarding \
  -d "collection_options[fields]"=currently_due
```

```ruby
# Set your secret key. Remember to switch to your live secret key in production.
# See your keys here: https://dashboard.stripe.com/apikeys
client = Stripe::StripeClient.new("<<YOUR_SECRET_KEY>>")

account_link = client.v1.account_links.create({
  account: '{{CONNECTEDACCOUNT_ID}}',
  refresh_url: 'https://example.com/refresh',
  return_url: 'https://example.com/return',
  type: 'account_onboarding',
  collection_options: {fields: 'currently_due'},
})
```

```python
# Set your secret key. Remember to switch to your live secret key in production.
# See your keys here: https://dashboard.stripe.com/apikeys
client = StripeClient("<<YOUR_SECRET_KEY>>")

# For SDK versions 12.4.0 or lower, remove '.v1' from the following line.
account_link = client.v1.account_links.create({
  "account": "{{CONNECTEDACCOUNT_ID}}",
  "refresh_url": "https://example.com/refresh",
  "return_url": "https://example.com/return",
  "type": "account_onboarding",
  "collection_options": {"fields": "currently_due"},
})
```

```php
// Set your secret key. Remember to switch to your live secret key in production.
// See your keys here: https://dashboard.stripe.com/apikeys
$stripe = new \Stripe\StripeClient('<<YOUR_SECRET_KEY>>');

$accountLink = $stripe->accountLinks->create([
  'account' => '{{CONNECTEDACCOUNT_ID}}',
  'refresh_url' => 'https://example.com/refresh',
  'return_url' => 'https://example.com/return',
  'type' => 'account_onboarding',
  'collection_options' => ['fields' => 'currently_due'],
]);
```

```java
// Set your secret key. Remember to switch to your live secret key in production.
// See your keys here: https://dashboard.stripe.com/apikeys
StripeClient client = new StripeClient("<<YOUR_SECRET_KEY>>");

AccountLinkCreateParams params =
  AccountLinkCreateParams.builder()
    .setAccount("{{CONNECTEDACCOUNT_ID}}")
    .setRefreshUrl("https://example.com/refresh")
    .setReturnUrl("https://example.com/return")
    .setType(AccountLinkCreateParams.Type.ACCOUNT_ONBOARDING)
    .setCollectionOptions(
      AccountLinkCreateParams.CollectionOptions.builder()
        .setFields(AccountLinkCreateParams.CollectionOptions.Fields.CURRENTLY_DUE)
        .build()
    )
    .build();

// For SDK versions 29.4.0 or lower, remove '.v1()' from the following line.
AccountLink accountLink = client.v1().accountLinks().create(params);
```

```node
// Set your secret key. Remember to switch to your live secret key in production.
// See your keys here: https://dashboard.stripe.com/apikeys
const stripe = require('stripe')('<<YOUR_SECRET_KEY>>');

const accountLink = await stripe.accountLinks.create({
  account: '{{CONNECTEDACCOUNT_ID}}',
  refresh_url: 'https://example.com/refresh',
  return_url: 'https://example.com/return',
  type: 'account_onboarding',
  collection_options: {
    fields: 'currently_due',
  },
});
```

```go
// Set your secret key. Remember to switch to your live secret key in production.
// See your keys here: https://dashboard.stripe.com/apikeys
sc := stripe.NewClient("<<YOUR_SECRET_KEY>>")
params := &stripe.AccountLinkCreateParams{
  Account: stripe.String("{{CONNECTEDACCOUNT_ID}}"),
  RefreshURL: stripe.String("https://example.com/refresh"),
  ReturnURL: stripe.String("https://example.com/return"),
  Type: stripe.String("account_onboarding"),
  CollectionOptions: &stripe.AccountLinkCreateCollectionOptionsParams{
    Fields: stripe.String("currently_due"),
  },
}
result, err := sc.V1AccountLinks.Create(context.TODO(), params)
```

```dotnet
// Set your secret key. Remember to switch to your live secret key in production.
// See your keys here: https://dashboard.stripe.com/apikeys
var options = new AccountLinkCreateOptions
{
    Account = "{{CONNECTEDACCOUNT_ID}}",
    RefreshUrl = "https://example.com/refresh",
    ReturnUrl = "https://example.com/return",
    Type = "account_onboarding",
    CollectionOptions = new AccountLinkCollectionOptionsOptions
    {
        Fields = "currently_due",
    },
};
var client = new StripeClient("<<YOUR_SECRET_KEY>>");
var service = client.V1.AccountLinks;
AccountLink accountLink = service.Create(options);
```

The account receives a prompt to complete the `proof_of_liveness` requirement, along with any other currently due requirements. Listen to the `account.updated` event sent to your webhook endpoint to be notified when the account completes requirements and updates their information. After the account completes the requirement, the account is redirected to the `return_url` specified.

#### Embedded

[Embedded onboarding](https://docs.stripe.com/connect/embedded-onboarding.md) can complete all forms of `proof_of_liveness` requirements.

When [creating an Account Session](https://docs.stripe.com/api/account_sessions/create.md), enable account onboarding by specifying `account_onboarding` in the `components` parameter.

If you don’t need to collect bank account information, disable `external_account_collection`. This typically applies to Connect platforms that want to use third-party external account collection providers.

```curl
curl https://api.stripe.com/v1/account_sessions \
  -u "<<YOUR_SECRET_KEY>>:" \
  -d account="{{CONNECTEDACCOUNT_ID}}" \
  -d "components[account_onboarding][enabled]"=true \
  -d "components[account_onboarding][features][external_account_collection]"=false
```

```cli
stripe account_sessions create  \
  --account="{{CONNECTEDACCOUNT_ID}}" \
  -d "components[account_onboarding][enabled]"=true \
  -d "components[account_onboarding][features][external_account_collection]"=false
```

```ruby
# Set your secret key. Remember to switch to your live secret key in production.
# See your keys here: https://dashboard.stripe.com/apikeys
client = Stripe::StripeClient.new("<<YOUR_SECRET_KEY>>")

account_session = client.v1.account_sessions.create({
  account: '{{CONNECTEDACCOUNT_ID}}',
  components: {
    account_onboarding: {
      enabled: true,
      features: {external_account_collection: false},
    },
  },
})
```

```python
# Set your secret key. Remember to switch to your live secret key in production.
# See your keys here: https://dashboard.stripe.com/apikeys
client = StripeClient("<<YOUR_SECRET_KEY>>")

# For SDK versions 12.4.0 or lower, remove '.v1' from the following line.
account_session = client.v1.account_sessions.create({
  "account": "{{CONNECTEDACCOUNT_ID}}",
  "components": {
    "account_onboarding": {
      "enabled": True,
      "features": {"external_account_collection": False},
    },
  },
})
```

```php
// Set your secret key. Remember to switch to your live secret key in production.
// See your keys here: https://dashboard.stripe.com/apikeys
$stripe = new \Stripe\StripeClient('<<YOUR_SECRET_KEY>>');

$accountSession = $stripe->accountSessions->create([
  'account' => '{{CONNECTEDACCOUNT_ID}}',
  'components' => [
    'account_onboarding' => [
      'enabled' => true,
      'features' => ['external_account_collection' => false],
    ],
  ],
]);
```

```java
// Set your secret key. Remember to switch to your live secret key in production.
// See your keys here: https://dashboard.stripe.com/apikeys
StripeClient client = new StripeClient("<<YOUR_SECRET_KEY>>");

AccountSessionCreateParams params =
  AccountSessionCreateParams.builder()
    .setAccount("{{CONNECTEDACCOUNT_ID}}")
    .setComponents(
      AccountSessionCreateParams.Components.builder()
        .setAccountOnboarding(
          AccountSessionCreateParams.Components.AccountOnboarding.builder()
            .setEnabled(true)
            .setFeatures(
              AccountSessionCreateParams.Components.AccountOnboarding.Features.builder()
                .setExternalAccountCollection(false)
                .build()
            )
            .build()
        )
        .build()
    )
    .build();

// For SDK versions 29.4.0 or lower, remove '.v1()' from the following line.
AccountSession accountSession = client.v1().accountSessions().create(params);
```

```node
// Set your secret key. Remember to switch to your live secret key in production.
// See your keys here: https://dashboard.stripe.com/apikeys
const stripe = require('stripe')('<<YOUR_SECRET_KEY>>');

const accountSession = await stripe.accountSessions.create({
  account: '{{CONNECTEDACCOUNT_ID}}',
  components: {
    account_onboarding: {
      enabled: true,
      features: {
        external_account_collection: false,
      },
    },
  },
});
```

```go
// Set your secret key. Remember to switch to your live secret key in production.
// See your keys here: https://dashboard.stripe.com/apikeys
sc := stripe.NewClient("<<YOUR_SECRET_KEY>>")
params := &stripe.AccountSessionCreateParams{
  Account: stripe.String("{{CONNECTEDACCOUNT_ID}}"),
  Components: &stripe.AccountSessionCreateComponentsParams{
    AccountOnboarding: &stripe.AccountSessionCreateComponentsAccountOnboardingParams{
      Enabled: stripe.Bool(true),
      Features: &stripe.AccountSessionCreateComponentsAccountOnboardingFeaturesParams{
        ExternalAccountCollection: stripe.Bool(false),
      },
    },
  },
}
result, err := sc.V1AccountSessions.Create(context.TODO(), params)
```

```dotnet
// Set your secret key. Remember to switch to your live secret key in production.
// See your keys here: https://dashboard.stripe.com/apikeys
var options = new AccountSessionCreateOptions
{
    Account = "{{CONNECTEDACCOUNT_ID}}",
    Components = new AccountSessionComponentsOptions
    {
        AccountOnboarding = new AccountSessionComponentsAccountOnboardingOptions
        {
            Enabled = true,
            Features = new AccountSessionComponentsAccountOnboardingFeaturesOptions
            {
                ExternalAccountCollection = false,
            },
        },
    },
};
var client = new StripeClient("<<YOUR_SECRET_KEY>>");
var service = client.V1.AccountSessions;
AccountSession accountSession = service.Create(options);
```

After creating the Account Session and [initializing ConnectJS](https://docs.stripe.com/connect/get-started-connect-embedded-components.md#account-sessions), you can render the Account onboarding component in the front end:

#### JavaScript

```js
// Include this element in your HTML
const accountOnboarding = stripeConnectInstance.create('account-onboarding');
accountOnboarding.setOnExit(() => {
  console.log('User exited the onboarding flow');
});
container.appendChild(accountOnboarding);

// Optional: make sure to follow our policy instructions above
// accountOnboarding.setFullTermsOfServiceUrl('{{URL}}')
// accountOnboarding.setRecipientTermsOfServiceUrl('{{URL}}')
// accountOnboarding.setPrivacyPolicyUrl('{{URL}}')
// accountOnboarding.setCollectionOptions({
//   fields: 'eventually_due',
//   futureRequirements: 'include',
// })
// accountOnboarding.setOnStepChange((stepChange) => {
//   console.log(`User entered: ${stepChange.step}`);
// });
```

#### React

```jsx
import * as React from "react";
import { ConnectAccountOnboarding, ConnectComponentsProvider } from "@stripe/react-connect-js";

const AccountOnboardingUI = () => {
  return (
    <ConnectComponentsProvider connectInstance={stripeConnectInstance}>
      <ConnectAccountOnboarding
          onExit={() => {
            console.log("The account has exited onboarding");
          }}
          // Optional: make sure to follow our policy instructions above
          // fullTermsOfServiceUrl="{{URL}}"
          // recipientTermsOfServiceUrl="{{URL}}"
          // privacyPolicyUrl="{{URL}}"
          // collectionOptions={{
          //   fields: 'eventually_due',
          //   futureRequirements: 'include',
          // }}
          // onStepChange={(stepChange) => {
          //   console.log(`User entered: ${stepChange.step}`);
          // }}
        />
    </ConnectComponentsProvider>
  );
}
```

The account receives a prompt to complete the `proof_of_liveness` requirement, along with any other currently due requirements. Listen to the `account.updated` event sent to your webhook endpoint to be notified when the account completes requirements and updates their information. After the account completes the requirements, ConnectJS calls your `onExit` JavaScript handler.

## Update the connected account [Server-side]

[Update the Account object](https://docs.stripe.com/api/accounts/update.md) with new information as your connected account progresses through each step of the onboarding flow. That allows Stripe to validate the information as soon as it’s added. After Stripe confirms acceptance of our terms of service, any change to the `Account` triggers reverification. For example, if you change the connected account’s name and ID number, Stripe reruns verifications.

```curl
curl https://api.stripe.com/v1/accounts/{{CONNECTEDACCOUNT_ID}} \
  -u "<<YOUR_SECRET_KEY>>:" \
  --data-urlencode "business_profile[url]"="https://furever.dev" \
  -d "tos_acceptance[date]"=1609798905 \
  -d "tos_acceptance[ip]"="8.8.8.8"
```

```cli
stripe accounts update {{CONNECTEDACCOUNT_ID}} \
  -d "business_profile[url]"="https://furever.dev" \
  -d "tos_acceptance[date]"=1609798905 \
  -d "tos_acceptance[ip]"="8.8.8.8"
```

```ruby
# Set your secret key. Remember to switch to your live secret key in production.
# See your keys here: https://dashboard.stripe.com/apikeys
client = Stripe::StripeClient.new("<<YOUR_SECRET_KEY>>")

account = client.v1.accounts.update(
  '{{CONNECTEDACCOUNT_ID}}',
  {
    business_profile: {url: 'https://furever.dev'},
    tos_acceptance: {
      date: 1609798905,
      ip: '8.8.8.8',
    },
  },
)
```

```python
# Set your secret key. Remember to switch to your live secret key in production.
# See your keys here: https://dashboard.stripe.com/apikeys
client = StripeClient("<<YOUR_SECRET_KEY>>")

# For SDK versions 12.4.0 or lower, remove '.v1' from the following line.
account = client.v1.accounts.update(
  "{{CONNECTEDACCOUNT_ID}}",
  {
    "business_profile": {"url": "https://furever.dev"},
    "tos_acceptance": {"date": 1609798905, "ip": "8.8.8.8"},
  },
)
```

```php
// Set your secret key. Remember to switch to your live secret key in production.
// See your keys here: https://dashboard.stripe.com/apikeys
$stripe = new \Stripe\StripeClient('<<YOUR_SECRET_KEY>>');

$account = $stripe->accounts->update(
  '{{CONNECTEDACCOUNT_ID}}',
  [
    'business_profile' => ['url' => 'https://furever.dev'],
    'tos_acceptance' => [
      'date' => 1609798905,
      'ip' => '8.8.8.8',
    ],
  ]
);
```

```java
// Set your secret key. Remember to switch to your live secret key in production.
// See your keys here: https://dashboard.stripe.com/apikeys
StripeClient client = new StripeClient("<<YOUR_SECRET_KEY>>");

AccountUpdateParams params =
  AccountUpdateParams.builder()
    .setBusinessProfile(
      AccountUpdateParams.BusinessProfile.builder().setUrl("https://furever.dev").build()
    )
    .setTosAcceptance(
      AccountUpdateParams.TosAcceptance.builder()
        .setDate(1609798905L)
        .setIp("8.8.8.8")
        .build()
    )
    .build();

// For SDK versions 29.4.0 or lower, remove '.v1()' from the following line.
Account account = client.v1().accounts().update("{{CONNECTEDACCOUNT_ID}}", params);
```

```node
// Set your secret key. Remember to switch to your live secret key in production.
// See your keys here: https://dashboard.stripe.com/apikeys
const stripe = require('stripe')('<<YOUR_SECRET_KEY>>');

const account = await stripe.accounts.update(
  '{{CONNECTEDACCOUNT_ID}}',
  {
    business_profile: {
      url: 'https://furever.dev',
    },
    tos_acceptance: {
      date: 1609798905,
      ip: '8.8.8.8',
    },
  }
);
```

```go
// Set your secret key. Remember to switch to your live secret key in production.
// See your keys here: https://dashboard.stripe.com/apikeys
sc := stripe.NewClient("<<YOUR_SECRET_KEY>>")
params := &stripe.AccountUpdateParams{
  BusinessProfile: &stripe.AccountUpdateBusinessProfileParams{
    URL: stripe.String("https://furever.dev"),
  },
  TOSAcceptance: &stripe.AccountUpdateTOSAcceptanceParams{
    Date: stripe.Int64(1609798905),
    IP: stripe.String("8.8.8.8"),
  },
  Account: stripe.String("{{CONNECTEDACCOUNT_ID}}"),
}
result, err := sc.V1Accounts.Update(context.TODO(), params)
```

```dotnet
// Set your secret key. Remember to switch to your live secret key in production.
// See your keys here: https://dashboard.stripe.com/apikeys
var options = new AccountUpdateOptions
{
    BusinessProfile = new AccountBusinessProfileOptions { Url = "https://furever.dev" },
    TosAcceptance = new AccountTosAcceptanceOptions
    {
        Date = DateTimeOffset.FromUnixTimeSeconds(1609798905).UtcDateTime,
        Ip = "8.8.8.8",
    },
};
var client = new StripeClient("<<YOUR_SECRET_KEY>>");
var service = client.V1.Accounts;
Account account = service.Update("{{CONNECTEDACCOUNT_ID}}", options);
```

When updating a connected account, you must handle any [verification errors](https://docs.stripe.com/connect/api-onboarding.md#handle-verification-errors) or [HTTP error codes](https://docs.stripe.com/error-handling.md).

## Handle verification errors [Server-side]

When the connected account’s data is submitted, Stripe verifies it. This process might take minutes or hours, depending on the nature of the verification. During this process, the capabilities you requested have a `status` of `pending`.

### Review status

You can retrieve the status of your connected account’s capabilities by:

- Inspecting the Account object’s [capabilities](https://docs.stripe.com/api/accounts/object.md#account_object-capabilities) hash for the relevant capability.
- Requesting capabilities directly from the [Capabilities API](https://docs.stripe.com/api/capabilities/retrieve.md) and inspecting the status of the relevant capability.
- Listening for `account.updated` [events](https://docs.stripe.com/api/events/types.md#event_types-account.updated) in your [webhook](https://docs.stripe.com/connect/webhooks.md) endpoint and inspecting the `capabilities` hash for the relevant capability.

After verifications are complete, a capability becomes `active` and available to the connected account. Account verifications run continuously, and if a future verification fails, a capability can transition out of `active`. Listen for `account.updated` events to detect changes to capability states.

Confirm that your Connect integration is compliant and operational by checking that the account’s `charges_enabled` and `payouts_enabled` are both true. You can use the API or listen for `account.updated` events. For details on other relevant fields, check the account’s [requirements](https://docs.stripe.com/api/accounts/object.md#account_object-requirements) hash. You can’t confirm the integration based on a single value because statuses can vary depending on the application and related policies.

- [charges_enabled](https://docs.stripe.com/api/accounts/object.md#account_object-charges_enabled) confirms that your full charge path including the charge and transfer works correctly and evaluates if either `card_payments` or `transfers` capabilities are active.
- [payouts_enabled](https://docs.stripe.com/api/accounts/object.md#account_object-payouts_enabled) evaluates whether your connected account can pay out to an external account. Depending on your risk policies, you can allow your connected account to start transacting without payouts enabled. You [must eventually enable payouts](https://docs.stripe.com/connect/manage-payout-schedule.md) to pay your connected accounts.

You can use the following logic as a starting point for defining a summary status to display to your connected account.

#### Ruby

```ruby

# Set your secret key. Remember to switch to your live secret key in production.
# See your keys here: https://dashboard.stripe.com/apikeys
Stripe.api_key = '<<YOUR_SECRET_KEY>>'

def account_state(account)
  reqs = account.requirements

  if reqs.disabled_reason && reqs.disabled_reason.include?("rejected")
    "rejected"
  elsif account.payouts_enabled && account.charges_enabled
    if reqs.pending_verification
      "pending enablement"
    elsif !reqs.disabled_reason && !reqs.currently_due
      if !reqs.eventually_due
        "complete"
      else
        "enabled"
      end
    else
      "restricted"
    end
  elsif !account.payouts_enabled && account.charges_enabled
    "restricted (payouts disabled)"
  elsif !account.charges_enabled && account.payouts_enabled
    "restricted (charges disabled)"
  elsif reqs.past_due
    "restricted (past due)"
  elsif reqs.pending_verification
    "pending (disabled)"
  else
    "restricted"
  end
end

accounts = Stripe::Account.list(limit: 10)

accounts.each do |account|
    puts "#{account.id} has state: #{account_state(account)}"
end
```

#### Python

```python

# Set your secret key. Remember to switch to your live secret key in production.
# See your keys here: https://dashboard.stripe.com/apikeys
stripe.api_key = '<<YOUR_SECRET_KEY>>'

def account_state(account):
    reqs = account.requirements

    if reqs.disabled_reason and "rejected" in reqs.disabled_reason:
        return "rejected"

    if account.payouts_enabled and account.charges_enabled:
        if reqs.pending_verification:
            return "pending enablement"

        if not reqs.disabled_reason and not reqs.currently_due:
            if not reqs.eventually_due:
                return "complete"
            else:
                return "enabled"
        else:
            return "restricted"

    if not account.payouts_enabled and account.charges_enabled:
        return "restricted (payouts disabled)"

    if not account.charges_enabled and account.payouts_enabled:
        return "restricted (charges disabled)"

    if reqs.past_due:
        return "restricted (past due)"

    if reqs.pending_verification:
        return "pending (disabled)"

    return "restricted"

accounts = stripe.Account.list(limit=10)
for account in accounts:
    print("{} has state: {}".format(account.id, account_state(account)))
```

#### Node.js

```javascript

// Set your secret key. Remember to switch to your live secret key in production.
// See your keys here: https://dashboard.stripe.com/apikeys
const stripe = require('stripe')('<<YOUR_SECRET_KEY>>');

const accountState = (account) => {
  const reqs = account.requirements;

  if (reqs.disabled_reason && reqs.disabled_reason.indexOf("rejected") > -1) {
    return "rejected";
  }

  if (account.payouts_enabled && account.charges_enabled) {
    if (reqs.pending_verification) {
      return "pending enablement";
    }

    if (!reqs.disabled_reason && !reqs.currently_due) {
      if (!reqs.eventually_due) {
        return "complete";
      } else {
        return "enabled";
      }
    } else {
      return "restricted";
    }
  }

  if (!account.payouts_enabled && account.charges_enabled) {
    return "restricted (payouts disabled)";
  }

  if (!account.charges_enabled && account.payouts_enabled) {
    return "restricted (charges disabled)";
  }

  if (reqs.past_due) {
    return "restricted (past due)";
  }

  if (reqs.pending_verification) {
    return "pending (disabled)";
  }

  return "restricted";
};

const main = async () => {
  const accounts = await stripe.accounts.list({ limit: 10 });

  accounts.data.forEach((account) => {
    console.log(`${account.id} has state: ${accountState(account)}`);
  });
};

main();
```

> You can’t use the API to respond to Stripe risk reviews. You can enable your connected accounts to respond using embedded components, Stripe-hosted onboarding, or remediation links. You can also use the Dashboard to respond to risk reviews on behalf of your connected accounts.

Listen to the [account.updated](https://docs.stripe.com/api/events/types.md#event_types-account.updated) event. If the account contains any `currently_due` fields when the `current_deadline` arrives, the corresponding functionality is disabled and those fields are added to `past_due`.

[Create a form](https://docs.stripe.com/connect/api-onboarding.md#create-forms-to-collect-information) with clear instructions that the account can use to correct the information. Notify the account, then [submit the corrected information](https://docs.stripe.com/connect/api-onboarding.md#update-the-connected-account) using the Accounts API.
 (See full diagram at https://docs.stripe.com/connect/api-onboarding)
If you plan to create custom flows to handle all your verification errors:

- Review the details regarding all possible [verification errors and how to handle them](https://docs.stripe.com/connect/handling-api-verification.md).
- [Test verification states](https://docs.stripe.com/connect/testing-verification.md).


# Identity verification for connected accounts

Use identity verification to reduce risk on your platform when using Connect.

Every country has its own requirements that accounts must meet so that Stripe can pay out funds to individuals and companies. These are typically known as [Know Your Customer](https://support.stripe.com/questions/know-your-customer-obligations) (KYC) requirements. Regardless of the country, broadly speaking, the requirements Stripe must meet are:

- Collecting information about the individual or company receiving funds
- Verifying information to establish that we know who our customers are

*Connect* (Connect is Stripe's solution for multi-party businesses, such as marketplace or software platforms, to route payments between sellers, customers, and other recipients) platforms collect the required information from users and provide it to Stripe. This can include information about the legal entity and personal information about the representative of the business, and those who own or control the business. Stripe then attempts verification. In some cases, Stripe might be able to verify an account by confirming some or all of the keyed-in data provided. In other cases, Stripe might require additional information, including, for example, a scan of a valid government-issued ID, a proof of address document, or both.

This page explains the verification flow options to meet Stripe KYC requirements, but the recommended way to manage verification is to integrate [Connect Onboarding](https://docs.stripe.com/connect/custom/hosted-onboarding.md), which lets Stripe take care of the complexity around the basic KYC obligations. Handling the details of account verification is initially complex and requires vigilance to keep up with the constantly evolving regulatory changes around the world.

If you decide to handle account verification yourself, continue reading to learn about the verification flow options, how API fields translate to companies and individuals, and how to localize information requests. Also, read [Handling Identity Verification with the API](https://docs.stripe.com/connect/handling-api-verification.md) to learn how to programmatically provide information and handle requests.

Even after Stripe verifies a connected account, platforms still must [monitor for and prevent fraud](https://docs.stripe.com/connect/risk-management/best-practices.md#fraud). Don’t rely on Stripe’s verification to meet any independent legal KYC or verification requirements.

## Verification requirements 

Verification requirements for connected accounts vary by account, depending on:

- Country
- Capabilities
- Business type (for example, individual, company)
- Business structure (for example, public corporation, private partnership)
- The service agreement type between Stripe and the connected account
- The risk level

You must collect and verify specific information to enable charges and *payouts* (A payout is the transfer of funds to an external account, usually a bank account, in the form of a deposit). For example, for a company in the US, you might need to collect:

- Information about the business (for example, name, address, tax ID number).
- Information about the person opening the Stripe account (for example, name, date of birth).
- Information about [beneficial owners](https://support.stripe.com/questions/beneficial-owner-and-director-definitions) (for example, name, email).

At certain variable thresholds—usually when a specified amount of time has passed or volume of charges have been made—you might need to collect and verify additional information. Stripe temporarily pauses charges or payouts if the information isn’t provided or verified according to the thresholds for [required information](https://docs.stripe.com/connect/required-verification-information.md). For example, additional information might include verification of the company tax ID number.

## Onboarding flows 

As the platform, you must decide if you want to collect the required information from your connected accounts *up front* (Upfront onboarding is a type of onboarding where you collect all required verification information from your users at sign-up) or *incrementally* (Incremental onboarding is a type of onboarding where you gradually collect required verification information from your users. You collect a minimum amount of information at sign-up, and you collect more information as the connected account earns more revenue). Up-front onboarding collects the `eventually_due` requirements for the account, while incremental onboarding only collects the `currently_due` requirements.

| Onboarding type | Advantages                                                                                                                                                                                                               |
| --------------- | ------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------ |
| **Up-front**    | - Normally requires only one request for all information
  - Avoids the possibility of payout and processing issues due to missed deadlines
  - Exposes potential risk early when accounts refuse to provide information |
| **Incremental** | - Accounts can onboard quickly because they don’t have to provide as much information                                                                                                                                    |

To determine whether to use up-front or incremental onboarding, review the [requirements](https://docs.stripe.com/connect/required-verification-information.md) for your connected accounts’ locations and capabilities. While Stripe tries to minimize any impact to connected accounts, requirements might change over time.

For connected accounts where you’re responsible for requirement collection, you can customize the behavior of [future requirements](https://docs.stripe.com/connect/handle-verification-updates.md) using the `collection_options` parameter. To collect the account’s future requirements, set [`collection_options.future_requirements`](https://docs.stripe.com/api/account_links/create.md#create_account_link-collection_options-future_requirements) to `include`.

## Business type 

The specific KYC information depends on the [type of business entity](https://docs.stripe.com/api/accounts/object.md#account_object-business_type). They’re:

- `individual`: Collect information about the person.
- `company`: Collect information about the company. Depending on the countries your connected accounts are in, you might also have to collect information about [beneficial owners](https://support.stripe.com/questions/beneficial-owner-and-director-definitions).
- `non_profit`: Collect information about the non-profit organization.
- `government_entity` (available for US connected accounts only): Collect information about the government entity.

If you or your users are unsure of their entity type, the information might be in the business formation documents or tax documents for that entity.

See the [list of requirements](https://docs.stripe.com/connect/required-verification-information.md) for different business types by country. When you know what information to collect, you can read more about [handling identity verification with the API](https://docs.stripe.com/connect/handling-api-verification.md).

## Business structure 

For all business types other than `individual`, you can further classify your user’s business by identifying its legal (business) structure. A business structure describes the details of a business entity such as day-to-day operations, tax burdens, liability, and organizational schema. You can classify it by using [company[structure]](https://docs.stripe.com/api/accounts/create.md#create_account-company-structure) in the `Accounts` object.

Providing this information to Stripe gets you the most accurate business classification for compliance purposes. While it isn’t required, it can reduce onboarding requirements. For example, you’re required to provide owner information for private companies, but not for public companies. If you don’t provide information on the `structure`, Stripe defaults to classifying the company as private and requires you to provide owner information. See the [list of requirements](https://docs.stripe.com/connect/required-verification-information.md) for the supported business structures in all countries.

#### Country - Canada (CA)

### Companies

See the table below for descriptions of the different business structures that you can use to classify a for-profit `company`.

| Business structure    | Description                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                       |
| --------------------- | ------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| `private_corporation` | A corporation in Canada that isn’t a public corporation and isn’t controlled by one or more public corporations.                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                  |
| `private_partnership` | An association or relationship between two or more individuals, corporations, trusts, or partnerships that join together to carry on a trade or business.                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                         |
| `sole_proprietorship` | An unincorporated business owned by one individual.                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                               |
| `public_corporation`  | A corporation in Canada that meets either of the following requirements at the end of the tax year:

  - It has a class of shares listed on a designated Canadian stock exchange.
  - It has elected, or the minister of National Revenue has designated it, to be a public corporation, and it has complied with prescribed conditions under Regulation 4800(1) of the [Income Tax Regulations](https://laws-lois.justice.gc.ca/eng/regulations/C.R.C.%2C_c._945/) on its number of shareholders, the dispersing of the ownership of its shares, the public trading of its shares, and its size.

  This business structure is restricted; please contact [Stripe support](https://support.stripe.com/contact) for information on how to use it. |

### Non-profits

See the table below for descriptions of the different business structures that you can use to classify a `non_profit`.

| Business structure   | Description                                                                                                                                                                                                |
| -------------------- | ---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| `registered_charity` | A charitable organization, public foundation, or private foundation registered with the Canada Revenue Agency.                                                                                             |
| `nil`                | A non-profit association, club, or society that isn’t a charity and is organized and operated exclusively for social welfare, civic improvement, pleasure, recreation, or any other purpose except profit. |

### Government entities

See the table below for a description of the business structure that you can use to classify a `government_entity`.

| Business structure | Description                                                                                                                                                                                                                                                                                                                                                    |
| ------------------ | -------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| `nil`              | A Canadian government entity includes any of the current Government of Canada departments, agencies, crown corporations or special operating agencies [listed](https://www.canada.ca/en/government/dept.html).

  This is a restricted business structure, please [contact support](https://support.stripe.com/contact) for more information on how to opt-in. |

#### Country - Germany (DE)

### Companies

See the table below for descriptions of the different business structures that you can use to classify a `company`.

| Business structure           | Description                                                                                                                                                                                                                                                                                                                    |
| ---------------------------- | ------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------ |
| `private_corporation`        | A business incorporated as a legal entity under the laws of Germany that’s privately owned. It doesn’t have shares that can be traded on a public stock exchange and it’s registered with the commercial register (Handelsregister). For example, limited liability company (GmbH) or entrepreneur company (UG).               |
| `public_corporation`         | A business incorporated as a legal entity under the laws of Germany. Ownership shares of this corporation can be traded on a public stock exchange and it’s registered with the commercial register (Handelsregister). For example, a joint-stock company (AG).                                                                |
| `incorporated_partnership`   | A business jointly owned by two or more people that’s created through a partnership agreement on a commercial scale and it’s registered with the commercial register (Handelsregister). Eg. General partnership (OHG), Limited partnership (KG), Limited Liability, Company & Compagnie Kommanditgesellschaft (GmbH & Co, KG). |
| `unincorporated_partnership` | A business jointly owned by two or more people that’s created through a partnership agreement and it’s not registered with a public register. For example, society under civil law (GbR) or partnership company (PartG).                                                                                                       |

### Non-profits

See the table below for descriptions of the different business structures that you can use to classify a `non_profit`.

| Business structure          | Description                                                                                                                                                                                                                                                 |
| --------------------------- | ----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| `incorporated_non_profit`   | An organization incorporated under the laws of Germany that is recognized as a non-profit organization by its competent tax office and it’s registered with a public register. For example, registered association (e.V.) or registered cooperative (e.G.). |
| `unincorporated_non_profit` | An organization that is recognized as a non-profit organization by its competent tax office and it’s not registered with a public register. For example, non-registered association (n.e.V.).                                                               |

#### Country - Singapore (SG)

### Companies

See the table below for descriptions of the different business structures that you can use to classify a `company`.

| Business structure    | Description                                                                                                                                                                                                                                                                                                                                  |
| --------------------- | -------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| `sole_proprietorship` | An unincorporated business owned by one individual. The business is registered as a Sole Proprietorship on [ACRA](https://www.acra.gov.sg/how-to-guides/before-you-start/choosing-a-business-structure).                                                                                                                                     |
| `private_partnership` | An association or relationship between two or more individuals, corporations, trusts, or partnerships that join together to carry on a trade or business. The business is registered as a Partnership on [ACRA](https://www.acra.gov.sg/how-to-guides/before-you-start/choosing-a-business-structure).                                       |
| `private_company`     | An association or relationship between two or more individuals, corporations, trusts, or partnerships that join together to carry on a trade or business. The business is registered as a Company on [ACRA](https://www.acra.gov.sg/how-to-guides/before-you-start/choosing-a-business-structure), including companies limited by guarantee. |
| `public_company`      | A company that offers its securities for sale to the general public. This business structure is restricted—contact [Stripe Support](https://support.stripe.com/) for information on how to use it. The business is registered as a Company on [ACRA](https://www.acra.gov.sg/how-to-guides/before-you-start/choosing-a-business-structure).  |

### Non-profits

See the table below for descriptions of the different business structures that you can use to classify a `non_profit`.

| Business structure | Description                                                                                                                                                                                                |
| ------------------ | ---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| `nil`              | A non-profit association, club, or society that isn’t a charity and is organized and operated exclusively for social welfare, civic improvement, pleasure, recreation, or any other purpose except profit. |

### Government entities

See the table below for a description of the business structure that you can use to classify a `government_entity`.

| Business structure | Description                                                                                                                                                                                                                                                                                                                                                                                                                                                             |
| ------------------ | ----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| `nil`              | A [government entity](https://support.stripe.com/questions/2025-updates-to-singapore-verification-requirements-government-entities) or organization who have at least 75% of their business owned by a [government entity](https://support.stripe.com/questions/2025-updates-to-singapore-verification-requirements-government-entities). This business structure is restricted—contact [Stripe Support](https://support.stripe.com/) for information on how to use it. |

#### Country - Thailand (TH)

### Companies

See the table below for descriptions of the different business structures that you can use to classify a `company`.

| Business structure           | Description                                                                                                                                                                                                                                                     |
| ---------------------------- | --------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| `private_corporation`        | These are incorporated businesses where the legal entity and its legal personality is separated and distinct from the shareholders. All private companies must be registered with the Ministry of Commerce.                                                     |
| `incorporated_partnership`   | Also called ‘Limited Partnerships’ or ‘Registered Ordinary Partnerships’, these are businesses registered in Thailand owned by two or more people. The business’ legal entity and its legal personality is separated and distinct from the individual partners. |
| `unincorporated_partnership` | Also called ‘Unregistered Ordinary Partnerships’, these are businesses in Thailand owned by two or more people. The business’ legal entity and its legal personality isn’t separated and distinct from the individual partners.                                 |
| `sole_proprietorship`        | These are businesses owned by a single individual. There is no specific concept of sole proprietorship in Thailand.                                                                                                                                             |

#### Country - United Arab Emirates (AE)

### Companies

See the table below for descriptions of the different business structures that you can use to classify a `company`.

| Business structure        | Description                                                                                                                            |
| ------------------------- | -------------------------------------------------------------------------------------------------------------------------------------- |
| `llc`                     | A business registered in the UAE (outside of any free zone) as a limited liability company with any number of members or shareholders. |
| `sole_establishment`      | A business that isn’t a separate legal entity from its individual owner.                                                               |
| `free_zone_llc`           | A business registered in the UAE (within any free zone) as a limited liability company with any number of shareholders.                |
| `free_zone_establishment` | A business registered in the UAE (within certain free zones) as a limited liability company with only one shareholder.                 |

#### Country - United Kingdom (GB)

### Companies

The following table describes the business structures that you can use to classify a for-profit `company`.

| Business structure           | Description                                                                                  |
| ---------------------------- | -------------------------------------------------------------------------------------------- |
| `private_corporation`        | A private, limited liability company incorporated in the United Kingdom.                     |
| `incorporated_partnership`   | An incorporated limited liability partnership (LLP) between at least two designated members. |
| `unincorporated_partnership` | An unincorporated partnership between two or more partners.                                  |

### Non-profits

The following table describes the business structures that you can use to classify a `non_profit`.

| Business structure          | Description                                                                                                                                                                             |
| --------------------------- | --------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| `incorporated_non_profit`   | An incorporated non-profit organisation in the United Kingdom. This includes charitable companies limited by guarantee.                                                                 |
| `unincorporated_non_profit` | An unincorporated non-profit organisation in the United Kingdom. This includes non-profit organisations and charities operating under an unincorporated association or trust structure. |

#### Country - United States (US)

### Companies

See the table below for descriptions of the different business structures that you can use to classify a `company`. Refer to the [US required verification information](https://docs.stripe.com/connect/required-verification-information.md#minimum-verification-requirements-for-united-states) section for more details on requirements.

If you or your users think the entity type should be `company` but are unsure, the information might be in the business formation documents or tax documents for that entity.

| Business structure           | Description                                                                                                                                                                                                                                                                                            |
| ---------------------------- | ------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------ |
| `multi_member_llc`           | A business with multiple owners or members that’s registered in a US state as a Limited Liability Company (LLC).                                                                                                                                                                                       |
| `private_corporation`        | A business incorporated in a US state that’s privately owned. It doesn’t have shares that are traded on a public stock exchange. It’s also called a closely-held corporation. If you’re a single-member LLC that has elected to be treated as a corporation for tax purposes, use this classification. |
| `private_partnership`        | A business jointly owned by two or more people that’s created through a partnership agreement.                                                                                                                                                                                                         |
| `public_corporation`         | A business incorporated under the laws of a US state. Ownership shares of this corporation are traded on a public stock exchange.                                                                                                                                                                      |
| `public_partnership`         | A business formed by a partnership agreement with one or more people, but has shares that are publicly traded on a stock exchange.                                                                                                                                                                     |
| `single_member_llc`          | A business entity registered with a US state as a limited liability company (LLC) and that has only one member or owner.                                                                                                                                                                               |
| `sole_proprietorship`        | A business that isn’t a separate legal entity from its individual owner.                                                                                                                                                                                                                               |
| `unincorporated_association` | A business venture of two or more people that doesn’t have a formal corporate or entity structure.                                                                                                                                                                                                     |

### Non-profits

See the table below for descriptions of the different business structures that you can use to classify a `non_profit` with. Refer to the [US required verification information](https://docs.stripe.com/connect/required-verification-information.md#minimum-verification-requirements-for-united-states) section for more details on requirements.

| Business structure          | Description                                                                                                                                                                                                                        |
| --------------------------- | ---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| `incorporated_non_profit`   | An organization incorporated under the laws of a US state that has obtained tax-exempt status as a non-profit entity under either state or federal law (for example, 501(c)(3)).                                                   |
| `unincorporated_non_profit` | An organization that’s pursuing an objective other than profits, such as a social cause, and has obtained tax-exempt status in the US under either state or federal law (for example, 501(c)(3)) but hasn’t formally incorporated. |

### Government entities

See the table below for descriptions of the different business structures that you can use to classify a `government_entity` with. Refer to the [US required verification information](https://docs.stripe.com/connect/required-verification-information.md#minimum-verification-requirements-for-united-states) section for more details on requirements.

| Business structure                      | Description                                                                                                                                                                                   |
| --------------------------------------- | --------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| `government_instrumentality`            | An organization formed by a government statute or body based in the US to perform a certain function, but not the actual government body itself.                                              |
| `governmental_unit`                     | A branch of the state, local, or federal government of the US                                                                                                                                 |
| `tax_exempt_government_instrumentality` | An organization created by or pursuant to government statute and operated for public purposes. It has obtained federal tax-exempt status under state or federal law (for example, 501(c)(3)). |

## Internationalization and localization

If you support users in multiple countries, consider internationalization and localization when asking for information. Creating an interface that uses not only the user’s preferred language but also the proper localized terminology results in a smoother onboarding experience.

For example, instead of requesting a business tax ID from your users, regardless of country, request:

- EIN, US
- Business Number, Canada
- Company Number, UK

You can find recommended country-specific labels along with the other [required verification information](https://docs.stripe.com/connect/required-verification-information.md).

## See also

- [Stripe-hosted onboarding](https://docs.stripe.com/connect/hosted-onboarding.md)
- [Updating Accounts](https://docs.stripe.com/connect/updating-service-agreements.md)
- [Handling additional verifications with the API](https://docs.stripe.com/connect/handling-api-verification.md)
- [Account tokens](https://docs.stripe.com/connect/account-tokens.md)
- [Testing verification](https://docs.stripe.com/connect/testing-verification.md)