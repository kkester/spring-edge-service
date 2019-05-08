Feature: the client can route requests through the edge service
  Scenario: public client makes a call to GET available products leveraging service override
    Given a typical "public" Client Key
    When the client calls for "products" a 500 times concurrently using "jsf"
    Then the client receives status codes of 200 and 429

  Scenario: public client makes a call to GET store representation
    Given a typical "public" Client Key
    When the client calls for "store" a 500 times concurrently using "cache"
    Then the client receives status codes of 200 and 429

  Scenario: confidential client makes a call to GET available products
    Given a typical "confidential" Client Key
    When the client calls for "products" a 500 times concurrently using "jsf"
    Then the client receives status codes of 200 and 429

  Scenario: confidential client makes a call to GET store representation
    Given a typical "confidential" Client Key
    When the client calls for "store" a 500 times concurrently using "cache"
    Then the client receives status codes of 200 and 429