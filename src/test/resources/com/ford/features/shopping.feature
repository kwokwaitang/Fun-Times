Feature: Shopping at Henry’s Grocery store

  # A background specifies a set of steps common to every scenario
  Background:
    Given the following available stock items
      | product | unit   | cost |
      | soup    | tin    | 0.65 |
      | bread   | loaf   | 0.80 |
      | milk    | bottle | 1.30 |
      | apple   | single | 0.10 |
    And the following discounts
      | the offer                                             | valid from        | valid to                             |
      | buy 2 tins of soup and get a loaf of bread half price | yesterday         | for 7 days                           |
      | apples have a 10% discount                            | from 3 days hence | until the end of the following month |

  # A scenario outline is a parameterised scenario
  Scenario Outline: To price up a shopping basket with a number of stock items and applying any discounts
    Given an empty shopping basket
    When adding <stock-items> to the shopping basket
    And is to be bought <date-of-purchase>
    And apply any discounts
    Then the cost of the shopping is expected to be <total-cost>

    Examples: Shopping to be bought today
      | stock-items                         | date-of-purchase | total-cost |
      | "3 tins of soup, 2 loaves of bread" | "today"          | 3.15       |
      | "6 apples,1 bottle of milk"         | "today"          | 1.90       |

    Examples: Shopping to be bought in advance by a specified number of days
      | stock-items                                 | date-of-purchase | total-cost |
      | "6 apples, 1 bottle of milk"                | "in 5 days time" | 1.84       |
      | "3 apples, 2 tins of soup, 1 loaf of bread" | "in 5 days time" | 1.97       |