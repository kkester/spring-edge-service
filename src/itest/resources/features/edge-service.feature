Feature: the client can route requests through the edge service
  Scenario: client makes a call to GET available products
    Given a typical "public" Client Key
    When the client calls /products
    Then the client receives status code of 200
