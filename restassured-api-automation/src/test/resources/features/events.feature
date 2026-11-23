Feature: Events

  Authenticated users can create events and browse the event catalogue.

  Scenario Outline: Create a new event
    Given I am authenticated
    When I create a new event with the following details
      | title       | <title>       |
      | description | <description> |
      | category    | <category>    |
      | venue       | <venue>       |
      | city        | <city>        |
      | price       | <price>       |
      | totalSeats  | <totalSeats>  |
      | imageUrl    | <imageUrl>    |
      | daysFromNow | <daysFromNow> |
    Then the event is created successfully

    Examples:
      | title            | description                      | category   | venue                           | city      | price | totalSeats | imageUrl                        | daysFromNow |
      | Tech Summit 2026  | A premier technology conference  | Conference | Bangalore International Centre  | Bangalore | 1500  | 500        | https://example.com/banner.jpg  | 60          |

  Scenario: Retrieve the list of events
    Given I am authenticated
    When I request the list of events
    Then the events are returned successfully
