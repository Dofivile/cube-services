# Working with Webhooks

> Implement simple, real-time, event notifications regarding account and transaction status, and more.

## Overview

A webhook is a means of notifying your application of the occurrence of an event with some relevant information. [Events](/docs/api-reference/events) are created each time a resource is created or updated in Dwolla. For example, when a [Customer is created](/docs/api-reference/customers/create-a-customer) or a [Funding Source is removed](/docs/api-reference/funding-sources/update-or-remove-a-funding-source), a `customer_created` and `customer_funding_source_removed` event will be created, respectively. These events are what trigger HTTP webhook requests to your subscribed URL if you have an active webhook subscription. It is important to note that a single API request can trigger multiple webhooks to be fired, e.g. [initiating a transfer](/docs/api-reference/transfers/initiate-a-transfer) from an Account to Customer can create the events `transfer_created` and `customer_transfer_created`.

<Card title="API Reference" icon="book-open" href="/docs/api-reference/events">
  Check out our API reference for an exhaustive list of all possible events.
</Card>

## Webhook Event

Each webhook contains an [Event](/docs/api-reference/events) with `_links` to the following resources:

* The unique event itself
* The [Dwolla Account](/docs/api-reference/accounts) associated with the event
* The Resource associated with the event
* The [Customer](/docs/api-reference/customers) that the resource relates to (if applicable)

Example webhook payload:

```json
{
  "id": "80d8ff7d-7e5a-4975-ade8-9e97306d6c15",
  "resourceId": "36E9DCB2-889B-4873-8E52-0C9404EA002A",
  "topic": "customer_created",
  "timestamp": "2015-10-22T14:44:11.407Z",
  "_links": {
    "self": {
      "href": "https://api-sandbox.dwolla.com/events/80d8ff7d-7e5a-4975-ade8-9e97306d6c15"
    },
    "account": {
      "href": "https://api-sandbox.dwolla.com/accounts/b4cdac07-eeca-4059-a29c-48900e453d54"
    },
    "resource": {
      "href": "https://api-sandbox.dwolla.com/customers/36E9DCB2-889B-4873-8E52-0C9404EA002A"
    },
    "customer": {
      "href": "https://api-sandbox.dwolla.com/customers/36E9DCB2-889B-4873-8E52-0C9404EA002A"
    }
  }
}
```

<Card title="Webhook Events" icon="bell" href="/docs/webhook-events">
  For detailed information on Dwolla's webhook request structure, refer to the Webhook Events resource.
</Card>

## What to Know About Dwolla Webhooks

