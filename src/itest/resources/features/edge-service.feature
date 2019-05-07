Feature: the client can route requests through the edge service
  Scenario: client makes a call to GET available products
    Given a typical "public" Client Key
    When the client calls for "products" a 100 times concurrently using "jsf"
    Then the client receives status codes of 200 and 429