* Each application can have multiple webhook subscriptions associated with it. While one subscription is sufficient, you can create up to **five** in Production and **ten** in <Tooltip tip="A testing environment that mimics the production environment but uses test data instead of real money">Sandbox</Tooltip> for redundancy.
* Webhooks are sent asynchronously and are not guaranteed to be delivered in order. We recommend that applications protect against duplicated events by [making event processing idempotent](/docs/working-with-webhooks#check-for-duplicate-events).
* Your application will need to respond to Dwolla webhook requests with a 200-level HTTP status code within 10 seconds of receipt. Otherwise, the attempt will be counted as a failure and Dwolla will retry sending the webhook according to the [back-off schedule](/docs/api-reference/webhook-subscriptions).
* If there are 400 consecutive failures, and it has been 24 hours since your last success, your webhook subscription will be automatically paused and an email will be sent to the Admin of the Dwolla account. After fixing the issue that is causing the failures, you can unpause the webhook subscription either via your Dashboard or [Dwolla's API](/docs/api-reference/webhook-subscriptions/update-a-webhook-subscription) in order to continue receiving new webhooks and to [retry failed webhooks](/docs/api-reference/webhooks/retry-a-webhook).

## Getting Started

In this guide we will walk through creating a webhook subscription along with validating and processing webhook requests.

You will need to have a [Sandbox account](https://accounts-sandbox.dwolla.com/sign-up) already set up. Although not required, this guide assumes that you have some familiarity with Amazon Web Services (AWS); specifically [Lambda](https://aws.amazon.com/lambda/) and [SQS](https://aws.amazon.com/sqs/), or other similar services.

# Step 1 - Create a Webhook Subscription

First, you will need to have a URL that is publicly accessible where Dwolla can send webhooks in the form of HTTP requests. This also means that anyone on the Internet can hit your endpoint. As such, here are some security concerns:

* Your webhook endpoint should only be accessible over [TLS (HTTPS)](https://www.dwolla.com/updates/improving-transport-layer-security/) and your server should have a valid SSL certificate.
* Your subscription should include a random, secret key, only known by your application. This secret key should be securely stored and used later when [validating the authenticity of the webhook request](#authentication) from Dwolla.

#### Request Parameters

| Parameters | Required | Type   | Description                                                                                                                                                                                    |
| ---------- | -------- | ------ | ---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| url        | yes      | string | The publicly-accessible URL where Dwolla should deliver the webhook notification.                                                                                                              |
| secret     | yes      | string | A random, secret key, only known by your application. This secret key should be securely stored and used later when [validating the authenticity of the webhook](#authentication) from Dwolla. |

##### Request and Response

```raw HTTP
POST https://api-sandbox.dwolla.com/webhook-subscriptions
Accept: application/vnd.dwolla.v1.hal+json
Content-Type: application/vnd.dwolla.v1.hal+json
Authorization: Bearer 0Sn0W6kzNicvoWhDbQcVSKLRUpGjIdlPSEYyrHqrDDoRnQwE7Q
{
    "url": "https://myapplication.com/webhooks",
    "secret": "sshhhhhh"
}


...

HTTP/1.1 201 Created
Location:
https://api-sandbox.dwolla.com/webhook-subscriptions/077dfffb-4852-412f-96b6-0fe668066589
```

When the webhook subscription is created, you will receive a `201 Created` HTTP response with an empty response body. You can refer to the `Location` header to retrieve a link to the newly-created subscription.

## Webhook Subscription Resource

| Parameter | Description                                                                                                                                                                                                                                                                                                                                 |
| --------- | ------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| id        | Unique webhook subscription identifier assigned by Dwolla.                                                                                                                                                                                                                                                                                  |
| url       | Subscribed URL where Dwolla will deliver webhook notifications.                                                                                                                                                                                                                                                                             |
| paused    | A boolean `true` or `false` value indicating if a webhook subscription is paused. A webhook subscription will be automatically paused after 400 consecutive failures. In addition, a subscription can be paused or unpaused by calling [this endpoint](/docs/api-reference/webhook-subscriptions/update-a-webhook-subscription) in our API. |
| created   | [ISO-8601](https://en.wikipedia.org/wiki/ISO_8601) timestamp this webhook subscription was created                                                                                                                                                                                                                                          |

### Request and Response

```raw
GET https://api-sandbox.dwolla.com/webhook-subscriptions/077dfffb-4852-412f-96b6-0fe668066589
Accept: application/vnd.dwolla.v1.hal+json
Authorization: Bearer pBA9fVDBEyYZCEsLf/wKehyh1RTpzjUj5KzIRfDi0wKTii7DqY

...

{
  "_links": {
    "self": {
      "href": "https://api-sandbox.dwolla.com/webhook-subscriptions/077dfffb-4852-412f-96b6-0fe668066589"
    },
    "webhooks": {
      "href": "https://api-sandbox.dwolla.com/webhook-subscriptions/077dfffb-4852-412f-96b6-0fe668066589/webhooks"
    }
  },
  "id": "077dfffb-4852-412f-96b6-0fe668066589",
  "url": "https://myapplication.com/webhooks",
  "created": "2022-01-20T16:20:47+00:00"
}
```

# Step 2 - Processing and Validating Webhooks

Before we begin, although not required, please note that many of the snippets we use include Amazon Web Services (AWS) as a dependency — specifically, [Lambda](https://aws.amazon.com/lambda/) and [SQS](https://aws.amazon.com/sqs/). We will use AWS' [v2 Node SDK](https://www.npmjs.com/package/aws-sdk); however, [v3 is also currently available](https://github.com/aws/aws-sdk-js-v3).

Now that you have created a webhook subscription in the previous step, we will work on creating a webhook listener/handler. In order to asynchronously process webhooks, we recommend implementing the four following steps: [**ingestion**](#ingestion), [**authentication**](#authentication), [**queueing**](#queueing) and [**processing**](#processing).

## Ingestion

### Receive the Webhook

At its core, a webhook listener is an HTTP endpoint that Dwolla will call. As such, your endpoint must:

* Be publicly accessible (Dwolla cannot send requests to `localhost`)
* Be able to receive `POST` HTTP requests (Dwolla will not send `GET`, `PUT`, `PATCH`, etc.)
* Have TLS enabled (with a valid SSL certificate issued to your domain)
* Be able to handle at least 10 concurrency requests, unless a lower value has otherwise been configured for your application by Dwolla

### Return HTTP 2xx Status Code

Once a webhook has fired to your endpoint (and you have received it), in order to fully "ingest" the webhook, you must respond to Dwolla with a 200-level status code (e.g., `200 OK`). Additionally, Dwolla must receive your response within 10,000 milliseconds (or, in other words, 10 seconds). If a response is not received in the allotted time, Dwolla will retry the request up to 8 times over the next 72 hours, according to our [back-off schedule](/docs/api-reference/webhook-subscriptions). Finally, if your application fails to response after 400 consecutive attempts and 24 hours has passed since the last success, [your webhook subscription will automatically pause](/docs/api-reference/webhook-subscriptions).

## Authentication

When you [set up your webhook subscription](#step-1-create-a-webhook-subscription), you sent over a `secret` value to Dwolla. This value is a shared secret that only your application and Dwolla should have access to. Before Dwolla sends a webhook, the JSON-encoded payload is used in conjunction with the shared `secret` to create the `X-Request-Signature-SHA-256` header value, which is sent with the webhook request.

When a webhook request is received, Dwolla recommends creating a SHA-256 HMAC signature of the JSON-encoded payload that your application receives with the shared `secret` as the key, and checking its value against the signature that Dwolla generated and supplied in the `X-Request-Signature-SHA-256` header.

```javascript
const crypto = require("crypto");

const isSignatureValue = (body, signature) =>
  signature ===
  crypto
    .createHmac("sha256", process.env.DWOLLA_WEBHOOK_SECRET)
    .update(body)
    .digest("hex");
```

### Considerations and Limitations

When implementing webhook authentication in your application, please consider the following:

* Dwolla uses a highly dynamic and wide range of IP addresses, meaning that your application cannot use IP whitelisting to authenticate webhook requests.
* The request body is already JSON-encoded prior to being sent. As such, the JSON body should never be re-encoded! If it is, the signatures will not match and authentication will fail, even if the request came from Dwolla.
* Your webhook endpoint must have access to the request body in order to authenticate the request. This means that some services, such as AWS Lambda Authorizers, are unable to authenticate a webhook Dwolla.

## Queueing

Once you ingest the webhook and authenticate that it is from Dwolla, instead of handling the business logic within the listener, we recommend passing the webhook off to a queue to be picked up by another Lambda function. By doing this, your webhook listener takes on the minimum amount of responsibility necessary, enabling your application to scale quickly and easily based on the volume of concurrent requests.

Which messaging broker you use is up to you; however, we recommend using [AWS SQS](https://aws.amazon.com/sqs/) or [GCP Pub/Sub](https://cloud.google.com/pubsub) if your application resides in a serverless environment. If your application is self-hosted, [Redis](https://redis.io/) is also a good alternative to the options listed above.

As an example, the following snippet will demonstrate how a webhook could be placed on an AWS SQS queue. In it `QueueUrl` would be the URL of your SQS queue, whereas `MessageBody` would be the "stringified" body of the webhook that Dwolla sends. (For more information on sending a message using AWS SQS, we recommend checking out their [official documentation](https://docs.aws.amazon.com/sdk-for-javascript/v2/developer-guide/sqs-examples-send-receive-messages.html#sqs-examples-send-receive-messages-sending).)

```javascript
const { SQS } = require("aws-sdk");
const sqs = new SQS();

try {
  await sqs
    .sendMessage({
      QueueUrl: "AWS_SQS_URL",
      MessageBody: JSON.stringify("WEBHOOK_JSON_BODY"),
    })
    .promise();
} catch (e) {
  console.error("Failed to place webhook in SQS queue", e);
}
```

## Processing

Now that the webhook has been ingested, authenticated, and queued, once the webhook has been fetched from the queue, we recommend checking for duplicate events and then handling any application-specific business logic.

### Check for Duplicate Events

Although Dwolla tries to only send a webhook once, it is possible that the same webhook may be sent more than once. Because of this, it is recommended to check an internal database against the webhook's [Event](/docs/api-reference/events) ID to ensure that the same business logic is not processed multiple times.

In other words, we recommend maintaining an internal database that keeps track of all of the events that are processed. Then, when a new webhook is received, your application ensures that the event (checked against its ID) has not already been processed previously. Additionally, once your application finishes processing an event, its ID is appended to your database, ensuring that if your application receives another webhook with the same event ID, it will not get processed a second time.

Finally, when checking if an event has already been processed, it's important to note that multiple events can fire for the same resource while still remaining unique. For example, when a transfer is created, there may be cases where you receive `customer_transfer_created` twice with a different event ID but the same resource ID. This is because when a transfer is created, an event is triggered for both the sender and the receiver. It is for this reason that we recommend checking webhooks against their event ID, not by their resource ID or topic.

<Info>
  When your application detects that you received an event that has already been
  processed, it is imperative that it returns with a 200-level status, such as{" "}
  <code>200 OK</code>. If it responds with another value like{" "}
  <code>409 Conflict</code>, this simply propagates irrelevant information and
  may result in your<a href="/docs/api-reference/webhook-subscriptions"> webhook getting automatically paused</a>.
</Info>

### Handle Business Logic

Once you reach this point, you're finally ready to handle any business logic related to the webhook — for example, creating or updating a database entry, sending a notification email or triggering an alert.

Although business logic will vary case by case based on your application's specific needs, in this final section, we will demonstrate how you can use an AWS Lambda function to pull the webhook off an SQS queue, and print it to the console.

```javascript
module.exports.queueHandler = async (event) => {
  try {
    event.Records.forEach((record) => {
      const webhook = JSON.parse(record.body);
      console.log(
        `Received ${webhook.topic}, body=${JSON.stringify(webhook, null, 2)}`
      );
    });
  } catch (e) {
    console.error(
      "An unexpected error occurred while processing the queue.",
      e
    );
  }
};
```

## Conclusion

We hope that this guide is helpful in getting your webhook listener/handler set up and configured for use with Dwolla! If you would like to take a closer look at the code that we used (or give it a try using your own account), check out our [webhook-receiver](https://github.com/Dwolla/webhook-receiver) repository on GitHub.

# Frequently Asked Questions

<Accordion title="What is an Event vs Webhook Subscription vs Webhook?">
  <ul>
    <li>
      <a href="/docs/api-reference/events">Event</a> — An event is a unique resource
      that gets created whenever an action occurs in Dwolla that changes the
      state of an API resource like a Customer being created or a funding source
      being verified.
    </li>

    <li>
      <a href="/docs/api-reference/webhook-subscriptions">Webhook Subscription</a> —
      A webhook subscription is a resource in the API that you can create in
      order to subscribe to Dwolla webhooks.
    </li>

    <li>
      <a href="/docs/api-reference/webhooks">Webhook</a> — A webhook is an HTTP
      request that Dwolla sends to your subscribed URL to notify your app of an
      event. In order to get webhook notifications, you will need to have an
      active webhook subscription.
    </li>
  </ul>
</Accordion>

<Accordion title="Is a webhook subscription required to integrate with the Dwolla API?">
  <p>
    While a webhook subscription is not required for you to integrate with the
    API, Dwolla requires all applications to have one in production for
    automated notifications of events to your application. Webhooks provide
    automatic near real-time status updates to your application versus polling
    the API which causes unnecessary load on your application and the API.
  </p>
</Accordion>

<Accordion title="How to validate that a webhook is coming from Dwolla? Is there a list of IP addresses that I can limit webhook requests from?">
  <p>
    Dwolla includes a <code>X-Request-Signature-SHA-256</code> header on each
    webhook request which is a SHA-256 HMAC hash of the request body with the
    key being the webhook <code>secret</code> you passed in when you created the
    webhook subscription. As a best practice, we recommend validating webhooks
    by generating the same SHA-256 HMAC hash and comparing it to the signature
    sent with the payload.

    <br />

    <br />

    We do not recommend nor support relying on IP whitelisting as a method of validating
    webhooks. Dwolla's IPs are dynamically allocated with no defined range and are
    subject to change. Refer to the Processing/Validating section for a more detailed
    guide.
  </p>
</Accordion>

<Accordion title="Can I subscribe to certain event topics?">
  <p>
    Dwolla sends webhooks for all events that occur in your platform and there
    isn't a way to filter what events you subscribe to.
  </p>
</Accordion>

<Accordion title="My webhook subscription was paused. Why?">
  <p>
    Dwolla automatically{" "}

    <a href="/docs/api-reference/webhook-subscriptions">
      pauses a webhook subscription
    </a>

    {" "}

    after 400 consecutive failed delivery attempts and sends an email to notify
    the Admin of the Dwolla account. While it's paused, Dwolla isn't able to
    send webhooks for new events to your URL. To resume webhooks, you need to{" "}

    <a href="/docs/api-reference/webhook-subscriptions">
      address the issue that's causing failures
    </a>

    , <a href="/docs/api-reference/webhook-subscriptions/update-a-webhook-subscription">
    unpause the subscription
    </a> and <a href="/docs/api-reference/webhooks/retry-a-webhook">
    retry missed webhooks
    </a>.
  </p>
</Accordion>

<Accordion title="Will webhooks be automatically retried if my subscription was previously paused or unavailable?">
  <p>
    No. If your webhook subscription was previously paused or unavailable,
    Dwolla will not attempt to re-deliver webhooks. In this case, you can <a href="/docs/api-reference/webhook-subscriptions/list-webhooks-for-a-webhook-subscription"> list all webhooks by subscription</a>, filtering all that have zero attempts (in other words, the `attempts` array is empty), and <a href="/docs/api-reference/webhooks/retry-a-webhook"> retrying each webhook individually
    </a>.
  </p>
</Accordion>

<Accordion title="How do I know if a webhook request failed and needs to be retried?">
  <p>
    When{" "}

    <a href="/docs/api-reference/webhook-subscriptions/list-webhooks-for-a-webhook-subscription">
      listing all webhooks by webhook subscription
    </a>

    , an embedded <code>attempts</code> array will include a <code>
    response
    </code> object that includes a <code>statusCode</code> property indicating the
    HTTP status code that Dwolla received when the webhook attempt was made. If the
    status code that Dwolla received was >=300, then the attempt was considered to
    have failed.
  </p>
</Accordion>

<Accordion title="Can I create more than one webhook subscription?">
  <p>
    Yes. You can create up to 5 webhook subscriptions in Production and 10 in
    Sandbox. While only one subscription is needed to be notified of all events,
    you can have multiple in case one or more of your URLs becomes unreachable.
  </p>
</Accordion>
